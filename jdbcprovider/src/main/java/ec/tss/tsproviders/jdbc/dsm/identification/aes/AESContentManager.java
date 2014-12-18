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
package ec.tss.tsproviders.jdbc.dsm.identification.aes;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import ec.tss.tsproviders.jdbc.dsm.identification.IContentManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import javax.crypto.NoSuchPaddingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of IContentManager using the AES encryption algorithm.
 *
 * @author Demortier Jeremy
 * @see IContentManager
 */
@Deprecated
public class AESContentManager implements IContentManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AESContentManager.class);
    private final Path m_directory;
    private final SecretKeySpec m_key;

    public AESContentManager(File directory, SecretKeySpec key) {
        m_directory = directory.toPath();
        m_key = key;
    }

    @Override
    public List<String> getContent() {
        List<String> result = new ArrayList<>();

        Path input = m_directory.resolve("accounts");
        if (Files.exists(input)) {
            try (InputStream fis = Files.newInputStream(input)) {
                Cipher cipher;
                cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, m_key);
                try (CipherInputStream cis = new CipherInputStream(fis, cipher)) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(cis))) {
                        String s;
                        while ((s = br.readLine()) != null) {
                            result.add(s);
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("While loading content", ex);
            }
        }
        return result;
    }

    @Override
    public void saveContent(final Map<String, String> content) {
        try {
            if (!Files.exists(m_directory)) {
                Files.createDirectory(m_directory);
            }
            Path output = m_directory.resolve("accounts");
            Files.createFile(output);

            try (OutputStream fos = Files.newOutputStream(output)) {
                Cipher cipher;
                cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, m_key);
                try (CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
                    try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(cos))) {
                        StringBuilder sb = new StringBuilder();
                        for (Entry<String, String> o : content.entrySet()) {
                            sb.append(o.getKey());
                            sb.append("/");
                            sb.append(o.getValue());
                            sb.append("\n");
                        }
                        pw.println(sb.toString());
                        pw.flush();
                        //cos.write(sb.toString().getBytes());
                        //cos.flush();
                    }
                }
            }
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
            LOGGER.error("While saving content", ex);
        }
    }
}
