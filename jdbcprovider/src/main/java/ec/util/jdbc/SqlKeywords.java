/*
 * Copyright 2013 National Bank of Belgium
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
package ec.util.jdbc;

import java.util.Set;

/**
 *
 * @author Philippe Charles
 */
@Deprecated
public final class SqlKeywords {

    private SqlKeywords() {
        // static class
    }

    public static Set<String> getSql92ReservedWords() {
        return nbbrd.sql.jdbc.SqlKeywords.SQL92_RESERVED_WORDS.getKeywords();
    }

    public static Set<String> getSql92NonReservedWords() {
        return nbbrd.sql.jdbc.SqlKeywords.SQL92_NON_RESERVED_WORDS.getKeywords();
    }

    public static Set<String> getSql99ReservedWords() {
        return nbbrd.sql.jdbc.SqlKeywords.SQL99_RESERVED_WORDS.getKeywords();
    }

    public static Set<String> getSql2003ReservedWords() {
        return nbbrd.sql.jdbc.SqlKeywords.SQL2003_RESERVED_WORDS.getKeywords();
    }

    public static Set<String> getSql2008ReservedWords() {
        return nbbrd.sql.jdbc.SqlKeywords.SQL2008_RESERVED_WORDS.getKeywords();
    }
}
