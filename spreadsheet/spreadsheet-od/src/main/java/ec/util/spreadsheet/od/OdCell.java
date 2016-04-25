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
package ec.util.spreadsheet.od;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jopendocument.dom.ODValueType;
import static org.jopendocument.dom.ODValueType.DATE;
import static org.jopendocument.dom.ODValueType.FLOAT;
import static org.jopendocument.dom.ODValueType.STRING;
import org.jopendocument.dom.spreadsheet.Cell;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

/**
 *
 * @author Philippe Charles
 */
//@FlyweightPattern
final class OdCell extends ec.util.spreadsheet.Cell {

    private Cell<SpreadSheet> cell = null;
    private Type type = null;
    private String textValue = null;

    @Nullable
    OdCell withCell(@Nonnull Cell<SpreadSheet> cell) {
        this.cell = cell;
        ODValueType valueType = cell.getValueType();
        this.textValue = null;
        if (valueType != null) {
            switch (valueType) {
                case DATE:
                    type = Type.DATE;
                    return this;
                case FLOAT:
                    type = Type.NUMBER;
                    return this;
                case STRING:
                    type = Type.STRING;
                    return this;
                default:
                    return null;
            }
        } else {
            // a null valueType might still contains a string !
            textValue = cell.getTextValue();
            type = Type.STRING;
            return textValue.isEmpty() ? null : this;
        }
    }

    @Override
    public String getString() {
        if (!isString()) {
            throw new UnsupportedOperationException();
        }
        return textValue != null ? textValue : (String) cell.getValue();
    }

    @Override
    public Date getDate() {
        if (!isDate()) {
            throw new UnsupportedOperationException();
        }
        return (Date) cell.getValue();
    }

    @Override
    public Number getNumber() {
        return getDouble();
    }

    @Override
    public boolean isNumber() {
        return type == Type.NUMBER;
    }

    @Override
    public boolean isString() {
        return type == Type.STRING;
    }

    @Override
    public boolean isDate() {
        return type == Type.DATE;
    }

    @Override
    public double getDouble() throws UnsupportedOperationException {
        if (!isNumber()) {
            throw new UnsupportedOperationException();
        }
        return ((BigDecimal) cell.getValue()).doubleValue();
    }
}
