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
package demetra.sql;

import demetra.design.IBuilder;
import sql.util.SqlIdentifierQuoter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "from")
final class SelectBuilder implements IBuilder<String> {

    @lombok.NonNull
    private final String table;

    private final List<String> select = new ArrayList<>();
    private final List<String> filter = new ArrayList<>();
    private final List<String> order = new ArrayList<>();

    private boolean distinct = false;
    private SqlIdentifierQuoter identifierQuoter = null;

    @Nonnull
    public SelectBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @Nonnull
    public SelectBuilder select(@Nonnull String... select) {
        return addIfNotNullOrEmpty(this.select, select);
    }

    @Nonnull
    public SelectBuilder filter(@Nonnull String... filter) {
        return addIfNotNullOrEmpty(this.filter, filter);
    }

    @Nonnull
    public SelectBuilder orderBy(@Nonnull String... order) {
        return addIfNotNullOrEmpty(this.order, order);
    }

    @Nonnull
    public SelectBuilder withQuoter(@Nonnull SqlIdentifierQuoter identifierQuoter) {
        this.identifierQuoter = identifierQuoter;
        return this;
    }

    @Override
    public String build() {
        Function<String, String> toQuotedIdentifier = identifierQuoter != null
                ? (o -> identifierQuoter.quote(o, false))
                : (o -> o);
        StringBuilder result = new StringBuilder();
        // SELECT
        result.append("SELECT ");
        if (distinct) {
            result.append("DISTINCT ");
        }
        result.append(select.stream().map(toQuotedIdentifier).collect(COMMA_JOINER));
        // FROM
        result.append(" FROM ").append(toQuotedIdentifier.apply(table));
        // WHERE
        if (!filter.isEmpty()) {
            result.append(" WHERE ");
            result.append(filter.stream().map(toQuotedIdentifier).collect(WHERE_JOINER));
        }
        // ORDER BY
        if (!order.isEmpty()) {
            result.append(" ORDER BY ");
            result.append(order.stream().map(toQuotedIdentifier).collect(COMMA_JOINER));
        }
        return result.toString();
    }

    @Nonnull
    private SelectBuilder addIfNotNullOrEmpty(@Nonnull List<String> list, @Nonnull String... values) {
        for (String o : values) {
            if (o != null && !o.isEmpty()) {
                list.add(o);
            }
        }
        return this;
    }

    private static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(",");
    private static final Collector<CharSequence, ?, String> WHERE_JOINER = Collectors.joining(" AND ", "", "=?");
}
