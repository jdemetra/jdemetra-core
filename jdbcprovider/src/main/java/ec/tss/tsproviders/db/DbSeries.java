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
package ec.tss.tsproviders.db;

import com.google.common.collect.Streams;
import ec.tss.tsproviders.utils.OptionalTsData;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
//@Immutable
public final class DbSeries {

    private final DbSetId id;
    private final OptionalTsData data;

    public DbSeries(@NonNull DbSetId id, @NonNull OptionalTsData data) {
        this.id = id;
        this.data = data;
    }

    @NonNull
    public DbSetId getId() {
        return id;
    }

    @NonNull
    public OptionalTsData getData() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DbSeries && equals((DbSeries) obj));
    }

    private boolean equals(DbSeries that) {
        return this.id.equals(that.id) && this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @NonNull
    public static DbSeries findById(@NonNull Iterable<DbSeries> iterable, @NonNull DbSetId id) throws NoSuchElementException {
        return Streams.stream(iterable)
                .filter(o -> (o != null) ? id.equals(o.getId()) : false)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    @NonNull
    public static List<DbSeries> filterByAncestor(@NonNull Iterable<DbSeries> iterable, @NonNull DbSetId ancestor) {
        return Streams.stream(iterable)
                .filter(o -> (o != null) ? isDescendant(ancestor, o.getId()) : false)
                .collect(Collectors.toList());
    }

    private static boolean isDescendant(@NonNull DbSetId ancestor, @NonNull DbSetId o) {
        for (int i = ancestor.getLevel() - 1; i >= 0; i--) {
            if (!o.getValue(i).equals(ancestor.getValue(i))) {
                return false;
            }
        }
        return true;
    }
}
