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

import demetra.timeseries.*;
import demetra.tsprovider.grid.GridLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Data {

    public final LocalDateTime JAN_2010 = LocalDate.of(2010, 1, 1).atStartOfDay();
    public final LocalDateTime FEB_2010 = LocalDate.of(2010, 2, 1).atStartOfDay();
    public final LocalDateTime MAR_2010 = LocalDate.of(2010, 3, 1).atStartOfDay();

    public final ArrayGridInput EMPTY = ArrayGridInput.of(new Object[][]{});

    public final ArrayGridInput HGRID = ArrayGridInput.of(new Object[][]{
            {null, JAN_2010, FEB_2010, MAR_2010},
            {"S1", 3.14, 4.56, 7.89}
    });

    public final ArrayGridInput HGRID_OVERFLOW = ArrayGridInput.of(new Object[][]{
            {null, JAN_2010, FEB_2010, MAR_2010},
            {"S1", 3.14, 4.56, 7.89, 666}
    });

    public final ArrayGridInput HGRID_UNDERFLOW = ArrayGridInput.of(new Object[][]{
            {null, JAN_2010, FEB_2010, MAR_2010},
            {"S1", 3.14, 4.56}
    });

    public final ArrayGridInput HGRID_NULL_NAME = ArrayGridInput.of(new Object[][]{
            {null, JAN_2010, FEB_2010, MAR_2010},
            {"S1", 3.14, 4.56, 7.89},
            {null, 3, 4, 5}
    });

    public final ArrayGridInput HGRID_CORNER_LABEL = ArrayGridInput.of(new Object[][]{
            {"Date", JAN_2010, FEB_2010, MAR_2010},
            {"S1", 3.14, 4.56, 7.89}
    });

    public final ArrayGridInput HGRID_NO_NAME = ArrayGridInput.of(new Object[][]{
            {JAN_2010, FEB_2010, MAR_2010},
            {3.14, 4.56, 7.89}
    });

    public final ArrayGridInput HGRID_MULTI_NAME = ArrayGridInput.of(new Object[][]{
            {null, null, JAN_2010, FEB_2010, MAR_2010},
            {"G1", "S1", 3.14, 4.56, 7.89},
            {null, "S2", 3, 4, 5},
            {"G2", "S1", 7, 8, 9},
            {"S1", null, 0, 1, 2}
    });

    public final ArrayGridInput VGRID = ArrayGridInput.of(new Object[][]{
            {null, "S1"},
            {JAN_2010, 3.14},
            {FEB_2010, 4.56},
            {MAR_2010, 7.89}
    });

    public final ArrayGridInput VGRID_OVERFLOW = ArrayGridInput.of(new Object[][]{
            {null, "S1"},
            {JAN_2010, 3.14},
            {FEB_2010, 4.56},
            {MAR_2010, 7.89},
            {null, 666}
    });

    public final ArrayGridInput VGRID_UNDERFLOW = ArrayGridInput.of(new Object[][]{
            {null, "S1"},
            {JAN_2010, 3.14},
            {FEB_2010, 4.56},
            {MAR_2010}
    });

    public final ArrayGridInput VGRID_NULL_NAME = ArrayGridInput.of(new Object[][]{
            {null, "S1", null},
            {JAN_2010, 3.14, 3},
            {FEB_2010, 4.56, 4},
            {MAR_2010, 7.89, 5}
    });

    public final ArrayGridInput VGRID_CORNER_LABEL = ArrayGridInput.of(new Object[][]{
            {"Date", "S1"},
            {JAN_2010, 3.14},
            {FEB_2010, 4.56},
            {MAR_2010, 7.89}
    });

    public final ArrayGridInput VGRID_NO_NAME = ArrayGridInput.of(new Object[][]{
            {JAN_2010, 3.14},
            {FEB_2010, 4.56},
            {MAR_2010, 7.89}
    });

    public final ArrayGridInput VGRID_MULTI_NAME = ArrayGridInput.of(new Object[][]{
            {null, "G1", null, "G2", "S1"},
            {null, "S1", "S2", "S1", null},
            {JAN_2010, 3.14, 3, 7, 0},
            {FEB_2010, 4.56, 4, 8, 1},
            {MAR_2010, 7.89, 5, 9, 2}
    });

    public static TsData d(TsUnit freq, int year, int position, double... values) {
        TsPeriod p = TsPeriod.yearly(year).withUnit(freq).plus(position);
        return TsData.ofInternal(p, values);
    }

    public static Ts s(String name, TsUnit freq, int year, int position, double... values) {
        return s(name, d(freq, year, position, values));
    }

    public static Ts s(String name, TsData data) {
        return Ts.builder()
                .type(TsInformationType.Data)
                .name(name)
                .data(data)
                .build();
    }

    public static TsCollection c(GridLayout layout, String seriesName, TsData data) {
        return c(layout, s(seriesName, data));
    }

    public static TsCollection c(GridLayout layout, Ts ts) {
        return TsCollection.builder()
                .type(TsInformationType.Data)
                .name("")
                .meta(GridLayout.PROPERTY, layout.name())
                .item(ts)
                .build();
    }
}
