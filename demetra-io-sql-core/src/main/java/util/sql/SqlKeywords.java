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
package util.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SqlKeywords {

    private static final Set<String> SQL92_RESERVED_WORDS = loadWords("Sql92ReservedWords.txt");
    private static final Set<String> SQL92_NON_RESERVED_WORDS = loadWords("Sql92NonReservedWords.txt");
    private static final Set<String> SQL99_RESERVED_WORDS = loadWords("Sql99ReservedWords.txt");
    private static final Set<String> SQL2003_RESERVED_WORDS = loadWords("Sql2003ReservedWords.txt");
    private static final Set<String> SQL2008_RESERVED_WORDS = loadWords("Sql2008ReservedWords.txt");

    @Nonnull
    public static Set<String> getSql92ReservedWords() {
        return SQL92_RESERVED_WORDS;
    }

    @Nonnull
    public static Set<String> getSql92NonReservedWords() {
        return SQL92_NON_RESERVED_WORDS;
    }

    @Nonnull
    public static Set<String> getSql99ReservedWords() {
        return SQL99_RESERVED_WORDS;
    }

    @Nonnull
    public static Set<String> getSql2003ReservedWords() {
        return SQL2003_RESERVED_WORDS;
    }

    @Nonnull
    public static Set<String> getSql2008ReservedWords() {
        return SQL2008_RESERVED_WORDS;
    }

    private static Set<String> loadWords(String resourceName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(SqlKeywords.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8))) {
            Set<String> result = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            return Collections.unmodifiableSet(result);
        } catch (IOException ex) {
            throw new RuntimeException("Missing resource '" + resourceName + "'", ex);
        }
    }
}
