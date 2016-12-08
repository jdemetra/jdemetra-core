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
package spreadsheet.xlsx.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ZipUtil {

    public IOUtil.ByteResource asByteResource(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        return new IOUtil.ByteResource() {
            @Override
            public InputStream openStream(String name) throws IOException {
                ZipEntry entry = zipFile.getEntry(name);
                if (entry == null) {
                    throw new IOException("Missing entry '" + name + "' in file '" + zipFile.getName() + "'");
                }
                return zipFile.getInputStream(entry);
            }

            @Override
            public void close() throws IOException {
                zipFile.close();
            }
        };
    }

    public IOUtil.ByteResource asByteResource(InputStream inputStream, Predicate<? super ZipEntry> filter) throws IOException {
        Map<String, byte[]> data = readAll(inputStream, filter);
        return new IOUtil.ByteResource() {

            @Override
            public InputStream openStream(String name) throws IOException {
                byte[] result = data.get(name);
                if (result == null) {
                    throw new IOException("Missing entry '" + name + "'");
                }
                return new ByteArrayInputStream(result);
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    private Map<String, byte[]> readAll(InputStream stream, Predicate<? super ZipEntry> filter) throws IOException {
        Map<String, byte[]> result = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(stream)) {
            ZipUtil.forEach(zis, filter, (k, v) -> result.put(k.getName(), v));
        }
        return result;
    }

    private void forEach(ZipInputStream zis, Predicate<? super ZipEntry> filter, BiConsumer<ZipEntry, byte[]> consumer) throws IOException {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (filter.test(entry)) {
                consumer.accept(entry, ZipUtil.toByteArray(entry, zis));
            }
        }
    }

    private byte[] toByteArray(ZipEntry entry, ZipInputStream stream) throws IOException {
        long size = entry.getSize();
        if (size >= Integer.MAX_VALUE) {
            throw new IOException("ZIP entry size is too large");
        }
        ByteArrayOutputStream result = new ByteArrayOutputStream(size > 0 ? (int) size : 4096);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            result.write(buffer, 0, len);
        }
        return result.toByteArray();
    }
}
