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

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Philippe Charles
 */
public final class SqlKeywords {

    private SqlKeywords() {
        // static class
    }
    //
    private static final Set<String> SQL92_RESERVED_WORDS = loadWords("ec/util/jdbc/Sql92ReservedWords.txt");
    private static final Set<String> SQL92_NON_RESERVED_WORDS = loadWords("ec/util/jdbc/Sql92NonReservedWords.txt");
    private static final Set<String> SQL99_RESERVED_WORDS = loadWords("ec/util/jdbc/Sql99ReservedWords.txt");
    private static final Set<String> SQL2003_RESERVED_WORDS = loadWords("ec/util/jdbc/Sql2003ReservedWords.txt");
    private static final Set<String> SQL2008_RESERVED_WORDS = loadWords("ec/util/jdbc/Sql2008ReservedWords.txt");

    public static Set<String> getSql92ReservedWords() {
        return SQL92_RESERVED_WORDS;
    }

    public static Set<String> getSql92NonReservedWords() {
        return SQL92_NON_RESERVED_WORDS;
    }

    public static Set<String> getSql99ReservedWords() {
        return SQL99_RESERVED_WORDS;
    }

    public static Set<String> getSql2003ReservedWords() {
        return SQL2003_RESERVED_WORDS;
    }

    public static Set<String> getSql2008ReservedWords() {
        return SQL2008_RESERVED_WORDS;
    }

    private static Set<String> loadWords(String resourceName) {
        try {
            Set<String> result = new HashSet<>();
            for (String o : Resources.readLines(Resources.getResource(resourceName), StandardCharsets.UTF_8)) {
                result.add(o);
            }
            return Collections.unmodifiableSet(result);
        } catch (IOException ex) {
            throw new RuntimeException("Missing resource '" + resourceName + "'", ex);
        }
    }
}
