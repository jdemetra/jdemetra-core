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

import com.google.common.base.StandardSystemProperty;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class managing the secret key used by the AES algorithm.
 *
 * @author Demortier Jeremy
 */
@Deprecated
public final class KeyGen {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyGen.class);

    private KeyGen() {
    }

    /**
     * Generates a new key.
     *
     * @return
     */
    public static SecretKey getKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(new SecureRandom());
            kgen.init(128); // 192 and 256 bits may not be available
            // Generate the secret key specs.
            SecretKey skey = kgen.generateKey();
            return skey;
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Unable to create secret key!", ex);
            return null;
        }
    }

    /**
     * Saves key to default location.
     *
     * @param skey
     */
    public static void saveKey(final SecretKey skey) {
        saveKey(skey, StandardSystemProperty.USER_HOME.value() + "/.encrypt/", "jdemetra.key");
    }

    /**
     * Saves key to specified location.
     *
     * @param skey
     * @param directory
     * @param file
     */
    public static void saveKey(final SecretKey skey, final String directory, final String file) {
        try {
            Path dir = Paths.get(directory);
            Files.createDirectory(dir);
            Path output = dir.resolve(file);
            Files.createFile(output);
            try (OutputStream fos = Files.newOutputStream(output)) {
                try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(skey.getEncoded());
                    oos.flush();
                }
            }
        } catch (IOException ex) {
            LOGGER.error("While saving key", ex);
        }
    }

    /**
     * Retrieves a previously serialized key from the default location.
     *
     * @return
     */
    public static SecretKeySpec retrieveKeySpec() {
        return retrieveKeySpec(StandardSystemProperty.USER_HOME.value() + "/.encrypt/jdemetra.key");
    }

    /**
     * Retrieves a previously serialized key.
     *
     * @param path Path to the serialized file.
     * @return The serialized key, if none is to be found, a new one is created.
     */
    public static SecretKeySpec retrieveKeySpec(final String path) {
        try {
            Path input = Paths.get(path);
            try (InputStream fis = Files.newInputStream(input)) {
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    byte[] raw = (byte[]) ois.readObject();
                    return new SecretKeySpec(raw, "AES");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.warn("No key was found... creating a new one.", ex);
            SecretKey sk = getKey();
            saveKey(sk);
            return retrieveKeySpec();
        }
    }
}
