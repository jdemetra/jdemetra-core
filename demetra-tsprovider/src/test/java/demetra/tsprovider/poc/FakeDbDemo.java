package demetra.tsprovider.poc;

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


import demetra.tsprovider.DataSource;
import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
final class FakeDbDemo {

    public static void main(String[] args) throws IllegalArgumentException, IOException {
        try (FakeDbProvider provider = new FakeDbProvider()) {

            FakeDbBean bean = provider.newBean();

            DataSource dataSource = provider.encodeBean(bean);
            provider.open(dataSource);

            DemoUtil.printTree(provider, dataSource);
            DemoUtil.printFirstSeries(provider, dataSource);
        }
    }
}
