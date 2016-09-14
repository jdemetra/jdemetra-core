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
package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.timeseries.simplets.ObsLists.PreSortedLongObsList;
import ec.tstoolkit.timeseries.simplets.ObsLists.SortableLongObsList;
import static ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class ObsListTest {

    @Test
    public void testSortableLongObsList() {
        SortableLongObsList obs = new SortableLongObsList((x, y) -> (int) y);

        assertThat(obs.isSorted()).isTrue();
        obs.add(201002, 20);
        assertThat(obs.isSorted()).isTrue();
        obs.add(201004, 40);
        assertThat(obs.isSorted()).isTrue();
        obs.add(201004, 41);
        assertThat(obs.isSorted()).isTrue();
        obs.add(201001, 10);
        assertThat(obs.isSorted()).isFalse();
        obs.add(201002, 21);
        assertThat(obs.isSorted()).isFalse();
        obs.clear();
        assertThat(obs.isSorted()).isTrue();

        assertThat(obs.size()).isEqualTo(0);
        obs.add(201002, 20);
        assertThat(obs.size()).isEqualTo(1);

        obs.clear();
        obs.add(201004, 40);
        assertThat(obs.getPeriodId(Monthly, 0)).isEqualTo(201004);
        assertThat(obs.getValue(0)).isEqualTo(40);
        obs.add(201001, 10);
        assertThat(obs.getPeriodId(Monthly, 1)).isEqualTo(201001);
        assertThat(obs.getValue(1)).isEqualTo(10);

        obs.sortByPeriod();
        assertThat(obs.getPeriodId(Monthly, 0)).isEqualTo(201001);
        assertThat(obs.getValue(0)).isEqualTo(10);
        assertThat(obs.getPeriodId(Monthly, 1)).isEqualTo(201004);
        assertThat(obs.getValue(1)).isEqualTo(40);
    }

    @Test
    public void testPreSortedLongObsList() {
        PreSortedLongObsList obs = new PreSortedLongObsList((x, y) -> (int) y, 1);

        assertThat(obs.size()).isEqualTo(0);
        obs.add(201002, 20);
        assertThat(obs.size()).isEqualTo(1);

        obs.clear();
        obs.add(201004, 40);
        assertThat(obs.getPeriodId(Monthly, 0)).isEqualTo(201004);
        assertThat(obs.getValue(0)).isEqualTo(40);
        obs.add(201001, 10);
        assertThat(obs.getPeriodId(Monthly, 1)).isEqualTo(201001);
        assertThat(obs.getValue(1)).isEqualTo(10);

        obs.sortByPeriod();
    }
}
