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
package ec.tss.tsproviders.odbc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.jdbc.JdbcBean;
import ec.tss.tsproviders.utils.Formatters;
import ec.tss.tsproviders.utils.Parsers;
import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Philippe Charles
 */
public class OdbcBean extends JdbcBean implements IFileBean {

    static final List<FileFilter> FILE_FILTERS = Collections.<FileFilter>singletonList(AccessFileFilter.INSTANCE);
    static final Parsers.Parser<File> FILE_PARSERS = new AccessConnectionStringParser();
    static final Formatters.Formatter<File> FILE_FORMATTERS = new AccessConnectionStringFormatter();

    public OdbcBean() {
        super();
    }

    public OdbcBean(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public File getFile() {
        return FILE_PARSERS.tryParse(getDbName()).or(new File(""));
    }

    @Override
    public void setFile(File file) {
        setDbName(FILE_FORMATTERS.tryFormatAsString(file).or(""));
    }

    /**
     * @see
     * http://msdn.microsoft.com/en-us/library/system.data.odbc.odbcconnection.connectionstring.aspx
     * @return
     */
    public boolean isDsnLess() {
        return getDbName().toUpperCase(Locale.ENGLISH).startsWith("DRIVER");
    }

    @VisibleForTesting
    static final MapSplitter CONNECTION_STRING_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings().withKeyValueSeparator("=");

    enum AccessFileFilter implements FileFilter {

        INSTANCE;

        @Override
        public boolean accept(File pathname) {
            String tmp = pathname.getPath().toLowerCase(Locale.ENGLISH);
            return tmp.endsWith(".mdb") || tmp.endsWith(".accdb");
        }
    }

    static class AccessConnectionStringParser extends Parsers.FailSafeParser<File> {

        @Override
        protected File doParse(CharSequence input) throws Exception {
            String file = CONNECTION_STRING_SPLITTER.split(input).get("DBQ");
            if (file != null) {
                File result = new File(file);
                return AccessFileFilter.INSTANCE.accept(result) ? result : null;
            }
            return null;
        }
    };

    static class AccessConnectionStringFormatter extends Formatters.Formatter<File> {

        @Override
        public CharSequence format(File value) {
            return AccessFileFilter.INSTANCE.accept(value) ? ("DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};DBQ=" + value.getPath()) : null;
        }
    };
}
