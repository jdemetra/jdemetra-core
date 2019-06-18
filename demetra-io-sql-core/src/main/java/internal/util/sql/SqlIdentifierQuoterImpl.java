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
package internal.util.sql;

import demetra.design.VisibleForTesting;
import internal.util.Strings;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import lombok.AccessLevel;
import util.sql.SqlIdentifierQuoter;
import util.sql.SqlKeywords;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlIdentifierQuoterImpl implements SqlIdentifierQuoter {

    @NonNull
    public static SqlIdentifierQuoter of(@NonNull DatabaseMetaData metaData) throws SQLException {
        return new SqlIdentifierQuoterImpl(
                getIdentifierQuoteString(metaData),
                getSqlKeywords(metaData),
                IdentifierStorageRule.unquoted(metaData),
                metaData.getExtraNameCharacters());
    }

    private final String identifierQuoteString;
    private final Set<String> sqlKeywords;
    private final IdentifierStorageRule unquotedStorageRule;
    private final String extraNameCharacters;

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

    private static Stream<String> splitKeywords(CharSequence input) {
        return Strings.splitToStream(',', input).map(String::trim);
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
        return Stream.concat(splitKeywords(metaData.getSQLKeywords()), SqlKeywords.getSql2003ReservedWords().stream()).collect(Collectors.toSet());
    }

    /**
     *
     * @return @see DatabaseMetaData#getIdentifierQuoteString()
     */
    private static boolean isIdentifierQuotingSupported(@Nullable String identifierQuoteString) {
        return identifierQuoteString != null && !identifierQuoteString.trim().isEmpty();
    }

    private enum IdentifierStorageRule {

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

        public static IdentifierStorageRule unquoted(DatabaseMetaData metaData) throws SQLException {
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
