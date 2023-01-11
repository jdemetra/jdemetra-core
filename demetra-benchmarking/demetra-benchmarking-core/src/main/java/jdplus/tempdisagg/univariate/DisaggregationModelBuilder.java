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
package jdplus.tempdisagg.univariate;

import jdplus.data.transformation.Cumulator;
import demetra.data.AggregationType;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import jdplus.data.normalizer.AbsMeanNormalizer;
import nbbrd.design.BuilderPattern;
import nbbrd.design.Development;
import demetra.timeseries.regression.ITsVariable;
import jdplus.modelling.regression.Regression;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.regression.Variable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import jdplus.data.normalizer.DataNormalizer;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(DisaggregationModel.class)
public class DisaggregationModelBuilder {

    final TsData y;
    final List<Variable> regressors = new ArrayList<>();
    private TsDomain disaggregationDomain;
    private AggregationType aType = AggregationType.Sum;
    private int observationPosition; // only used in custom interpolation
    private boolean rescale = true;

    // local information used in the building operation
    double[] hO, hY, hEY;
    FastMatrix hX, hXC;
    FastMatrix hEX;
    TsDomain lEDom, hDom, hEDom;
    int frequencyRatio;
    double yfactor = 1;
    double[] xfactor;
    int start;

    public DisaggregationModelBuilder(TsData y) {
        this.y = y;
    }

    public DisaggregationModelBuilder disaggregationDomain(TsDomain domain) {
        this.disaggregationDomain = domain;
        return this;
    }

    public DisaggregationModelBuilder aggregationType(AggregationType type) {
        this.aType = type;
        return this;
    }

    public DisaggregationModelBuilder observationPosition(int pos) {
        this.observationPosition = pos - 1;
        this.aType = AggregationType.UserDefined;
        return this;
    }

    public DisaggregationModelBuilder rescale(boolean rescale) {
        this.rescale = rescale;
        return this;
    }

    public DisaggregationModelBuilder addX(@NonNull Variable... vars) {
        for (int i = 0; i < vars.length; ++i) {
            regressors.add(vars[i]);
        }
        return this;
    }

    public DisaggregationModelBuilder addX(@NonNull Collection<Variable> vars) {
        regressors.addAll(vars);
        return this;
    }

    public DisaggregationModel build() {
        clearTmp();
        startDataPreparation();
        prepare();
        return new DisaggregationModel(this);
    }

    private void clearTmp() {
        hO = null;
        hY = null;
        hEY = null;
        hX = null;
        hXC = null;
        hEX = null;
        hDom = null;
        hEDom = null;
        frequencyRatio = 0;
        yfactor = 1;
        xfactor = null;
        start = 0;
    }

    private void startDataPreparation() {
        if (y == null) {
            throw new IllegalArgumentException("y not set");
        }
        if (disaggregationDomain == null) {
            throw new IllegalArgumentException("disaggregation domain not set");
        }
        int c = disaggregationDomain.getTsUnit().ratioOf(y.getTsUnit());
        if (c <= 1) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        // local data
        frequencyRatio = c;
        hDom = disaggregationDomain;
    }

    /**
     *
     * @param domain
     * @param rescale
     * @return
     */
    private void prepare() {
        TsDomain lDom = y.getDomain();
        int lN = lDom.getLength(), hN = hDom.getLength();
        if (lN == 0 || hN == 0) {
            throw new IllegalArgumentException("Empty model"); //To change body of generated methods, choose Tools | Templates.
        }
        TsPeriod lStart = lDom.getStartPeriod(), lEnd = lDom.getEndPeriod();
        TsPeriod hStart = hDom.getStartPeriod(), hEnd = hDom.getEndPeriod();
        TsUnit lUnit = lStart.getUnit(), hUnit = hStart.getUnit();

        // common periods in lFreq. First, we start from hdom and then we adjust for ldom
        TsPeriod cStart = TsPeriod.of(lUnit, hStart.start());
        TsPeriod cEnd = TsPeriod.of(lUnit, hEnd.previous().start()).next();

        // adjust start and end...
        switch (aType) {
            case Last -> {
                if (!hEnd.start().equals(cEnd.start())) {
                    cEnd = cEnd.previous();
                }
            }
            case First -> {
                if (!hStart.start().equals(cStart.start())) {
                    cStart = cStart.next();
                }
            }
            case UserDefined -> {
                TsPeriod c = TsPeriod.of(hUnit, cStart.start());
                if (c.until(hStart) > observationPosition) {
                    cStart = cStart.next();
                }
                TsPeriod d = TsPeriod.of(hUnit, cEnd.previous().start());
                if (d.until(hEnd) <= observationPosition) {
                    cEnd = cEnd.previous();
                }
            }
            case Sum, Average -> {
                if (!hStart.start().equals(cStart.start())) {
                    cStart = cStart.next();
                }
                if (!hEnd.start().equals(cEnd.start())) {
                    cEnd = cEnd.previous();
                }
            }
            default ->
                throw new IllegalArgumentException("Invalid aggregation type");
        }
        if (lStart.isAfter(cStart)) {
            cStart = lStart;
        }
        if (lEnd.isBefore(cEnd)) {
            cEnd = lEnd;
        }
        TsPeriod eStart;
        switch (aType) {
            case Last ->
                eStart = TsPeriod.of(hUnit, cStart.end()).previous();
            case First ->
                eStart = TsPeriod.of(hUnit, cStart.start());
            case UserDefined -> {
                TsPeriod c = TsPeriod.of(hUnit, cStart.start());
                eStart = c.plus(observationPosition);
            }
            default ->
                eStart = TsPeriod.of(hUnit, cStart.start());
        }

        // Number of common lowfreq data
        int ny = cStart.until(cEnd);

        // TODO: should be adjusted for diffuse orders
        if (ny < regressors.size()) {
            throw new IllegalArgumentException("Empty model");
        }
        lEDom = TsDomain.of(cStart, ny);
        // estimation domain in high frequency (start include, end excluded)
        // the "estimation" data of the indicators will correspond to the "estimation domain".
        // it is as small as possible.
        // the estimation domain is defined as follow, depending on the aggregation type
        // average, sum: full low-freq periods
        // last: start at the last high freq period of a low freq period 
        // and ends at a first high freq period of a low freq period
        // first: start at the first high freq period of a low freq period 
        // and ends at a second high freq period of a low freq period
        // TODO: custom interpolation
        int np;
        np = switch (aType) {
            case Average, Sum ->
                ny * frequencyRatio;
            default ->
                (ny - 1) * frequencyRatio + 1;
        };
        hEDom = TsDomain.of(eStart, np);
        prepareY(lEDom);
        if (!regressors.isEmpty()) {
            prepareX();
        }
        scale(rescale ? new AbsMeanNormalizer() : null);
    }

    private void prepareX() {
        ITsVariable[] vars = new ITsVariable[regressors.size()];
        int vpos = 0;
        for (Variable var : regressors) {
            vars[vpos++] = var.getCore();
        }
        hX = Regression.matrix(disaggregationDomain, vars);
        int pos = hDom.indexOf(hEDom.getStartPeriod());
        int del = pos % frequencyRatio;
        if (del != 0) {
            start = frequencyRatio - del;;;
        } else {
            start = 0;
        }
        if (aType != AggregationType.Average
                && aType != AggregationType.Sum) {
            hEX = hX.extract(pos, hEDom.length(), 0, hX.getColumnsCount());
            hXC = hX;
        } else {
            hXC = hX.deepClone();
            FastMatrix xc;
            if (del != 0){
                xc=hXC.dropTopLeft(del, 0);
                hXC.top(del).get().set(Double.NaN);
            }else
                xc=hXC;
            hEX = hX.extract(pos, hEDom.length(), 0, hX.getColumnsCount()).deepClone();
            Cumulator cumul = new Cumulator(frequencyRatio);
            DataBlockIterator cX = hEX.columnsIterator(), cXC=xc.columnsIterator();
            while (cX.hasNext()) {
                cumul.transform(cX.next());
                cumul.transform(cXC.next());
            }
        }
    }

    private void prepareY(TsDomain yDom) {

        int ny = yDom.getLength();
        int pos;
        if (aType == AggregationType.Sum
                || aType == AggregationType.Average) {
            pos = frequencyRatio - 1;
        } else {
            pos = 0;
        }
        double[] hy = new double[hEDom.getLength()];
        for (int i = 0; i < hy.length; ++i) {
            hy[i] = Double.NaN;
        }

        DoubleSeqCursor reader = y.getValues().cursor();
        int k = y.getStart().until(yDom.getStartPeriod());
        reader.moveTo(k);
        for (int j = pos, i = 0; i < ny; ++i, j += frequencyRatio) {
            hy[j] = reader.getAndNext();
        }
        hEY = hy;

        hY = new double[hDom.getLength()];
        int beg = hDom.getStartPeriod().until(hEDom.getStartPeriod());
        for (int i = 0; i < beg; ++i) {
            hY[i] = Double.NaN;
        }
        System.arraycopy(hEY, 0, hY, beg, hEY.length);
        for (int i = beg + hEY.length; i < hY.length; ++i) {
            hY[i] = Double.NaN;
        }
    }

    private void scale(DataNormalizer normalizer) {
        if (normalizer != null) {
            hO = hY.clone();
            yfactor = normalizer.normalize(DataBlock.of(hY));
            for (int i = 0; i < hEY.length; ++i) {
                if (Double.isFinite(hEY[i])) {
                    hEY[i] *= yfactor;
                }
            }
        } else {
            hO = hY;
            yfactor = 1;
        }
        if (hX == null) {
            return;
        }

        int nx = hX.getColumnsCount();
        xfactor = new double[nx];

        if (normalizer != null) {
            DataBlockIterator cols = hX.columnsIterator();
            int i = 0;
            while (cols.hasNext()) {
                double z = normalizer.normalize(cols.next());
                xfactor[i++] = z;
            }
            if (aType == AggregationType.Average
                    || aType == AggregationType.Sum) {
                // in the other cases, hEX is a sub-matrix of hX; so it is already
                // scaled;
                DataBlockIterator ecols = hEX.columnsIterator();
                i = 0;
                while (ecols.hasNext()) {
                    ecols.next().mul(xfactor[i++]);
                }
                DataBlockIterator eccols = hXC.columnsIterator();
                i = 0;
                while (eccols.hasNext()) {
                    eccols.next().mul(xfactor[i++]);
                }
            }
        } else {
            for (int i = 0; i < xfactor.length; ++i) {
                xfactor[i] = 1;
            }
        }
    }

}
