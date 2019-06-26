/*
 * Copyright 2015 National Bank of Belgium
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
package adodb.wsh;

import java.io.BufferedReader;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;
import net.jcip.annotations.ThreadSafe;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
@ThreadSafe
interface Wsh {

    @NonNull
    BufferedReader exec(@NonNull String scriptName, @NonNull String... args) throws IOException;

    @NonNull
    public static Wsh getDefault() {
        return CScript.INSTANCE;
    }
}