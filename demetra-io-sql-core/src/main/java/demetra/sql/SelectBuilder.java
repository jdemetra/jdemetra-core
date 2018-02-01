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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import demetra.design.IBuilder;
import sql.util.SqlIdentifierQuoter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "from")
final class SelectBuilder implements IBuilder<String> {

    private static final Joiner COMMA_JOINER = Joiner.on(',');

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
        COMMA_JOINER.appendTo(result, select.stream().map(toQuotedIdentifier).iterator());
        // FROM
        result.append(" FROM ").append(toQuotedIdentifier.apply(table));
        // WHERE
        if (!filter.isEmpty()) {
            result.append(" WHERE ");
            Iterator<String> iter = filter.stream().map(toQuotedIdentifier).iterator();
            result.append(iter.next()).append("=?");
            while (iter.hasNext()) {
                result.append(" AND ").append(iter.next()).append("=?");
            }
        }
        // ORDER BY
        if (!order.isEmpty()) {
            result.append(" ORDER BY ");
            COMMA_JOINER.appendTo(result, order.stream().map(toQuotedIdentifier).iterator());
        }
        return result.toString();
    }

    @Nonnull
    private SelectBuilder addIfNotNullOrEmpty(@Nonnull List<String> list, @Nonnull String... values) {
        for (String o : values) {
            if (!Strings.isNullOrEmpty(o)) {
                list.add(o);
            }
        }
        return this;
    }
}
