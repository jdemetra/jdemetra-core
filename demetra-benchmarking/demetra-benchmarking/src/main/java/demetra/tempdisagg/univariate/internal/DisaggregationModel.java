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
package demetra.tempdisagg.univariate.internal;

import demetra.benchmarking.Cumulator;
import demetra.data.AggregationType;
import demetra.data.DataBlockIterator;
import demetra.data.normalizer.AbsMeanNormalizer;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.RegressionUtility;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)

class DisaggregationModel implements IBuilder<DisaggregationData> {

    private TsData y;
    private final List<ITsVariable<TsDomain>> regressors = new ArrayList<>();
    private TsDomain disaggregationDomain;
    private AggregationType aType = AggregationType.Sum;
    private boolean rescale = true;

    DisaggregationModel y(TsData y) {
        this.y = y;
        return this;
    }

    DisaggregationModel disaggregationDomain(TsDomain domain) {
        this.disaggregationDomain = domain;
        return this;
    }

    DisaggregationModel aggregationType(AggregationType type) {
        this.aType = type;
        return this;
    }

    DisaggregationModel rescale(boolean rescale) {
        this.rescale = rescale;
        return this;
    }

    DisaggregationModel addX(@Nonnull ITsVariable... vars) {
        for (int i = 0; i < vars.length; ++i) {
            regressors.add(vars[i]);
        }
        return this;
    }

    @Override
    public DisaggregationData build() {
        DisaggregationData data = startDataPreparation();
        if (data == null) {
            return null;
        }
        if (!prepare(data, rescale)) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        return data;
    }

    private DisaggregationData startDataPreparation() {
        if (y == null) {
            return null;
        }
        TsDomain lDom = y.getDomain(), hDom = disaggregationDomain;
        int c = lDom.getTsUnit().ratioOf(hDom.getTsUnit());
        if (c <= 1) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        DisaggregationData data = new DisaggregationData();
        data.FrequencyRatio = c;
        data.hDom = hDom;
        return data;
    }

    /**
     *
     * @param domain
     * @param rescale
     * @return
     */
//    public DisaggregationData data(TsDomain domain, boolean rescale) {
//        DisaggregationData data = startDataPreparation(domain);
//        if (data == null) {
//            return null;
//        }
//        if (!prepare(data, rescale)) {
//            return null;
//        }
//        return data;
//    }
    private boolean prepare(DisaggregationData data, boolean rescale) {
        TsDomain lDom = y.getDomain();
        int lN = lDom.getLength(), hN = data.hDom.getLength();
        if (lN == 0 || hN == 0) {
            return false;
        }
        TsPeriod lStart = lDom.getStartPeriod(), lEnd = lDom.getEndPeriod();
        TsPeriod hStart = data.hDom.getStartPeriod(), hEnd = data.hDom.getEndPeriod();
        TsUnit lUnit = lStart.getUnit(), hUnit = hStart.getUnit();

        // common periods in lFreq
        TsPeriod cStart = TsPeriod.of(lUnit, hStart.start());
        TsPeriod cEnd = TsPeriod.of(lUnit, hEnd.start());

        // adjust for complete periods...
        if (aType != AggregationType.Last) {
            if (!hStart.start().equals(cStart.start())) {
                cStart = cStart.next();
            }
        }
        if (aType != AggregationType.First) {
            if (!hEnd.end().equals(cEnd.end())) {
                cEnd = cEnd.previous();
            }
        }
        if (lStart.isAfter(cStart)) {
            cStart = lStart;
        }
        if (lEnd.isBefore(cEnd)) {
            cEnd = lEnd;
        }

        int ny = cStart.until(cEnd);
        if (ny < regressors.size()) {
            return false;
        }
        // common domain
        TsDomain yDom = TsDomain.of(cStart, ny);

        if (aType == AggregationType.Last) {
            hStart = TsPeriod.of(hUnit, cStart.end()).previous();
        } else {
            hStart = TsPeriod.of(hUnit, cStart.start());
        }
        if (aType == AggregationType.First) {
            hEnd = TsPeriod.of(hUnit, cEnd.start());
        } else {
            hEnd = TsPeriod.of(hUnit, cEnd.end()).previous();
        }
        
        data.hEDom = TsDomain.of(hStart, hStart.until(hEnd));

        prepareY(data, yDom);
        if (regressors.size() > 0) {
            prepareX(data, rescale);
        } else {
            data.scale(rescale ? new AbsMeanNormalizer() : null);
        }

        return true;
    }

    private void prepareX(DisaggregationData data, boolean rescale) {
        
        data.hX = RegressionUtility.data(disaggregationDomain, regressors.toArray(new ITsVariable[regressors.size()]));
        if (rescale) {
            data.scale(new AbsMeanNormalizer());
        } else {
            data.scale(null);
        }

        if (aType != AggregationType.Average
                && aType != AggregationType.Sum) {
            data.hEX = data.hX;
        } else {
            data.hEX = data.hX.deepClone();
            Cumulator cumul = new Cumulator(data.FrequencyRatio);
            DataBlockIterator cX = data.hEX.columnsIterator();
            while (cX.hasNext()) {
                cumul.transform(cX.next());
            }
        }
    }

    private void prepareY(DisaggregationData data, TsDomain yDom) {
        double[] s = y.getValues().toArray();

        int ny = yDom.getLength();
        int pos;
        if (aType == AggregationType.First
                || aType == AggregationType.Last) {
            pos = 0;
        } else {
            pos = data.FrequencyRatio - 1;
        }
        double[] y = new double[data.hDom.getLength()];
        for (int i = 0; i < y.length; ++i) {
            y[i] = Double.NaN;
        }

        int xstart = data.hDom.getStartPeriod().until(data.hEDom.getStartPeriod()), ystart = this.y.getStart().until(yDom.getStartPeriod());
        for (int i = 0, j = xstart + pos, k = ystart; i < ny; ++i, j += data.FrequencyRatio, ++k) {
            y[j] = s[k];
        }
        data.hY = y;
    }

}
