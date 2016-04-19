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
package ec.util.spreadsheet;

import static ec.util.spreadsheet.Assertions.msg;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Objects;

/**
 * {@link Cell} specific assertions.
 */
public class CellAssert extends AbstractAssert<CellAssert, Cell> {

    /**
     * Creates a new <code>{@link CellAssert}</code> to make assertions on
     * actual Cell.
     *
     * @param actual the Cell we want to make assertions on.
     */
    public CellAssert(Cell actual) {
        super(actual, CellAssert.class);
    }

    /**
     * An entry point for CellAssert to follow AssertJ standard
     * <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly:
     * <code>assertThat(myCell)</code> and get specific assertion with code
     * completion.
     *
     * @param actual the Cell we want to make assertions on.
     * @return a new <code>{@link CellAssert}</code>
     */
    public static CellAssert assertThat(Cell actual) {
        return new CellAssert(actual);
    }

    /**
     * Verifies that the actual Cell's date is equal to the given one.
     *
     * @param date the given date to compare the actual Cell's date to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell's date is not equal to the
     * given one.
     * @throws UnsupportedOperationException if actual.getDate() throws one.
     */
    public CellAssert hasDate(java.util.Date date) throws UnsupportedOperationException {
        isNotNull();
        java.util.Date actualDate = actual.getDate();
        if (!Objects.areEqual(actualDate, date)) {
            failWithMessage("\nExpecting date of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>", actual, date, actualDate);
        }
        return this;
    }

    /**
     * Verifies that the actual Cell's number is equal to the given one.
     *
     * @param number the given number to compare the actual Cell's number to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell's number is not equal to the
     * given one.
     * @throws UnsupportedOperationException if actual.getNumber() throws one.
     */
    public CellAssert hasNumber(Number number) throws UnsupportedOperationException {
        isNotNull();
        Number actualNumber = actual.getNumber();
        if (!Objects.areEqual(actualNumber, number)) {
            failWithMessage("\nExpecting number of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>", actual, number, actualNumber);
        }
        return this;
    }

    /**
     * Verifies that the actual Cell's double is equal to the given one.
     *
     * @param number the given double to compare the actual Cell's number to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell's double is not equal to the
     * given one.
     * @throws UnsupportedOperationException if actual.getDouble() throws one.
     */
    public CellAssert hasDouble(double number) throws UnsupportedOperationException {
        isNotNull();
        double actualDouble = actual.getDouble();
        if (actualDouble != number) {
            failWithMessage("\nExpecting double of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>", actual, number, actualDouble);
        }
        return this;
    }

    /**
     * Verifies that the actual Cell's string is equal to the given one.
     *
     * @param string the given string to compare the actual Cell's string to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell's string is not equal to the
     * given one.
     * @throws UnsupportedOperationException if actual.getString() throws one.
     */
    public CellAssert hasString(String string) throws UnsupportedOperationException {
        isNotNull();
        String actualString = actual.getString();
        if (!Objects.areEqual(actualString, string)) {
            failWithMessage("\nExpecting string of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>", actual, string, actualString);
        }
        return this;
    }

    /**
     * Verifies that the actual Cell's value is equal to the given one.
     *
     * @param value the given value to compare the actual Cell's value to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell's value is not equal to the
     * given one.
     * @throws UnsupportedOperationException if actual.getValue() throws one.
     */
    public CellAssert hasValue(Object value) throws UnsupportedOperationException {
        isNotNull();
        Object actualValue = actual.getValue();
        if (!Objects.areEqual(actualValue, value)) {
            failWithMessage("\nExpecting value of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>", actual, value, actualValue);
        }
        return this;
    }

    /**
     * Verifies that the actual Cell's type is equal to the given one.
     *
     * @param type the given value to compare the actual Cell's type to.
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell's type is not equal to the
     * given one.
     */
    public CellAssert hasType(Cell.Type type) throws UnsupportedOperationException {
        isNotNull();
        Cell.Type actualType = actual.getType();
        if (!Objects.areEqual(actualType, type)) {
            failWithMessage("\nExpecting type of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>", actual, type, actualType);
        }
        return this;
    }

    /**
     * Verifies that the actual Cell is date.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell is not date.
     */
    public CellAssert isDate() {
        isNotNull();
        if (!actual.isDate()) {
            failWithMessage("\nExpecting that actual Cell is date but is not.");
        }
        return this;
    }

    /**
     * Verifies that the actual Cell is not date.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell is date.
     */
    public CellAssert isNotDate() {
        isNotNull();
        if (actual.isDate()) {
            failWithMessage("\nExpecting that actual Cell is not date but is.");
        }
        return this;
    }

    /**
     * Verifies that the actual Cell is number.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell is not number.
     */
    public CellAssert isNumber() {
        isNotNull();
        if (!actual.isNumber()) {
            failWithMessage("\nExpecting that actual Cell is number but is not.");
        }
        return this;
    }

    /**
     * Verifies that the actual Cell is not number.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell is number.
     */
    public CellAssert isNotNumber() {
        isNotNull();
        if (actual.isNumber()) {
            failWithMessage("\nExpecting that actual Cell is not number but is.");
        }
        return this;
    }

    /**
     * Verifies that the actual Cell is string.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell is not string.
     */
    public CellAssert isString() {
        isNotNull();
        if (!actual.isString()) {
            failWithMessage("\nExpecting that actual Cell is string but is not.");
        }
        return this;
    }

    /**
     * Verifies that the actual Cell is not string.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual Cell is string.
     */
    public CellAssert isNotString() {
        isNotNull();
        if (actual.isString()) {
            failWithMessage("\nExpecting that actual Cell is not string but is.");
        }
        return this;
    }

    //<editor-fold defaultstate="collapsed" desc="Cell compliance">
    static void assertCompliance(SoftAssertions s, Cell cell) {
        if (cell.isDate()) {
            s.assertThat(cell.getType()).isEqualTo(Cell.Type.DATE);
            s.assertThat(cell.getDate()).isNotNull().isEqualTo(cell.getValue());
            s.assertThatThrownBy(cell::getDouble)
                    .as(msg(cell, "getDouble()", UnsupportedOperationException.class))
                    .isInstanceOf(UnsupportedOperationException.class);
            s.assertThatThrownBy(cell::getNumber)
                    .as(msg(cell, "getNumber()", UnsupportedOperationException.class))
                    .isInstanceOf(UnsupportedOperationException.class);
            s.assertThatThrownBy(cell::getString)
                    .as(msg(cell, "getString()", UnsupportedOperationException.class))
                    .isInstanceOf(UnsupportedOperationException.class);
        } else if (cell.isNumber()) {
            s.assertThat(cell.getType()).isEqualTo(Cell.Type.NUMBER);
            s.assertThat(cell.getNumber()).isNotNull().isEqualTo(cell.getValue());
            s.assertThat(cell.getNumber().doubleValue()).isEqualTo(cell.getDouble());
            s.assertThatThrownBy(cell::getDate)
                    .as(msg(cell, "getDate()", UnsupportedOperationException.class))
                    .isInstanceOf(UnsupportedOperationException.class);
            s.assertThatThrownBy(cell::getString)
                    .as(msg(cell, "getString()", UnsupportedOperationException.class))
                    .isInstanceOf(UnsupportedOperationException.class);
        } else if (cell.isString()) {
            s.assertThat(cell.getType()).isEqualTo(Cell.Type.STRING);
            s.assertThat(cell.getString()).isNotNull().isEqualTo(cell.getValue());
            s.assertThatThrownBy(cell::getDate)
                    .as(msg(cell, "getDate()", UnsupportedOperationException.class))
                    .isInstanceOf(UnsupportedOperationException.class);
            s.assertThatThrownBy(cell::getDouble)
                    .as(msg(cell, "getDouble()", UnsupportedOperationException.class))
                    .isInstanceOf(UnsupportedOperationException.class);
            s.assertThatThrownBy(cell::getNumber)
                    .as(msg(cell, "getNumber()", UnsupportedOperationException.class))
                    .isInstanceOf(UnsupportedOperationException.class);
        } else {
            throw new RuntimeException("Invalid data type");
        }
    }
    //</editor-fold>
}
