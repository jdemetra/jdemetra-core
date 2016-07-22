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
package ec.tstoolkit.utilities;

import ec.tstoolkit.utilities.RawObsList.RawObsListImpl;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class RawObsListTest {

    @Test
    public void testArrayList() {
        RawObsListImpl list = (RawObsListImpl) RawObsList.fromArrayList();

        assertThat(list)
                .extracting(RawObsList::size, RawObsListImpl::isSorted)
                .containsExactly(0, true);

        list.add(201002, 20);
        assertThat(list)
                .extracting(RawObsList::size, RawObsListImpl::isSorted)
                .containsExactly(1, true);

        list.add(201004, 40);
        assertThat(list)
                .extracting(RawObsList::size, RawObsListImpl::isSorted)
                .containsExactly(2, true);

        list.add(201004, 41);
        assertThat(list)
                .extracting(RawObsList::size, RawObsListImpl::isSorted)
                .containsExactly(3, true);

        list.add(201001, 10);
        assertThat(list)
                .extracting(RawObsList::size, RawObsListImpl::isSorted)
                .containsExactly(4, false);

        list.add(201002, 21);
        assertThat(list)
                .extracting(RawObsList::size, RawObsListImpl::isSorted)
                .containsExactly(5, false);

        list.clear();
        assertThat(list)
                .extracting(RawObsList::size, RawObsListImpl::isSorted)
                .containsExactly(0, true);

        list.add(201004, 40);
        list.add(201001, 10);
        assertThat(list)
                .extracting(o -> o.getPeriod(0), o -> o.getValue(0), o -> o.getPeriod(1), o -> o.getValue(1), RawObsListImpl::isSorted)
                .containsExactly(201004L, 40D, 201001L, 10D, false);

        list.sortByPeriod();
        assertThat(list)
                .extracting(o -> o.getPeriod(0), o -> o.getValue(0), o -> o.getPeriod(1), o -> o.getValue(1), RawObsListImpl::isSorted)
                .containsExactly(201001L, 10D, 201004L, 40D, true);
    }
}
