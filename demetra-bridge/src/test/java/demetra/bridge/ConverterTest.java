package demetra.bridge;


import static demetra.bridge.Converter.*;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.text.ParseException;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

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
/**
 *
 * @author Philippe Charles
 */
public class ConverterTest {

    @Test
    public void testTsUnit() {
        assertThat(fromTsUnit(TsUnit.MONTH)).isEqualTo(TsFrequency.Monthly);
        assertThatThrownBy(() -> fromTsUnit(TsUnit.DAY)).isInstanceOf(ConverterException.class);

        assertThat(toTsUnit(TsFrequency.Monthly)).isEqualTo(TsUnit.MONTH);
    }

    @Test
    public void testDateTime() throws ParseException {
        assertThat(fromDateTime(LocalDateTime.of(2010, 1, 1, 0, 0))).isEqualTo(Day.fromString("2010-01-01"));

        assertThat(toDateTime(Day.fromString("2010-01-01"))).isEqualTo(LocalDateTime.of(2010, 1, 1, 0, 0));
    }

    @Test
    public void testPeriod() {
        assertThat(fromTsPeriod(TsPeriod.monthly(2010, 1))).isEqualTo(new ec.tstoolkit.timeseries.simplets.TsPeriod(TsFrequency.Monthly, 2010, 0));

        assertThat(toTsPeriod(new ec.tstoolkit.timeseries.simplets.TsPeriod(TsFrequency.Monthly, 2010, 0))).isEqualTo(TsPeriod.monthly(2010, 1));
    }

    @Test
    public void testDomain() {
        assertThat(fromRegularDomain(TsDomain.of(TsPeriod.monthly(2010, 1), 4))).isEqualTo(new ec.tstoolkit.timeseries.simplets.TsDomain(TsFrequency.Monthly, 2010, 0, 4));

        assertThat(toRegularDomain(new ec.tstoolkit.timeseries.simplets.TsDomain(TsFrequency.Monthly, 2010, 0, 4))).isEqualTo(TsDomain.of(TsPeriod.monthly(2010, 1), 4));
    }

    @Test
    public void testData() {
        double[] values = {1, 2, 3};

        assertThat(fromTsData(TsData.ofInternal(TsPeriod.monthly(2010, 1), values))).isEqualTo(new ec.tstoolkit.timeseries.simplets.TsData(TsFrequency.Monthly, 2010, 0, values, true));

        assertThat(toTsData(new ec.tstoolkit.timeseries.simplets.TsData(TsFrequency.Monthly, 2010, 0, values, false))).isEqualTo(TsData.ofInternal(TsPeriod.monthly(2010, 1), values));
    }
}
