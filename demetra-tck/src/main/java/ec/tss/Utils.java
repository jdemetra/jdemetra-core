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
package ec.tss;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
final class Utils {

    private Utils() {
        // static class
    }

    static final TsCollectionInformation NULL_TS_COLLECTION_INFO = null;
    static final TsInformation NULL_TS_INFO = null;
    static final TsMoniker NULL_MONIKER = null;

    static String throwDescription(Object o, String code, Class<? extends Throwable> exClass) {
        return throwDescription(o.getClass(), code, exClass);
    }

    static String throwDescription(Class<?> codeClass, String code, Class<? extends Throwable> exClass) {
        return String.format("Expecting '%s#%s' to raise '%s'", codeClass.getName(), code, exClass.getName());
    }
}
