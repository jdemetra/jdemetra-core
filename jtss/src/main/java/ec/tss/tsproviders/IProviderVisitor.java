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
package ec.tss.tsproviders;

import java.io.IOException;

/**
 *
 * @author Philippe Charles
 * @param <P>
 * @since 1.0.0
 */
@Deprecated
public interface IProviderVisitor<P> {

    default boolean preVisitSource(P provider, DataSource dataSource) throws IOException {
        return true;
    }

    default boolean postVisitSource(P provider, DataSource dataSource, IOException ex) throws IOException {
        if (ex != null) {
            throw ex;
        }
        return true;
    }

    default boolean preVisitCollection(P provider, DataSet dataSet, int level) throws IOException {
        return true;
    }

    default boolean postVisitCollection(P provider, DataSet dataSet, int level, IOException ex) throws IOException {
        if (ex != null) {
            throw ex;
        }
        return true;
    }

    default boolean visitDummy(P provider, DataSet dataSet, int level) throws IOException {
        return true;
    }

    default boolean visitSeries(P provider, DataSet dataSet, int level) throws IOException {
        return true;
    }
}
