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

import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CubeIdTest {

    public static final CubeId EMPTY = CubeId.root();
    public static final CubeId SECTOR_REGION = CubeId.root("sector", "region");
    public static final CubeId INDUSTRY = SECTOR_REGION.child("industry");
    public static final CubeId INDUSTRY_BE = INDUSTRY.child("be");

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatThrownBy(() -> CubeId.root((String[]) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeId.root("hello", (String) null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CubeId.root((List<String>) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CubeId.root(Arrays.asList("hello", null))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetLevel() {
        assertThat(EMPTY.getLevel()).isEqualTo(0);
        assertThat(SECTOR_REGION.getLevel()).isEqualTo(0);
        assertThat(INDUSTRY.getLevel()).isEqualTo(1);
    }

    @Test
    public void testGetMaxLevel() {
        assertThat(EMPTY.getMaxLevel()).isEqualTo(0);
        assertThat(SECTOR_REGION.getMaxLevel()).isEqualTo(2);
    }

    @Test
    public void testGetDimensionValue() {
        assertThatThrownBy(() -> SECTOR_REGION.getDimensionValue(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> SECTOR_REGION.getDimensionValue(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(INDUSTRY.getDimensionValue(0)).isEqualTo("industry");
    }

    @Test
    public void testGetDimensionId() {
        assertThatThrownBy(() -> SECTOR_REGION.getDimensionId(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> SECTOR_REGION.getDimensionId(2)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(SECTOR_REGION.getDimensionId(0)).isEqualTo("sector");
    }

    @Test
    public void testGetDepth() {
        assertThat(EMPTY.getDepth()).isEqualTo(0);
        assertThat(SECTOR_REGION.getDepth()).isEqualTo(2);
        assertThat(INDUSTRY.getDepth()).isEqualTo(1);
    }

    @Test
    public void testIsRoot() {
        assertThat(EMPTY.isRoot()).isTrue();
        assertThat(SECTOR_REGION.isRoot()).isTrue();
        assertThat(INDUSTRY.isRoot()).isFalse();
    }

    @Test
    public void testIsSeries() {
        assertThat(EMPTY.isSeries()).isTrue();
        assertThat(SECTOR_REGION.isSeries()).isFalse();
        assertThat(INDUSTRY.isSeries()).isFalse();
        assertThat(INDUSTRY.child("be").isSeries()).isTrue();
    }

    @Test
    public void testIsVoid() {
        assertThat(EMPTY.isVoid()).isTrue();
        assertThat(SECTOR_REGION.isVoid()).isFalse();
        assertThat(INDUSTRY.isVoid()).isFalse();
    }

    @Test
    public void testChild() {
        assertThatThrownBy(() -> EMPTY.child("hello")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EMPTY.child((String) null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EMPTY.child("hello", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EMPTY.child((String[]) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testGetParent() {
        assertThat(EMPTY.getParent()).isNull();
        assertThat(SECTOR_REGION.getParent()).isNull();
        assertThat(INDUSTRY.getParent()).isEqualTo(SECTOR_REGION);
    }

    @Test
    public void testGetAncestor() {
        assertThat(EMPTY.getAncestor(0)).isNull();
        assertThat(SECTOR_REGION.getAncestor(0)).isNull();
        assertThat(INDUSTRY.getAncestor(0)).isEqualTo(SECTOR_REGION);
        assertThat(INDUSTRY.getAncestor(1)).isNull();
        assertThat(INDUSTRY_BE.getAncestor(0)).isEqualTo(SECTOR_REGION);
        assertThat(INDUSTRY_BE.getAncestor(1)).isEqualTo(INDUSTRY);
        assertThat(INDUSTRY_BE.getAncestor(2)).isNull();
    }

    @Test
    @SuppressWarnings("null")
    public void testIsAncestorOf() {
        assertThatThrownBy(() -> EMPTY.isAncestorOf(null)).isInstanceOf(NullPointerException.class);
        assertThat(EMPTY.isAncestorOf(EMPTY)).isFalse();
        assertThat(SECTOR_REGION.isAncestorOf(SECTOR_REGION)).isFalse();
        assertThat(SECTOR_REGION.isAncestorOf(INDUSTRY)).isTrue();
        assertThat(INDUSTRY.isAncestorOf(SECTOR_REGION)).isFalse();
    }

    @Test
    public void testEquals() {
        assertThat(EMPTY).isEqualTo(CubeId.root());
        assertThat(EMPTY).isNotEqualTo(null);
        assertThat(EMPTY).isNotEqualTo(SECTOR_REGION);
        assertThat(SECTOR_REGION).isEqualTo(CubeId.root("sector", "region"));
        assertThat(SECTOR_REGION).isNotEqualTo(INDUSTRY);
    }

    @Test
    public void testHashCode() {
        assertThat(EMPTY.hashCode())
                .isEqualTo(EMPTY.hashCode())
                .isNotEqualTo(SECTOR_REGION.hashCode());
        assertThat(SECTOR_REGION.hashCode())
                .isEqualTo(SECTOR_REGION.hashCode())
                .isNotEqualTo(INDUSTRY.hashCode());
        assertThat(INDUSTRY.hashCode())
                .isEqualTo(INDUSTRY.hashCode())
                .isEqualTo(SECTOR_REGION.child("industry").hashCode());
    }
}
