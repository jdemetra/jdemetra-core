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
package util.sql;

import internal.util.sql.SqlIdentifierQuoterImpl;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * A class that quotes identifiers in SQL queries.
 *
 * @author Philippe Charles
 */
@FunctionalInterface
public interface SqlIdentifierQuoter {

    @Nonnull
    String quote(@Nonnull String identifier, boolean force);

    @Nonnull
    default String quote(@Nonnull String identifier) {
        return quote(identifier, false);
    }

    @Nonnull
    static SqlIdentifierQuoter of(@Nonnull DatabaseMetaData metaData) throws SQLException {
        return SqlIdentifierQuoterImpl.of(metaData);
    }

    @Nonnull
    static SqlIdentifierQuoter noOp() {
        return (o, f) -> o;
    }
}
