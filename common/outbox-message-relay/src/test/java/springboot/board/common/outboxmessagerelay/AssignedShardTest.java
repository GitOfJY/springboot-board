package springboot.board.common.outboxmessagerelay;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

class AssignedShardTest {
    @Test
    void ofTest() {
        // given
        Long shardCount = 64L;
        List<String> appList = List.of("app1", "app2", "app3");

        // when
        AssignedShard assignedShard1 = AssignedShard.of(appList.get(0), appList, shardCount);
        AssignedShard assignedShard2 = AssignedShard.of(appList.get(1), appList, shardCount);
        AssignedShard assignedShard3 = AssignedShard.of(appList.get(2), appList, shardCount);
        AssignedShard assignedShard4 = AssignedShard.of("invalid", appList, shardCount);

        // then
        List<Long> result = Stream.of(assignedShard1.getShards(), assignedShard2.getShards(),
                assignedShard3.getShards(), assignedShard4.getShards())
                .flatMap(List::stream)
                .toList();

        Assertions.assertThat(result).hasSize(shardCount.intValue());

        for (int i = 0; i < 64; i++) {
            Assertions.assertThat(result.get(i)).isEqualTo(i);
        }

        Assertions.assertThat(assignedShard4.getShards()).isEmpty();
    }

}