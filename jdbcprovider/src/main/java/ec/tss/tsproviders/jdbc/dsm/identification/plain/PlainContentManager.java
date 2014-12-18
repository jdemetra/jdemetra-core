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
package ec.tss.tsproviders.jdbc.dsm.identification.plain;

import ec.tss.tsproviders.jdbc.dsm.identification.IContentManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplest implementation of IContentManager; stores users and passwords
 * without any kind of encryption.
 *
 * @author Demortier Jeremy
 * @see IContentManager
 */
@Deprecated
public class PlainContentManager implements IContentManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlainContentManager.class);
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final String m_directory;

    public PlainContentManager(String directory) {
        m_directory = directory;
    }

    @Override
    public List<String> getContent() {
        List<String> result = new ArrayList<>();

        Path input = Paths.get(m_directory, "accounts");
        if (Files.exists(input)) {
            try (BufferedReader reader = Files.newBufferedReader(input, CHARSET)) {
                String s;
                while ((s = reader.readLine()) != null) {
                    result.add(s);
                }
            } catch (IOException ex) {
                LOGGER.error("While getting content", ex);
            }
        }
        return result;
    }

    @Override
    public void saveContent(final Map<String, String> content) {
        Path dir = Paths.get(m_directory);
        try {
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }
            Path output = dir.resolve("accounts");
            Files.createFile(output);

            try (BufferedWriter bw = Files.newBufferedWriter(output, CHARSET)) {
                for (Entry<String, String> o : content.entrySet()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(o.getKey());
                    sb.append("/");
                    sb.append(o.getValue());
                    bw.write(sb.toString());
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            LOGGER.error("While saving content", ex);
        }
    }
}
