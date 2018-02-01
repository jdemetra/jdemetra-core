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
package internal.sql.util;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import ec.tstoolkit.design.VisibleForTesting;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that quotes identifiers in SQL queries.
 *
 * @author Philippe Charles
 */
public abstract class SqlIdentifierQuoter {

    @Nonnull
    abstract public String quote(@Nonnull String identifier, boolean force);

    @Nonnull
    public static SqlIdentifierQuoter create(@Nonnull DatabaseMetaData metaData) throws SQLException {
        return new SqlIdentifierQuoterImpl(
                getIdentifierQuoteString(metaData),
                getSqlKeywords(metaData),
                StorageRule.unquoted(metaData),
                metaData.getExtraNameCharacters());
    }

    @VisibleForTesting
    @Nonnull
    static String getIdentifierQuoteString(@Nonnull DatabaseMetaData metaData) throws SQLException {
        String identifierQuoteString = metaData.getIdentifierQuoteString();
        return !isIdentifierQuotingSupported(identifierQuoteString) ? "\"" : identifierQuoteString;
    }

    @VisibleForTesting
    @Nonnull
    static Set<String> getSqlKeywords(@Nonnull DatabaseMetaData metaData) throws SQLException {
        return ImmutableSet.<String>builder()
                .addAll(KEYWORDS_SPLITTER.split(metaData.getSQLKeywords()))
                .addAll(SqlKeywords.getSql2003ReservedWords())
                .build();
    }

    private static final Splitter KEYWORDS_SPLITTER = Splitter.on(',').trimResults();

    private static final class SqlIdentifierQuoterImpl extends SqlIdentifierQuoter {

        private final String identifierQuoteString;
        private final Set<String> sqlKeywords;
        private final StorageRule unquotedStorageRule;
        private final String extraNameCharacters;

        public SqlIdentifierQuoterImpl(String identifierQuoteString, Set<String> sqlKeywords, StorageRule unquotedStorageRule, String extraNameCharacters) {
            this.identifierQuoteString = identifierQuoteString;
            this.sqlKeywords = sqlKeywords;
            this.unquotedStorageRule = unquotedStorageRule;
            this.extraNameCharacters = extraNameCharacters;
        }

        @Override
        public String quote(String identifier, boolean force) {
            if (isQuoted(identifier)) {
                return identifier;
            }
            if (force || containsExtraCharacters(identifier) || breaksStorageRule(identifier) || isSqlKeyword(identifier)) {
                return quoteIdentifier(identifier);
            }
            return identifier;
        }

        private boolean isQuoted(String identifier) {
            return identifier.startsWith(identifierQuoteString)
                    && identifier.endsWith(identifierQuoteString)
                    && identifier.length() != identifierQuoteString.length();
        }

        private boolean isSqlKeyword(String identifier) {
            return sqlKeywords.contains(identifier.toUpperCase(Locale.ROOT));
        }

        private boolean breaksStorageRule(String identifier) {
            return false;
            // FIXME: seems to follow API but fails in tests!
            //return !unquotedStorageRule.isValid(identifier);
        }

        private boolean containsExtraCharacters(String identifier) {
            for (int i = 0; i < identifier.length(); i++) {
                if (extraNameCharacters.indexOf(identifier.charAt(i)) != -1) {
                    return true;
                }
            }
            return false;
        }

        private String quoteIdentifier(String identifier) {
            return identifierQuoteString + identifier.replace(identifierQuoteString, identifierQuoteString + identifierQuoteString) + identifierQuoteString;
        }
    }

    /**
     *
     * @return @see DatabaseMetaData#getIdentifierQuoteString()
     */
    private static boolean isIdentifierQuotingSupported(@Nullable String identifierQuoteString) {
        return identifierQuoteString != null && !identifierQuoteString.trim().isEmpty();
    }

    private enum StorageRule {

        UPPER {
                    @Override
                    public boolean isValid(String identifier) {
                        return identifier.toUpperCase(Locale.ROOT).equals(identifier);
                    }
                },
        LOWER {
                    @Override
                    public boolean isValid(String identifier) {
                        return identifier.toLowerCase(Locale.ROOT).equals(identifier);
                    }
                },
        MIXED {
                    @Override
                    public boolean isValid(String identifier) {
                        return true;
                    }
                };

        abstract public boolean isValid(String identifier);

        public static StorageRule unquoted(DatabaseMetaData metaData) throws SQLException {
            if (metaData.storesUpperCaseIdentifiers()) {
                return UPPER;
            }
            if (metaData.storesLowerCaseIdentifiers()) {
                return LOWER;
            }
            if (metaData.storesMixedCaseIdentifiers()) {
                return MIXED;
            }
            return UPPER;

        }
    }
}
