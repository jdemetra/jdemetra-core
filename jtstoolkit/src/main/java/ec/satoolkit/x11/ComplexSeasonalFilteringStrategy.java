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
package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.PeriodIterator;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ComplexSeasonalFilteringStrategy implements IFiltering {

    private SymmetricFilter[] filters;
    private IEndPointsProcessor[] endPointsProcessors;

    @Override
    public String getDescription() {
        return "Composite filter";
    }

    /**
     *
     * @param options
     */
    public ComplexSeasonalFilteringStrategy(
            DefaultSeasonalFilteringStrategy[] options) {
        filters = new SymmetricFilter[options.length];
        endPointsProcessors = new IEndPointsProcessor[options.length];
        for (int i = 0; i < options.length; ++i) {
            if (options[i] != null) {
                filters[i] = options[i].filter;
                endPointsProcessors[i] = options[i].endPointsProcessor;
            }
        }
    }

    /**
     *
     * @param filters
     * @param endPoints
     */
    public ComplexSeasonalFilteringStrategy(SymmetricFilter[] filters,
                                            IEndPointsProcessor[] endPoints) {
        this.filters = filters;
        this.endPointsProcessors = endPoints;
    }

    /**
     *
     * @return
     */
    @Override
    public SymmetricFilter getCentralFilter() {
        return null;
    }

    /**
     *
     * @param s
     * @param domain
     *
     * @return
     */
    @Override
    public TsData process(TsData s, TsDomain domain) {
        TsDomain rdomain = domain == null ? s.getDomain() : domain;
        TsData out = new TsData(rdomain);
        PeriodIterator pin = new PeriodIterator(s, domain);
        PeriodIterator pout = new PeriodIterator(out);
        int p = 0;
        while (pin.hasMoreElements()) {
            DataBlock bin = pin.nextElement().data;
            DataBlock bout = pout.nextElement().data;
            int nf = 0, len = bin.getLength();
            if (filters[p] != null) {
                nf = filters[p].getUpperBound();
            }
            if (filters[p] != null && 2 * nf < len && (nf < 8 || len >= 20)) {
                filters[p].filter(bin, bout.drop(nf, nf));
                endPointsProcessors[p].process(bin, bout);
            } else {
                bout.set(bin.sum() / len);
            }
            ++p;
        }
        return out;
    }
}
