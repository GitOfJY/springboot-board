# [ SpringBoot Board ]

Spring Boot 기반의 대규모 트래픽을 고려한 게시판 서비스입니다.

게시글·댓글·좋아요·조회수 도메인을 **독립된 서비스 모듈**로 분리하고, **Kafka 이벤트 기반 아키텍처**로 연동하여 확장 가능한 구조를 설계했습니다.  
조회 성능 최적화를 위해 **Redis 캐시**, **커스텀 캐시 어노테이션(@OptimizedCacheable + AOP)**, **커서 기반 페이징**을 적용했습니다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2 |
| ORM | Spring Data JPA |
| Database | MySQL |
| Cache | Redis (Spring Data Redis) |
| Messaging | Apache Kafka (Spring Kafka) |
| AOP | Spring AOP (캐시 최적화) |
| ID 생성 | Snowflake (커스텀 분산 ID) |
| Build | Gradle (Multi-Module) |
| etc | Lombok, Logback |

---

## 프로젝트 구조 (멀티 모듈)

```
springboot-board/
├── common/                         # 공통 모듈
│   ├── snowflake/                  # Snowflake 분산 ID 생성기
│   ├── data-serializer/            # Jackson 기반 직렬화/역직렬화
│   ├── event/                      # 이벤트 정의 (EventType, EventPayload)
│   └── outbox-message-relay/       # Transactional Outbox 패턴 + Kafka 발행
│
├── service/                        # 서비스 모듈
│   ├── article/                    # 게시글 서비스
│   ├── comment/                    # 댓글 서비스
│   ├── like/                       # 좋아요 서비스
│   ├── view/                       # 조회수 서비스
│   ├── hot-article/                # 인기글 서비스 (Kafka Consumer)
│   └── article-read/               # 게시글 조회 최적화 서비스 (CQRS Read)
│
├── build.gradle                    # 루트 빌드 설정
└── settings.gradle                 # 모듈 정의
```

### 패키지 구조 (서비스 모듈 공통)

```
springboot.board.{module}/
├── controller/         # REST API 엔드포인트
├── entity/             # JPA 엔티티
├── repository/         # 데이터 접근 계층
└── service/            # 비즈니스 로직
    ├── request/        # 요청 DTO
    └── response/       # 응답 DTO
```

---

## 주요 기능

### 게시글 (article)

- 게시글 CRUD (조회, 생성, 수정, 삭제)
- 게시글 목록 조회
    - 게시판별(boardId) 최신순 정렬
    - **Offset 기반 페이징** (page, pageSize)
    - **커서 기반 무한 스크롤** (lastArticleId)
- 게시판별 게시글 수 조회
- Snowflake ID를 사용한 분산 환경 대응

### 댓글 (comment)

- 댓글 조회, 생성, 삭제
- **계층형 댓글 (Adjacency List)**
    - parentCommentId로 부모-자식 관계 관리
    - 최대 2 depth 대댓글 지원
    - root 댓글: `parentCommentId == commentId`
- 계층별 오래된순 정렬
- **Offset 페이징** + **커서 기반 무한 스크롤** (lastParentCommentId, lastCommentId)
- 삭제 정책 (Soft Delete)
    - 하위 댓글이 없으면 즉시 삭제
    - 하위 댓글이 있으면 `deleted = true` 표시만

### 좋아요 (like)

- 게시글 좋아요 / 좋아요 취소
- 사용자당 게시글 1회 좋아요 제한
- 좋아요 수 조회
- **동시성 제어 전략 비교 구현**
    - 기본 (Unique 제약)
    - Pessimistic Lock (2가지 방식)
    - Optimistic Lock

### 조회수 (view)

- Redis 기반 조회수 관리
- **어뷰징 방지 — Redis 분산 락 + TTL**
    - `view::article::{id}::user::{userId}::lock` 키로 10분 TTL 설정
    - `setIfAbsent`로 동일 사용자 10분 내 중복 집계 방지
- Redis INCR 기반 원자적 조회수 증가
- 일정 횟수(100회)마다 DB 백업 처리

### 인기글 (hot-article)

- **Kafka Consumer** 기반 이벤트 수집
- 게시글 생성/좋아요/조회수/댓글 이벤트를 수신하여 인기글 산출
- 날짜별 인기글 목록 조회 (Redis 기반)

### 게시글 조회 최적화 (article-read)

- **CQRS Read Model** — 쓰기와 분리된 조회 전용 서비스
- Kafka Consumer로 게시글/댓글/좋아요/조회수 이벤트 수신 후 Read Model 갱신
- **커스텀 캐시 최적화**
    - `@OptimizedCacheable` 커스텀 어노테이션
    - `OptimizedCacheAspect` (AOP)로 캐시 조회/갱신 자동 처리
    - `OptimizedCacheManager` / `OptimizedCacheLockProvider`로 Cache Stampede 방지
- Offset 페이징 + 커서 기반 무한 스크롤

---

## 핵심 기술 포인트

### 1. 이벤트 기반 아키텍처 (Transactional Outbox Pattern)

```
[article / comment / like / view 서비스]
    │
    ├── DB 트랜잭션 (비즈니스 로직 + Outbox 테이블 저장)
    │
    └── outbox-message-relay → Kafka 발행
                                    │
                          ┌─────────┴──────────┐
                          ▼                    ▼
                   [hot-article]         [article-read]
                   (인기글 집계)         (Read Model 갱신)
```

- 각 서비스의 상태 변경 시 `OutboxEventPublisher`로 이벤트 발행
- Kafka Topic 분리: `springboot-board-article`, `springboot-board-comment`, `springboot-board-like`, `springboot-board-view`
- EventType: `ARTICLE_CREATED`, `ARTICLE_UPDATED`, `ARTICLE_DELETED`, `COMMENT_CREATED`, `COMMENT_DELETED`, `ARTICLE_LIKED`, `ARTICLE_UNLIKED`, `ARTICLE_VIEWED`

### 2. 조회수 어뷰징 방지 (Redis Distributed Lock)

```
1. 요청: POST /v1/article-views/articles/{id}/users/{userId}

2. Redis SETNX (setIfAbsent)
   Key: view::article::{id}::user::{userId}::lock
   TTL: 600초 (10분)
   ├── 키 없음 (첫 조회) → Redis INCR로 조회수 +1
   └── 키 있음 (10분 내 재조회) → 조회수 증가 없이 현재 count 반환

3. 조회수 100회마다 → DB 백업 (articleViewCountBackUpProcessor)
```

### 3. 좋아요 동시성 제어 비교

| 방식 | 엔드포인트 suffix | 특징 |
|------|------------------|------|
| 기본 | (없음) | DB Unique 제약 |
| Pessimistic Lock 1 | `/pessimistic-lock-1` | SELECT FOR UPDATE |
| Pessimistic Lock 2 | `/pessimistic-lock-2` | 다른 비관적 락 전략 |
| Optimistic Lock | `/optimistic-lock` | @Version 기반 재시도 |

### 4. 커스텀 캐시 최적화 (@OptimizedCacheable)

```java
@OptimizedCacheable(type = "article", ttlSeconds = 300)
public ArticleReadResponse read(Long articleId) { ... }
```

- `@OptimizedCacheable` 어노테이션 선언만으로 캐시 자동 처리
- `OptimizedCacheAspect` (AOP)가 캐시 히트/미스 판별 후 원본 데이터 조회
- `OptimizedCacheLockProvider`로 동시 캐시 갱신 요청 시 Cache Stampede 방지
- `OptimizedCacheTTL`로 TTL 관리

### 5. Snowflake 분산 ID 생성

- Auto Increment 대신 Snowflake 알고리즘으로 고유 ID 생성
- 분산 환경에서 충돌 없는 시간순 정렬 가능한 ID 보장

---

## ERD

```
Article
├── article_id (PK, Snowflake ID)
├── title
├── content
├── board_id
├── writer_id
├── created_at
└── modified_at

Comment
├── comment_id (PK, Snowflake ID)
├── content
├── parent_comment_id (자기 참조, Adjacency List)
├── article_id
├── writer_id
├── deleted (boolean, Soft Delete)
└── created_at

ArticleLike
├── article_like_id (PK, Snowflake ID)
├── article_id
├── user_id
└── created_at
```

### Redis Key 설계

```
# 조회수
view::article::{articleId}::view_count          → 조회수 (INCR)

# 조회수 어뷰징 방지 락
view::article::{articleId}::user::{userId}::lock → TTL 600초
```

---

## 📡 API 명세

### 게시글 (Article)

| Method | URI | 설명 |
|--------|-----|------|
| GET | `/v1/articles/{articleId}` | 게시글 단건 조회 |
| GET | `/v1/articles?boardId=&page=&pageSize=` | 게시글 목록 (페이징) |
| GET | `/v1/articles/infinite-scroll?boardId=&pageSize=&lastArticleId=` | 게시글 목록 (무한 스크롤) |
| POST | `/v1/articles` | 게시글 생성 |
| PUT | `/v1/articles/{articleId}` | 게시글 수정 |
| DELETE | `/v1/articles/{articleId}` | 게시글 삭제 |
| GET | `/v1/articles/boards/{boardId}/count` | 게시판별 게시글 수 |

### 댓글 (Comment)

| Method | URI | 설명 |
|--------|-----|------|
| GET | `/v1/comments/{commentId}` | 댓글 단건 조회 |
| GET | `/v1/comments?articleId=&page=&pageSize=` | 댓글 목록 (페이징) |
| GET | `/v1/comments/infinite-scroll?articleId=&lastParentCommentId=&lastCommentId=&pageSize=` | 댓글 목록 (무한 스크롤) |
| POST | `/v1/comments` | 댓글 생성 |
| DELETE | `/v1/comments/{commentId}` | 댓글 삭제 |

### 좋아요 (Like)

| Method | URI | 설명 |
|--------|-----|------|
| GET | `/v1/article-likes/articles/{articleId}/users/{userId}` | 좋아요 여부 조회 |
| POST | `/v1/article-likes/articles/{articleId}/users/{userId}` | 좋아요 |
| DELETE | `/v1/article-likes/articles/{articleId}/users/{userId}` | 좋아요 취소 |
| GET | `/v1/article-likes/articles/{articleId}/count` | 좋아요 수 |

### 조회수 (View)

| Method | URI | 설명 |
|--------|-----|------|
| POST | `/v1/article-views/articles/{articleId}/users/{userId}` | 조회수 증가 |
| GET | `/v1/article-views/articles/{articleId}/count` | 조회수 조회 |

### 인기글 (Hot Article)

| Method | URI | 설명 |
|--------|-----|------|
| GET | `/v1/hot-articles/articles/date/{dateStr}` | 날짜별 인기글 목록 |
