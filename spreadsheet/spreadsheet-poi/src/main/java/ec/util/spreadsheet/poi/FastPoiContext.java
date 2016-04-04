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
package ec.util.spreadsheet.poi;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

//    @VisibleForTesting
@Immutable
final class FastPoiContext {

    private final String[] sharedStrings;
    private final boolean[] styles;
    private final boolean date1904;

    public FastPoiContext(@Nonnull String[] sharedStrings, @Nonnull boolean[] styles, boolean date1904) {
        this.sharedStrings = sharedStrings;
        this.styles = styles;
        this.date1904 = date1904;
    }

    public String getSharedString(int index) throws IndexOutOfBoundsException {
        return sharedStrings[index];
    }

    public boolean isADateFormat(int styleIndex) throws IndexOutOfBoundsException {
        return styles[styleIndex];
    }

    public boolean isDate1904() {
        return date1904;
    }
}
