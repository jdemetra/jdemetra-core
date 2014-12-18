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
package ec.util.spreadsheet.junit;

import ec.util.spreadsheet.Sheet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;

/**
 *
 * @author Philippe Charles
 */
public class SheetMatcher {

    public static Matcher<Sheet> dimensionEqualTo(final int rowCount, final int columnCount) {
        return new TypeSafeMatcher<Sheet>() {

            @Override
            protected boolean matchesSafely(Sheet item) {
                return item.getRowCount() == rowCount && item.getColumnCount() == columnCount;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("dimension ");
            }
        };
    }

    public static Matcher<Sheet> sameDimensionAs(final Sheet expected) {
        return dimensionEqualTo(expected.getRowCount(), expected.getColumnCount());
    }

    public static Matcher<Sheet> nameEqualTo(final String name) {
        return new TypeSafeDiagnosingMatcher<Sheet>() {

            @Override
            protected boolean matchesSafely(Sheet item, Description mismatchDescription) {
                if (!item.getName().equals(name)) {
                    mismatchDescription.appendValue(item.getName());
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("sheet name equals to ").appendValue(name);
            }
        };
    }

    public static Matcher<Sheet> sameNameAs(final Sheet expected) {
        return nameEqualTo(expected.getName());
    }

    public static Matcher<Sheet> cellValueEqualTo(final int rowIndex, final int columnIndex, final Object expected) {
        return new TypeSafeDiagnosingMatcher<Sheet>() {

            @Override
            protected boolean matchesSafely(Sheet item, Description mismatchDescription) {
                Object value = item.getCellValue(rowIndex, columnIndex);
                if (!expected.equals(value)) {
                    mismatchDescription.appendValue(value);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cell value ").appendValue(expected);
            }
        };
    }
}
