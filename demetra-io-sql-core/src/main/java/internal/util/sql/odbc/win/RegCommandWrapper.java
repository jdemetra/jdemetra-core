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
package internal.util.sql.odbc.win;

import internal.util.sql.ProcessReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class RegCommandWrapper {

    @NonNull
    public Map<String, List<RegValue>> query(@NonNull String key) throws IOException {
        try (BufferedReader r = ProcessReader.newReader("reg", "query", key, "/s")) {
            Map<String, List<RegValue>> result = new HashMap<>();
            String line;
            String subKey = null;
            List<RegValue> value = new ArrayList<>();
            while ((line = r.readLine()) != null) {
                if (line.isEmpty()) {
                    if (subKey != null) {
                        result.put(subKey, value);
                        subKey = null;
                        value = new ArrayList<>();
                    }
                } else if (subKey == null) {
                    subKey = line;
                } else {
                    RegValue regValue = RegValue.parse(line);
                    if (regValue != null) {
                        value.add(regValue);
                    }
                }
            }
            return result;
        }
    }

    @lombok.Value
    public static final class RegValue {

        private static final Pattern PATTERN = Pattern.compile("^[ ]{4}(.+)[ ]{4}(REG_(?:SZ|MULTI_SZ|EXPAND_SZ|DWORD|QWORD|BINARY|NONE))[ ]{4}(.+)$");

        @Nullable
        public static RegValue parse(@NonNull CharSequence line) {
            Matcher m = PATTERN.matcher(line);
            return m.matches() ? new RegValue(m.group(1), m.group(3)) : null;
        }

        @lombok.NonNull
        private String name;

        @lombok.NonNull
        private String value;
    }
}
