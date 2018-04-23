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
package internal.sql;

import demetra.design.IBuilder;
import util.sql.SqlIdentifierQuoter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "from")
public final class SelectBuilder implements IBuilder<String> {

    @lombok.NonNull
    private final String table;

    private final List<String> select = new ArrayList<>();
    private final List<String> filter = new ArrayList<>();
    private final List<String> order = new ArrayList<>();

    private boolean distinct = false;
    private SqlIdentifierQuoter identifierQuoter = SqlIdentifierQuoter.noOp();

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
        this.identifierQuoter = Objects.requireNonNull(identifierQuoter);
        return this;
    }

    @Override
    public String build() {
        StringBuilder result = new StringBuilder();
        appendSelect(result);
        appendFrom(result);
        appendWhere(result);
        appendOrderBy(result);
        return result.toString();
    }

    private void appendOrderBy(StringBuilder result) {
        if (!order.isEmpty()) {
            result.append(" ORDER BY ");
            result.append(order.stream().map(identifierQuoter::quote).collect(COMMA_JOINER));
        }
    }

    private void appendWhere(StringBuilder result) {
        if (!filter.isEmpty()) {
            result.append(" WHERE ");
            result.append(filter.stream().map(identifierQuoter::quote).collect(WHERE_JOINER));
        }
    }

    private void appendFrom(StringBuilder result) {
        result.append(" FROM ").append(identifierQuoter.quote(table));
    }

    private void appendSelect(StringBuilder result) {
        result.append("SELECT ");
        if (distinct) {
            result.append("DISTINCT ");
        }
        result.append(select.stream().map(identifierQuoter::quote).collect(COMMA_JOINER));
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
