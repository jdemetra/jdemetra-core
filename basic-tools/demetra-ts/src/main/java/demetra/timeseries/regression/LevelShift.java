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
import demetra.timeseries.IRegularPeriod;
import demetra.timeseries.ITimeDomain;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

/**
 *
 * @author Jean Palate
 * @param <E>
 */
public class LevelShift<E extends IRegularPeriod> extends AbstractOutlier<E> {

    public static final String LS = "LS";
    
    private final boolean zeroEnded;

    public LevelShift(LocalDateTime pos, boolean zeroEnded) {
        super(pos, defaultName(LS, pos, null));
        this.zeroEnded=zeroEnded;
    }

    public LevelShift(LocalDateTime pos, boolean zeroEnded, String name) {
        super(pos, name);
        this.zeroEnded=zeroEnded;
    }

    @Override
    protected void data(int xpos, DataBlock buffer) {
        int n=buffer.length();
        double Zero = zeroEnded ? -1 : 0, One = zeroEnded ? 0 : 1;
        if (xpos <= 0) {
            buffer.set(One);
        } else if (xpos >= n) {
            buffer.set(Zero);
        } else {
            buffer.range(0, xpos).set(Zero);
            buffer.range(xpos, n).set(One);
        }
    }

    @Override
    public String getCode() {
        return LS;
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.ONE, 0), 0);
    }


    @Override
    public boolean isSignificant(ITimeDomain<E> domain) {
        return domain.search(position) >= 0;
    }

    @Override
    public LevelShift<E> rename(String name) {
        return new LevelShift<>(position, zeroEnded, name);
    }

}
