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
package demetra.tempdisagg.univariate;

import demetra.data.Cumulator;
import demetra.data.AggregationType;
import demetra.data.DataBlockIterator;
import demetra.data.normalizer.AbsMeanNormalizer;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.Regression;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(DisaggregationModel.class)
class DisaggregationModelBuilder {

    private TsData y;
    private final List<ITsVariable> regressors = new ArrayList<>();
    private TsDomain disaggregationDomain;
    private AggregationType aType = AggregationType.Sum;
//    private boolean rescale = true;

    DisaggregationModelBuilder y(TsData y) {
        this.y = y;
        return this;
    }

    DisaggregationModelBuilder disaggregationDomain(TsDomain domain) {
        this.disaggregationDomain = domain;
        return this;
    }

    DisaggregationModelBuilder aggregationType(AggregationType type) {
        this.aType = type;
        return this;
    }

//    DisaggregationModel rescale(boolean rescale) {
//        this.rescale = rescale;
//        return this;
//    }
    DisaggregationModelBuilder addX(@Nonnull ITsVariable... vars) {
        for (int i = 0; i < vars.length; ++i) {
            regressors.add(vars[i]);
        }
        return this;
    }

    DisaggregationModelBuilder addX(@Nonnull Collection<ITsVariable> vars) {
        regressors.addAll(vars);
        return this;
    }

    public DisaggregationModel build() {
        DisaggregationModel data = startDataPreparation();
        if (data == null) {
            return null;
        }
        if (!prepare(data)) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        return data;
    }

    private DisaggregationModel startDataPreparation() {
        if (y == null) {
            return null;
        }
        TsDomain lDom = y.getDomain(), hDom = disaggregationDomain;
        int c = hDom.getTsUnit().ratioOf(lDom.getTsUnit());
        if (c <= 1) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        DisaggregationModel data = new DisaggregationModel();
        data.frequencyRatio = c;
        data.hDom = hDom;
        return data;
    }

    /**
     *
     * @param domain
     * @param rescale
     * @return
     */
    private boolean prepare(DisaggregationModel data) {
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
            prepareX(data);
//        } else {
//            data.scale(rescale ? new AbsMeanNormalizer() : null);
        }

        return true;
    }

    private void prepareX(DisaggregationModel data) {

        data.hX = Regression.matrix(disaggregationDomain, regressors.toArray(new ITsVariable[regressors.size()]));
//        if (rescale) {
//            data.scale(new AbsMeanNormalizer());
//        } else {
//            data.scale(null);
//        }

        if (aType != AggregationType.Average
                && aType != AggregationType.Sum) {
            data.hEX = data.hX;
        } else {
            data.hEX = data.hX.deepClone();
            Cumulator cumul = new Cumulator(data.frequencyRatio);
            DataBlockIterator cX = data.hEX.columnsIterator();
            while (cX.hasNext()) {
                cumul.transform(cX.next());
            }
        }
    }

    private void prepareY(DisaggregationModel data, TsDomain yDom) {
        double[] s = y.getValues().toArray();

        int ny = yDom.getLength();
        int pos;
        if (aType == AggregationType.First
                || aType == AggregationType.Last) {
            pos = 0;
        } else {
            pos = data.frequencyRatio - 1;
        }
        double[] y = new double[data.hDom.getLength()];
        for (int i = 0; i < y.length; ++i) {
            y[i] = Double.NaN;
        }

        int xstart = data.hDom.getStartPeriod().until(data.hEDom.getStartPeriod()), ystart = this.y.getStart().until(yDom.getStartPeriod());
        for (int i = 0, j = xstart + pos, k = ystart; i < ny; ++i, j += data.frequencyRatio, ++k) {
            y[j] = s[k];
        }
        data.hY = y;
    }

}
