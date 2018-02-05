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
package sql.util.odbc;

import java.io.File;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.NonNull;

/**
 * https://docs.microsoft.com/en-us/sql/odbc/reference/install/registry-entries-for-odbc-components
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
@lombok.Builder(builderClassName = "Builder")
public class OdbcDriver {

    /**
     * Driver name.
     */
    @NonNull
    private final String name;

    /**
     * ODBC interface conformance level supported by the driver.
     */
    @NonNull
    private final ApiLevel apiLevel;

    /**
     * Value indicating whether the driver supports SQLConnect,
     * SQLDriverConnect, and SQLBrowseConnect.
     */
    private final ConnectFunctions connectFunctions;

    /**
     * Driver DLL path.
     */
    private final File driverPath;

    /**
     * Version of ODBC that the driver supports.
     */
    private final String driverOdbcVer;

    /**
     * For file-based drivers, a list of extensions of the files the driver can
     * use.
     */
    @NonNull
    private final List<String> fileExtensions;

    /**
     * Value indicating how a file-based driver directly treats files in a data
     * source.
     */
    @NonNull
    private final FileUsage fileUsage;

    /**
     * Setup DLL path.
     */
    private final File setupPath;

    /**
     * SQL-92 grammar supported by the driver.
     */
    @NonNull
    private final SqlLevel sqlLevel;

    private final int usageCount;

    public enum ApiLevel implements IntSupplier {

        NONE(0), LEVEL1(1), LEVEL2(2);
        final int value;

        private ApiLevel(int value) {
            this.value = value;
        }

        @Override
        public int getAsInt() {
            return value;
        }
    };

    public enum FileUsage implements IntSupplier {

        NONE(0), TABLE(1), CATALOG(2);
        final int value;

        private FileUsage(int value) {
            this.value = value;
        }

        @Override
        public int getAsInt() {
            return value;
        }

        public boolean isFileBased() {
            return this != NONE;
        }
    };

    public enum SqlLevel implements IntSupplier {

        SQL_92_ENTRY(0), FIPS127_2_TRANSACTIONAL(1), SQL_92_INTERMEDIATE(2), SQL_92_FULL(3);
        final int value;

        private SqlLevel(int value) {
            this.value = value;
        }

        @Override
        public int getAsInt() {
            return value;
        }
    };

    @lombok.Value(staticConstructor = "of")
    public static class ConnectFunctions {

        private final boolean sqlConnect, sqlDriverConnect, sqlBrowseConnect;

        @Override
        public String toString() {
            return (sqlConnect ? "Y" : "N") + (sqlDriverConnect ? "Y" : "N") + (sqlBrowseConnect ? "Y" : "N");
        }
        static final Pattern INPUT_PATTERN = Pattern.compile("(Y|N){3}");

        @Nullable
        public static ConnectFunctions parse(@Nonnull CharSequence input, @Nullable ConnectFunctions defaultValue) {
            return INPUT_PATTERN.matcher(input).matches()
                    ? new ConnectFunctions(input.charAt(0) == 'Y', input.charAt(1) == 'Y', input.charAt(2) == 'Y')
                    : defaultValue;
        }
    }
}
