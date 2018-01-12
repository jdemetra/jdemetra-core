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
package internal.tsprovider.grid;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static test.tsprovider.grid.Data.*;
import test.tsprovider.grid.GridInputs;

/**
 *
 * @author Philippe Charles
 */
public class InvGridInputTest {

    @Test
    public void test() {
        InvGridInput input = InvGridInput.of(HGRID_WITH_HEADER);
        assertThat(input.getName()).isEqualTo("hello");
        assertThat(GridInputs.toArray(input)).containsExactly(GridInputs.toArray(VGRID_WITH_HEADER));
    }
}
