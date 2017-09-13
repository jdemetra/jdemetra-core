/*
 * Copyright 2013 National Bank of Belgium
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

import demetra.tsprovider.grid.GridImport;
import demetra.tsprovider.grid.GridFactory;
import demetra.tsprovider.grid.GridLayout;
import demetra.tsprovider.grid.GridInput;
import demetra.tsprovider.grid.GridReader;
import demetra.tsprovider.grid.TsCollectionGrid;
import demetra.tsprovider.grid.TsGrid;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import static demetra.timeseries.TsUnit.MONTHLY;
import demetra.timeseries.simplets.TsData;
import demetra.tsprovider.OptionalTsData;
import static demetra.tsprovider.grid.GridLayout.HORIZONTAL;
import static demetra.tsprovider.grid.GridLayout.VERTICAL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class GridFactoryImplTest {

    static TsData data(TsUnit freq, int year, int position, double... values) {
        TsPeriod p = TsPeriod.yearly(year).withUnit(freq).plus(position);
        return TsData.ofInternal(p, values);
    }

    static TsGrid s(String name, TsData data) {
        return TsGrid.of(name, OptionalTsData.present(data));
    }

    static TsCollectionGrid of(GridLayout layout, String name, TsData data) {
        return TsCollectionGrid.builder().name("").layout(layout).item(s(name, data)).build();
    }

    private final LocalDateTime jan2010 = LocalDate.of(2010, 1, 1).atStartOfDay();
    private final LocalDateTime feb2010 = LocalDate.of(2010, 2, 1).atStartOfDay();
    private final LocalDateTime mar2010 = LocalDate.of(2010, 3, 1).atStartOfDay();
    private final GridFactory factory = GridFactoryImpl.INSTANCE;

    @Test
    public void testReadHorizontal() {
        GridReader reader = factory.getReader(GridImport.DEFAULT);

        Object[][] basic = {
            {null, jan2010, feb2010, mar2010},
            {"S1", 3.14, 4.56, 7.89}
        };

        assertThat(reader.read(ArrayGridInput.of(basic))).isEqualTo(of(HORIZONTAL, "S1", data(MONTHLY, 2010, 0, 3.14, 4.56, 7.89)));

        Object[][] withDateHeader = {
            {"Date", jan2010, feb2010, mar2010},
            {"S1", 3.14, 4.56, 7.89}
        };

        assertThat(reader.read(ArrayGridInput.of(withDateHeader))).isEqualTo(of(HORIZONTAL, "S1", data(MONTHLY, 2010, 0, 3.14, 4.56, 7.89)));

        Object[][] withoutHeader = {
            {jan2010, feb2010, mar2010},
            {3.14, 4.56, 7.89}
        };

        assertThat(reader.read(ArrayGridInput.of(withoutHeader))).isEqualTo(of(HORIZONTAL, "S1", data(MONTHLY, 2010, 0, 3.14, 4.56, 7.89)));

        Object[][] withMultipleHeaders = {
            {null, null, jan2010, feb2010, mar2010},
            {"G1", "S1", 3.14, 4.56, 7.89},
            {null, "S2", 3, 4, 5},
            {"G2", "S1", 7, 8, 9},
            {"S1", null, 0, 1, 2}
        };

        assertThat(reader.read(ArrayGridInput.of(withMultipleHeaders)))
                .isEqualTo(TsCollectionGrid.builder().name("").layout(HORIZONTAL)
                        .item(s("G1\nS1", data(MONTHLY, 2010, 0, 3.14, 4.56, 7.89)))
                        .item(s("G1\nS2", data(MONTHLY, 2010, 0, 3, 4, 5)))
                        .item(s("G2\nS1", data(MONTHLY, 2010, 0, 7, 8, 9)))
                        .item(s("S1", data(MONTHLY, 2010, 0, 0, 1, 2)))
                        .build());
    }

    @Test
    public void testReadVertical() {
        GridReader reader = factory.getReader(GridImport.DEFAULT);

        Object[][] basic = {
            {null, "S1"},
            {jan2010, 3.14},
            {feb2010, 4.56},
            {mar2010, 7.89}
        };

        assertThat(reader.read(ArrayGridInput.of(basic))).isEqualTo(of(VERTICAL, "S1", data(MONTHLY, 2010, 0, 3.14, 4.56, 7.89)));

        Object[][] withDateHeader = {
            {"Date", "S1"},
            {jan2010, 3.14},
            {feb2010, 4.56},
            {mar2010, 7.89}
        };

        assertThat(reader.read(ArrayGridInput.of(withDateHeader))).isEqualTo(of(VERTICAL, "S1", data(MONTHLY, 2010, 0, 3.14, 4.56, 7.89)));

        Object[][] withoutHeader = {
            {jan2010, 3.14},
            {feb2010, 4.56},
            {mar2010, 7.89}
        };

        assertThat(reader.read(ArrayGridInput.of(withoutHeader))).isEqualTo(of(VERTICAL, "S1", data(MONTHLY, 2010, 0, 3.14, 4.56, 7.89)));

        Object[][] withMultipleHeaders = {
            {null, "G1", null, "G2", "S1"},
            {null, "S1", "S2", "S1", null},
            {jan2010, 3.14, 3, 7, 0},
            {feb2010, 4.56, 4, 8, 1},
            {mar2010, 7.89, 5, 9, 2}
        };

        assertThat(reader.read(ArrayGridInput.of(withMultipleHeaders)))
                .isEqualTo(TsCollectionGrid.builder().name("").layout(VERTICAL)
                        .item(s("G1\nS1", data(MONTHLY, 2010, 0, 3.14, 4.56, 7.89)))
                        .item(s("G1\nS2", data(MONTHLY, 2010, 0, 3, 4, 5)))
                        .item(s("G2\nS1", data(MONTHLY, 2010, 0, 7, 8, 9)))
                        .item(s("S1", data(MONTHLY, 2010, 0, 0, 1, 2)))
                        .build());
    }

    @lombok.AllArgsConstructor(staticName = "of")
    private static final class ArrayGridInput implements GridInput {

        private final Object[][] array;

        @Override
        public String getName() {
            return "";
        }

        @Override
        public int getRowCount() {
            return array.length;
        }

        @Override
        public int getColumnCount() {
            return array.length > 0 ? array[0].length : 0;
        }

        @Override
        public Object getValue(int i, int j) {
            return array[i][j];
        }
    }
}
