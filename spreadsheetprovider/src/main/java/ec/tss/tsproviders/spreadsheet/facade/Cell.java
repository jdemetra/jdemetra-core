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
package ec.tss.tsproviders.spreadsheet.facade;

import java.util.Date;
import org.checkerframework.checker.nullness.qual.NonNull;

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
@Deprecated
public abstract class Cell {

    @NonNull
    public String getString() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @NonNull
    public Date getDate() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @NonNull
    public Number getNumber() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isDate() {
        return false;
    }
}
