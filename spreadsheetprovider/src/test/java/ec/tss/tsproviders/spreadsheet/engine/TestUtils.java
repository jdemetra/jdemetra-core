/*
 * Copyright 2015 National Bank of Belgium
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
package ec.tss.tsproviders.spreadsheet.engine;

import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Philippe Charles
 */
final class TestUtils {

    private TestUtils() {
        // static class
    }

    static TsData data(TsFrequency freq, int year, int position, double... values) {
        return new TsData(freq, year, position, values, false);
    }

    static Date date(int year, int month, int day) {
        return new GregorianCalendar(year, month, day).getTime();
    }

    static Sheet sheet(Object[][] table) {
        return ArraySheet.copyOf("", table);
    }
}
