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
public class DefaultSeasonalFilteringStrategy implements IFiltering {

    SymmetricFilter filter;
    IEndPointsProcessor endPointsProcessor;
    private String description_;

    public void setDescription(String desc) {
        description_ = desc;
    }

    @Override
    public String getDescription() {
        return description_ == null ? "" : description_;
    }

    /**
     *
     * @param filter
     * @param endPoints
     */
    public DefaultSeasonalFilteringStrategy(SymmetricFilter filter,
                                            IEndPointsProcessor endPoints) {
        this.filter = filter;
        this.endPointsProcessor = endPoints;
    }

    public DefaultSeasonalFilteringStrategy(SymmetricFilter filter,
                                            IEndPointsProcessor endPoints, String desc) {
        this.filter = filter;
        this.endPointsProcessor = endPoints;
        this.description_ = desc;
    }

    private TsData compositeProcess(TsData s, TsDomain rdomain) {
        TsData out = new TsData(rdomain);
        PeriodIterator pin = new PeriodIterator(s, rdomain);
        PeriodIterator pout = new PeriodIterator(out);
        int nf = filter.getUpperBound();
        while (pin.hasMoreElements()) {
            DataBlock bin = pin.nextElement().data;
            DataBlock bout = pout.nextElement().data;
            int len = bin.getLength();
            if (2 * nf < len) {
                filter.filter(bin, bout.drop(nf, nf));
                endPointsProcessor.process(bin, bout);
            } else {
                endPointsProcessor.process(bin, bout);
            }
        }
        return out;
    }

    /**
     *
     * @return
     */
    @Override
    public SymmetricFilter getCentralFilter() {
        return filter;
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
        int ny = rdomain.getLength() / rdomain.getFrequency().intValue();
        if (ny < 5 || (ny < 20 && filter.getDegree() >= 8)) {
            return new StableSeasonalFilteringStrategy().process(s, rdomain);
        }

        if (this.endPointsProcessor == null) {
            return simpleProcess(s, rdomain);
        } else {
            return compositeProcess(s, rdomain);
        }
    }

    private TsData simpleProcess(TsData s, TsDomain rdomain) {
        int nf = filter.getLength();
        int freq = rdomain.getFrequency().intValue();
        int nout = rdomain.getLength() - (nf - 1) * freq;
        TsData out = new TsData(rdomain.getStart().minus(
                filter.getLowerBound() * freq), nout);
        PeriodIterator pin = new PeriodIterator(s, rdomain);
        PeriodIterator pout = new PeriodIterator(out);
        while (pin.hasMoreElements()) {
            DataBlock bin = pin.nextElement().data;
            DataBlock bout = pout.nextElement().data;
            filter.filter(bin, bout);
        }
        return out;
    }
}
