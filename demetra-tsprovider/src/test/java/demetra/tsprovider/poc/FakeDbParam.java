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
package demetra.tsprovider.poc;

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.IParam;
import demetra.tsprovider.util.Params;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
interface FakeDbParam extends IParam<DataSource, FakeDbBean> {

    String getVersion();

    @Nonnull
    IParam<DataSet, CubeId> getCubeIdParam(@Nonnull CubeId root);

    static final class V1 implements FakeDbParam {

        private final IParam<DataSource, String> dbName = Params.onString("", "db");
        private final IParam<DataSource, String> tableName = Params.onString("", "table");

        private final IParam<DataSet, CubeId> dimValues = CubeSupport.idBySeparator(CubeId.root("REGION", "SECTOR"), ",", "q");

        @Override
        public String getVersion() {
            return "20150909";
        }

        @Override
        public FakeDbBean defaultValue() {
            FakeDbBean result = new FakeDbBean();
            result.setDbName(dbName.defaultValue());
            result.setTableName(tableName.defaultValue());
            return result;
        }

        @Override
        public FakeDbBean get(DataSource dataSource) {
            FakeDbBean result = new FakeDbBean();
            result.setDbName(dbName.get(dataSource));
            result.setTableName(tableName.get(dataSource));
            return result;
        }

        @Override
        public void set(IConfig.Builder<?, DataSource> builder, FakeDbBean value) {
            dbName.set(builder, value.getDbName());
            tableName.set(builder, value.getTableName());
        }

        @Override
        public IParam<DataSet, CubeId> getCubeIdParam(CubeId root) {
            return dimValues;
        }
    }
}
