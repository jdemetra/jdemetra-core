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
import demetra.tsprovider.cube.CubeConnection;
import demetra.tsprovider.cube.CubeId;
import demetra.tsprovider.cube.CubeSupport;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import nbbrd.io.text.Property;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author Philippe Charles
 */
interface FakeDbParam extends DataSource.Converter<FakeDbBean> {

    String getVersion();

    DataSet.@NonNull Converter<CubeId> getCubeIdParam(@NonNull CubeConnection connection);

    final class V1 implements FakeDbParam {

        private final Property<String> dbName = Property.of("db", "", Parser.onString(), Formatter.onString());
        private final Property<String> tableName = Property.of("table", "", Parser.onString(), Formatter.onString());

        private final DataSet.Converter<CubeId> dimValues = CubeSupport.idBySeparator(CubeId.root("REGION", "SECTOR"), ",", "q");

        @Override
        public String getVersion() {
            return "20150909";
        }

        @Override
        public FakeDbBean getDefaultValue() {
            FakeDbBean result = new FakeDbBean();
            result.setDbName(dbName.getDefaultValue());
            result.setTableName(tableName.getDefaultValue());
            return result;
        }

        @Override
        public FakeDbBean get(DataSource dataSource) {
            FakeDbBean result = new FakeDbBean();
            result.setDbName(dbName.get(dataSource::getParameter));
            result.setTableName(tableName.get(dataSource::getParameter));
            return result;
        }

        @Override
        public void set(DataSource.Builder builder, FakeDbBean value) {
            dbName.set(builder::parameter, value.getDbName());
            tableName.set(builder::parameter, value.getTableName());
        }

        @Override
        public DataSet.Converter<CubeId> getCubeIdParam(CubeConnection connection) {
            return dimValues;
        }
    }
}
