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
package demetra.tsprovider.grid;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public interface GridInput {

    @Nonnull
    String getName();

    @Nonnegative
    int getRowCount();

    @Nonnegative
    int getColumnCount();

    @Nullable
    Object getValue(int i, int j);

    @Nonnull
    default GridInput inv() {
        GridInput original = this;
        return new GridInput() {
            @Override
            public String getName() {
                return original.getName();
            }

            @Override
            public int getRowCount() {
                return original.getColumnCount();
            }

            @Override
            public int getColumnCount() {
                return original.getRowCount();
            }

            @Override
            public Object getValue(int i, int j) {
                return original.getValue(j, i);
            }
        };
    }
}
