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

import com.google.common.base.Optional;
import ec.tss.tsproviders.utils.IConstraint;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.utilities.Arrays2;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@Immutable
public final class DbSetId {

    //<editor-fold defaultstate="collapsed" desc="Factory methods">
    @Nonnull
    public static DbSetId root(@Nonnull String... dimColumns) throws IllegalArgumentException {
        for (String o : dimColumns) {
            if (o == null) {
                throw new IllegalArgumentException("Columns cannot be null");
            }
        }
        return new DbSetId(Arrays2.EMPTY_STRING_ARRAY, dimColumns.clone());
    }
    //</editor-fold>
    private final String[] dimValues;
    private final String[] dimColumns;

    private DbSetId(@Nonnull String[] dimValues, @Nonnull String[] dimColumns) {
        this.dimValues = dimValues;
        this.dimColumns = dimColumns;
    }

    public int getLevel() {
        return dimValues.length;
    }

    @Nonnull
    public String getValue(int index) throws IndexOutOfBoundsException {
        return dimValues[index];
    }

    public int getMaxLevel() {
        return dimColumns.length;
    }

    @Nonnull
    public String getColumn(int index) throws IndexOutOfBoundsException {
        return dimColumns[index];
    }

    public int getDepth() {
        return getMaxLevel() - getLevel();
    }

    public boolean isSeries() {
        return dimValues.length == dimColumns.length;
    }

    @Nonnull
    public DbSetId child(@Nonnull String... dimValues) throws IllegalArgumentException {
        if (this.dimValues.length + dimValues.length > dimColumns.length) {
            throw new IllegalArgumentException("Too much values");
        }
        for (String o : dimValues) {
            if (o == null) {
                throw new IllegalArgumentException("Values cannot be null");
            }
        }
        return new DbSetId(Arrays2.concat(this.dimValues, dimValues), dimColumns);
    }

    public boolean isRoot() {
        return getLevel() == 0;
    }

    @Nonnull
    public Optional<DbSetId> parent() {
        return isRoot() ? Optional.<DbSetId>absent() : Optional.of(new DbSetId(Arrays.copyOf(dimValues, getLevel() - 1), dimColumns));
    }

    @Nonnull
    public String[] selectColumns() {
        String[] result = new String[getDepth()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getColumn(getLevel() + i);
        }
        return result;
    }

    @Nonnull
    public String[] filterColumns() {
        String[] result = new String[getLevel()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getColumn(i);
        }
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(dimValues);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DbSetId && equals((DbSetId) obj));
    }

    private boolean equals(@Nonnull DbSetId that) {
        // reversed values first to fail fast
        return equalsReversed(dimValues, that.dimValues) && Arrays.equals(dimColumns, that.dimColumns);
    }

    private static boolean equalsReversed(@Nullable Object[] l, @Nullable Object[] r) {
        if (l == r) {
            return true;
        }
        if (l == null || r == null || l.length != r.length) {
            return false;
        }
        for (int i = l.length - 1; i >= 0; i--) {
            if (!com.google.common.base.Objects.equal(l[i], r[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(dimColumns) + Arrays.deepHashCode(dimValues);
    }
    //
    public static final IConstraint<DbSetId> SERIES_CONSTRAINT = new IConstraint<DbSetId>() {
        @Override
        public String check(DbSetId t) {
            return t.isSeries() ? null : "Not a series";
        }
    };
    public static final IConstraint<DbSetId> COLLECTION_CONSTRAINT = new IConstraint<DbSetId>() {
        @Override
        public String check(DbSetId t) {
            return !t.isSeries() ? null : "Not a collection";
        }
    };
}
