/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

import demetra.data.accumulator.KahanAccumulator;
import demetra.maths.matrices.Matrix;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import org.junit.Ignore;
import static demetra.data.DataBlock.ofInternal;
import java.util.Random;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DataBlockTest {

    public DataBlockTest() {
    }

    static long K = 100000000;

    @Test
    @Ignore
    public void testDummy() {
        System.out.println(DataBlock.ofInternal(getSample(20), 0, 10, 2).toString());
        System.out.println(DataBlock.ofInternal(getSample(20), 2, 8, 2).toString());
        System.out.println(DataBlock.ofInternal(getSample(20), 10, 0, -2).toString());
        System.out.println(DataBlock.ofInternal(getSample(20), 8, 2, -2).toString());
    }

    @Test
    @Ignore
    public void testSomeMethod() {
        DataBlock x = DataBlock.make(50);
        DataBlock y = DataBlock.make(50);
        x.set(i -> i);
        y.set(i -> i);

        ec.tstoolkit.data.DataBlock X = new ec.tstoolkit.data.DataBlock(50);
        ec.tstoolkit.data.DataBlock Y = new ec.tstoolkit.data.DataBlock(50);
        X.set(i -> i);
        Y.set(i -> i);

        long t0 = System.currentTimeMillis();
        double s = 0;
        for (long k = 0; k < K; ++k) {
            s += X.dot(Y.reverse());
        }

        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        s = 0;
        for (long k = 0; k < K; ++k) {
            s += x.dot(y.reverse());
        }

        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    @Test
    public void testMake() {
        assertThat(DataBlock.make(0))
                .isInstanceOf(DataBlock.class)
                .isEqualTo(DataBlock.EMPTY);
        assertThat(DataBlock.make(10))
                .isInstanceOf(DataBlock.class);
        assertThatThrownBy(() -> DataBlock.make(-1))
                .isInstanceOf(NegativeArraySizeException.class);
    }

    @Test
    public void testOfInternal() {
        assertThat(ofInternal(getSample(10))).isNotNull();
        assertThat(ofInternal(getSample(10))).isExactlyInstanceOf(DataBlock.class);

        //When data is null
        assertThatThrownBy(() -> DataBlock.ofInternal(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testOfInternalStartEnd() {
        assertThat(DataBlock.ofInternal(getSample(10), 0, 10)).isNotNull();
        assertThat(DataBlock.ofInternal(getSample(10), 0, 10)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 0, 0)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 5, 5)).isExactlyInstanceOf(DataBlock.class);
        assertThatThrownBy(() -> DataBlock.ofInternal(getSample(10), 1, -2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DataBlock.ofInternal(getSample(10), 5, 4)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testOfInternalStartEndInc() {
        // When inc == 1
        assertThat(DataBlock.ofInternal(getSample(8), 0, 10, 1)).isNotNull();
        assertThat(DataBlock.ofInternal(getSample(10), 0, 10, 1)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 0, 0, 1)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 0, 0, 3)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 5, 5, 3)).isExactlyInstanceOf(DataBlock.class);
        assertThatThrownBy(() -> DataBlock.ofInternal(getSample(10), 1, -2, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DataBlock.ofInternal(getSample(10), 5, 4, 1)).isInstanceOf(IllegalArgumentException.class);

        // When data is null
        assertThatThrownBy(() -> DataBlock.ofInternal(null, 0, 10, 2)).isInstanceOf(NullPointerException.class);

        // When (end - start) % inc != 0
        assertThat(DataBlock.ofInternal(getSample(10), 0, 10, 2)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 3, 9, 3)).isExactlyInstanceOf(DataBlock.class);
        assertThatThrownBy(() -> DataBlock.ofInternal(getSample(10), 0, 10, 3)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DataBlock.ofInternal(getSample(10), 0, 10, -2)).isInstanceOf(IllegalArgumentException.class);

        //When (end - start) / inc < 0
        assertThat(DataBlock.ofInternal(getSample(10), 10, 0, -1)).isExactlyInstanceOf(DataBlock.class);
        assertThatThrownBy(() -> DataBlock.ofInternal(getSample(10), 0, 10, -1)).isInstanceOf(IllegalArgumentException.class);

        assertThat(DataBlock.ofInternal(getSample(10), 0, 10, 1)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 2, 10, 1)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 10, 0, -1)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 10, 2, -1)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 0, 10, 2)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 2, 10, 2)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 10, 0, -2)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 10, 2, -2)).isExactlyInstanceOf(DataBlock.class);
        assertThat(DataBlock.ofInternal(getSample(10), 9, -1, -2)).isExactlyInstanceOf(DataBlock.class);

    }

    @Test
    public void testCopyOfDouble() {
        assertThat(DataBlock.copyOf(getSample(10)))
                .isNotNull()
                .isExactlyInstanceOf(DataBlock.class);

        DataBlock dbTest = DataBlock.copyOf(getSample(10));
        assertThat(dbTest.data).isEqualTo(getSample(10));
    }

    @Test
    public void testCopyOfDoubleSeq() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            assertThat(DataBlock.of(o).toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            assertThat(DataBlock.of(o).getStorage()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });
        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            assertThat(DataBlock.of(o).toArray()).containsExactly(3, 4, 5, 6, 7, 8, 9, 10);
            assertThat(DataBlock.of(o).getStorage()).containsExactly(3, 4, 5, 6, 7, 8, 9, 10);
        });
        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            assertThat(DataBlock.of(o).toArray()).containsExactly(1, 3, 5, 7, 9);
            assertThat(DataBlock.of(o).getStorage()).containsExactly(1, 3, 5, 7, 9);
        });
        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            assertThat(DataBlock.of(o).toArray()).containsExactly(3, 5, 7, 9);
            assertThat(DataBlock.of(o).getStorage()).containsExactly(3, 5, 7, 9);
        });
        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            assertThat(DataBlock.of(o).toArray()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
            assertThat(DataBlock.of(o).getStorage()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
        });
        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            assertThat(DataBlock.of(o).toArray()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3);
            assertThat(DataBlock.of(o).getStorage()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3);
        });
        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            assertThat(DataBlock.of(o).toArray()).containsExactly(10, 8, 6, 4, 2);
            assertThat(DataBlock.of(o).getStorage()).containsExactly(10, 8, 6, 4, 2);
        });
        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            assertThat(DataBlock.of(o).toArray()).containsExactly(10, 8, 6, 4);
            assertThat(DataBlock.of(o).getStorage()).containsExactly(10, 8, 6, 4);
        });
    }

    @Test
    public void testSelect() {
        DoubleSeq data = DoubleSeq.of(getSample(10));
        DoublePredicate dp = (d) -> d > 0;
        assertThat(DataBlock.select(data, dp))
                .isInstanceOf(DataBlock.class);
        DataBlock db = DataBlock.select(data, dp);
        assertThat(db.data).isEqualTo(getSample(10));

        DoublePredicate dp2 = (d) -> d % 2 == 0;
        DataBlock db2 = DataBlock.select(data, dp2);
        assertThat(db2.data).containsExactly(2, 4, 6, 8, 10);

        DoublePredicate dp3 = (d) -> d > 20;
        DataBlock db3 = DataBlock.select(data, dp3);
        assertThat(db3.data).isEmpty();
    }

    @Test
    public void testReader() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).cursor()).isInstanceOf(DoubleSeqCursor.class);
    }

    @Test
    public void testCells() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).cursor()).isInstanceOf(DoubleVectorCursor.class);
    }

    @Test
    public void testCopyFrom() {
        assertThatThrownBy(()
                -> ofInternal(getSample(4)).copyFrom(null, 0))
                .describedAs("Buffer is null")
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(()
                -> ofInternal(getSample(4))
                .copyFrom(new double[]{5.55, 6.66, 7.77, 8.88}, -2))
                .describedAs("Start is negative")
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(ofInternal(getSample(4))).satisfies(o -> {
            o.copyFrom(new double[]{5.55, 6.66, 7.77, 8.88}, 0);
            assertThat(o.toArray())
                    .describedAs("When buffer.length==data.length")
                    .containsExactly(5.55, 6.66, 7.77, 8.88);
        });

        assertThat(ofInternal(getSample(5))).satisfies(o -> {
            assertThatThrownBy(() -> o.copyFrom(new double[]{5.55, 6.66, 7.77}, 0))
                    .describedAs("When buffer.length < data.length")
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        });

        assertThat(ofInternal(getSample(4))).satisfies(o -> {
            o.copyFrom(new double[]{5.55, 6.66, 7.77, 8.88, 9.99, 10.10}, 0);
            assertThat(o.toArray())
                    .describedAs("buffer.length>data.length")
                    .containsExactly(5.55, 6.66, 7.77, 8.88);
        });

        assertThat(ofInternal(getSample(4))).satisfies(o -> {
            o.copyFrom(new double[]{5.55, 6.66, 7.77, 8.88, 9.99, 10.10, 11.11}, 2);
            assertThat(o.toArray())
                    .describedAs("buffer.length-start>data.length")
                    .containsExactly(7.77, 8.88, 9.99, 10.10);
        });

        assertThat(ofInternal(getSample(10), 2, 10)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0);
            assertThat(o.toArray())
                    .describedAs("o.beg!=0 && o.end==data.length")
                    .containsExactly(11, 12, 13, 14, 15, 16, 17, 18);
        });

        assertThat(ofInternal(getSample(10), 2, 6)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18}, 0);
            assertThat(o.toArray())
                    .describedAs("o.beg!=0 && o.end<data.length")
                    .containsExactly(11, 12, 13, 14);
        });

        assertThat(ofInternal(getSample(10), 2, 6)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18}, 2);
            assertThat(o.toArray())
                    .describedAs("o.beg!=0 && o.end<data.length && buffer.start < 0")
                    .containsExactly(13, 14, 15, 16);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0);
            assertThat(o.toArray())
                    .describedAs("o.beg==0 && o.end==data.length && inc>1 && buffer.start==0")
                    .containsExactly(11, 12, 13, 14, 15);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15}, 0);
            assertThat(o.toArray())
                    .describedAs("o.beg==0 && o.end==data.length && inc>1 && buffer.start==0")
                    .containsExactly(11, 12, 13, 14, 15);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 2);
            assertThat(o.toArray())
                    .describedAs("o.beg==0 && o.end==data.length && inc>1 && buffer.start>0")
                    .containsExactly(13, 14, 15, 16, 17);
        });

        assertThat(ofInternal(getSample(10), 2, 8, 2)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0);
            assertThat(o.toArray())
                    .describedAs("o.beg>0 && o.end<data.length && inc>1 && buffer.start==0")
                    .containsExactly(11, 12, 13);
        });

        assertThat(ofInternal(getSample(10), 2, 8, 2)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 2);
            assertThat(o.toArray())
                    .describedAs("o.beg>0 && o.end<data.length && inc>1 && buffer.start>0")
                    .containsExactly(13, 14, 15);
        });

        assertThat(ofInternal(getSample(12), 10, 0, -1)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0);
            assertThat(o.toArray())
                    .containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(12), 10, 0, -1)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22}, 2);
            assertThat(o.toArray())
                    .containsExactly(13, 14, 15, 16, 17, 18, 19, 20, 21, 22);
        });

        assertThat(ofInternal(getSample(12), 10, 0, -2)).satisfies(o -> {
            o.copyFrom(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22}, 2);
            assertThat(o.toArray())
                    .containsExactly(13, 14, 15, 16, 17);
        });
    }

    @Test
    public void testCopyTo() {
        assertThatThrownBy(()
                -> ofInternal(getSample(4)).copyTo(null, 0))
                .describedAs("Buffer is null")
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(()
                -> ofInternal(getSample(4))
                .copyTo(new double[]{5.55, 6.66, 7.77, 8.88}, -2))
                .describedAs("Start is negative")
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThatThrownBy(()
                -> ofInternal(getSample(4))
                .copyTo(new double[]{5.55, 6.66, 7.77}, 0))
                .describedAs("buffer.length < data.length")
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(new double[]{11, 12, 13, 14, 15}).satisfies(b -> {
            ofInternal(getSample(5)).copyTo(b, 0);
            assertThat(b)
                    .describedAs("buffer.length = data.length")
                    .containsExactly(1, 2, 3, 4, 5);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(5)).copyTo(b, 0);
            assertThat(b)
                    .describedAs("buffer.length > data.length")
                    .containsExactly(1, 2, 3, 4, 5, 16, 17, 18, 19, 20);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(5)).copyTo(b, 5);
            assertThat(b)
                    .describedAs("buffer.length > data.length && start > 0")
                    .containsExactly(11, 12, 13, 14, 15, 1, 2, 3, 4, 5);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(10), 0, 10).copyTo(b, 0);
            assertThat(b)
                    .describedAs("buffer.length = data.length && data with bounds")
                    .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(10), 2, 8).copyTo(b, 0);
            assertThat(b)
                    .describedAs("buffer.length = data.length && data.beg<0 && data.end<data.length")
                    .containsExactly(3, 4, 5, 6, 7, 8, 17, 18, 19, 20);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(10), 0, 10, 2).copyTo(b, 0);
            assertThat(b)
                    .describedAs("buffer.length = data.length && data with bound && inc > 1")
                    .containsExactly(1, 3, 5, 7, 9, 16, 17, 18, 19, 20);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(10), 2, 8, 2).copyTo(b, 0);
            assertThat(b)
                    .describedAs("buffer.length = data.length && data.beg<0 && data.end<data.length && inc>1")
                    .containsExactly(3, 5, 7, 14, 15, 16, 17, 18, 19, 20);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(10), 2, 8, 2).copyTo(b, 3);
            assertThat(b)
                    .describedAs("buffer.length = data.length && data.beg<0 && data.end<data.length && inc>1 && start>0")
                    .containsExactly(11, 12, 13, 3, 5, 7, 17, 18, 19, 20);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(10), 9, -1, -1).copyTo(b, 0);
            assertThat(b)
                    .describedAs("buffer.length = data.length && reverse order && inc = -1")
                    .containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
        });

        assertThat(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}).satisfies(b -> {
            ofInternal(getSample(10), 9, -1, -2).copyTo(b, 0);
            assertThat(b)
                    .describedAs("buffer.length = data.length && reverse order && inc = -2")
                    .containsExactly(10, 8, 6, 4, 2, 16, 17, 18, 19, 20);
        });

    }

    @Test
    public void testExtract() {
        assertThat(ofInternal(getSample(10)).extract(0, 10)).isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10)).extract(0, 10)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10)).extract(2, 5)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(3, 4, 5, 6, 7);
        });

        assertThat(ofInternal(getSample(10)).extract(0, 0)).satisfies(db -> {
            assertThat(db.toArray()).isNullOrEmpty();
        });

        assertThat(ofInternal(getSample(10), 0, 5).extract(0, 5)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(1, 2, 3, 4, 5);
        });

        assertThat(ofInternal(getSample(10), 0, 5).extract(0, 10)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10).extract(0, 5)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(3, 4, 5, 6, 7);
        });

        assertThat(ofInternal(getSample(10), 8, 0, -1).extract(0, 5)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(9, 8, 7, 6, 5);
        });

        assertThat(ofInternal(getSample(20), 0, 10, 2).extract(0, 10)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(1, 3, 5, 7, 9, 11, 13, 15, 17, 19);
        });

        assertThat(ofInternal(getSample(10), 8, 0, -2).extract(0, 4)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(9, 7, 5, 3);
        });

        assertThat(ofInternal(getSample(10), 8, 0, -2).extract(1, 4)).satisfies(db -> {
            assertThat(db.toArray())
                    .containsExactly(7, 5, 3, 1);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1).extract(2, 10)).satisfies(db -> {
            assertThat(db.data).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });
    }

    @Test
    public void testNorm2() {
        assertThat(DataBlock.ofInternal(getSample(10), 0, 0, 1).norm2())
                .describedAs("Length is zero")
                .isEqualTo(0);

        assertThat(DataBlock.ofInternal(getSample(10), 0, 0, -1).norm2())
                .describedAs("Length is zero and inc is negative")
                .isEqualTo(0);

        assertThat(DataBlock.ofInternal(getSample(10), 3, 4, 1).norm2())
                .describedAs("Length is one")
                .isEqualTo(4);

        assertThat(DataBlock.ofInternal(getSample(10), 4, 3, -1).norm2())
                .describedAs("Length is one and inc is negative")
                .isEqualTo(5);

        assertThat(DataBlock.ofInternal(getSample(10), 0, 10, 1).norm2())
                .describedAs("Length > 1 and inc is positive")
                .isNotNaN()
                .isNotNull()
                .isNotNegative();

        assertThat(DataBlock.ofInternal(getSample(10), 2, 10, 2).norm2())
                .describedAs("Length > 1 and inc is positive")
                .isNotNaN()
                .isNotNull()
                .isNotNegative();

        assertThat(DataBlock.ofInternal(getSample(10), 9, -1, -1).norm2())
                .describedAs("Length > 1 and inc is positive")
                .isNotNaN()
                .isNotNull()
                .isNotNegative();

        assertThat(DataBlock.ofInternal(getSample(10), 9, 1, -2).norm2())
                .describedAs("Length > 1 and inc is positive")
                .isNotNaN()
                .isNotNull()
                .isNotNegative();
    }

    @Test
    public void testFastNorm2() {
        assertThat(DataBlock.ofInternal(getSample(10), 0, 0, 1).fastNorm2())
                .describedAs("Length is zero")
                .isEqualTo(0);

        assertThat(DataBlock.ofInternal(getSample(10), 0, 0, -1).fastNorm2())
                .describedAs("Length is zero and inc is negative")
                .isEqualTo(0);

        assertThat(DataBlock.ofInternal(getSample(10), 3, 4, 1).fastNorm2())
                .describedAs("Length is one")
                .isEqualTo(4);

        assertThat(DataBlock.ofInternal(getSample(10), 4, 3, -1).fastNorm2())
                .describedAs("Length is one and inc is negative")
                .isEqualTo(5);

        assertThat(DataBlock.ofInternal(getSample(10), 0, 10, 1).fastNorm2())
                .describedAs("Length > 1 and inc is positive")
                .isNotNaN()
                .isNotNull()
                .isNotNegative();

        assertThat(DataBlock.ofInternal(getSample(10), 2, 10, 2).fastNorm2())
                .describedAs("Length > 1 and inc is positive")
                .isNotNaN()
                .isNotNull()
                .isNotNegative();

        assertThat(DataBlock.ofInternal(getSample(10), 9, -1, -1).fastNorm2())
                .describedAs("Length > 1 and inc is positive")
                .isNotNaN()
                .isNotNull()
                .isNotNegative();

        assertThat(DataBlock.ofInternal(getSample(10), 9, 1, -2).fastNorm2())
                .describedAs("Length > 1 and inc is positive")
                .isNotNaN()
                .isNotNull()
                .isNotNegative();
    }

    @Test
    public void testExtractWithIncr() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).extract(0, 10, 1)).satisfies(o -> {
            assertThat(o.toArray())
                    .isEqualTo(ofInternal(getSample(10)).toArray());
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1).extract(0, 5, 2)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(1, 3, 5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1).extract(0, 5, 1)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(3, 4, 5, 6, 7);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1).extract(0, 4, 2)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(3, 5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2).extract(0, 4, 1)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(1, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2).extract(0, 2, 2)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(1, 5);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2).extract(0, 4, 1)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(3, 5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2).extract(0, 2, 2)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(3, 7);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1).extract(0, 4, 1)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(10, 9, 8, 7);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1).extract(0, 4, 2)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(10, 8, 6, 4);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2).extract(0, 4, 1)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(10, 8, 6, 4);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2).extract(0, 2, 2)).satisfies(o -> {
            assertThat(o.toArray())
                    .containsExactly(10, 6);
        });
        assertThat(ofInternal(getSample(10), 0, 10, 1).extract(0, 15, 1)).satisfies(o -> {
            assertThatThrownBy(() -> o.toArray()).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        });
    }

    @Test
    public void testRange() {
        assertThat(ofInternal(getSample(10)).range(0, 10)).isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1).range(0, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1).range(3, 7)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(4, 5, 6, 7);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1).range(0, 15)).satisfies(o -> {
            assertThat(o).isInstanceOf(DataBlock.class);
            assertThatThrownBy(() -> o.toArray()).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2).range(0, 3)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(1, 3, 5);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1).range(0, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 9, 8, 7, 6);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2).range(0, 3)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 8, 6);
        });
    }

    @Test
    public void testDrop() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).drop(0, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1).drop(2, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(3, 4, 5);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1).drop(0, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(3, 4, 5);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1).drop(2, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(5);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2).drop(0, 3)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(1, 3);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2).drop(2, 3)).satisfies(o -> {
            assertThat(o.toArray()).isEmpty();
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2).drop(0, 3)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(3);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2).drop(2, 3)).satisfies(o -> {
            assertThatThrownBy(() -> o.toArray()).isInstanceOf(NegativeArraySizeException.class);
        });

        // >>>> Reverse order <<<<<<//
        assertThat(ofInternal(getSample(10), 9, -1, -1).drop(0, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 9, 8, 7, 6);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1).drop(2, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(8, 7, 6);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1).drop(0, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 9, 8);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1).drop(2, 5)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(8);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2).drop(0, 3)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 8);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2).drop(2, 3)).satisfies(o -> {
            assertThat(o.toArray()).isEmpty();
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2).drop(0, 3)).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2).drop(2, 3)).satisfies(o -> {
            assertThatThrownBy(() -> o.toArray()).isInstanceOf(NegativeArraySizeException.class);
        });
    }

    @Test
    public void testReverse() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).reverse())
                .isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1).reverse().toString())
                .isEqualTo(ofInternal(getSample(10), 9, -1, -1).toString());

        assertThat(ofInternal(getSample(10), 2, 10, 1).reverse().toString())
                .isEqualTo(ofInternal(getSample(10), 9, 1, -1).toString());

        assertThat(ofInternal(getSample(10), 0, 10, 2).reverse().toString())
                .isEqualTo(ofInternal(getSample(10), 8, -2, -2).toString());

        assertThat(ofInternal(getSample(10), 2, 10, 2).reverse().toString())
                .isEqualTo(ofInternal(getSample(10), 8, 0, -2).toString());

        assertThat(ofInternal(getSample(10), 9, -1, -1).reverse().toString())
                .isEqualTo(ofInternal(getSample(10), 0, 10, 1).toString());

        assertThat(ofInternal(getSample(10), 9, 1, -1).reverse().toString())
                .isEqualTo(ofInternal(getSample(10), 2, 10, 1).toString());

        assertThat(ofInternal(getSample(10), 8, -2, -2).reverse().toString())
                .isEqualTo(ofInternal(getSample(10), 0, 10, 2).toString());

        assertThat(ofInternal(getSample(10), 8, 0, -2).reverse().toString())
                .isEqualTo(ofInternal(getSample(10), 2, 10, 2).toString());
    }

    @Test
    public void testExtend() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).extend(2, 3))
                .satisfies(o -> {
                    assertThat(o.beg).isEqualTo(-2);
                    assertThat(o.end).isEqualTo(13);
                })
                .isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 2, 10, 1).extend(2, 3))
                .satisfies(o -> {
                    assertThat(o.beg).isEqualTo(0);
                    assertThat(o.end).isEqualTo(13);
                })
                .isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 0, 10, 2).extend(2, 3))
                .satisfies(o -> {
                    assertThat(o.beg).isEqualTo(-4);
                    assertThat(o.end).isEqualTo(16);
                })
                .isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 2, 10, 2).extend(2, 3))
                .satisfies(o -> {
                    assertThat(o.beg).isEqualTo(-2);
                    assertThat(o.end).isEqualTo(16);
                })
                .isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 10, 0, -1).extend(2, 3))
                .satisfies(o -> {
                    assertThat(o.beg).isEqualTo(12);
                    assertThat(o.end).isEqualTo(-3);
                })
                .isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 10, 2, -1).extend(2, 3))
                .satisfies(o -> {
                    assertThat(o.beg).isEqualTo(12);
                    assertThat(o.end).isEqualTo(-1);
                })
                .isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 10, 0, -2).extend(2, 3))
                .satisfies(o -> {
                    assertThat(o.beg).isEqualTo(14);
                    assertThat(o.end).isEqualTo(-6);
                })
                .isInstanceOf(DataBlock.class);

        assertThat(ofInternal(getSample(10), 10, 2, -2).extend(2, 3))
                .satisfies(o -> {
                    assertThat(o.beg).isEqualTo(14);
                    assertThat(o.end).isEqualTo(-4);
                })
                .isInstanceOf(DataBlock.class);
    }

    @Test
    @Ignore
    public void testWindow() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).window())
                .isInstanceOf(DataWindow.class);
    }

    @Test
    @Ignore
    public void testWindowWithStartEnd() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).window(2, 3))
                .isInstanceOf(DataWindow.class);
        assertThat(ofInternal(getSample(10), 0, 10, 2).window(2, 3))
                .isInstanceOf(DataWindow.class);
    }

    @Test
    @Ignore
    public void testLeft() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).left())
                .isInstanceOf(DataWindow.class);
    }

    @Test
    public void testDeepClone() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(getSample(10));
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1).deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2).deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(1, 3, 5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2).deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(3, 5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1).deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1).deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2).deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 8, 6, 4, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2).deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(10, 8, 6, 4);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2).reverse().deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(9, 7, 5, 3, 1);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2).reverse().deepClone()).satisfies(o -> {
            assertThat(o.toArray()).containsExactly(9, 7, 5, 3);
        });
    }

    @Test
    public void testBshiftAndNegSum() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.bshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(2, 3, 4, 5, 6, 7, 8, 9, 10, -55);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.bshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(4, 5, 6, 7, 8, 9, 10, -52);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.bshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(3, 5, 7, 9, -25);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.bshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(5, 7, 9, -24);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.bshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, 2, 1, -55);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.bshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, -52);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.bshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(8, 6, 4, 2, -30);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.bshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(8, 6, 4, -28);
        });
    }

    @Test
    public void testBshiftAndSum() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.bshiftAndSum();
            assertThat(o.toArray()).containsExactly(2, 3, 4, 5, 6, 7, 8, 9, 10, 55);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.bshiftAndSum();
            assertThat(o.toArray()).containsExactly(4, 5, 6, 7, 8, 9, 10, 52);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.bshiftAndSum();
            assertThat(o.toArray()).containsExactly(3, 5, 7, 9, 25);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.bshiftAndSum();
            assertThat(o.toArray()).containsExactly(5, 7, 9, 24);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.bshiftAndSum();
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, 2, 1, 55);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.bshiftAndSum();
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, 52);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.bshiftAndSum();
            assertThat(o.toArray()).containsExactly(8, 6, 4, 2, 30);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.bshiftAndSum();
            assertThat(o.toArray()).containsExactly(8, 6, 4, 28);
        });
    }

    @Test
    public void testBshiftAndZero() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.bshiftAndZero();
            assertThat(o.toArray()).containsExactly(2, 3, 4, 5, 6, 7, 8, 9, 10, 0);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.bshiftAndZero();
            assertThat(o.toArray()).containsExactly(4, 5, 6, 7, 8, 9, 10, 0);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.bshiftAndZero();
            assertThat(o.toArray()).containsExactly(3, 5, 7, 9, 0);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.bshiftAndZero();
            assertThat(o.toArray()).containsExactly(5, 7, 9, 0);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.bshiftAndZero();
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.bshiftAndZero();
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, 0);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.bshiftAndZero();
            assertThat(o.toArray()).containsExactly(8, 6, 4, 2, 0);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.bshiftAndZero();
            assertThat(o.toArray()).containsExactly(8, 6, 4, 0);
        });
    }

    @Test
    public void testBrotate() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.brotate();
            assertThat(o.toArray()).containsExactly(2, 3, 4, 5, 6, 7, 8, 9, 10, 1);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.brotate();
            assertThat(o.toArray()).containsExactly(4, 5, 6, 7, 8, 9, 10, 3);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.brotate();
            assertThat(o.toArray()).containsExactly(3, 5, 7, 9, 1);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.brotate();
            assertThat(o.toArray()).containsExactly(5, 7, 9, 3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.brotate();
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, 2, 1, 10);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.brotate();
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.brotate();
            assertThat(o.toArray()).containsExactly(8, 6, 4, 2, 10);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.brotate();
            assertThat(o.toArray()).containsExactly(8, 6, 4, 10);
        });
    }

    @Test
    public void testFshiftAndNegSum() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.fshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(-55, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.fshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(-52, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.fshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(-25, 1, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.fshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(-24, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.fshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(-55, 10, 9, 8, 7, 6, 5, 4, 3, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.fshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(-52, 10, 9, 8, 7, 6, 5, 4);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.fshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(-30, 10, 8, 6, 4);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.fshiftAndNegSum();
            assertThat(o.toArray()).containsExactly(-28, 10, 8, 6);
        });
    }

    @Test
    public void testFshiftAndSum() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.fshiftAndSum();
            assertThat(o.toArray()).containsExactly(55, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.fshiftAndSum();
            assertThat(o.toArray()).containsExactly(52, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.fshiftAndSum();
            assertThat(o.toArray()).containsExactly(25, 1, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.fshiftAndSum();
            assertThat(o.toArray()).containsExactly(24, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.fshiftAndSum();
            assertThat(o.toArray()).containsExactly(55, 10, 9, 8, 7, 6, 5, 4, 3, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.fshiftAndSum();
            assertThat(o.toArray()).containsExactly(52, 10, 9, 8, 7, 6, 5, 4);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.fshiftAndSum();
            assertThat(o.toArray()).containsExactly(30, 10, 8, 6, 4);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.fshiftAndSum();
            assertThat(o.toArray()).containsExactly(28, 10, 8, 6);
        });
    }

    @Test
    public void testFshiftAndZero() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.fshiftAndZero();
            assertThat(o.toArray()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.fshiftAndZero();
            assertThat(o.toArray()).containsExactly(0, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.fshiftAndZero();
            assertThat(o.toArray()).containsExactly(0, 1, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.fshiftAndZero();
            assertThat(o.toArray()).containsExactly(0, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.fshiftAndZero();
            assertThat(o.toArray()).containsExactly(0, 10, 9, 8, 7, 6, 5, 4, 3, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.fshiftAndZero();
            assertThat(o.toArray()).containsExactly(0, 10, 9, 8, 7, 6, 5, 4);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.fshiftAndZero();
            assertThat(o.toArray()).containsExactly(0, 10, 8, 6, 4);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.fshiftAndZero();
            assertThat(o.toArray()).containsExactly(0, 10, 8, 6);
        });
    }

    @Test
    public void testFrotate() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.frotate();
            assertThat(o.toArray()).containsExactly(10, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.frotate();
            assertThat(o.toArray()).containsExactly(10, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.frotate();
            assertThat(o.toArray()).containsExactly(9, 1, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.frotate();
            assertThat(o.toArray()).containsExactly(9, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.frotate();
            assertThat(o.toArray()).containsExactly(1, 10, 9, 8, 7, 6, 5, 4, 3, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.frotate();
            assertThat(o.toArray()).containsExactly(3, 10, 9, 8, 7, 6, 5, 4);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.frotate();
            assertThat(o.toArray()).containsExactly(2, 10, 8, 6, 4);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.frotate();
            assertThat(o.toArray()).containsExactly(4, 10, 8, 6);
        });
    }

    @Test
    public void testBshift() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.bshift(1);
            assertThat(o.toArray()).containsExactly(2, 3, 4, 5, 6, 7, 8, 9, 10, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.bshift(0);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.bshift(1);
            assertThat(o.toArray()).containsExactly(4, 5, 6, 7, 8, 9, 10, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.bshift(1);
            assertThat(o.toArray()).containsExactly(3, 5, 7, 9, 9);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.bshift(0);
            assertThat(o.toArray()).containsExactly(1, 3, 5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.bshift(1);
            assertThat(o.toArray()).containsExactly(5, 7, 9, 9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.bshift(1);
            assertThat(o.toArray()).containsExactly(9, 8, 7, 6, 5, 4, 3, 2, 1, 1);
        });

        assertThatThrownBy(() -> ofInternal(getSample(10), 0, 10, 1).bshift(-1))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.bshift(10);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThatThrownBy(() -> ofInternal(getSample(10), 0, 10, 2).bshift(10))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    public void testFshift() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.fshift(1);
            assertThat(o.toArray()).containsExactly(1, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.fshift(0);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.fshift(1);
            assertThat(o.toArray()).containsExactly(1, 1, 3, 5, 7);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.fshift(0);
            assertThat(o.toArray()).containsExactly(1, 3, 5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.fshift(1);
            assertThat(o.toArray()).containsExactly(10, 10, 9, 8, 7, 6, 5, 4, 3, 2);
        });

        assertThatThrownBy(() -> ofInternal(getSample(10), 0, 10, 1).fshift(-1))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.fshift(10);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThatThrownBy(() -> ofInternal(getSample(10), 0, 10, 2).fshift(10))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    public void testSet() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.set(3, 42);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 42, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.set(3, 42);
            assertThat(o.toArray()).containsExactly(3, 4, 5, 42, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.set(3, 42);
            assertThat(o.toArray()).containsExactly(1, 3, 5, 42, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.set(3, 42);
            assertThat(o.toArray()).containsExactly(3, 5, 7, 42);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.set(3, 42);
            assertThat(o.toArray()).containsExactly(10, 9, 8, 42, 6, 5, 4, 3, 2, 1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.set(3, 42);
            assertThat(o.toArray()).containsExactly(10, 9, 8, 42, 6, 5, 4, 3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.set(3, 42);
            assertThat(o.toArray()).containsExactly(10, 8, 6, 42, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.set(3, 42);
            assertThat(o.toArray()).containsExactly(10, 8, 6, 42);
        });
    }

    @Test
    public void testGet() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            assertThat(o.get(3)).isEqualTo(4);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            assertThatThrownBy(() -> o.get(-1))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            assertThatThrownBy(() -> o.get(10))
                    .isInstanceOf(ArrayIndexOutOfBoundsException.class);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            assertThat(o.get(3)).isEqualTo(6);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            assertThat(o.get(3)).isEqualTo(7);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            assertThat(o.get(3)).isEqualTo(9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            assertThat(o.get(3)).isEqualTo(7);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            assertThat(o.get(3)).isEqualTo(7);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            assertThat(o.get(3)).isEqualTo(4);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            assertThat(o.get(3)).isEqualTo(4);
        });
    }

    @Test
    public void testLength() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            assertThat(o.length()).isEqualTo(10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            assertThat(o.length()).isEqualTo(8);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            assertThat(o.length()).isEqualTo(5);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            assertThat(o.length()).isEqualTo(4);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            assertThat(o.length()).isEqualTo(10);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            assertThat(o.length()).isEqualTo(8);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            assertThat(o.length()).isEqualTo(5);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            assertThat(o.length()).isEqualTo(4);
        });
    }

    @Test
    public void testLastPosition() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            assertThat(o.getLastPosition()).isEqualTo(9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            assertThat(o.getLastPosition()).isEqualTo(9);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            assertThat(o.getLastPosition()).isEqualTo(8);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            assertThat(o.getLastPosition()).isEqualTo(8);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            assertThat(o.getLastPosition()).isEqualTo(0);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            assertThat(o.getLastPosition()).isEqualTo(2);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            assertThat(o.getLastPosition()).isEqualTo(1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            assertThat(o.getLastPosition()).isEqualTo(3);
        });
    }

    @Test
    public void testDot() {
        assertThat(ofInternal(getSample(5), 0, 5, 1)
                .dot(ofInternal(getSample(5), 0, 5, 1)))
                .isEqualTo(55);

        assertThat(ofInternal(getSample(5), 0, 5, 1)
                .dot(ofInternal(getSample(10), 0, 10, 2)))
                .isEqualTo(95);

        assertThat(ofInternal(getSample(10), 0, 10, 2)
                .dot(ofInternal(getSample(5), 0, 5, 1)))
                .isEqualTo(95);

        assertThat(ofInternal(getSample(10), 0, 10, 2)
                .dot(ofInternal(getSample(10), 0, 10, 2)))
                .isEqualTo(165);

        assertThat(ofInternal(getSample(5), 4, -1, -1)
                .dot(ofInternal(getSample(10), 0, 10, 2)))
                .isEqualTo(55);

        assertThat(ofInternal(getSample(10), 9, -1, -2)
                .dot(ofInternal(getSample(10), 9, -1, -2)))
                .isEqualTo(220);

    }

    @Test
    public void testRobustDot() {
        KahanAccumulator ka = new KahanAccumulator();

        assertThat(ofInternal(getSample(5), 0, 5, 1)).satisfies(o -> {
            ka.reset();
            o.robustDot(ofInternal(getSample(5), 0, 5, 1), ka);
            assertThat(ka.sum()).isEqualTo(55);
        });

        assertThat(ofInternal(getSample(5), 0, 5, 1)).satisfies(o -> {
            ka.reset();
            o.robustDot(ofInternal(getSample(10), 0, 10, 2), ka);
            assertThat(ka.sum()).isEqualTo(95);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            ka.reset();
            o.robustDot(ofInternal(getSample(5), 0, 5, 1), ka);
            assertThat(ka.sum()).isEqualTo(95);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            ka.reset();
            o.robustDot(ofInternal(getSample(10), 0, 10, 2), ka);
            assertThat(ka.sum()).isEqualTo(165);
        });

        assertThat(ofInternal(getSample(5), 4, -1, -1)).satisfies(o -> {
            ka.reset();
            o.robustDot(ofInternal(getSample(10), 0, 10, 2), ka);
            assertThat(ka.sum()).isEqualTo(55);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            ka.reset();
            o.robustDot(ofInternal(getSample(10), 9, -1, -2), ka);
            assertThat(ka.sum()).isEqualTo(220);
        });
    }

    @Test
    public void testSum() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).sum())
                .isEqualTo(55);
        assertThat(ofInternal(getSample(10), 2, 10, 1).sum())
                .isEqualTo(52);
        assertThat(ofInternal(getSample(10), 0, 10, 2).sum())
                .isEqualTo(25);
        assertThat(ofInternal(getSample(10), 2, 10, 2).sum())
                .isEqualTo(24);
        assertThat(ofInternal(getSample(10), 9, -1, -1).sum())
                .isEqualTo(55);
        assertThat(ofInternal(getSample(10), 9, 1, -1).sum())
                .isEqualTo(52);
        assertThat(ofInternal(getSample(10), 9, -1, -2).sum())
                .isEqualTo(30);
        assertThat(ofInternal(getSample(10), 9, 1, -2).sum())
                .isEqualTo(28);
    }

    @Test
    public void testSsq() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).ssq())
                .isEqualTo(385);
        assertThat(ofInternal(getSample(10), 2, 10, 1).ssq())
                .isEqualTo(380);
        assertThat(ofInternal(getSample(10), 0, 10, 2).ssq())
                .isEqualTo(165);
        assertThat(ofInternal(getSample(10), 2, 10, 2).ssq())
                .isEqualTo(164);
        assertThat(ofInternal(getSample(10), 9, -1, -1).ssq())
                .isEqualTo(385);
        assertThat(ofInternal(getSample(10), 9, 1, -1).ssq())
                .isEqualTo(380);
        assertThat(ofInternal(getSample(10), 9, -1, -2).ssq())
                .isEqualTo(220);
        assertThat(ofInternal(getSample(10), 9, 1, -2).ssq())
                .isEqualTo(216);
    }

    @Test
    public void testSsqc() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).ssqc(5))
                .isEqualTo(85);
        assertThat(ofInternal(getSample(10), 2, 10, 1).ssqc(5))
                .isEqualTo(60);
        assertThat(ofInternal(getSample(10), 0, 10, 2).ssqc(5))
                .isEqualTo(40);
        assertThat(ofInternal(getSample(10), 2, 10, 2).ssqc(5))
                .isEqualTo(24);
        assertThat(ofInternal(getSample(10), 9, -1, -1).ssqc(5))
                .isEqualTo(85);
        assertThat(ofInternal(getSample(10), 9, 1, -1).ssqc(5))
                .isEqualTo(60);
        assertThat(ofInternal(getSample(10), 9, -1, -2).ssqc(5))
                .isEqualTo(45);
        assertThat(ofInternal(getSample(10), 9, 1, -2).ssqc(5))
                .isEqualTo(36);
    }

    @Test
    public void testAverage() {
        assertThat(ofInternal(getSample(10), 0, 10, 1).average())
                .isEqualTo(5.5);
        assertThat(ofInternal(getSample(10), 2, 10, 1).average())
                .isEqualTo(6.5);
        assertThat(ofInternal(getSample(10), 0, 10, 2).average())
                .isEqualTo(5);
        assertThat(ofInternal(getSample(10), 2, 10, 2).average())
                .isEqualTo(6);
        assertThat(ofInternal(getSample(10), 9, -1, -1).average())
                .isEqualTo(5.5);
        assertThat(ofInternal(getSample(10), 9, 1, -1).average())
                .isEqualTo(6.5);
        assertThat(ofInternal(getSample(10), 9, -1, -2).average())
                .isEqualTo(6);
        assertThat(ofInternal(getSample(10), 9, 1, -2).average())
                .isEqualTo(7);
    }

    @Test
    public void testCopyDataBlock() {
        assertThatThrownBy(() -> ofInternal(getSample(10), 0, 10, 1)
                .copy(ofInternal(new double[]{11, 12, 13, 14, 15}, 0, 5, 1)))
                .describedAs("length < datablock")
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(o.getStorage()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15);
            assertThat(o.getStorage()).containsExactly(11, 2, 12, 4, 13, 6, 14, 8, 15, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 11, 12, 13, 14, 15, 16, 17, 18);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14);
            assertThat(o.getStorage()).containsExactly(1, 2, 11, 4, 12, 6, 13, 8, 14, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(o.getStorage()).containsExactly(20, 19, 18, 17, 16, 15, 14, 13, 12, 11);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15);
            assertThat(o.getStorage()).containsExactly(1, 15, 3, 14, 5, 13, 7, 12, 9, 11);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 18, 17, 16, 15, 14, 13, 12, 11);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 14, 5, 13, 7, 12, 9, 11);
        });

    }

    @Test
    public void testCopyDouble() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DoubleSeq x = DoubleSeq.of(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(o.getStorage()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DoubleSeq x = DoubleSeq.of(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15);
            assertThat(o.getStorage()).containsExactly(11, 2, 12, 4, 13, 6, 14, 8, 15, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DoubleSeq x = DoubleSeq.of(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 11, 12, 13, 14, 15, 16, 17, 18);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DoubleSeq x = DoubleSeq.of(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14);
            assertThat(o.getStorage()).containsExactly(1, 2, 11, 4, 12, 6, 13, 8, 14, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DoubleSeq x = DoubleSeq.of(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(o.getStorage()).containsExactly(20, 19, 18, 17, 16, 15, 14, 13, 12, 11);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DoubleSeq x = DoubleSeq.of(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15);
            assertThat(o.getStorage()).containsExactly(1, 15, 3, 14, 5, 13, 7, 12, 9, 11);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DoubleSeq x = DoubleSeq.of(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 18, 17, 16, 15, 14, 13, 12, 11);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DoubleSeq x = DoubleSeq.of(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10);
            o.copy(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 14, 5, 13, 7, 12, 9, 11);
        });
    }

    @Test
    public void testSwap() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.swap(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(o.getStorage()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(x.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            assertThat(x.getStorage()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.swap(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15);
            assertThat(o.getStorage()).containsExactly(11, 2, 12, 4, 13, 6, 14, 8, 15, 10);
            assertThat(x.toArray()).containsExactly(1, 3, 5, 7, 9, 16, 17, 18, 19, 20);
            assertThat(x.getStorage()).containsExactly(1, 3, 5, 7, 9, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.swap(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 11, 12, 13, 14, 15, 16, 17, 18);
            assertThat(x.toArray()).containsExactly(3, 4, 5, 6, 7, 8, 9, 10, 19, 20);
            assertThat(x.getStorage()).containsExactly(3, 4, 5, 6, 7, 8, 9, 10, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.swap(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14);
            assertThat(o.getStorage()).containsExactly(1, 2, 11, 4, 12, 6, 13, 8, 14, 10);
            assertThat(x.toArray()).containsExactly(3, 5, 7, 9, 15, 16, 17, 18, 19, 20);
            assertThat(x.getStorage()).containsExactly(3, 5, 7, 9, 15, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.swap(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(o.getStorage()).containsExactly(20, 19, 18, 17, 16, 15, 14, 13, 12, 11);
            assertThat(x.toArray()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
            assertThat(x.getStorage()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.swap(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15);
            assertThat(o.getStorage()).containsExactly(1, 15, 3, 14, 5, 13, 7, 12, 9, 11);
            assertThat(x.toArray()).containsExactly(10, 8, 6, 4, 2, 16, 17, 18, 19, 20);
            assertThat(x.getStorage()).containsExactly(10, 8, 6, 4, 2, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.swap(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 18, 17, 16, 15, 14, 13, 12, 11);
            assertThat(x.toArray()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 19, 20);
            assertThat(x.getStorage()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.swap(x);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 14, 5, 13, 7, 12, 9, 11);
            assertThat(x.toArray()).containsExactly(10, 8, 6, 4, 15, 16, 17, 18, 19, 20);
            assertThat(x.getStorage()).containsExactly(10, 8, 6, 4, 15, 16, 17, 18, 19, 20);
        });
    }

    @Test
    public void testProductDataBlockIterator() {
        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3}, 0, 3, 1);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols);
            assertThat(o.toArray()).containsExactly(32, 50, 68);
            assertThat(o.getStorage()).containsExactly(32, 50, 68);
        });

        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols);
            assertThat(o.toArray()).containsExactly(49, 76, 103);
            assertThat(o.getStorage()).containsExactly(49, 76, 103);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 2)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols);
            assertThat(o.toArray()).containsExactly(49, 76, 103);
            assertThat(o.getStorage()).containsExactly(49, 2, 76, 4, 103, 6);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 1)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols);
            assertThat(o.toArray()).containsExactly(49, 76, 103, 4, 5, 6);
            assertThat(o.getStorage()).containsExactly(49, 76, 103, 4, 5, 6);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -2)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols);
            assertThat(o.toArray()).containsExactly(49, 76, 103);
            assertThat(o.getStorage()).containsExactly(1, 103, 3, 76, 5, 49);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -1)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols);
            assertThat(o.toArray()).containsExactly(49, 76, 103, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 103, 76, 49);
        });
    }

    @Test
    public void testProductDataBlockIteratorDoubleAccumulator() {
        KahanAccumulator ka = new KahanAccumulator();
        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            ka.reset();
            DataBlock row = ofInternal(new double[]{1, 2, 3}, 0, 3, 1);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols, ka);
            assertThat(o.toArray()).containsExactly(32, 50, 68);
            assertThat(o.getStorage()).containsExactly(32, 50, 68);
        });

        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            ka.reset();
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols, ka);
            assertThat(o.toArray()).containsExactly(49, 76, 103);
            assertThat(o.getStorage()).containsExactly(49, 76, 103);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 2)).satisfies(o -> {
            ka.reset();
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols, ka);
            assertThat(o.toArray()).containsExactly(49, 76, 103);
            assertThat(o.getStorage()).containsExactly(49, 2, 76, 4, 103, 6);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 1)).satisfies(o -> {
            ka.reset();
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols, ka);
            assertThat(o.toArray()).containsExactly(49, 76, 103, 4, 5, 6);
            assertThat(o.getStorage()).containsExactly(49, 76, 103, 4, 5, 6);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -2)).satisfies(o -> {
            ka.reset();
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols, ka);
            assertThat(o.toArray()).containsExactly(49, 76, 103);
            assertThat(o.getStorage()).containsExactly(1, 103, 3, 76, 5, 49);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -1)).satisfies(o -> {
            ka.reset();
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.product(row, cols, ka);
            assertThat(o.toArray()).containsExactly(49, 76, 103, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 103, 76, 49);
        });
    }

    @Test
    public void testProductIteratorDataBlock() {
        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3}, 0, 3, 1);
            o.product(rows, col);
            assertThat(o.toArray()).containsExactly(48, 54, 60);
            assertThat(o.getStorage()).containsExactly(48, 54, 60);
        });

        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.product(rows, col);
            assertThat(o.toArray()).containsExactly(75, 84, 93);
            assertThat(o.getStorage()).containsExactly(75, 84, 93);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 2)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.product(rows, col);
            assertThat(o.toArray()).containsExactly(75, 84, 93);
            assertThat(o.getStorage()).containsExactly(75, 2, 84, 4, 93, 6);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.product(rows, col);
            assertThat(o.toArray()).containsExactly(75, 84, 93, 4, 5, 6);
            assertThat(o.getStorage()).containsExactly(75, 84, 93, 4, 5, 6);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -2)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.product(rows, col);
            assertThat(o.toArray()).containsExactly(75, 84, 93);
            assertThat(o.getStorage()).containsExactly(1, 93, 3, 84, 5, 75);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.product(rows, col);
            assertThat(o.toArray()).containsExactly(75, 84, 93, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 93, 84, 75);
        });
    }

    @Test
    public void testRobustProduct() {
        KahanAccumulator ka = new KahanAccumulator();

        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            ka.reset();
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3}, 0, 3, 1);
            o.robustProduct(rows, col, ka);
            assertThat(o.toArray()).containsExactly(48, 54, 60);
            assertThat(o.getStorage()).containsExactly(48, 54, 60);
        });

        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            ka.reset();
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.robustProduct(rows, col, ka);
            assertThat(o.toArray()).containsExactly(75, 84, 93);
            assertThat(o.getStorage()).containsExactly(75, 84, 93);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 2)).satisfies(o -> {
            ka.reset();
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.robustProduct(rows, col, ka);
            assertThat(o.toArray()).containsExactly(75, 84, 93);
            assertThat(o.getStorage()).containsExactly(75, 2, 84, 4, 93, 6);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 1)).satisfies(o -> {
            ka.reset();
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.robustProduct(rows, col, ka);
            assertThat(o.toArray()).containsExactly(75, 84, 93, 4, 5, 6);
            assertThat(o.getStorage()).containsExactly(75, 84, 93, 4, 5, 6);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -2)).satisfies(o -> {
            ka.reset();
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.robustProduct(rows, col, ka);
            assertThat(o.toArray()).containsExactly(75, 84, 93);
            assertThat(o.getStorage()).containsExactly(1, 93, 3, 84, 5, 75);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -1)).satisfies(o -> {
            ka.reset();
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.robustProduct(rows, col, ka);
            assertThat(o.toArray()).containsExactly(75, 84, 93, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 93, 84, 75);
        });
    }

    @Test
    public void testAddProductDataBlockIterator() {
        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3}, 0, 3, 1);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.addProduct(row, cols);
            assertThat(o.toArray()).containsExactly(33, 52, 71);
            assertThat(o.getStorage()).containsExactly(33, 52, 71);
        });

        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.addProduct(row, cols);
            assertThat(o.toArray()).containsExactly(50, 78, 106);
            assertThat(o.getStorage()).containsExactly(50, 78, 106);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 2)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.addProduct(row, cols);
            assertThat(o.toArray()).containsExactly(50, 79, 108);
            assertThat(o.getStorage()).containsExactly(50, 2, 79, 4, 108, 6);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 1)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.addProduct(row, cols);
            assertThat(o.toArray()).containsExactly(50, 78, 106, 4, 5, 6);
            assertThat(o.getStorage()).containsExactly(50, 78, 106, 4, 5, 6);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -2)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.addProduct(row, cols);
            assertThat(o.toArray()).containsExactly(55, 80, 105);
            assertThat(o.getStorage()).containsExactly(1, 105, 3, 80, 5, 55);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -1)).satisfies(o -> {
            DataBlock row = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator cols = m.columnsIterator();
            o.addProduct(row, cols);
            assertThat(o.toArray()).containsExactly(55, 81, 107, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 107, 81, 55);
        });
    }

    @Test
    public void testAddProductIteratorDataBlock() {
        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3}, 0, 3, 1);
            o.addProduct(rows, col);
            assertThat(o.toArray()).containsExactly(49, 56, 63);
            assertThat(o.getStorage()).containsExactly(49, 56, 63);
        });

        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.addProduct(rows, col);
            assertThat(o.toArray()).containsExactly(76, 86, 96);
            assertThat(o.getStorage()).containsExactly(76, 86, 96);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 2)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.addProduct(rows, col);
            assertThat(o.toArray()).containsExactly(76, 87, 98);
            assertThat(o.getStorage()).containsExactly(76, 2, 87, 4, 98, 6);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.addProduct(rows, col);
            assertThat(o.toArray()).containsExactly(76, 86, 96, 4, 5, 6);
            assertThat(o.getStorage()).containsExactly(76, 86, 96, 4, 5, 6);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -2)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.addProduct(rows, col);
            assertThat(o.toArray()).containsExactly(81, 88, 95);
            assertThat(o.getStorage()).containsExactly(1, 95, 3, 88, 5, 81);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator rows = m.rowsIterator();
            DataBlock col = ofInternal(new double[]{1, 2, 3, 4, 5, 6}, 0, 6, 2);
            o.addProduct(rows, col);
            assertThat(o.toArray()).containsExactly(81, 89, 97, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 97, 89, 81);
        });
    }

    @Test
    public void testApplyIntDoubleUnaryOperator() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.apply(3, (x) -> x + 5);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 9, 5, 6, 7, 8, 9, 10);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 9, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.apply(3, (x) -> x + 5);
            assertThat(o.toArray()).containsExactly(3, 4, 5, 11, 7, 8, 9, 10);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 4, 5, 11, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.apply(3, (x) -> x + 5);
            assertThat(o.toArray()).containsExactly(1, 3, 5, 12, 9);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 4, 5, 6, 12, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.apply(3, (x) -> x + 5);
            assertThat(o.toArray()).containsExactly(3, 5, 7, 14);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 14, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.apply(3, (x) -> x + 5);
            assertThat(o.toArray()).containsExactly(10, 9, 8, 12, 6, 5, 4, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 4, 5, 6, 12, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.apply(3, (x) -> x + 5);
            assertThat(o.toArray()).containsExactly(10, 9, 8, 12, 6, 5, 4, 3);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 4, 5, 6, 12, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.apply(3, (x) -> x + 5);
            assertThat(o.toArray()).containsExactly(10, 8, 6, 9, 2);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 9, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.apply(3, (x) -> x + 5);
            assertThat(o.toArray()).containsExactly(10, 8, 6, 9);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 9, 5, 6, 7, 8, 9, 10);
        });
    }

    @Test
    public void testAdd() {
        assertThatThrownBy(()
                -> ofInternal(getSample(10), 0, 10, 1).add(-2, 10))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.add(2, 10);
            assertThat(o.toArray()).containsExactly(1, 2, 13, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.add(2, 10);
            assertThat(o.toArray()).containsExactly(3, 4, 15, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.add(2, 10);
            assertThat(o.toArray()).containsExactly(1, 3, 15, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.add(2, 10);
            assertThat(o.toArray()).containsExactly(3, 5, 17, 9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.add(2, 10);
            assertThat(o.toArray()).containsExactly(10, 9, 18, 7, 6, 5, 4, 3, 2, 1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.add(2, 10);
            assertThat(o.toArray()).containsExactly(10, 9, 18, 7, 6, 5, 4, 3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.add(2, 10);
            assertThat(o.toArray()).containsExactly(10, 8, 16, 4, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.add(2, 10);
            assertThat(o.toArray()).containsExactly(10, 8, 16, 4);
        });
    }

    @Test
    public void testMul() {
        assertThatThrownBy(()
                -> ofInternal(getSample(10), 0, 10, 1).mul(-2, 10))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.mul(2, 10);
            assertThat(o.toArray()).containsExactly(1, 2, 30, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.mul(2, 10);
            assertThat(o.toArray()).containsExactly(3, 4, 50, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.mul(2, 10);
            assertThat(o.toArray()).containsExactly(1, 3, 50, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.mul(2, 10);
            assertThat(o.toArray()).containsExactly(3, 5, 70, 9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.mul(2, 10);
            assertThat(o.toArray()).containsExactly(10, 9, 80, 7, 6, 5, 4, 3, 2, 1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.mul(2, 10);
            assertThat(o.toArray()).containsExactly(10, 9, 80, 7, 6, 5, 4, 3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.mul(2, 10);
            assertThat(o.toArray()).containsExactly(10, 8, 60, 4, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.mul(2, 10);
            assertThat(o.toArray()).containsExactly(10, 8, 60, 4);
        });
    }

    @Test
    public void testSub() {
        assertThatThrownBy(()
                -> ofInternal(getSample(10), 0, 10, 1).sub(-2, 10))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.sub(2, 10);
            assertThat(o.toArray()).containsExactly(1, 2, -7, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.sub(2, 10);
            assertThat(o.toArray()).containsExactly(3, 4, -5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.sub(2, 10);
            assertThat(o.toArray()).containsExactly(1, 3, -5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.sub(2, 10);
            assertThat(o.toArray()).containsExactly(3, 5, -3, 9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.sub(2, 10);
            assertThat(o.toArray()).containsExactly(10, 9, -2, 7, 6, 5, 4, 3, 2, 1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.sub(2, 10);
            assertThat(o.toArray()).containsExactly(10, 9, -2, 7, 6, 5, 4, 3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.sub(2, 10);
            assertThat(o.toArray()).containsExactly(10, 8, -4, 4, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.sub(2, 10);
            assertThat(o.toArray()).containsExactly(10, 8, -4, 4);
        });
    }

    @Test
    public void testDiv() {
        assertThatThrownBy(()
                -> ofInternal(getSample(10), 0, 10, 1).div(-2, 10))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.div(2, 10);
            assertThat(o.toArray()).containsExactly(1, 2, 0.3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.div(2, 10);
            assertThat(o.toArray()).containsExactly(3, 4, 0.5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.div(2, 10);
            assertThat(o.toArray()).containsExactly(1, 3, 0.5, 7, 9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.div(2, 10);
            assertThat(o.toArray()).containsExactly(3, 5, 0.7, 9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.div(2, 10);
            assertThat(o.toArray()).containsExactly(10, 9, 0.8, 7, 6, 5, 4, 3, 2, 1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.div(2, 10);
            assertThat(o.toArray()).containsExactly(10, 9, 0.8, 7, 6, 5, 4, 3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.div(2, 10);
            assertThat(o.toArray()).containsExactly(10, 8, 0.6, 4, 2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.div(2, 10);
            assertThat(o.toArray()).containsExactly(10, 8, 0.6, 4);
        });
    }

    @Test
    public void testSetIteratorFunction() {
        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.set(blocks, x -> x.sum());
            assertThat(o.toArray()).containsExactly(21, 24, 27);
            assertThat(o.getStorage()).containsExactly(21, 24, 27);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 2)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.set(blocks, x -> x.sum());
            assertThat(o.toArray()).containsExactly(21, 24, 27);
            assertThat(o.getStorage()).containsExactly(21, 2, 24, 4, 27, 6);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.set(blocks, x -> x.sum());
            assertThat(o.toArray()).containsExactly(21, 24, 27, 4, 5, 6);
            assertThat(o.getStorage()).containsExactly(21, 24, 27, 4, 5, 6);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -2)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.set(blocks, x -> x.sum());
            assertThat(o.toArray()).containsExactly(21, 24, 27);
            assertThat(o.getStorage()).containsExactly(1, 27, 3, 24, 5, 21);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.set(blocks, x -> x.sum());
            assertThat(o.toArray()).containsExactly(21, 24, 27, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 27, 24, 21);
        });
    }

    @Test
    public void testApplyIteratorFunction() {
        assertThat(ofInternal(getSample(3), 0, 3, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.apply(blocks, x -> x.sum(), (y, z) -> y + z);
            assertThat(o.toArray()).containsExactly(22, 26, 30);
            assertThat(o.getStorage()).containsExactly(22, 26, 30);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 2)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.apply(blocks, x -> x.sum(), (y, z) -> y + z);
            assertThat(o.toArray()).containsExactly(22, 27, 32);
            assertThat(o.getStorage()).containsExactly(22, 2, 27, 4, 32, 6);
        });

        assertThat(ofInternal(getSample(6), 0, 6, 1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.apply(blocks, x -> x.sum(), (y, z) -> y + z);
            assertThat(o.toArray()).containsExactly(22, 26, 30, 4, 5, 6);
            assertThat(o.getStorage()).containsExactly(22, 26, 30, 4, 5, 6);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -2)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.apply(blocks, x -> x.sum(), (y, z) -> y + z);
            assertThat(o.toArray()).containsExactly(27, 28, 29);
            assertThat(o.getStorage()).containsExactly(1, 29, 3, 28, 5, 27);
        });

        assertThat(ofInternal(getSample(6), 5, -1, -1)).satisfies(o -> {
            double[] data = new double[]{4, 5, 6, 7, 8, 9, 10, 11, 12};
            Matrix m = Matrix.builder(data).ncolumns(3).nrows(3).build();
            DataBlockIterator blocks = m.rowsIterator();
            o.apply(blocks, x -> x.sum(), (y, z) -> y + z);
            assertThat(o.toArray()).containsExactly(27, 29, 31, 3, 2, 1);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 31, 29, 27);
        });
    }

    @Test
    public void testApplyDataBlockDoubleBinaryOPerator() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock db = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.apply(db, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock db = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.apply(db, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(14, 16, 18, 20, 22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 14, 16, 18, 20, 22, 24, 26, 28);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock db = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.apply(db, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(12, 15, 18, 21, 24);
            assertThat(o.getStorage()).containsExactly(12, 2, 15, 4, 18, 6, 21, 8, 24, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock db = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.apply(db, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(14, 17, 20, 23);
            assertThat(o.getStorage()).containsExactly(1, 2, 14, 4, 17, 6, 20, 8, 23, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock db = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.apply(db, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21, 21, 21);
            assertThat(o.getStorage()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21, 21, 21);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock db = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.apply(db, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21);
            assertThat(o.getStorage()).containsExactly(1, 2, 21, 21, 21, 21, 21, 21, 21, 21);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock db = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.apply(db, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(21, 20, 19, 18, 17);
            assertThat(o.getStorage()).containsExactly(1, 17, 3, 18, 5, 19, 7, 20, 9, 21);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock db = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.apply(db, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(21, 20, 19, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 18, 5, 19, 7, 20, 9, 21);
        });
    }

    @Test
    public void testApplyDoublesDoubleBinaryOPerator() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DoubleSeq d = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.apply(d, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DoubleSeq d = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.apply(d, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(14, 16, 18, 20, 22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 14, 16, 18, 20, 22, 24, 26, 28);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DoubleSeq d = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.apply(d, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(12, 15, 18, 21, 24);
            assertThat(o.getStorage()).containsExactly(12, 2, 15, 4, 18, 6, 21, 8, 24, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DoubleSeq d = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.apply(d, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(14, 17, 20, 23);
            assertThat(o.getStorage()).containsExactly(1, 2, 14, 4, 17, 6, 20, 8, 23, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DoubleSeq d = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.apply(d, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21, 21, 21);
            assertThat(o.getStorage()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21, 21, 21);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DoubleSeq d = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.apply(d, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21);
            assertThat(o.getStorage()).containsExactly(1, 2, 21, 21, 21, 21, 21, 21, 21, 21);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DoubleSeq d = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.apply(d, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(21, 20, 19, 18, 17);
            assertThat(o.getStorage()).containsExactly(1, 17, 3, 18, 5, 19, 7, 20, 9, 21);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DoubleSeq d = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.apply(d, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(21, 20, 19, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 18, 5, 19, 7, 20, 9, 21);
        });
    }

    @Test
    public void testApplyDoubleUnaryOperator() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.apply((x) -> x * 2);
            assertThat(o.toArray()).containsExactly(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
            assertThat(o.getStorage()).containsExactly(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.apply((x) -> x * 2);
            assertThat(o.toArray()).containsExactly(6, 8, 10, 12, 14, 16, 18, 20);
            assertThat(o.getStorage()).containsExactly(1, 2, 6, 8, 10, 12, 14, 16, 18, 20);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.apply((x) -> x * 2);
            assertThat(o.toArray()).containsExactly(2, 6, 10, 14, 18);
            assertThat(o.getStorage()).containsExactly(2, 2, 6, 4, 10, 6, 14, 8, 18, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.apply((x) -> x * 2);
            assertThat(o.toArray()).containsExactly(6, 10, 14, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 6, 4, 10, 6, 14, 8, 18, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.apply((x) -> x * 2);
            assertThat(o.toArray()).containsExactly(20, 18, 16, 14, 12, 10, 8, 6, 4, 2);
            assertThat(o.getStorage()).containsExactly(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.apply((x) -> x * 2);
            assertThat(o.toArray()).containsExactly(20, 18, 16, 14, 12, 10, 8, 6);
            assertThat(o.getStorage()).containsExactly(1, 2, 6, 8, 10, 12, 14, 16, 18, 20);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.apply((x) -> x * 2);
            assertThat(o.toArray()).containsExactly(20, 16, 12, 8, 4);
            assertThat(o.getStorage()).containsExactly(1, 4, 3, 8, 5, 12, 7, 16, 9, 20);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.apply((x) -> x * 2);
            assertThat(o.toArray()).containsExactly(20, 16, 12, 8);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 8, 5, 12, 7, 16, 9, 20);
        });
    }

    @Test
    public void testAddDouble() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.add(0);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.add(10);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.add(10);
            assertThat(o.toArray()).containsExactly(13, 14, 15, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.add(10);
            assertThat(o.toArray()).containsExactly(11, 13, 15, 17, 19);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.add(10);
            assertThat(o.toArray()).containsExactly(13, 15, 17, 19);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.add(10);
            assertThat(o.toArray()).containsExactly(20, 19, 18, 17, 16, 15, 14, 13, 12, 11);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.add(10);
            assertThat(o.toArray()).containsExactly(20, 19, 18, 17, 16, 15, 14, 13);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.add(10);
            assertThat(o.toArray()).containsExactly(20, 18, 16, 14, 12);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.add(10);
            assertThat(o.toArray()).containsExactly(20, 18, 16, 14);
        });
    }

    @Test
    public void testChs() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.chs();
            assertThat(o.toArray()).containsExactly(-1, -2, -3, -4, -5, -6, -7, -8, -9, -10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.chs();
            assertThat(o.toArray()).containsExactly(-3, -4, -5, -6, -7, -8, -9, -10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.chs();
            assertThat(o.toArray()).containsExactly(-1, -3, -5, -7, -9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.chs();
            assertThat(o.toArray()).containsExactly(-3, -5, -7, -9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.chs();
            assertThat(o.toArray()).containsExactly(-10, -9, -8, -7, -6, -5, -4, -3, -2, -1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.chs();
            assertThat(o.toArray()).containsExactly(-10, -9, -8, -7, -6, -5, -4, -3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.chs();
            assertThat(o.toArray()).containsExactly(-10, -8, -6, -4, -2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.chs();
            assertThat(o.toArray()).containsExactly(-10, -8, -6, -4);
        });
    }

    @Test
    public void testSubDouble() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.sub(0);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.sub(10);
            assertThat(o.toArray()).containsExactly(-9, -8, -7, -6, -5, -4, -3, -2, -1, 0);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.sub(10);
            assertThat(o.toArray()).containsExactly(-7, -6, -5, -4, -3, -2, -1, 0);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.sub(10);
            assertThat(o.toArray()).containsExactly(-9, -7, -5, -3, -1);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.sub(10);
            assertThat(o.toArray()).containsExactly(-7, -5, -3, -1);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.sub(10);
            assertThat(o.toArray()).containsExactly(0, -1, -2, -3, -4, -5, -6, -7, -8, -9);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.sub(10);
            assertThat(o.toArray()).containsExactly(0, -1, -2, -3, -4, -5, -6, -7);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.sub(10);
            assertThat(o.toArray()).containsExactly(0, -2, -4, -6, -8);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.sub(10);
            assertThat(o.toArray()).containsExactly(0, -2, -4, -6);
        });
    }

    @Test
    public void testMulDouble() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.mul(0);
            assertThat(o.toArray()).containsExactly(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.mul(1);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.mul(10);
            assertThat(o.toArray()).containsExactly(10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.mul(10);
            assertThat(o.toArray()).containsExactly(30, 40, 50, 60, 70, 80, 90, 100);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.mul(10);
            assertThat(o.toArray()).containsExactly(10, 30, 50, 70, 90);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.mul(10);
            assertThat(o.toArray()).containsExactly(30, 50, 70, 90);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.mul(10);
            assertThat(o.toArray()).containsExactly(100, 90, 80, 70, 60, 50, 40, 30, 20, 10);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.mul(10);
            assertThat(o.toArray()).containsExactly(100, 90, 80, 70, 60, 50, 40, 30);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.mul(10);
            assertThat(o.toArray()).containsExactly(100, 80, 60, 40, 20);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.mul(10);
            assertThat(o.toArray()).containsExactly(100, 80, 60, 40);
        });
    }

    @Test
    public void testDivDouble() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.div(0);
            assertThat(o.toArray()).containsExactly(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.div(1);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.div(10);
            assertThat(o.toArray()).containsExactly(.1, .2, .3, .4, .5, .6, .7, .8, .9, 1);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.div(10);
            assertThat(o.toArray()).containsExactly(.3, .4, .5, .6, .7, .8, .9, 1);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.div(10);
            assertThat(o.toArray()).containsExactly(.1, .3, .5, .7, .9);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.div(10);
            assertThat(o.toArray()).containsExactly(.3, .5, .7, .9);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.div(10);
            assertThat(o.toArray()).containsExactly(1, .9, .8, .7, .6, .5, .4, .3, .2, .1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.div(10);
            assertThat(o.toArray()).containsExactly(1, .9, .8, .7, .6, .5, .4, .3);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.div(10);
            assertThat(o.toArray()).containsExactly(1, .8, .6, .4, .2);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.div(10);
            assertThat(o.toArray()).containsExactly(1, .8, .6, .4);
        });
    }

    @Test
    public void testSetDataBlockDoubleUnaryOperator() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36);
            assertThat(o.getStorage()).containsExactly(1, 2, 22, 24, 26, 28, 30, 32, 34, 36);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(22, 2, 24, 4, 26, 6, 28, 8, 30, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 22, 4, 24, 6, 26, 8, 28, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(40, 38, 36, 34, 32, 30, 28, 26, 24, 22);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36);
            assertThat(o.getStorage()).containsExactly(1, 2, 36, 34, 32, 30, 28, 26, 24, 22);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(1, 30, 3, 28, 5, 26, 7, 24, 9, 22);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 28, 5, 26, 7, 24, 9, 22);
        });
    }

    @Test
    public void testSetDataBlockDataBlockDoubleBinaryOperator() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            DataBlock db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 0, 10, 1);
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46, 48, 50);
            assertThat(o.getStorage()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46, 48, 50);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            DataBlock db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 0, 10, 1);
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46);
            assertThat(o.getStorage()).containsExactly(1, 2, 32, 34, 36, 38, 40, 42, 44, 46);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            DataBlock db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 0, 10, 1);
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(32, 2, 34, 4, 36, 6, 38, 8, 40, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            DataBlock db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 0, 10, 1);
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38);
            assertThat(o.getStorage()).containsExactly(1, 2, 32, 4, 34, 6, 36, 8, 38, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            DataBlock db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 0, 10, 1);
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46, 48, 50);
            assertThat(o.getStorage()).containsExactly(50, 48, 46, 44, 42, 40, 38, 36, 34, 32);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            DataBlock db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 0, 10, 1);
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46);
            assertThat(o.getStorage()).containsExactly(1, 2, 46, 44, 42, 40, 38, 36, 34, 32);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            DataBlock db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 0, 10, 1);
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(1, 40, 3, 38, 5, 36, 7, 34, 9, 32);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            DataBlock db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30}, 0, 10, 1);
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 38, 5, 36, 7, 34, 9, 32);
        });
    }

    @Test
    public void testSetDoublesDoubleUnaryOperator() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DoubleSeq x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DoubleSeq x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36);
            assertThat(o.getStorage()).containsExactly(1, 2, 22, 24, 26, 28, 30, 32, 34, 36);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DoubleSeq x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(22, 2, 24, 4, 26, 6, 28, 8, 30, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DoubleSeq x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 22, 4, 24, 6, 26, 8, 28, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DoubleSeq x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(40, 38, 36, 34, 32, 30, 28, 26, 24, 22);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DoubleSeq x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36);
            assertThat(o.getStorage()).containsExactly(1, 2, 36, 34, 32, 30, 28, 26, 24, 22);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DoubleSeq x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(1, 30, 3, 28, 5, 26, 7, 24, 9, 22);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DoubleSeq x = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            o.set(x, (y) -> y * 2);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 28, 5, 26, 7, 24, 9, 22);
        });
    }

    @Test
    public void testSetDoublesDoublesDoubleBinaryOperator() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DoubleSeq db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            DoubleSeq db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46, 48, 50);
            assertThat(o.getStorage()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46, 48, 50);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DoubleSeq db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            DoubleSeq db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46);
            assertThat(o.getStorage()).containsExactly(1, 2, 32, 34, 36, 38, 40, 42, 44, 46);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DoubleSeq db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            DoubleSeq db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(32, 2, 34, 4, 36, 6, 38, 8, 40, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DoubleSeq db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            DoubleSeq db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38);
            assertThat(o.getStorage()).containsExactly(1, 2, 32, 4, 34, 6, 36, 8, 38, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DoubleSeq db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            DoubleSeq db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46, 48, 50);
            assertThat(o.getStorage()).containsExactly(50, 48, 46, 44, 42, 40, 38, 36, 34, 32);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DoubleSeq db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            DoubleSeq db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40, 42, 44, 46);
            assertThat(o.getStorage()).containsExactly(1, 2, 46, 44, 42, 40, 38, 36, 34, 32);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DoubleSeq db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            DoubleSeq db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(1, 40, 3, 38, 5, 36, 7, 34, 9, 32);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DoubleSeq db1 = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
            DoubleSeq db2 = ofInternal(new double[]{21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
            o.set(db1, db2, (x, y) -> x + y);
            assertThat(o.toArray()).containsExactly(32, 34, 36, 38);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 38, 5, 36, 7, 34, 9, 32);
        });
    }

    @Test
    public void testSetAY() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(0, y);
            assertThat(o.toArray()).containsExactly(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            assertThat(o.getStorage()).containsExactly(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(1, y);
            assertThat(o.toArray()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
            assertThat(o.getStorage()).containsExactly(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(-1, y);
            assertThat(o.toArray()).containsExactly(-11, -12, -13, -14, -15, -16, -17, -18, -19, -20);
            assertThat(o.getStorage()).containsExactly(-11, -12, -13, -14, -15, -16, -17, -18, -19, -20);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(2, y);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(2, y);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36);
            assertThat(o.getStorage()).containsExactly(1, 2, 22, 24, 26, 28, 30, 32, 34, 36);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(2, y);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(22, 2, 24, 4, 26, 6, 28, 8, 30, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(2, y);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 22, 4, 24, 6, 26, 8, 28, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(2, y);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36, 38, 40);
            assertThat(o.getStorage()).containsExactly(40, 38, 36, 34, 32, 30, 28, 26, 24, 22);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(2, y);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30, 32, 34, 36);
            assertThat(o.getStorage()).containsExactly(1, 2, 36, 34, 32, 30, 28, 26, 24, 22);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(2, y);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(1, 30, 3, 28, 5, 26, 7, 24, 9, 22);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.setAY(2, y);
            assertThat(o.toArray()).containsExactly(22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 28, 5, 26, 7, 24, 9, 22);
        });
    }

    @Test
    public void testAddAY() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(0, y);
            assertThat(o.toArray()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(1, y);
            assertThat(o.toArray()).containsExactly(12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(-1, y);
            assertThat(o.toArray()).containsExactly(-10, -10, -10, -10, -10, -10, -10, -10, -10, -10);
            assertThat(o.getStorage()).containsExactly(-10, -10, -10, -10, -10, -10, -10, -10, -10, -10);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(2, y);
            assertThat(o.toArray()).containsExactly(23, 26, 29, 32, 35, 38, 41, 44, 47, 50);
            assertThat(o.getStorage()).containsExactly(23, 26, 29, 32, 35, 38, 41, 44, 47, 50);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(2, y);
            assertThat(o.toArray()).containsExactly(25, 28, 31, 34, 37, 40, 43, 46);
            assertThat(o.getStorage()).containsExactly(1, 2, 25, 28, 31, 34, 37, 40, 43, 46);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(2, y);
            assertThat(o.toArray()).containsExactly(23, 27, 31, 35, 39);
            assertThat(o.getStorage()).containsExactly(23, 2, 27, 4, 31, 6, 35, 8, 39, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(2, y);
            assertThat(o.toArray()).containsExactly(25, 29, 33, 37);
            assertThat(o.getStorage()).containsExactly(1, 2, 25, 4, 29, 6, 33, 8, 37, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(2, y);
            assertThat(o.toArray()).containsExactly(32, 33, 34, 35, 36, 37, 38, 39, 40, 41);
            assertThat(o.getStorage()).containsExactly(41, 40, 39, 38, 37, 36, 35, 34, 33, 32);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(2, y);
            assertThat(o.toArray()).containsExactly(32, 33, 34, 35, 36, 37, 38, 39);
            assertThat(o.getStorage()).containsExactly(1, 2, 39, 38, 37, 36, 35, 34, 33, 32);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(2, y);
            assertThat(o.toArray()).containsExactly(32, 32, 32, 32, 32);
            assertThat(o.getStorage()).containsExactly(1, 32, 3, 32, 5, 32, 7, 32, 9, 32);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.addAY(2, y);
            assertThat(o.toArray()).containsExactly(32, 32, 32, 32);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 32, 5, 32, 7, 32, 9, 32);
        });
    }

    @Test
    public void testAddDataBlock() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.add(y);
            assertThat(o.toArray()).containsExactly(12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
            assertThat(o.getStorage()).containsExactly(12, 14, 16, 18, 20, 22, 24, 26, 28, 30);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.add(y);
            assertThat(o.toArray()).containsExactly(14, 16, 18, 20, 22, 24, 26, 28);
            assertThat(o.getStorage()).containsExactly(1, 2, 14, 16, 18, 20, 22, 24, 26, 28);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.add(y);
            assertThat(o.toArray()).containsExactly(12, 15, 18, 21, 24);
            assertThat(o.getStorage()).containsExactly(12, 2, 15, 4, 18, 6, 21, 8, 24, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.add(y);
            assertThat(o.toArray()).containsExactly(14, 17, 20, 23);
            assertThat(o.getStorage()).containsExactly(1, 2, 14, 4, 17, 6, 20, 8, 23, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.add(y);
            assertThat(o.toArray()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21, 21, 21);
            assertThat(o.getStorage()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21, 21, 21);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.add(y);
            assertThat(o.toArray()).containsExactly(21, 21, 21, 21, 21, 21, 21, 21);
            assertThat(o.getStorage()).containsExactly(1, 2, 21, 21, 21, 21, 21, 21, 21, 21);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.add(y);
            assertThat(o.toArray()).containsExactly(21, 20, 19, 18, 17);
            assertThat(o.getStorage()).containsExactly(1, 17, 3, 18, 5, 19, 7, 20, 9, 21);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.add(y);
            assertThat(o.toArray()).containsExactly(21, 20, 19, 18);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 18, 5, 19, 7, 20, 9, 21);
        });
    }

    @Test
    public void testSubDataBlock() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.sub(y);
            assertThat(o.toArray()).containsExactly(-10, -10, -10, -10, -10, -10, -10, -10, -10, -10);
            assertThat(o.getStorage()).containsExactly(-10, -10, -10, -10, -10, -10, -10, -10, -10, -10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.sub(y);
            assertThat(o.toArray()).containsExactly(-8, -8, -8, -8, -8, -8, -8, -8);
            assertThat(o.getStorage()).containsExactly(1, 2, -8, -8, -8, -8, -8, -8, -8, -8);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.sub(y);
            assertThat(o.toArray()).containsExactly(-10, -9, -8, -7, -6);
            assertThat(o.getStorage()).containsExactly(-10, 2, -9, 4, -8, 6, -7, 8, -6, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.sub(y);
            assertThat(o.toArray()).containsExactly(-8, -7, -6, -5);
            assertThat(o.getStorage()).containsExactly(1, 2, -8, 4, -7, 6, -6, 8, -5, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.sub(y);
            assertThat(o.toArray()).containsExactly(-1, -3, -5, -7, -9, -11, -13, -15, -17, -19);
            assertThat(o.getStorage()).containsExactly(-19, -17, -15, -13, -11, -9, -7, -5, -3, -1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.sub(y);
            assertThat(o.toArray()).containsExactly(-1, -3, -5, -7, -9, -11, -13, -15);
            assertThat(o.getStorage()).containsExactly(1, 2, -15, -13, -11, -9, -7, -5, -3, -1);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.sub(y);
            assertThat(o.toArray()).containsExactly(-1, -4, -7, -10, -13);
            assertThat(o.getStorage()).containsExactly(1, -13, 3, -10, 5, -7, 7, -4, 9, -1);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.sub(y);
            assertThat(o.toArray()).containsExactly(-1, -4, -7, -10);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, -10, 5, -7, 7, -4, 9, -1);
        });
    }

    @Test
    public void testMulDataBlock() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.mul(y);
            assertThat(o.toArray()).containsExactly(11, 24, 39, 56, 75, 96, 119, 144, 171, 200);
            assertThat(o.getStorage()).containsExactly(11, 24, 39, 56, 75, 96, 119, 144, 171, 200);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.mul(y);
            assertThat(o.toArray()).containsExactly(33, 48, 65, 84, 105, 128, 153, 180);
            assertThat(o.getStorage()).containsExactly(1, 2, 33, 48, 65, 84, 105, 128, 153, 180);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.mul(y);
            assertThat(o.toArray()).containsExactly(11, 36, 65, 98, 135);
            assertThat(o.getStorage()).containsExactly(11, 2, 36, 4, 65, 6, 98, 8, 135, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.mul(y);
            assertThat(o.toArray()).containsExactly(33, 60, 91, 126);
            assertThat(o.getStorage()).containsExactly(1, 2, 33, 4, 60, 6, 91, 8, 126, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.mul(y);
            assertThat(o.toArray()).containsExactly(110, 108, 104, 98, 90, 80, 68, 54, 38, 20);
            assertThat(o.getStorage()).containsExactly(20, 38, 54, 68, 80, 90, 98, 104, 108, 110);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.mul(y);
            assertThat(o.toArray()).containsExactly(110, 108, 104, 98, 90, 80, 68, 54);
            assertThat(o.getStorage()).containsExactly(1, 2, 54, 68, 80, 90, 98, 104, 108, 110);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.mul(y);
            assertThat(o.toArray()).containsExactly(110, 96, 78, 56, 30);
            assertThat(o.getStorage()).containsExactly(1, 30, 3, 56, 5, 78, 7, 96, 9, 110);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{11, 12, 13, 14, 15, 16, 17, 18, 19, 20}, 0, 10, 1);
            o.mul(y);
            assertThat(o.toArray()).containsExactly(110, 96, 78, 56);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 56, 5, 78, 7, 96, 9, 110);
        });
    }

    @Test
    public void testDivDataBlock() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{10, 2, 10, 2, 10, 2, 10, 2, 10, 2}, 0, 10, 1);
            o.div(y);
            assertThat(o.toArray()).containsExactly(0.1, 1, 0.3, 2, 0.5, 3, 0.7, 4, 0.9, 5);
            assertThat(o.getStorage()).containsExactly(0.1, 1, 0.3, 2, 0.5, 3, 0.7, 4, 0.9, 5);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{10, 2, 10, 2, 10, 2, 10, 2, 10, 2}, 0, 10, 1);
            o.div(y);
            assertThat(o.toArray()).containsExactly(0.3, 2, 0.5, 3, 0.7, 4, 0.9, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 0.3, 2, 0.5, 3, 0.7, 4, 0.9, 5);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{10, 2, 10, 2, 10, 2, 10, 2, 10, 2}, 0, 10, 1);
            o.div(y);
            assertThat(o.toArray()).containsExactly(0.1, 1.5, 0.5, 3.5, 0.9);
            assertThat(o.getStorage()).containsExactly(0.1, 2, 1.5, 4, 0.5, 6, 3.5, 8, 0.9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{10, 2, 10, 2, 10, 2, 10, 2, 10, 2}, 0, 10, 1);
            o.div(y);
            assertThat(o.toArray()).containsExactly(0.3, 2.5, 0.7, 4.5);
            assertThat(o.getStorage()).containsExactly(1, 2, 0.3, 4, 2.5, 6, 0.7, 8, 4.5, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{10, 2, 10, 2, 10, 2, 10, 2, 10, 2}, 0, 10, 1);
            o.div(y);
            assertThat(o.toArray()).containsExactly(1.0, 4.5, 0.8, 3.5, 0.6, 2.5, 0.4, 1.5, 0.2, 0.5);
            assertThat(o.getStorage()).containsExactly(0.5, 0.2, 1.5, 0.4, 2.5, 0.6, 3.5, 0.8, 4.5, 1.0);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{10, 2, 10, 2, 10, 2, 10, 2, 10, 2}, 0, 10, 1);
            o.div(y);
            assertThat(o.toArray()).containsExactly(1.0, 4.5, 0.8, 3.5, 0.6, 2.5, 0.4, 1.5);
            assertThat(o.getStorage()).containsExactly(1, 2, 1.5, 0.4, 2.5, 0.6, 3.5, 0.8, 4.5, 1.0);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{10, 2, 10, 2, 10, 2, 10, 2, 10, 2}, 0, 10, 1);
            o.div(y);
            assertThat(o.toArray()).containsExactly(1.0, 4.0, 0.6, 2.0, 0.2);
            assertThat(o.getStorage()).containsExactly(1, 0.2, 3, 2.0, 5, 0.6, 7, 4.0, 9, 1.0);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DataBlock y = ofInternal(new double[]{10, 2, 10, 2, 10, 2, 10, 2, 10, 2}, 0, 10, 1);
            o.div(y);
            assertThat(o.toArray()).containsExactly(1.0, 4.0, 0.6, 2.0);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 2.0, 5, 0.6, 7, 4.0, 9, 1.0);
        });
    }

    @Test
    public void testSetDoubleSupplier() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.set(() -> 5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.set(() -> 5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 5, 5, 5, 5, 5, 5, 5, 5);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.set(() -> 5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(5, 2, 5, 4, 5, 6, 5, 8, 5, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.set(() -> 5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 5, 4, 5, 6, 5, 8, 5, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.set(() -> 5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.set(() -> 5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 5, 5, 5, 5, 5, 5, 5, 5);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.set(() -> 5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 5, 3, 5, 5, 5, 7, 5, 9, 5);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.set(() -> 5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 5, 5, 5, 7, 5, 9, 5);
        });
    }

    @Test
    public void testSetIntToDoubleFunction() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.set(y -> y + 5);
            assertThat(o.toArray()).containsExactly(5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
            assertThat(o.getStorage()).containsExactly(5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.set(y -> y + 5);
            assertThat(o.toArray()).containsExactly(5, 6, 7, 8, 9, 10, 11, 12);
            assertThat(o.getStorage()).containsExactly(1, 2, 5, 6, 7, 8, 9, 10, 11, 12);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.set(y -> y + 5);
            assertThat(o.toArray()).containsExactly(5, 6, 7, 8, 9);
            assertThat(o.getStorage()).containsExactly(5, 2, 6, 4, 7, 6, 8, 8, 9, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.set(y -> y + 5);
            assertThat(o.toArray()).containsExactly(5, 6, 7, 8);
            assertThat(o.getStorage()).containsExactly(1, 2, 5, 4, 6, 6, 7, 8, 8, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.set(y -> y + 5);
            assertThat(o.toArray()).containsExactly(5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
            assertThat(o.getStorage()).containsExactly(14, 13, 12, 11, 10, 9, 8, 7, 6, 5);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.set(y -> y + 5);
            assertThat(o.toArray()).containsExactly(5, 6, 7, 8, 9, 10, 11, 12);
            assertThat(o.getStorage()).containsExactly(1, 2, 12, 11, 10, 9, 8, 7, 6, 5);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.set(y -> y + 5);
            assertThat(o.toArray()).containsExactly(5, 6, 7, 8, 9);
            assertThat(o.getStorage()).containsExactly(1, 9, 3, 8, 5, 7, 7, 6, 9, 5);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.set(y -> y + 5);
            assertThat(o.toArray()).containsExactly(5, 6, 7, 8);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 8, 5, 7, 7, 6, 9, 5);
        });
    }

    @Test
    public void testSetDouble() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            o.set(5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            o.set(5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 5, 5, 5, 5, 5, 5, 5, 5);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            o.set(5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(5, 2, 5, 4, 5, 6, 5, 8, 5, 10);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            o.set(5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 5, 4, 5, 6, 5, 8, 5, 10);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            o.set(5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            o.set(5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 5, 5, 5, 5, 5, 5, 5, 5);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            o.set(5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 5, 3, 5, 5, 5, 7, 5, 9, 5);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            o.set(5);
            assertThat(o.toArray()).containsExactly(5, 5, 5, 5);
            assertThat(o.getStorage()).containsExactly(1, 2, 3, 5, 5, 5, 7, 5, 9, 5);
        });
    }

    @Test
    public void testComputeIteratively() {
        assertThat(ofInternal(getSample(10), 0, 10, 1)).satisfies(o -> {
            DoubleBinaryOperator fn = (x, y) -> x + y;
            assertThat(o.reduce(2, fn)).isEqualTo(57);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 1)).satisfies(o -> {
            DoubleBinaryOperator fn = (x, y) -> x + y;
            assertThat(o.reduce(2, fn)).isEqualTo(54);
        });

        assertThat(ofInternal(getSample(10), 0, 10, 2)).satisfies(o -> {
            DoubleBinaryOperator fn = (x, y) -> x + y;
            assertThat(o.reduce(2, fn)).isEqualTo(27);
        });

        assertThat(ofInternal(getSample(10), 2, 10, 2)).satisfies(o -> {
            DoubleBinaryOperator fn = (x, y) -> x + y;
            assertThat(o.reduce(2, fn)).isEqualTo(26);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -1)).satisfies(o -> {
            DoubleBinaryOperator fn = (x, y) -> x + y;
            assertThat(o.reduce(2, fn)).isEqualTo(57);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -1)).satisfies(o -> {
            DoubleBinaryOperator fn = (x, y) -> x + y;
            assertThat(o.reduce(2, fn)).isEqualTo(54);
        });

        assertThat(ofInternal(getSample(10), 9, -1, -2)).satisfies(o -> {
            DoubleBinaryOperator fn = (x, y) -> x + y;
            assertThat(o.reduce(2, fn)).isEqualTo(32);
        });

        assertThat(ofInternal(getSample(10), 9, 1, -2)).satisfies(o -> {
            DoubleBinaryOperator fn = (x, y) -> x + y;
            assertThat(o.reduce(2, fn)).isEqualTo(30);
        });
    }

    @Test
    public void testMin() {
        assertThat(ofInternal(getSample(10), 2, 2, 1).min()).isEqualTo(0);
        assertThat(ofInternal(getSample(10), 0, 10, 1).min()).isEqualTo(1);
        assertThat(ofInternal(getSample(10), 2, 10, 1).min()).isEqualTo(3);
        assertThat(ofInternal(getSample(10), 0, 10, 2).min()).isEqualTo(1);
        assertThat(ofInternal(getSample(10), 2, 10, 2).min()).isEqualTo(3);
        assertThat(ofInternal(getSample(10), 9, -1, -1).min()).isEqualTo(1);
        assertThat(ofInternal(getSample(10), 9, 1, -1).min()).isEqualTo(3);
        assertThat(ofInternal(getSample(10), 9, -1, -2).min()).isEqualTo(2);
        assertThat(ofInternal(getSample(10), 9, 1, -2).min()).isEqualTo(4);
    }

    @Test
    public void testMax() {
        assertThat(ofInternal(getSample(10), 2, 2, 1).max()).isEqualTo(0);
        assertThat(ofInternal(getSample(10), 0, 10, 1).max()).isEqualTo(10);
        assertThat(ofInternal(getSample(10), 2, 10, 1).max()).isEqualTo(10);
        assertThat(ofInternal(getSample(10), 0, 10, 2).max()).isEqualTo(9);
        assertThat(ofInternal(getSample(10), 2, 10, 2).max()).isEqualTo(9);
        assertThat(ofInternal(getSample(10), 9, -1, -1).max()).isEqualTo(10);
        assertThat(ofInternal(getSample(10), 9, 1, -1).max()).isEqualTo(10);
        assertThat(ofInternal(getSample(10), 9, -1, -2).max()).isEqualTo(10);
        assertThat(ofInternal(getSample(10), 9, 1, -2).max()).isEqualTo(10);
    }

    @Test
    public void testAutoApply() {
        double[] data = new double[100];
        DataBlock A = DataBlock.ofInternal(data, 2, 102, 10);
        A.set(i -> i + 1);
        A.autoApply(-1, (a, b) -> (a - b));
        assertThat(A.drop(1, 0).allMatch(x -> x == 1)).isTrue();
        A.set(i -> i + 1);
        A.autoApply(1, (a, b) -> (b - a));
        assertThat(A.drop(0, 1).allMatch(x -> x == 1)).isTrue();
        A.set(i -> i + 1);
        A.autoApply(-2, (a, b) -> (a - b));
        assertThat(A.drop(2, 0).allMatch(x -> x == 2)).isTrue();
        A.set(i -> i + 1);
        A.autoApply(2, (a, b) -> (b - a));
        assertThat(A.drop(0, 2).allMatch(x -> x == 2)).isTrue();
    }

    @Test
    public void testApplyRecursively() {
        double[] data = new double[100];
        DataBlock A = DataBlock.ofInternal(data, 2, 102, 10);
        A.set(i -> 1);
        A.applyRecursively(1, (a, b) -> (a + b));
        assertThat(A.last() == A.length()).isTrue();
        A.set(i -> 1);
        A.applyRecursively(-1, (a, b) -> (a + b));
        assertThat(A.first() == A.length()).isTrue();
    }

    private double[] getSample(int size) {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = i + 1;
        }
        return result;
    }
}
