/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.stl;

import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
public class SeasonalLoessFilter {

    private final LoessFilter filter;
    private final IPeriodicDataOrganizer organizer;

    public SeasonalLoessFilter(LoessSpecification spec, IPeriodicDataOrganizer organizer) {
        this.filter = new LoessFilter(spec);
        this.organizer = organizer;
    }

    /**
     * Defines the seasonal filter
     * @param spec The specification of the filter
     * @param period The period used by the filter
     */
    public SeasonalLoessFilter(LoessSpecification spec, int period) {
        this.filter = new LoessFilter(spec);
        this.organizer = IPeriodicDataOrganizer.of(period);
    }
    
    public int getPeriod(){
        return organizer.getPeriod();
    }

    /**
     * @return the spec
     */
    public LoessSpecification getSpec() {
        return filter.getSpec();
    }

    public boolean filter(IDataGetter y, IntToDoubleFunction userWeights, IDataSelector ys) {
        IPeriodicDataGetters yp = organizer.getters(y);
        IPeriodicDataSelectors ysp = organizer.selectors(ys);
        int np = organizer.getPeriod();
        if (np < 1) {
            return false;
        }
        for (int j = 0; j < np; ++j) {
            // last index fo period j (excluded)
            IDataGetter src = yp.get(j);
            IDataSelector tgt = ysp.get(j);
            filter.filter(src, organizer.weights(userWeights, j), tgt);
        }
        return true;
    }

}
