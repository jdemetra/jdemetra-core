package demetra.timeseries;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TsCollectionTest {

    @Test
    public void testToTsCollection() {
        assertThat(Stream.<Ts>empty().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.EMPTY);

        List<Ts> list = IntStream.range(0, 100).mapToObj(i -> Ts.builder().name("ts" + i).build()).collect(Collectors.toList());

        assertThat(list.stream().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.of(TsSeq.of(list)));

        assertThat(list.parallelStream().collect(TsCollection.toTsCollection()))
                .isEqualTo(TsCollection.of(TsSeq.of(list)));
    }
}
