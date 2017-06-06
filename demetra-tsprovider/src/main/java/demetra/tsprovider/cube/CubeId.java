/*
 * Copyright 2015 National Bank of Belgium
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
package demetra.tsprovider.cube;

import demetra.design.Immutable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an identifier of a cube resource such as a time series or a
 * collection.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@Immutable
public final class CubeId {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Creates a CubeId from the specified dimensions.
     *
     * @param dimensionIds the dimensions of the id
     * @return a non-null id
     * @throws IllegalArgumentException if one dimension is null
     */
    @Nonnull
    public static CubeId root(@Nonnull String... dimensionIds) throws IllegalArgumentException {
        for (String o : dimensionIds) {
            if (o == null) {
                throw new IllegalArgumentException("Dimensions cannot be null");
            }
        }
        return dimensionIds.length == 0 ? VOID : new CubeId(dimensionIds.clone(), EMPTY_STRING_ARRAY);
    }

    /**
     * Creates a CubeId from the specified dimensions.
     *
     * @param dimensionIds the dimensions of the id
     * @return a non-null id
     * @throws IllegalArgumentException if one dimension is null
     */
    @Nonnull
    public static CubeId root(@Nonnull List<String> dimensionIds) throws IllegalArgumentException {
        for (String o : dimensionIds) {
            if (o == null) {
                throw new IllegalArgumentException("Dimensions cannot be null");
            }
        }
        return dimensionIds.isEmpty() ? VOID : new CubeId(dimensionIds.toArray(new String[dimensionIds.size()]), EMPTY_STRING_ARRAY);
    }

    private static final CubeId VOID = new CubeId(EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);

    private final String[] dimensionIds;
    private final String[] dimensionsValues;

    private CubeId(@Nonnull String[] dimensionIds, @Nonnull String[] dimensionsValues) {
        this.dimensionsValues = dimensionsValues;
        this.dimensionIds = dimensionIds;
    }

    @Nonnegative
    public int getLevel() {
        return dimensionsValues.length;
    }

    @Nonnegative
    public int getMaxLevel() {
        return dimensionIds.length;
    }

    @Nonnull
    public String getDimensionValue(@Nonnegative int index) throws IndexOutOfBoundsException {
        return dimensionsValues[index];
    }

    @Nonnull
    public Stream<String> getDimensionValueStream() {
        return Stream.of(dimensionsValues);
    }

    @Nonnull
    public String getDimensionId(@Nonnegative int index) throws IndexOutOfBoundsException {
        return dimensionIds[index];
    }

    @Nonnegative
    public int getDepth() {
        return getMaxLevel() - getLevel();
    }

    public boolean isRoot() {
        return getLevel() == 0;
    }

    public boolean isSeries() {
        return getLevel() == getMaxLevel();
    }

    public boolean isVoid() {
        return getMaxLevel() == 0;
    }

    @Nonnull
    public CubeId child(@Nonnull String... dimensionValues) throws IllegalArgumentException {
        if (this.dimensionsValues.length + dimensionValues.length > dimensionIds.length) {
            throw new IllegalArgumentException("Too much values");
        }
        for (String o : dimensionValues) {
            if (o == null) {
                throw new IllegalArgumentException("Dimension values cannot be null");
            }
        }
        if (dimensionValues.length == 0) {
            return this;
        }
        String[] result = Arrays.copyOf(this.dimensionsValues, this.dimensionsValues.length + dimensionValues.length);
        System.arraycopy(dimensionValues, 0, result, this.dimensionsValues.length, dimensionValues.length);
        return new CubeId(dimensionIds, result);
    }

    public boolean isAncestorOf(@Nonnull CubeId input) {
        if (!haveSameDimensions(this, input) || getLevel() >= input.getLevel()) {
            return false;
        }
        for (int i = getLevel() - 1; i >= 0; i--) {
            if (!input.getDimensionValue(i).equals(getDimensionValue(i))) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    CubeId getParent() {
        return getAncestor(getLevel() - 1);
    }

    @Nullable
    public CubeId getAncestor(int level) {
        return 0 <= level && level < getLevel() ? new CubeId(dimensionIds, Arrays.copyOf(dimensionsValues, level)) : null;
    }

    @Override
    public String toString() {
        return Arrays.toString(dimensionsValues);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof CubeId && equals((CubeId) obj));
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(dimensionIds) + Arrays.deepHashCode(dimensionsValues);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private boolean equals(@Nonnull CubeId that) {
        // reversed values first to fail fast
        return equalsReversed(dimensionsValues, that.dimensionsValues) && Arrays.equals(dimensionIds, that.dimensionIds);
    }

    private static boolean equalsReversed(@Nullable Object[] l, @Nullable Object[] r) {
        if (l == r) {
            return true;
        }
        if (l == null || r == null || l.length != r.length) {
            return false;
        }
        for (int i = l.length - 1; i >= 0; i--) {
            if (!Objects.equals(l[i], r[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean haveSameDimensions(CubeId l, CubeId r) {
        return Arrays.equals(l.dimensionIds, r.dimensionIds);
    }
    //</editor-fold>
}
