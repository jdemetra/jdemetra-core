/*
 * Copyright 2017 National Bank of Belgium
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
package test.tsprovider.grid;

import demetra.tsprovider.grid.GridInput;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class GridInputs {

    public Object[][] toArray(GridInput input) {
        Object[][] result = new Object[input.getRowCount()][input.getColumnCount()];
        forEach(input, (i, j, o) -> result[i][j] = o);
        return result;
    }

    public interface ValueConsumer {

        void accept(int row, int column, Object value);
    }

    public interface IndexConsumer {

        void accept(int row, int column);
    }

    public void forEach(GridInput input, ValueConsumer consumer) {
        for (int i = 0; i < input.getRowCount(); i++) {
            for (int j = 0; j < input.getColumnCount(); j++) {
                consumer.accept(i, j, input.getValue(i, j));
            }
        }
    }

    public void forEach(GridInput input, IndexConsumer consumer) {
        for (int i = 0; i < input.getRowCount(); i++) {
            for (int j = 0; j < input.getColumnCount(); j++) {
                consumer.accept(i, j);
            }
        }
    }
}
