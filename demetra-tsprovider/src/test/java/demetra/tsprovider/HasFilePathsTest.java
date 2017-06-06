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
package demetra.tsprovider;

import java.io.File;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class HasFilePathsTest {

    @Test
    @SuppressWarnings("null")
    public void testFactory() {
        assertThat(HasFilePaths.of()).isNotNull();
        assertThatThrownBy(() -> HasFilePaths.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testPaths() {
        HasFilePaths support = HasFilePaths.of();
        assertThat(support.getPaths())
                .isNotSameAs(support.getPaths())
                .isEmpty();
        File[] files = new File[]{new File("hello"), new File("world")};
        support.setPaths(files);
        assertThat(support.getPaths())
                .containsExactly(files)
                .isNotSameAs(files)
                .isNotSameAs(support.getPaths());
        support.setPaths(null);
        assertThat(support.getPaths()).isEmpty();
    }
}
