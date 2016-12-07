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
package spreadsheet.xlsx.internal;

import java.util.Calendar;
import java.util.Date;
import spreadsheet.xlsx.XlsxDateSystem;

/**
 *
 * @author Philippe Charles
 */
public enum XlsxDateSystems implements XlsxDateSystem {

    X1900 {
        @Override
        public boolean isValidExcelDate(double value) {
            return value >= INDEX_ORIGIN;
        }

        @Override
        public Date getJavaDate(Calendar calendar, double date) {
            int datePart = (int) Math.floor(date);
            int timePart = (int) Math.round((date - datePart) * NUMBER_OF_SECONDS_IN_DAY);
            calendar.clear();
            calendar.set(1900, 0, 1, 0, 0, 0);
            calendar.add(Calendar.DAY_OF_MONTH, adjustLastDayOfFebruary1900(datePart) - INDEX_ORIGIN);
            calendar.add(Calendar.SECOND, timePart);
            return calendar.getTime();
        }

        private int adjustLastDayOfFebruary1900(int datePart) {
            return datePart < 61 ? datePart : (datePart - INDEX_ORIGIN);
        }

        private static final int INDEX_ORIGIN = 1;
    },
    X1904 {
        @Override
        public boolean isValidExcelDate(double value) {
            return value >= INDEX_ORIGIN;
        }

        @Override
        public Date getJavaDate(Calendar calendar, double date) {
            int datePart = (int) Math.floor(date);
            int timePart = (int) Math.round((date - datePart) * NUMBER_OF_SECONDS_IN_DAY);
            calendar.clear();
            calendar.set(1904, 0, 1, 0, 0, 0);
            calendar.add(Calendar.DAY_OF_MONTH, datePart);
            calendar.add(Calendar.SECOND, timePart);
            return calendar.getTime();
        }

        private static final int INDEX_ORIGIN = 0;
    };

    private static final int NUMBER_OF_SECONDS_IN_DAY = 60 * 60 * 24;
}
