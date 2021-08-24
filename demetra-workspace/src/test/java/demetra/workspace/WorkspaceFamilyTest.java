/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.workspace;

import demetra.util.LinearId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class WorkspaceFamilyTest {

    private final WorkspaceFamily empty = WorkspaceFamily.parse("");
    private final WorkspaceFamily single = WorkspaceFamily.parse("single");
    private final WorkspaceFamily dual = WorkspaceFamily.parse("hello@world");

    @Test
    @SuppressWarnings("null")
    public void testOf() {
        assertThatThrownBy(() -> WorkspaceFamily.of(null)).isInstanceOf(NullPointerException.class);
        assertThat(WorkspaceFamily.of(empty)).isSameAs(empty);
        assertThat(WorkspaceFamily.of(new LinearId())).isEqualTo(empty);
        assertThat(WorkspaceFamily.of(new LinearId("hello", "world"))).isEqualTo(dual);
        assertThat(LinearId.of(dual)).isEqualTo(new LinearId("hello", "world"));
    }

    @Test
    public void testGet() {
        assertThatThrownBy(() -> empty.get(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(single.get(0)).isEqualTo("single");
        assertThat(dual.get(0)).isEqualTo("hello");
        assertThat(dual.get(1)).isEqualTo("world");
    }

    @Test
    public void testGetCount() {
        assertThat(empty.getCount()).isEqualTo(0);
        assertThat(single.getCount()).isEqualTo(1);
        assertThat(dual.getCount()).isEqualTo(2);
    }

    @Test
    public void testStartWith() {
        assertThat(empty.startsWith(empty)).isTrue();
        assertThat(empty.startsWith(single)).isFalse();
        assertThat(empty.startsWith(dual)).isFalse();
        assertThat(single.startsWith(empty)).isTrue();
        assertThat(single.startsWith(single)).isTrue();
        assertThat(single.startsWith(dual)).isFalse();
        assertThat(dual.startsWith(empty)).isTrue();
        assertThat(dual.startsWith(single)).isFalse();
        assertThat(dual.startsWith(dual)).isTrue();
        assertThat(dual.startsWith(WorkspaceFamily.parse("hello"))).isTrue();
    }

    @Test
    public void testEquals() {
        assertThat(empty)
                .isEqualTo(WorkspaceFamily.parse(""))
                .isNotEqualTo(single)
                .isNotEqualTo(dual);
        assertThat(single)
                .isEqualTo(WorkspaceFamily.parse("single"))
                .isNotEqualTo(empty)
                .isNotEqualTo(dual);
        assertThat(dual)
                .isEqualTo(WorkspaceFamily.parse("hello@world"))
                .isNotEqualTo(empty)
                .isNotEqualTo(single);
    }

    @Test
    public void testHashcode() {
        assertThat(empty.hashCode())
                .isEqualTo(WorkspaceFamily.parse("").hashCode())
                .isNotEqualTo(single.hashCode())
                .isNotEqualTo(dual.hashCode());
        assertThat(single.hashCode())
                .isEqualTo(WorkspaceFamily.parse("single").hashCode())
                .isNotEqualTo(empty.hashCode())
                .isNotEqualTo(dual.hashCode());
        assertThat(dual.hashCode())
                .isEqualTo(WorkspaceFamily.parse("hello@world").hashCode())
                .isNotEqualTo(empty.hashCode())
                .isNotEqualTo(single.hashCode());
    }

    @Test
    public void testTail() {
        assertThat(empty.tail()).isEqualTo(null);
        assertThat(single.tail()).isEqualTo("single");
        assertThat(dual.tail()).isEqualTo("world");
    }

    @Test
    public void testToString() {
        assertThat(empty.toString()).isEqualTo("");
        assertThat(single.toString()).isEqualTo("single");
        assertThat(dual.toString()).isEqualTo("hello@world");
    }

    @Test
    public void testExtend() {
        assertThat(empty.extend("other")).isEqualTo(WorkspaceFamily.parse("other"));
        assertThat(single.extend("other")).isEqualTo(WorkspaceFamily.parse("single@other"));
        assertThat(dual.extend("other")).isEqualTo(WorkspaceFamily.parse("hello@world@other"));
    }

    @Test
    public void testParent() {
        assertThat(empty.parent()).isNull();
        assertThat(single.parent()).isEqualTo(empty);
        assertThat(dual.parent()).isEqualTo(WorkspaceFamily.parse("hello"));
    }

    @Test
    public void testPath() {
        assertThat(empty.path()).isEmpty();
        assertThat(single.path()).containsExactly(single);
        assertThat(dual.path()).containsExactly(WorkspaceFamily.parse("hello"), WorkspaceFamily.parse("hello@world"));
    }

    @Test
    public void testCompareTo() {
        assertThat(single).isLessThan(dual);
        assertThat(dual).isGreaterThan(single);
        assertThat(dual).isEqualByComparingTo(dual);
        assertThat(WorkspaceFamily.parse("a")).isLessThan(WorkspaceFamily.parse("b"));
        assertThat(WorkspaceFamily.parse("b")).isGreaterThan(WorkspaceFamily.parse("a"));
    }

    @Test
    public void testToArray() {
        assertThat(empty.toArray()).isEmpty();
        assertThat(single.toArray()).containsExactly("single");
        assertThat(dual.toArray()).containsExactly("hello", "world");
        assertThat(single.toArray()).isNotSameAs(single.toArray());
    }
}
