/*
 * Copyright 2018 National Bank of Belgium
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
package internal.sql.odbc.legacy;

import demetra.design.DemetraPlusLegacy;
import internal.util.AbstractIterator;
import java.util.Iterator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@DemetraPlusLegacy
@lombok.Value
@lombok.Builder
class LegacyOdbcId {

    @lombok.NonNull
    private String dbName;

    @lombok.NonNull
    private String table;

    @lombok.NonNull
    private String domainColumn;

    @lombok.NonNull
    private String seriesColumn;

    @lombok.NonNull
    private String periodColumn;

    @lombok.NonNull
    private String valueColumn;

    private String domainName;

    private String seriesName;

    public boolean isMultiCollection() {
        return null == seriesName && null == domainName;
    }

    public boolean isCollection() {
        return null == seriesName && null != domainName;
    }

    public boolean isSeries() {
        return seriesName != null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
                .append(BSEP).append(dbName).append(ESEP)
                .append(BSEP).append(table).append(ESEP)
                .append(BSEP).append(domainColumn).append(ESEP)
                .append(BSEP).append(seriesColumn).append(ESEP)
                .append(BSEP).append(periodColumn).append(ESEP)
                .append(BSEP).append(valueColumn).append(ESEP);
        if (isSeries()) {
            builder.append(BSEP).append(seriesName).append(ESEP);
        }
        return builder.toString();
    }

    @Nullable
    public static LegacyOdbcId parse(@NonNull String input) {
        LegacyOdbcId.Builder result = LegacyOdbcId.builder();
        Iterator<String> iter = new OdbcIdParser(input);

        if (!iter.hasNext()) {
            return null;
        }
        result.dbName(iter.next());

        if (!iter.hasNext()) {
            return null;
        }
        result.table(iter.next());

        if (!iter.hasNext()) {
            return null;
        }
        result.domainColumn(iter.next());

        if (!iter.hasNext()) {
            return null;
        }
        result.seriesColumn(iter.next());

        if (!iter.hasNext()) {
            return null;
        }
        result.periodColumn(iter.next());

        if (!iter.hasNext()) {
            return null;
        }
        result.valueColumn(iter.next());

        if (iter.hasNext()) {
            result.domainName(iter.next());
            if (iter.hasNext()) {
                result.seriesName(iter.next());
            }
        }

        return result.build();
    }

    @lombok.RequiredArgsConstructor
    private static final class OdbcIdParser extends AbstractIterator<String> {

        private final String input;
        private int beg = 0;
        private int end = -ESEP.length();

        @Override
        protected String get() {
            return input.substring(beg, end);
        }

        @Override
        protected boolean moveNext() {
            beg = end + ESEP.length();
            beg = input.indexOf(BSEP, beg);
            if (beg != 0) {
                return false;
            }
            beg += BSEP.length();

            end = input.indexOf(ESEP, beg);
            if (end < 0) {
                return false;
            }
            return true;
        }
    }

    private static final String BSEP = "<<";
    private static final String ESEP = ">>";
}
