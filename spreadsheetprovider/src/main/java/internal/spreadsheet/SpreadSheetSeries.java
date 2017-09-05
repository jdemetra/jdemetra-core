/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import ec.tss.tsproviders.utils.OptionalTsData;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
public class SpreadSheetSeries implements Comparable<SpreadSheetSeries> {

    String seriesName;
    int ordering;
    AlignType alignType;
    OptionalTsData data;

    @Override
    public int compareTo(SpreadSheetSeries o) {
        int result = Integer.compare(ordering, o.ordering);
        if (result != 0) {
            return result;
        }
        return seriesName.compareTo(o.seriesName);
    }
}
