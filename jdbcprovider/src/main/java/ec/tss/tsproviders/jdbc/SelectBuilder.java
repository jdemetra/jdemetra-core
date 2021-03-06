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
package ec.tss.tsproviders.jdbc;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import ec.tstoolkit.design.IBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import nbbrd.sql.jdbc.SqlIdentifierQuoter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
final class SelectBuilder implements IBuilder<String> {

    @NonNull
    public static SelectBuilder from(@NonNull String table) {
        return new SelectBuilder(table);
    }

    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private final String table;
    private final List<String> select;
    private final List<String> filter;
    private final List<String> order;
    private boolean distinct;
    private SqlIdentifierQuoter identifierQuoter;

    private SelectBuilder(@NonNull String table) {
        this.table = table;
        this.select = new ArrayList<>();
        this.filter = new ArrayList<>();
        this.order = new ArrayList<>();
        this.distinct = false;
        this.identifierQuoter = null;
    }

    @NonNull
    private SelectBuilder addIfNotNullOrEmpty(@NonNull List<String> list, @NonNull String... values) {
        for (String o : values) {
            if (!Strings.isNullOrEmpty(o)) {
                list.add(o);
            }
        }
        return this;
    }

    @NonNull
    SelectBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @NonNull
    SelectBuilder select(@NonNull String... select) {
        return addIfNotNullOrEmpty(this.select, select);
    }

    @NonNull
    SelectBuilder filter(@NonNull String... filter) {
        return addIfNotNullOrEmpty(this.filter, filter);
    }

    @NonNull
    SelectBuilder orderBy(@NonNull String... order) {
        return addIfNotNullOrEmpty(this.order, order);
    }

    @NonNull
    SelectBuilder withQuoter(@NonNull SqlIdentifierQuoter identifierQuoter) {
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
}
