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

package ec.tss.tsproviders.odbc.registry;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.design.IntValue;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author Philippe Charles
 */
@Deprecated
@Immutable
public final class OdbcDriver {

    private final String name;
    private final ApiLevel apiLevel;
    private final ConnectFunctions connectFunctions;
    private final String driver;
    private final String driverOdbcVer;
    private final ImmutableList<String> fileExtns;
    private final FileUsage fileUsage;
    private final String setup;
    private final SqlLevel sqlLevel;
    private final int usageCount;

    public OdbcDriver(String name, ApiLevel apiLevel, ConnectFunctions connectFunctions, String driver, String driverOdbcVer, ImmutableList<String> fileExtns, FileUsage fileUsage, String setup, SqlLevel sqlLevel, int usageCount) {
        this.name = name;
        this.apiLevel = apiLevel;
        this.connectFunctions = connectFunctions;
        this.driver = driver;
        this.driverOdbcVer = driverOdbcVer;
        this.fileExtns = fileExtns;
        this.fileUsage = fileUsage;
        this.setup = setup;
        this.sqlLevel = sqlLevel;
        this.usageCount = usageCount;
    }

    public String getName() {
        return name;
    }

    public ApiLevel getApiLevel() {
        return apiLevel;
    }

    public ConnectFunctions getConnectFunctions() {
        return connectFunctions;
    }

    public String getDriver() {
        return driver;
    }

    public String getDriverOdbcVer() {
        return driverOdbcVer;
    }

    public List<String> getFileExtns() {
        return fileExtns;
    }

    public FileUsage getFileUsage() {
        return fileUsage;
    }

    public String getSetup() {
        return setup;
    }

    public SqlLevel getSqlLevel() {
        return sqlLevel;
    }

    public int getUsageCount() {
        return usageCount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("apiLevel", apiLevel)
                .add("connectFunctions", connectFunctions)
                .add("driver", driver)
                .add("driverOdbcVer", driverOdbcVer)
                .add("fileExtns", fileExtns)
                .add("fileUsage", fileUsage)
                .add("setup", setup)
                .add("sqlLevel", sqlLevel)
                .add("usageCount", usageCount)
                .toString();
    }

    public enum ApiLevel implements IntValue {

        NONE(0), LEVEL1(1), LEVEL2(2);
        final int value;

        private ApiLevel(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }
    };

    public enum FileUsage implements IntValue {

        NONE(0), TABLE(1), CATALOG(2);
        final int value;

        private FileUsage(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }
        
        public boolean isFileBased() {
            return this != NONE;
        }
    };

    public enum SqlLevel implements IntValue {

        SQL_92_ENTRY(0), FIPS127_2_TRANSACTIONAL(1), SQL_92_INTERMEDIATE(2), SQL_92_FULL(3);
        final int value;

        private SqlLevel(int value) {
            this.value = value;
        }

        @Override
        public int intValue() {
            return value;
        }
    };

    @Immutable
    public static class ConnectFunctions {

        final boolean sqlConnect, sqlDriverConnect, sqlBrowseConnect;

        public ConnectFunctions(boolean sqlConnect, boolean sqlDriverConnect, boolean sqlBrowseConnect) {
            this.sqlConnect = sqlConnect;
            this.sqlDriverConnect = sqlDriverConnect;
            this.sqlBrowseConnect = sqlBrowseConnect;
        }

        public boolean isSqlConnect() {
            return sqlConnect;
        }

        public boolean isSqlDriverConnect() {
            return sqlDriverConnect;
        }

        public boolean isSqlBrowseConnect() {
            return sqlBrowseConnect;
        }

        @Override
        public String toString() {
            return (sqlConnect ? "Y" : "N") + (sqlDriverConnect ? "Y" : "N") + (sqlBrowseConnect ? "Y" : "N");
        }
        static final Pattern INPUT_PATTERN = Pattern.compile("(Y|N){3}");

        public static ConnectFunctions valueOf(String input) {
            return input != null && INPUT_PATTERN.matcher(input).matches()
                    ? new ConnectFunctions(input.charAt(0) == 'Y', input.charAt(1) == 'Y', input.charAt(2) == 'Y')
                    : null;
        }
    }
}
