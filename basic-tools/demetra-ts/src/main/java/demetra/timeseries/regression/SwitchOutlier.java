/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.timeseries.RegularDomain;
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
public class SwitchOutlier extends AbstractOutlier {

    public static final String WO = "WO";

    public SwitchOutlier(LocalDateTime pos) {
        super(pos, defaultName(WO, pos, null));
    }

    public SwitchOutlier(LocalDateTime pos, String name) {
        super(pos, name);
    }

    @Override
    protected void data(int pos, DataBlock buffer) {
        buffer.set(0);
        if (pos >= 0 && pos <buffer.length()) {
            buffer.set(pos, 1);
        }
        int npos=pos+1;
        if (npos >= 0 && npos < buffer.length()) {
            buffer.set(npos, -1);
        }
    }

    @Override
    public String getCode() {
        return WO;
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.D1, BackFilter.ONE, 0), 0);
    }


    @Override
    public boolean isSignificant(RegularDomain domain) {
        int p = domain.indexOf(position);
        return p >= 0 && p < domain.length() - 1;
    }

    @Override
    public SwitchOutlier rename(String name) {
        return new SwitchOutlier(position, name);
    }

}
