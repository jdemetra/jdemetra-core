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
package ec.util.spreadsheet;

import java.util.Date;
import javax.annotation.Nonnull;

/**
 * Facade that represents <b>a cell in a spreadsheet</b>.
 * <br>Note that you should not store a cell since some implementations may use
 * the flyweight pattern.
 *
 * @see Book
 * @see Sheet
 * @author Philippe Charles
 */
//@FacadePattern
public abstract class Cell {

    /**
     * @since 2.2.0
     */
    public enum Type {
        DATE, NUMBER, STRING;
    }

    /**
     * Returns the value of this cell as a String.
     *
     * @return a non-null value
     * @throws UnsupportedOperationException if this cell does not contain a
     * string
     */
    @Nonnull
    public String getString() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the value of this cell as a Date.
     *
     * @return a non-null value
     * @throws UnsupportedOperationException if this cell does not contain a
     * date
     */
    @Nonnull
    public Date getDate() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns the value of this cell as a Number.
     *
     * @return a non-null value
     * @throws UnsupportedOperationException if this cell does not contain a
     * number
     */
    @Nonnull
    public Number getNumber() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Checks if this cell contains a number.
     *
     * @return true if this cell contains a number, false otherwise
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * Checks if this cell contains a string.
     *
     * @return true if this cell contains a string, false otherwise
     */
    public boolean isString() {
        return false;
    }

    /**
     * Checks if this cell contains a date.
     *
     * @return true if this cell contains a date, false otherwise
     */
    public boolean isDate() {
        return false;
    }

    /**
     * Returns the type of this cell.
     *
     * @return a non-null type
     * @since 2.2.0
     */
    @Nonnull
    public Type getType() {
        if (isDate()) {
            return Type.DATE;
        }
        if (isNumber()) {
            return Type.NUMBER;
        }
        if (isString()) {
            return Type.STRING;
        }
        throw new RuntimeException();
    }

    /**
     * Returns the value of this cell.
     *
     * @return a value if available, null otherwise
     * @since 2.2.0
     */
    @Nonnull
    public Object getValue() {
        switch (getType()) {
            case DATE:
                return getDate();
            case NUMBER:
                return getNumber();
            case STRING:
                return getString();
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Returns the value of this cell as a double.
     *
     * @return a value
     * @throws UnsupportedOperationException if this cell does not contain a
     * number
     * @since 2.2.0
     */
    public double getDouble() throws UnsupportedOperationException {
        return getNumber().doubleValue();
    }
}
