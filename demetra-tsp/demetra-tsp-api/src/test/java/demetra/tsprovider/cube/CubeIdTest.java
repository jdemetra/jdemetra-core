/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.tsprovider.cube;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Philippe Charles
 */
public class CubeIdTest {

    static final CubeId DIM0_LEV0 = CubeId.root();

    static final CubeId DIM1_LEV0 = CubeId.root("sector");
    static final CubeId DIM1_LEV1 = DIM1_LEV0.child("industry");

    static final CubeId DIM2_LEV0 = CubeId.root("sector", "region");
    static final CubeId DIM2_LEV1 = DIM2_LEV0.child("industry");
    static final CubeId DIM2_LEV2 = DIM2_LEV1.child("be");

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatNullPointerException().isThrownBy(() -> CubeId.root((String[]) null));
        assertThatIllegalArgumentException().isThrownBy(() -> CubeId.root("hello", (String) null));
        assertThatNullPointerException().isThrownBy(() -> CubeId.root((List<String>) null));
        assertThatIllegalArgumentException().isThrownBy(() -> CubeId.root(Arrays.asList("hello", null)));
    }

    @Test
    public void testGetLevel() {
        assertThat(DIM0_LEV0.getLevel()).isEqualTo(0);

        assertThat(DIM1_LEV0.getLevel()).isEqualTo(0);
        assertThat(DIM1_LEV1.getLevel()).isEqualTo(1);

        assertThat(DIM2_LEV0.getLevel()).isEqualTo(0);
        assertThat(DIM2_LEV1.getLevel()).isEqualTo(1);
        assertThat(DIM2_LEV2.getLevel()).isEqualTo(2);
    }

    @Test
    public void testGetMaxLevel() {
        assertThat(DIM0_LEV0.getMaxLevel()).isEqualTo(0);

        assertThat(DIM1_LEV0.getMaxLevel()).isEqualTo(1);
        assertThat(DIM1_LEV1.getMaxLevel()).isEqualTo(1);

        assertThat(DIM2_LEV0.getMaxLevel()).isEqualTo(2);
        assertThat(DIM2_LEV1.getMaxLevel()).isEqualTo(2);
        assertThat(DIM2_LEV2.getMaxLevel()).isEqualTo(2);
    }

    @Test
    public void testGetDimensionValue() {
        assertThatThrownBy(() -> DIM2_LEV0.getDimensionValue(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> DIM2_LEV0.getDimensionValue(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> DIM2_LEV0.getDimensionValue(1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> DIM2_LEV0.getDimensionValue(2)).isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> DIM2_LEV1.getDimensionValue(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(DIM2_LEV1.getDimensionValue(0)).isEqualTo("industry");
        assertThatThrownBy(() -> DIM2_LEV1.getDimensionValue(1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> DIM2_LEV1.getDimensionValue(2)).isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> DIM2_LEV2.getDimensionValue(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(DIM2_LEV2.getDimensionValue(0)).isEqualTo("industry");
        assertThat(DIM2_LEV2.getDimensionValue(1)).isEqualTo("be");
        assertThatThrownBy(() -> DIM2_LEV2.getDimensionValue(2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testGetDimensionId() {
        assertThatThrownBy(() -> DIM2_LEV0.getDimensionId(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(DIM2_LEV0.getDimensionId(0)).isEqualTo("sector");
        assertThat(DIM2_LEV0.getDimensionId(1)).isEqualTo("region");
        assertThatThrownBy(() -> DIM2_LEV0.getDimensionId(2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testGetDepth() {
        assertThat(DIM0_LEV0.getDepth()).isEqualTo(0);

        assertThat(DIM1_LEV0.getDepth()).isEqualTo(1);
        assertThat(DIM1_LEV1.getDepth()).isEqualTo(0);

        assertThat(DIM2_LEV0.getDepth()).isEqualTo(2);
        assertThat(DIM2_LEV1.getDepth()).isEqualTo(1);
        assertThat(DIM2_LEV2.getDepth()).isEqualTo(0);
    }

    @Test
    public void testIsRoot() {
        assertThat(DIM0_LEV0.isRoot()).isTrue();

        assertThat(DIM1_LEV0.isRoot()).isTrue();
        assertThat(DIM1_LEV1.isRoot()).isFalse();

        assertThat(DIM2_LEV0.isRoot()).isTrue();
        assertThat(DIM2_LEV1.isRoot()).isFalse();
        assertThat(DIM2_LEV2.isRoot()).isFalse();
    }

    @Test
    public void testIsSeries() {
        assertThat(DIM0_LEV0.isSeries()).isTrue();

        assertThat(DIM1_LEV0.isSeries()).isFalse();
        assertThat(DIM1_LEV1.isSeries()).isTrue();

        assertThat(DIM2_LEV0.isSeries()).isFalse();
        assertThat(DIM2_LEV1.isSeries()).isFalse();
        assertThat(DIM2_LEV2.isSeries()).isTrue();
    }

    @Test
    public void testIsVoid() {
        assertThat(DIM0_LEV0.isVoid()).isTrue();

        assertThat(DIM1_LEV0.isVoid()).isFalse();
        assertThat(DIM1_LEV1.isVoid()).isFalse();

        assertThat(DIM2_LEV0.isVoid()).isFalse();
        assertThat(DIM2_LEV1.isVoid()).isFalse();
        assertThat(DIM2_LEV2.isVoid()).isFalse();
    }

    @Test
    public void testChild() {
        assertThatThrownBy(() -> DIM0_LEV0.child("hello")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DIM0_LEV0.child((String) null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DIM0_LEV0.child("hello", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DIM0_LEV0.child((String[]) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetParent() {
        assertThat(DIM0_LEV0.getParent()).isNull();

        assertThat(DIM1_LEV0.getParent()).isNull();
        assertThat(DIM1_LEV1.getParent()).isEqualTo(DIM1_LEV0);

        assertThat(DIM2_LEV0.getParent()).isNull();
        assertThat(DIM2_LEV1.getParent()).isEqualTo(DIM2_LEV0);
        assertThat(DIM2_LEV2.getParent()).isEqualTo(DIM2_LEV1);
    }

    @Test
    public void testGetAncestor() {
        assertThat(DIM0_LEV0.getAncestor(0)).isNull();

        assertThat(DIM1_LEV0.getAncestor(0)).isNull();
        assertThat(DIM1_LEV1.getAncestor(0)).isEqualTo(DIM1_LEV0);
        assertThat(DIM1_LEV1.getAncestor(1)).isNull();

        assertThat(DIM2_LEV0.getAncestor(0)).isNull();
        assertThat(DIM2_LEV1.getAncestor(0)).isEqualTo(DIM2_LEV0);
        assertThat(DIM2_LEV1.getAncestor(1)).isNull();
        assertThat(DIM2_LEV2.getAncestor(0)).isEqualTo(DIM2_LEV0);
        assertThat(DIM2_LEV2.getAncestor(1)).isEqualTo(DIM2_LEV1);
        assertThat(DIM2_LEV2.getAncestor(2)).isNull();
    }

    @Test
    @SuppressWarnings("null")
    public void testIsAncestorOf() {
        assertThatNullPointerException().isThrownBy(() -> DIM0_LEV0.isAncestorOf(null));

        assertThat(DIM0_LEV0.isAncestorOf(DIM0_LEV0)).isFalse();

        assertThat(DIM1_LEV0.isAncestorOf(DIM1_LEV0)).isFalse();
        assertThat(DIM1_LEV0.isAncestorOf(DIM1_LEV1)).isTrue();
        assertThat(DIM1_LEV1.isAncestorOf(DIM1_LEV0)).isFalse();

        assertThat(DIM2_LEV0.isAncestorOf(DIM2_LEV0)).isFalse();
        assertThat(DIM2_LEV0.isAncestorOf(DIM2_LEV1)).isTrue();
        assertThat(DIM2_LEV1.isAncestorOf(DIM2_LEV0)).isFalse();
        assertThat(DIM2_LEV0.isAncestorOf(DIM2_LEV2)).isTrue();
        assertThat(DIM2_LEV2.isAncestorOf(DIM2_LEV0)).isFalse();
        assertThat(DIM2_LEV1.isAncestorOf(DIM2_LEV2)).isTrue();
        assertThat(DIM2_LEV2.isAncestorOf(DIM2_LEV1)).isFalse();
    }

    @Test
    public void testEquals() {
        assertThat(DIM0_LEV0)
                .isEqualTo(CubeId.root())
                .isNotEqualTo(null)
                .isEqualTo(DIM0_LEV0)
                .isNotEqualTo(DIM1_LEV0)
                .isNotEqualTo(DIM1_LEV1)
                .isNotEqualTo(DIM2_LEV0)
                .isNotEqualTo(DIM2_LEV1)
                .isNotEqualTo(DIM2_LEV2);

        assertThat(DIM2_LEV0)
                .isEqualTo(CubeId.root("sector", "region"))
                .isNotEqualTo(null)
                .isNotEqualTo(DIM0_LEV0)
                .isNotEqualTo(DIM1_LEV0)
                .isNotEqualTo(DIM1_LEV1)
                .isEqualTo(DIM2_LEV0)
                .isNotEqualTo(DIM2_LEV1)
                .isNotEqualTo(DIM2_LEV2);
    }

    @Test
    public void testHashCode() {
        assertThat(DIM0_LEV0.hashCode())
                .isEqualTo(DIM0_LEV0.hashCode())
                .isNotEqualTo(DIM2_LEV0.hashCode());

        assertThat(DIM2_LEV0.hashCode())
                .isEqualTo(DIM2_LEV0.hashCode())
                .isNotEqualTo(DIM2_LEV1.hashCode());

        assertThat(DIM2_LEV1.hashCode())
                .isEqualTo(DIM2_LEV1.hashCode())
                .isEqualTo(DIM2_LEV0.child("industry").hashCode());
    }
}
