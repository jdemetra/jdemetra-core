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
package demetra.tsprovider.grid;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class GridExport {

    public static final GridExport DEFAULT = new GridExport(GridLayout.VERTICAL, true, true, true);

    /**
     * true : one series per column, false : one series per line
     */
    private GridLayout layout;
    /**
     * show or not the dates
     */
    private boolean showDates;
    /**
     * show or not the titles of the series
     */
    private boolean showTitle;
    /**
     * true to set the dates at the beginning of the period, false for the end
     * of the period
     */
    private boolean beginPeriod;
}
