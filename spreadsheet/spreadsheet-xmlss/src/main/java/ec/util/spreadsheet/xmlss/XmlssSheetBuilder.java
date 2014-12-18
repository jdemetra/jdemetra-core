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
package ec.util.spreadsheet.xmlss;

import ec.util.spreadsheet.Sheet;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
abstract class XmlssSheetBuilder {

    @Nonnull
    abstract public XmlssSheetBuilder put(@Nullable String rawValue, @Nullable String rawDataType, int row, int col);

    @Nonnull
    abstract public XmlssSheetBuilder name(@Nonnull String name);

    @Nonnull
    abstract public XmlssSheetBuilder clear();

    @Nonnull
    abstract public Sheet build();

    @Nonnull
    public static XmlssSheetBuilder create() {
        return new Builder();
    }

    private static final class Builder extends XmlssSheetBuilder {

        private final DateFormat dateFormat;
        private final NumberFormat numberFormat;
        private final ArraySheet.Builder sheetDataBuilder;

        public Builder() {
            this.numberFormat = NumberFormat.getNumberInstance(Locale.ROOT);
            numberFormat.setMaximumFractionDigits(9);
            numberFormat.setMaximumIntegerDigits(12);
            this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.sheetDataBuilder = ArraySheet.builder();
        }

        private Object getCellValue(String rawValue, String rawDataType) {
            switch (rawDataType) {
                case "String":
                    return rawValue;
                case "Number":
                    try {
                        return numberFormat.parse(rawValue);
                    } catch (ParseException ex) {
                        return null;
                    }
                case "DateTime":
                    try {
                        return dateFormat.parse(rawValue);
                    } catch (ParseException ex) {
                        return null;
                    }
            }
            return null;
        }

        private boolean isNullOrEmpty(String s) {
            return s == null || s.isEmpty();
        }

        @Override
        public Builder put(String rawValue, String rawDataType, int row, int col) {
            if (isNullOrEmpty(rawValue) || isNullOrEmpty(rawDataType)) {
                return this;
            }

            Object cellValue = getCellValue(rawValue, rawDataType);
            if (cellValue == null) {
                return this;
            }

            sheetDataBuilder.value(row, col, cellValue);
            return this;
        }

        @Override
        public Builder name(String name) {
            sheetDataBuilder.name(name);
            return this;
        }

        @Override
        public Builder clear() {
            sheetDataBuilder.clear();
            return this;
        }

        @Override
        public Sheet build() {
            return sheetDataBuilder.build();
        }
    }
}
