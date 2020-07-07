/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package ec.tss.tsproviders.odbc.registry;

import java.util.List;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @see
 * http://msdn.microsoft.com/en-us/library/windows/desktop/ms715432(v=vs.85).aspx
 * @author Philippe Charles
 */
@Deprecated
@ServiceDefinition(
        quantifier = Quantifier.OPTIONAL,
        singleton = true
)
public interface IOdbcRegistry {

    List<OdbcDataSource> getDataSources(OdbcDataSource.Type... types) throws Exception;

    List<OdbcDriver> getDrivers() throws Exception;
}
