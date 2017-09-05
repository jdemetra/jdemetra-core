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
package internal.spreadsheet;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class TsExportOptions {

    public static final TsExportOptions DEFAULT = new TsExportOptions(true, true, true, true);

    /**
     * true : one series per column, false : one series per line
     */
    boolean vertical;
    /**
     * show or not the dates
     */
    boolean showDates;
    /**
     * show or not the titles of the series
     */
    boolean showTitle;
    /**
     * true to set the dates at the beginning of the period, false for the end
     * of the period
     */
    boolean beginPeriod;
}
