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
package ec.tstoolkit.utilities;

import com.google.common.base.StandardSystemProperty;
import java.util.Map;

/**
 * A convenient enum to handle Standard System Properties.
 *
 * @see http://www.mindspring.com/~mgrand/java-system-properties.htm
 * @author Philippe Charles
 * @deprecated use {@link StandardSystemProperty} instead.
 */
@Deprecated
public enum SystemProperties implements Map.Entry<String, String> {

    /**
     * The character encoding for the default locale
     *
     * @since 1.1
     */
    FILE_ENCODING("file.encoding"),
    /**
     * The package that contains the converters that handle converting between
     * local encodings and Unicode
     *
     * @since 1.1
     */
    FILE_ENCODING_PKG("file.encoding.pkg"),
    /**
     * The platform-dependent file separator (e.g., "/" on UNIX, "\" for
     * Windows)
     *
     * @since 1.0
     */
    FILE_SEPARATOR("file.separator"),
    /**
     * The value of the CLASSPATH environment variable
     *
     * @since 1.0
     */
    JAVA_CLASS_PATH("java.class.path"),
    /**
     * The version of the Java API
     *
     * @since 1.0
     */
    JAVA_CLASS_VERSION("java.class.version"),
    /**
     * The just-in-time compiler to use, if any. The java interpreter provided
     * with the JDK initializes this property from the environment variable
     * JAVA_COMPILER.
     *
     * @since 1.0
     */
    JAVA_COMPILER("java.compiler"),
    /**
     * The directory in which Java is installed
     *
     * @since 1.0
     */
    JAVA_HOME("java.home"),
    /**
     * The directory in which java should create temporary files
     *
     * @since 1.2
     */
    JAVA_IO_TMPDIR("java.io.tmpdir"),
    /**
     * The version of the Java interpreter
     *
     * @since 1.0
     */
    JAVA_VERSION("java.version"),
    /**
     * A vendor-specific string
     *
     * @since 1.0
     */
    JAVA_VENDOR("java.vendor"),
    /**
     * A vendor URL
     *
     * @since 1.0
     */
    JAVA_VENDOR_URL("java.vendor.url"),
    /**
     * The platform-dependent line separator (e.g., "\n" on UNIX, "\r\n" for
     * Windows)
     *
     * @since 1.0
     */
    LINE_SEPARATOR("line.separator"),
    /**
     * The name of the operating system
     *
     * @since 1.0
     */
    OS_NAME("os.name"),
    /**
     * The system architecture
     *
     * @since 1.0
     */
    OS_ARCH("os.arch"),
    /**
     * The operating system version
     *
     * @since 1.0
     */
    OS_VERSION("os.version"),
    /**
     * The platform-dependent path separator (e.g., ":" on UNIX, "," for
     * Windows)
     *
     * @since 1.0
     */
    PATH_SEPARATOR("path.separator"),
    /**
     * The current working directory when the properties were initialized
     *
     * @since 1.0
     */
    USER_DIR("user.dir"),
    /**
     * The home directory of the current user
     *
     * @since 1.0
     */
    USER_HOME("user.home"),
    /**
     * The two-letter language code of the default locale
     *
     * @since 1.0
     */
    USER_LANGUAGE("user.language"),
    /**
     * The username of the current user
     *
     * @since 1.1
     */
    USER_NAME("user.name"),
    /**
     * The two-letter country code of the default locale
     *
     * @since 1.1
     */
    USER_REGION("user.region"),
    /**
     * The default time zone
     *
     * @since 1.1
     */
    USER_TIMEZONE("user.timezone");
    //
    final String key;

    private SystemProperties(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return System.getProperty(key);
    }

    @Override
    public String setValue(String value) {
        return System.setProperty(key, value);
    }
}
