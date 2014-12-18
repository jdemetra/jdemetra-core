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
class OdCell extends ec.util.spreadsheet.Cell {

    Cell<SpreadSheet> cell = null;
    ODValueType valueType = null;

    @Nullable
    OdCell withCell(@Nonnull Cell<SpreadSheet> cell) {
        this.valueType = cell.getValueType();
        if (valueType != null) {
            switch (valueType) {
                case DATE:
                case FLOAT:
                case STRING:
                    this.cell = cell;
                    return this;
            }
        }
        this.cell = null;
        return null;
    }

    @Override
    public String getString() {
        return cell.getTextValue();
    }

    @Override
    public Date getDate() {
        return (Date) cell.getValue();
    }

    @Override
    public Number getNumber() {
        return (Number) cell.getValue();
    }

    @Override
    public boolean isNumber() {
        return ODValueType.FLOAT.equals(valueType);
    }

    @Override
    public boolean isString() {
        return ODValueType.STRING.equals(valueType);
    }

    @Override
    public boolean isDate() {
        return ODValueType.DATE.equals(valueType);
    }
}
