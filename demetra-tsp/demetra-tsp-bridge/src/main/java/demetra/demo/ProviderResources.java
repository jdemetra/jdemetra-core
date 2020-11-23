/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.demo;

import demetra.tsprovider.DataSourceLoader;
import demetra.tsprovider.DataSourceProvider;
import demetra.tsprovider.FileBean;
import demetra.tsprovider.FileLoader;
import ec.tss.tsproviders.IDataSourceLoader;
import ec.tss.tsproviders.IDataSourceProvider;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.IFileLoader;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ProviderResources {

    public interface Provider2<P2 extends IDataSourceProvider> {

        P2 getProvider2();
    }

    public interface Provider3<P3 extends DataSourceProvider> {

        P3 getProvider3();
    }

    public interface Loader2<P2 extends IDataSourceLoader> extends Provider2<P2> {

        Object getBean2(P2 provider);
    }

    public interface Loader3<P3 extends DataSourceLoader<? extends Object>> extends Provider3<P3> {

        Object getBean3(P3 provider);
    }

    public interface FileLoader2<P2 extends IFileLoader> extends Loader2<P2> {

        @Override
        IFileBean getBean2(P2 provider);
    }

    public interface FileLoader3<P3 extends FileLoader<? extends FileBean>> extends Loader3<P3> {

        @Override
        FileBean getBean3(P3 provider);
    }
}
