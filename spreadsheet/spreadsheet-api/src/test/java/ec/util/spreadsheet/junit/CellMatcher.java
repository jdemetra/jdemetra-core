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

import ec.util.spreadsheet.Cell;
import java.util.Date;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;

/**
 *
 * @author Philippe Charles
 */
public class CellMatcher {

    public static Matcher<Cell> isDate() {
        return new TypeSafeMatcher<Cell>() {

            @Override
            protected boolean matchesSafely(Cell item) {
                return item.isDate();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cell of type date");
            }
        };
    }

    public static Matcher<Cell> valueEqualTo(final Date expected) {
        return new TypeSafeDiagnosingMatcher<Cell>() {

            @Override
            protected boolean matchesSafely(Cell item, Description mismatchDescription) {
                if (!item.isDate()) {
                    mismatchDescription.appendText("not a date");
                    return false;
                }
                if (!expected.equals(item.getDate())) {
                    mismatchDescription.appendValue(item);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cell of type date with value ").appendValue(expected);
            }
        };
    }

    public static Matcher<Cell> isNumber() {
        return new TypeSafeMatcher<Cell>() {

            @Override
            protected boolean matchesSafely(Cell item) {
                return item.isNumber();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cell of type number");
            }
        };
    }

    public static Matcher<Cell> valueEqualTo(final Number expected) {
        return new TypeSafeDiagnosingMatcher<Cell>() {

            @Override
            protected boolean matchesSafely(Cell item, Description mismatchDescription) {
                if (!item.isNumber()) {
                    mismatchDescription.appendText("not a number");
                    return false;
                }
                if (!expected.equals(item.getNumber())) {
                    mismatchDescription.appendValue(item);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cell of type number with value ").appendValue(expected);
            }
        };
    }

    public static Matcher<Cell> isString() {
        return new TypeSafeMatcher<Cell>() {

            @Override
            protected boolean matchesSafely(Cell item) {
                return item.isDate();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cell of type string");
            }
        };
    }

    public static Matcher<Cell> valueEqualTo(final String expected) {
        return new TypeSafeDiagnosingMatcher<Cell>() {

            @Override
            protected boolean matchesSafely(Cell item, Description mismatchDescription) {
                if (!item.isString()) {
                    mismatchDescription.appendText("not a string");
                    return false;
                }
                if (!expected.equals(item.getString())) {
                    mismatchDescription.appendValue(item);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("cell of type string with value ").appendValue(expected);
            }
        };
    }

}
