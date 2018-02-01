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
package internal.sql.adodb;

import com.google.common.collect.ImmutableMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 * @author Philippe Charles
 */
public class WshTest {

    static Wsh good() {
        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("DbProperties", "MyDbConnProperties.tsv")
                .put("OpenSchema", "MyDbTables.tsv")
                .build();
        return (a, b) -> {
            try {
                Path path = Paths.get(AdoConnectionTest.class.getResource(map.get(a)).toURI());
                return Files.newBufferedReader(path, StandardCharsets.UTF_8);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    static Wsh bad() {
        return ofException(FileNotFoundException::new);
    }

    static Wsh ugly() {
        return ofContent("helloworld");
    }

    static Wsh err() {
        return ofResource("MyDbErr.tsv");
    }

    static Wsh ofResource(String name) {
        try {
            Path path = Paths.get(AdoConnectionTest.class.getResource(name).toURI());
            return (a, b) -> Files.newBufferedReader(path, StandardCharsets.UTF_8);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    static Wsh ofException(Supplier<? extends IOException> supplier) {
        return (a, b) -> {
            throw supplier.get();
        };
    }

    static Wsh ofContent(CharSequence content) {
        return (a, b) -> new BufferedReader(new StringReader(content.toString()));
    }
}
