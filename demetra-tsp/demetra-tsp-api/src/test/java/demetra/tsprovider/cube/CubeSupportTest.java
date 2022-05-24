/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package demetra.tsprovider.cube;

import _util.tsproviders.XCubeConnection;
import demetra.io.ResourceWatcher;
import demetra.timeseries.TsInformationType;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.stream.DataSetTs;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static demetra.tsprovider.DataSet.Kind.COLLECTION;
import static demetra.tsprovider.DataSet.Kind.SERIES;
import static demetra.tsprovider.cube.CubeIdTest.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class CubeSupportTest {

    private final String providerName = "provider";
    private final DataSource dataSource = DataSource.of(providerName, "");
    private final DataSet col = DataSet.builder(dataSource, COLLECTION).parameter("sector", "industry").build();
    private final DataSet series = DataSet.builder(dataSource, SERIES).parameter("sector", "industry").parameter("region", "be").build();
    private final DataSet.Converter<CubeId> cubeIdParam = CubeSupport.idByName(DIM2_LEV0);

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatThrownBy(() -> CubeSupport.of(null, null, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.of(providerName, null, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.idByName(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.idBySeparator(null, ",", "name")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.idBySeparator(DIM0_LEV0, null, "name")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.idBySeparator(DIM0_LEV0, ",", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testChildrenOfDataSource() throws IOException {
        CubeSupport x = CubeSupport.of("ABC", MockedCubeConnection::new, o -> CubeSupport.idByName(o.getRoot()));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.children(MockedCubeConnection.sourceOf("other")))
                .withMessageContaining("Invalid provider name; expected: 'ABC' found: 'other'");

        assertThat(x.children(MockedCubeConnection.D0))
                .hasSize(1)
                .allMatch(dataSet -> dataSet.getKind().equals(SERIES));

        assertThat(x.children(MockedCubeConnection.D1))
                .hasSize(4)
                .allMatch(dataSet -> dataSet.getKind().equals(SERIES));

        assertThat(x.children(MockedCubeConnection.D2))
                .hasSize(4)
                .allMatch(dataSet -> dataSet.getKind().equals(COLLECTION));
    }

    @Test
    public void testChildrenOfDataSet() throws IOException {
        CubeSupport x = CubeSupport.of("ABC", MockedCubeConnection::new, o -> CubeSupport.idByName(o.getRoot()));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.children(DataSet.of(MockedCubeConnection.sourceOf("other"), COLLECTION)))
                .withMessageContaining("Invalid provider name; expected: 'ABC' found: 'other'");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> x.children(DataSet.of(MockedCubeConnection.D0, SERIES)))
                .withMessageContaining("Not a collection");

        assertThat(DataSet.of(MockedCubeConnection.D0, COLLECTION))
                .satisfies(o -> {
                    assertThatExceptionOfType(RuntimeException.class)
                            .isThrownBy(() -> x.children(o));
                });

        assertThat(DataSet.of(MockedCubeConnection.D1, COLLECTION))
                .satisfies(o -> {
                    assertThat(x.children(o))
                            .hasSize(4)
                            .allMatch(dataSet -> dataSet.getKind().equals(SERIES));

                    assertThatExceptionOfType(RuntimeException.class)
                            .isThrownBy(() -> x.children(o.toBuilder().parameter("0", "0").build()));
                });

        assertThat(DataSet.of(MockedCubeConnection.D2, COLLECTION))
                .satisfies(o -> {
                    assertThat(x.children(o))
                            .hasSize(4)
                            .allMatch(dataSet -> dataSet.getKind().equals(COLLECTION));

                    assertThat(x.children(o.toBuilder().parameter("0", "0").build()))
                            .hasSize(5)
                            .allMatch(dataSet -> dataSet.getKind().equals(SERIES));

                    assertThatExceptionOfType(RuntimeException.class)
                            .isThrownBy(() -> x.children(o.toBuilder().parameter("0", "0").parameter("1", "0").build()));
                });
    }

    @Test
    public void testResourceLeak() throws IOException {
        ResourceWatcher watcher = new ResourceWatcher();

        CubeSupport support = CubeSupport.of(providerName, dataSource1 -> new XCubeConnection(DIM2_LEV0, watcher), o -> cubeIdParam);
        support.children(dataSource);
        support.children(col);
        readAllAndClose(support.getData(dataSource, TsInformationType.All));
        readAllAndClose(support.getData(col, TsInformationType.All));
        readAllAndClose(support.getData(series, TsInformationType.All));
        assertThat(watcher.isLeaking()).isFalse();
    }

    @Test
    public void testIdByName() {
        assertThat(CubeSupport.idByName(DIM2_LEV0)).satisfies(o -> {
            assertThat(o.getDefaultValue()).isEqualTo(DIM2_LEV0);
            assertThat(o.get(col)).isEqualTo(DIM2_LEV1);
            assertThat(o.get(series)).isEqualTo(DIM2_LEV2);
        });
    }

    @Test
    public void testIdBySeparator() {
        assertThat(CubeSupport.idBySeparator(DIM2_LEV0, ".", "id")).satisfies(o -> {
            assertThat(o.getDefaultValue()).isEqualTo(DIM2_LEV0);
            assertThat(o.get(DataSet.builder(dataSource, COLLECTION).parameter("id", "industry").build())).isEqualTo(DIM2_LEV1);
            assertThat(o.get(DataSet.builder(dataSource, SERIES).parameter("id", "industry.be").build())).isEqualTo(DIM2_LEV2);
        });
    }

    private static long readAllAndClose(Stream<DataSetTs> cursor) throws IOException {
        try (Stream<DataSetTs> closeable = cursor) {
            return closeable.count();
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    @lombok.AllArgsConstructor
    private static final class MockedCubeConnection implements CubeConnection {

        public static final DataSource D0 = sourceOf("ABC");
        public static final DataSource D1 = sourceOf("ABC", 4);
        public static final DataSource D2 = sourceOf("ABC", 4, 5);

        public static DataSource sourceOf(String provider, int... dimensions) {
            return DataSource.of(provider, "")
                    .toBuilder()
                    .parameter("dimensions", Arrays.toString(dimensions))
                    .build();
        }

        @lombok.NonNull
        private final DataSource dataSource;

        private int getDimensionCount() {
            return getDimensions().length;
        }

        private int[] getDimensions() {
            return parseIntArray(dataSource.getParameter("dimensions"));
        }

        @Override
        public @NonNull Optional<IOException> testConnection() {
            return Optional.empty();
        }

        @Override
        public @NonNull CubeId getRoot() throws IOException {
            return CubeId.root(IntStream.range(0, getDimensionCount()).mapToObj(String::valueOf).toArray(String[]::new));
        }

        @Override
        public @NonNull Stream<CubeSeries> getAllSeries(@NonNull CubeId id) throws IOException {
            return Stream.empty();
        }

        @Override
        public @NonNull Stream<CubeSeriesWithData> getAllSeriesWithData(@NonNull CubeId id) throws IOException {
            return Stream.empty();
        }

        @Override
        public @NonNull Optional<CubeSeries> getSeries(@NonNull CubeId id) throws IOException {
            return Optional.empty();
        }

        @Override
        public @NonNull Optional<CubeSeriesWithData> getSeriesWithData(@NonNull CubeId id) throws IOException {
            return Optional.empty();
        }

        @Override
        public @NonNull Stream<CubeId> getChildren(@NonNull CubeId id) throws IOException {
            return IntStream
                    .range(0, getDimensions()[id.getLevel()])
                    .mapToObj(index -> id.child(String.valueOf(index)));
        }

        @Override
        public @NonNull String getDisplayName() throws IOException {
            return dataSource.getProviderName();
        }

        @Override
        public @NonNull String getDisplayName(@NonNull CubeId id) throws IOException {
            return id.toString();
        }

        @Override
        public @NonNull String getDisplayNodeName(@NonNull CubeId id) throws IOException {
            return id.toString();
        }

        @Override
        public void close() throws IOException {
        }

        private static int[] parseIntArray(CharSequence input) {
            if (input != null) {
                String tmp = input.toString();
                try {
                    int beginIndex = tmp.indexOf('[');
                    int endIndex = tmp.lastIndexOf(']');
                    if (beginIndex == -1 || endIndex == -1) {
                        return null;
                    }
                    if (input.length() == 2) {
                        return new int[0];
                    }
                    String[] values = tmp.substring(beginIndex + 1, endIndex).split("\\s*,\\s*");
                    int[] result = new int[values.length];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = Integer.parseInt(values[i].trim());
                    }
                    return result;
                } catch (Exception ex) {
                }
            }
            return null;
        }
    }
}
