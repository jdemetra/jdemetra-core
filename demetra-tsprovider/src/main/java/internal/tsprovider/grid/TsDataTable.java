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

import demetra.timeseries.RegularDomain;
import demetra.timeseries.simplets.TsData;

/**
 *
 * @author Philippe Charles
 */
public class TsDataTable {

    public enum TsDataTableInfo {
        Valid;
    }

    public RegularDomain getDomain() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TsDataTableInfo getDataInfo(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getData(int i, int j) {
        return Double.NaN;
    }

    public void insert(int i, TsData get) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
