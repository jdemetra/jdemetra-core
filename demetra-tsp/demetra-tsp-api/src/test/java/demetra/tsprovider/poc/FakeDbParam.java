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
import demetra.tsprovider.util.Param;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
interface FakeDbParam extends Param<DataSource, FakeDbBean> {

    String getVersion();

    @NonNull
    Param<DataSet, CubeId> getCubeIdParam(@NonNull CubeId root);

    static final class V1 implements FakeDbParam {

        private final Param<DataSource, String> dbName = Param.onString("", "db");
        private final Param<DataSource, String> tableName = Param.onString("", "table");

        private final Param<DataSet, CubeId> dimValues = CubeSupport.idBySeparator(CubeId.root("REGION", "SECTOR"), ",", "q");

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
        public Param<DataSet, CubeId> getCubeIdParam(CubeId root) {
            return dimValues;
        }
    }
}
