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
package ec.util.spreadsheet.helpers;

import org.assertj.core.api.AbstractAssert;

/**
 *
 * @author Philippe Charles
 */
final class ArraySheetAssert extends AbstractAssert<ArraySheetAssert, ArraySheet> {

    public static ArraySheetAssert assertThat(ArraySheet actual) {
        return new ArraySheetAssert(actual);
    }

    public ArraySheetAssert(ArraySheet actual) {
        super(actual, ArraySheetAssert.class);
    }

    public ArraySheetAssert hasName(String name) {
        isNotNull();
        if (!actual.getName().equals(name)) {
            failWithMessage("Expected sheet's name to be <%s> but was <%s>", name, actual.getName());
        }
        return this;
    }

    public ArraySheetAssert hasRowCount(int rowCount) {
        isNotNull();
        if (actual.getRowCount() != rowCount) {
            failWithMessage("Expected sheet's row count to be <%s> but was <%s>", rowCount, actual.getRowCount());
        }
        return this;
    }

    public ArraySheetAssert hasColumnCount(int columnCount) {
        isNotNull();
        if (actual.getColumnCount() != columnCount) {
            failWithMessage("Expected sheet's column count to be <%s> but was <%s>", columnCount, actual.getColumnCount());
        }
        return this;
    }

    public ArraySheetAssert hasValue(int row, int column, Object value) {
        isNotNull();
        if (actual.getCellValue(row, column) != value) {
            failWithMessage("Expected value to be <%s> but was <%s>", value, actual.getCellValue(row, column));
        }
        return this;
    }
}
