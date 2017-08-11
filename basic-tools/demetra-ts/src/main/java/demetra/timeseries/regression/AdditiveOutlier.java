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
 */
public class AdditiveOutlier<E extends IRegularPeriod> extends AbstractOutlier<E> {

    public static final String AO = "AO";

    public AdditiveOutlier(LocalDateTime pos) {
        super(pos, defaultName(AO, pos, null));
    }

    public AdditiveOutlier(LocalDateTime pos, String name) {
        super(pos, name);
    }

    @Override
    protected void data(int outlierPos, DataBlock buffer) {
        buffer.set(0);
        if (outlierPos >= 0 && outlierPos < buffer.length()) {
            buffer.set(outlierPos, 1);
        }
    }

    @Override
    public String getCode() {
        return AO;
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
    public AdditiveOutlier<E> rename(String name) {
        return new AdditiveOutlier<>(position, name);
    }

}
