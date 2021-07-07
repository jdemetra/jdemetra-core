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

import demetra.io.ResourceWatcher;
import _util.tsproviders.XCubeAccessor;
import _util.tsproviders.XCubeSupportResource;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.timeseries.TsInformationType;
import static demetra.tsprovider.cube.CubeIdTest.EMPTY;
import static demetra.tsprovider.cube.CubeIdTest.INDUSTRY;
import static demetra.tsprovider.cube.CubeIdTest.INDUSTRY_BE;
import static demetra.tsprovider.cube.CubeIdTest.SECTOR_REGION;
import demetra.tsprovider.stream.DataSetTs;

import java.io.IOException;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class CubeSupportTest {

    private final String providerName = "provider";
    private final DataSource dataSource = DataSource.of(providerName, "");
    private final DataSet col = DataSet.builder(dataSource, DataSet.Kind.COLLECTION).parameter("sector", "industry").build();
    private final DataSet series = DataSet.builder(dataSource, DataSet.Kind.SERIES).parameter("sector", "industry").parameter("region", "be").build();
    private final DataSet.Converter<CubeId> cubeIdParam = CubeSupport.idByName(SECTOR_REGION);

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatThrownBy(() -> CubeSupport.of(null, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.of(providerName, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.idByName(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.idBySeparator(null, ",", "name")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.idBySeparator(EMPTY, null, "name")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeSupport.idBySeparator(EMPTY, ",", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testResourceLeak() throws IOException {
        ResourceWatcher watcher = new ResourceWatcher();

        CubeSupport support = CubeSupport.of(providerName, new XCubeSupportResource(new XCubeAccessor(SECTOR_REGION, watcher), cubeIdParam));
        support.children(dataSource);
        support.children(col);
        readAllAndClose(support.getData(dataSource, TsInformationType.All));
        readAllAndClose(support.getData(col, TsInformationType.All));
        readAllAndClose(support.getData(series, TsInformationType.All));
        assertThat(watcher.isLeaking()).isFalse();
    }

    @Test
    public void testIdByName() {
        assertThat(CubeSupport.idByName(SECTOR_REGION)).satisfies(o -> {
            assertThat(o.getDefaultValue()).isEqualTo(SECTOR_REGION);
            assertThat(o.get(col)).isEqualTo(INDUSTRY);
            assertThat(o.get(series)).isEqualTo(INDUSTRY_BE);
        });
    }

    @Test
    public void testidBySeparator() {
        assertThat(CubeSupport.idBySeparator(SECTOR_REGION, ".", "id")).satisfies(o -> {
            assertThat(o.getDefaultValue()).isEqualTo(SECTOR_REGION);
            assertThat(o.get(DataSet.builder(dataSource, DataSet.Kind.COLLECTION).parameter("id", "industry").build())).isEqualTo(INDUSTRY);
            assertThat(o.get(DataSet.builder(dataSource, DataSet.Kind.SERIES).parameter("id", "industry.be").build())).isEqualTo(INDUSTRY_BE);
        });
    }

    private static long readAllAndClose(Stream<DataSetTs> cursor) throws IOException {
        try (Stream<DataSetTs> closeable = cursor) {
            return closeable.count();
        }
    }
}
