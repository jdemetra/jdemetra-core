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
package test.tsprovider.grid;

import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import demetra.tsprovider.OptionalTsData;
import demetra.tsprovider.grid.GridInput;
import demetra.tsprovider.grid.GridLayout;
import demetra.tsprovider.grid.TsCollectionGrid;
import demetra.tsprovider.grid.TsGrid;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Data {

    public final LocalDateTime JAN_2010 = LocalDate.of(2010, 1, 1).atStartOfDay();
    public final LocalDateTime FEB_2010 = LocalDate.of(2010, 2, 1).atStartOfDay();
    public final LocalDateTime MAR_2010 = LocalDate.of(2010, 3, 1).atStartOfDay();

    public final GridInput HGRID_WITH_HEADER = ArrayGridInput.of("hello", new Object[][]{
        {null, JAN_2010, FEB_2010, MAR_2010},
        {"S1", 3.14, 4.56, 7.89}
    });

    public final GridInput HGRID_WITH_DATE_HEADER = ArrayGridInput.of("hello", new Object[][]{
        {"Date", JAN_2010, FEB_2010, MAR_2010},
        {"S1", 3.14, 4.56, 7.89}
    });

    public final GridInput HGRID_WITHOUT_HEADER = ArrayGridInput.of("hello", new Object[][]{
        {JAN_2010, FEB_2010, MAR_2010},
        {3.14, 4.56, 7.89}
    });

    public final GridInput HGRID_WITH_HEADERS = ArrayGridInput.of("hello", new Object[][]{
        {null, null, JAN_2010, FEB_2010, MAR_2010},
        {"G1", "S1", 3.14, 4.56, 7.89},
        {null, "S2", 3, 4, 5},
        {"G2", "S1", 7, 8, 9},
        {"S1", null, 0, 1, 2}
    });

    public final GridInput VGRID_WITH_HEADER = ArrayGridInput.of("hello", new Object[][]{
        {null, "S1"},
        {JAN_2010, 3.14},
        {FEB_2010, 4.56},
        {MAR_2010, 7.89}
    });

    public final GridInput VGRID_WITH_DATE_HEADER = ArrayGridInput.of("hello", new Object[][]{
        {"Date", "S1"},
        {JAN_2010, 3.14},
        {FEB_2010, 4.56},
        {MAR_2010, 7.89}
    });

    public final GridInput VGRID_WITHOUT_HEADER = ArrayGridInput.of("hello", new Object[][]{
        {JAN_2010, 3.14},
        {FEB_2010, 4.56},
        {MAR_2010, 7.89}
    });

    public final GridInput VGRID_WITH_HEADERS = ArrayGridInput.of("hello", new Object[][]{
        {null, "G1", null, "G2", "S1"},
        {null, "S1", "S2", "S1", null},
        {JAN_2010, 3.14, 3, 7, 0},
        {FEB_2010, 4.56, 4, 8, 1},
        {MAR_2010, 7.89, 5, 9, 2}
    });

    public static TsData data(TsUnit freq, int year, int position, double... values) {
        TsPeriod p = TsPeriod.yearly(year).withUnit(freq).plus(position);
        return TsData.ofInternal(p, values);
    }

    public static TsGrid s(String name, TsData data) {
        return TsGrid.of(name, OptionalTsData.present(data));
    }

    public static TsCollectionGrid of(String gridName, GridLayout layout, String seriesName, TsData data) {
        return TsCollectionGrid.builder().name(gridName).layout(layout).item(s(seriesName, data)).build();
    }
}
