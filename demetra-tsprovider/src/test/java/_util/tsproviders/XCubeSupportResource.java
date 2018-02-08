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
package _util.tsproviders;

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.CubeAccessor;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.util.IParam;
import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author Philippe Charles
 */
public final class XCubeSupportResource implements CubeSupport.Resource {

    private final DataSource dataSource;
    private final CubeAccessor accessor;
    private final IParam<DataSet, CubeId> param;

    public XCubeSupportResource(DataSource dataSource, CubeAccessor accessor, IParam<DataSet, CubeId> param) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.accessor = Objects.requireNonNull(accessor);
        this.param = Objects.requireNonNull(param);
    }

    @Override
    public CubeAccessor getAccessor(DataSource dataSource) throws IOException, IllegalArgumentException {
        if (!dataSource.equals(this.dataSource)) {
            throw new IllegalArgumentException();
        }
        return accessor;
    }

    @Override
    public IParam<DataSet, CubeId> getIdParam(CubeId root) throws IOException, IllegalArgumentException {
        return param;
    }
}
