/*
 * Copyright 2013 National Bank of Belgium
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
package ec.util.jdbc;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import ec.tstoolkit.design.VisibleForTesting;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A class that quotes identifiers in SQL queries.
 *
 * @author Philippe Charles
 */
@Deprecated
public abstract class SqlIdentifierQuoter {

    @NonNull
    abstract public String quote(@NonNull String identifier, boolean force);

    @NonNull
    public static SqlIdentifierQuoter create(@NonNull DatabaseMetaData metaData) throws SQLException {
        return new SqlIdentifierQuoterImpl(nbbrd.sql.jdbc.SqlIdentifierQuoter.of(metaData));
    }

    @VisibleForTesting
    @NonNull
    static String getIdentifierQuoteString(@NonNull DatabaseMetaData metaData) throws SQLException {
        String identifierQuoteString = metaData.getIdentifierQuoteString();
        return !isIdentifierQuotingSupported(identifierQuoteString) ? "\"" : identifierQuoteString;
    }

    @VisibleForTesting
    @NonNull
    static Set<String> getSqlKeywords(@NonNull DatabaseMetaData metaData) throws SQLException {
        return ImmutableSet.<String>builder()
                .addAll(KEYWORDS_SPLITTER.split(metaData.getSQLKeywords()))
                .addAll(SqlKeywords.getSql2003ReservedWords())
                .build();
    }

    private static final Splitter KEYWORDS_SPLITTER = Splitter.on(',').trimResults();

    @lombok.AllArgsConstructor
    private static final class SqlIdentifierQuoterImpl extends SqlIdentifierQuoter {

        @lombok.NonNull
        private final nbbrd.sql.jdbc.SqlIdentifierQuoter delegate;

        @Override
        public String quote(String identifier, boolean force) {
            return delegate.quote(identifier, force);
        }
    }

    /**
     *
     * @return @see DatabaseMetaData#getIdentifierQuoteString()
     */
    private static boolean isIdentifierQuotingSupported(@Nullable String identifierQuoteString) {
        return identifierQuoteString != null && !identifierQuoteString.trim().isEmpty();
    }
}
