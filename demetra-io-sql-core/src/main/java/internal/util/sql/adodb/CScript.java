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
package internal.util.sql.adodb;

import demetra.design.ThreadSafe;
import internal.util.sql.ProcessReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
@ThreadSafe
final class CScript implements Wsh {

    static final CScript INSTANCE = new CScript();

    private final ConcurrentMap<String, File> scripts;

    private CScript() {
        this.scripts = new ConcurrentHashMap<>();
    }

    @Override
    public BufferedReader exec(String scriptName, String... args) throws IOException {
        File script = getScript(scriptName);
        Process process = exec(script, args);
        return ProcessReader.newReader(process);
    }

    private File getScript(String scriptName) throws IOException {
        File result = scripts.get(scriptName);
        if (result == null || !isValidScript(result)) {
            result = extractResource(scriptName + ".vbs", "adodb", ".vbs");
            scripts.put(scriptName, result);
        }
        return result;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    @NonNull
    private static Process exec(@NonNull File script, @NonNull String... args) throws IOException {
        // http://technet.microsoft.com/en-us/library/ff920171.aspx
        String[] result = new String[3 + args.length];
        result[0] = "cscript";
        result[1] = "/nologo";
        result[2] = "\"" + script.getAbsolutePath() + "\"";
        System.arraycopy(args, 0, result, 3, args.length);
        return new ProcessBuilder(result).start();
    }

    private static boolean isValidScript(@NonNull File script) {
        return script.exists() && script.isFile() && script.canRead();
    }

    @NonNull
    private static File extractResource(@NonNull String resourceName, @NonNull String filePrefix, @NonNull String fileSuffix) throws IOException {
        File result = File.createTempFile(filePrefix, fileSuffix);
        result.deleteOnExit();
        try (InputStream in = Wsh.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new FileNotFoundException(resourceName);
            }
            Files.copy(in, result.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return result;
    }
    //</editor-fold>
}
