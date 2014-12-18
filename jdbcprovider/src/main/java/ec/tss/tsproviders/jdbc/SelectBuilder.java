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

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import ec.tstoolkit.design.IBuilder;
import ec.util.jdbc.SqlIdentifierQuoter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
class SelectBuilder implements IBuilder<String> {

    @Nonnull
    public static SelectBuilder from(@Nonnull String table) {
        return new SelectBuilder(table);
    }
    //
    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private final String table;
    private final List<String> select;
    private final List<String> filter;
    private final List<String> order;
    private boolean distinct;
    private SqlIdentifierQuoter identifierQuoter;

    private SelectBuilder(@Nonnull String table) {
        this.table = table;
        this.select = new ArrayList<>();
        this.filter = new ArrayList<>();
        this.order = new ArrayList<>();
        this.distinct = false;
        this.identifierQuoter = null;
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

    @Nonnull
    SelectBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    @Nonnull
    SelectBuilder select(@Nonnull String... select) {
        return addIfNotNullOrEmpty(this.select, select);
    }

    @Nonnull
    SelectBuilder filter(@Nonnull String... filter) {
        return addIfNotNullOrEmpty(this.filter, filter);
    }

    @Nonnull
    SelectBuilder orderBy(@Nonnull String... order) {
        return addIfNotNullOrEmpty(this.order, order);
    }

    @Nonnull
    SelectBuilder withQuoter(@Nonnull SqlIdentifierQuoter identifierQuoter) {
        this.identifierQuoter = identifierQuoter;
        return this;
    }

    @Override
    public String build() {
        Function<String, String> toQuotedIdentifier = getIdentifierQuotingFunc();
        StringBuilder result = new StringBuilder();
        // SELECT
        result.append("SELECT ");
        if (distinct) {
            result.append("DISTINCT ");
        }
        COMMA_JOINER.appendTo(result, Iterables.transform(select, toQuotedIdentifier));
        // FROM
        result.append(" FROM ").append(toQuotedIdentifier.apply(table));
        // WHERE
        if (!filter.isEmpty()) {
            result.append(" WHERE ");
            Iterator<String> iter = Iterables.transform(filter, toQuotedIdentifier).iterator();
            result.append(iter.next()).append("=?");
            while (iter.hasNext()) {
                result.append(" AND ").append(iter.next()).append("=?");
            }
        }
        // ORDER BY
        if (!order.isEmpty()) {
            result.append(" ORDER BY ");
            COMMA_JOINER.appendTo(result, Iterables.transform(order, toQuotedIdentifier));
        }
        return result.toString();
    }

    private Function<String, String> getIdentifierQuotingFunc() {
        return identifierQuoter != null ? new IdentifierQuotingFunc(identifierQuoter) : Functions.<String>identity();
    }

    private static final class IdentifierQuotingFunc implements Function<String, String> {

        private final SqlIdentifierQuoter identifierQuoter;

        public IdentifierQuotingFunc(@Nonnull SqlIdentifierQuoter identifierQuoter) {
            this.identifierQuoter = identifierQuoter;
        }

        @Override
        public String apply(String identifier) {
            return identifierQuoter.quote(identifier, false);
        }
    }
}
