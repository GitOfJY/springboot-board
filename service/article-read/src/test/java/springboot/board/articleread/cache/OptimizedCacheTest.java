package springboot.board.articleread.cache;

import lombok.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class OptimizedCacheTest {
    @Test
    void parseDataTest() {
        parseDataTest("data", 10);
        parseDataTest(3L, 10);
        parseDataTest(3, 10);
        parseDataTest(new TestClass("test"), 10);

    }

    void parseDataTest(Object data, long ttlSeconds) {
        // given
        OptimizedCache cache = new OptimizedCache().of(data, Duration.ofSeconds(ttlSeconds));
        System.out.println("optimizedCache: " + cache);

        // when
        Object resolvedData = cache.parseData(data.getClass());

        // then
        System.out.println("resolvedData: " + resolvedData);
        assertThat(resolvedData).isEqualTo(data);
    }

    @Test
    void isExpiredTest() {
        assertThat(OptimizedCache.of("datat", Duration.ofDays(-30)).isExpired()).isTrue();
        assertThat(OptimizedCache.of("datat", Duration.ofDays(30)).isExpired()).isFalse();
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestClass {
        String testData;
    }
}