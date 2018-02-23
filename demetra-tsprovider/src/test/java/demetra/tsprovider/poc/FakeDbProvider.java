/*
 * Copyright 2015 National Bank of Belgium
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
package demetra.tsprovider.poc;

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceLoader;
import demetra.tsprovider.HasDataMoniker;
import demetra.tsprovider.HasDataSourceBean;
import demetra.tsprovider.HasDataSourceMutableList;
import demetra.tsprovider.TsProvider;
import demetra.tsprovider.cube.CubeAccessor;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.cursor.HasTsCursor;
import demetra.tsprovider.cursor.TsCursorAsProvider;
import demetra.tsprovider.util.IParam;
import demetra.tsprovider.util.ResourceMap;
import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
public final class FakeDbProvider implements DataSourceLoader {

    private static final String NAME = "FakeDbProvider";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;
    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;
    @lombok.experimental.Delegate
    private final HasDataSourceBean<FakeDbBean> beanSupport;
    @lombok.experimental.Delegate(excludes = HasTsCursor.class)
    private final CubeSupport cubeSupport;
    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    public FakeDbProvider() {
        ResourceMap<CubeAccessor> accessors = ResourceMap.newInstance();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, accessors::remove);
        this.monikerSupport = HasDataMoniker.usingUri(NAME);
        FakeDbParam fakeDbParam = new FakeDbParam.V1();
        this.beanSupport = HasDataSourceBean.of(NAME, fakeDbParam, fakeDbParam.getVersion());
        this.cubeSupport = CubeSupport.of(NAME, new FakeDbCubeResource(accessors, fakeDbParam));
        this.tsSupport = TsCursorAsProvider.of(NAME, cubeSupport, monikerSupport, accessors::clear);
    }

    @lombok.AllArgsConstructor
    private static final class FakeDbCubeResource implements CubeSupport.Resource {

        private final ResourceMap<CubeAccessor> accessors;
        private final FakeDbParam fakeDbParam;

        @Override
        public CubeAccessor getAccessor(DataSource dataSource) throws IOException {
            return accessors.computeIfAbsent(dataSource, o -> new FakeDbAccessor());
        }

        @Override
        public IParam<DataSet, CubeId> getIdParam(CubeId root) {
            return fakeDbParam.getCubeIdParam(root);
        }
    }
}
