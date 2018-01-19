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
package demetra.demo;

import demetra.timeseries.TsDataTable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class DataTypesDemo {

    public void printDataTable(TsDataTable table, TsDataTable.DistributionType distribution) {
        StringBuilder result = new StringBuilder();
        TsDataTable.Cursor cursor = table.cursor(distribution);
        for (int i = 0; i < cursor.getPeriodCount(); i++) {
            result.append(table.getDomain().get(i).display()).append('\t');
            for (int j = 0; j < cursor.getSeriesCount(); j++) {
                result.append(valuetoString(cursor.moveTo(i, j))).append('\t');
            }
            result.append(System.lineSeparator());
        }
        System.out.println(result.toString());
    }

    private String valuetoString(TsDataTable.Cursor cursor) {
        switch (cursor.getStatus()) {
            case PRESENT:
                double value = cursor.getValue();
                return Double.isNaN(value) ? "?" : Double.toString(value);
            case UNUSED:
                return "x";
            case EMPTY:
                return " ";
            case BEFORE:
                return "-";
            case AFTER:
                return "-";
            default:
                throw new RuntimeException();
        }
    }
}
