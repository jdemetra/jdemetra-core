/*
 * Copyright 2018 National Bank of Belgium
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
package _demo;

import sql.util.odbc.OdbcDataSource;
import sql.util.odbc.OdbcRegistry;
import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
public class OdbcRegistryDemo {

    public static void main(String[] args) throws IOException {
        OdbcRegistry registry = OdbcRegistry.getDefault();

        registry
                .getDataSources(OdbcDataSource.Type.USER)
                .forEach(System.out::println);
        System.out.println("");

        registry
                .getDrivers()
                .forEach(System.out::println);
        System.out.println("");

        registry
                .getDrivers()
                .stream()
                .filter(o -> o.getFileUsage().isFileBased() && o.getFileExtensions().contains("mdb"))
                .forEach(System.out::println);
    }
}
