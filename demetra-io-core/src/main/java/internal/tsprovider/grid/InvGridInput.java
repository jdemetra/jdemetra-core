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
package internal.tsprovider.grid;

import demetra.tsprovider.grid.GridInput;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class InvGridInput implements GridInput {

    @lombok.NonNull
    private final GridInput delegate;

    @Override
    public boolean isSupportedDataType(Class<?> type) {
        return delegate.isSupportedDataType(type);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public int getRowCount() {
        return delegate.getColumnCount();
    }

    @Override
    public int getColumnCount() {
        return delegate.getRowCount();
    }

    @Override
    public Object getValue(int row, int column) {
        return delegate.getValue(column, row);
    }
}
