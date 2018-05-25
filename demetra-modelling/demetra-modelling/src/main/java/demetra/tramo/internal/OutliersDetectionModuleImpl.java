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
package demetra.tramo.internal;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArmaModel;
import demetra.regarima.internal.ConcentratedLikelihoodComputer;
import demetra.regarima.outlier.SingleOutlierDetector;
import demetra.regarima.outlier.FastOutlierDetector;
import demetra.sarima.HannanRissanen;
import demetra.sarima.SarimaMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.SarmaSpecification;
import demetra.modelling.regression.IOutlier;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.TransitoryChange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import demetra.regarima.ami.IGenericOutliersDetectionModule;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class OutliersDetectionModuleImpl implements IGenericOutliersDetectionModule<SarimaModel> {

    static SingleOutlierDetector<SarimaModel> defaultOutlierDetector(){
        FastOutlierDetector detector=new FastOutlierDetector(null);
        detector.addOutlierFactory(AdditiveOutlier.FACTORY);
        detector.addOutlierFactory(LevelShift.FACTORY_ZEROSTARTED);
        detector.addOutlierFactory(new TransitoryChange.Factory(.7));
        return detector;
    }

    static int DEF_MAXROUND = 100;
    static int DEF_MAXOUTLIERS = 50;

    static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(OutliersDetectionModuleImpl.class)
    static class Builder {

        private double cv = 0;
        private boolean mvx;
        private IRegArimaProcessor<SarimaModel> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private SingleOutlierDetector<SarimaModel> sod;

        private Builder() {
        }

        Builder criticalValue(double cv) {
            this.cv = cv;
            return this;
        }

        Builder maximumLikelihood(boolean mvx) {
            this.mvx = mvx;
            return this;
        }

        Builder processor(IRegArimaProcessor<SarimaModel> processor) {
            this.processor = processor;
            return this;
        }

        Builder maxOutliers(int max) {
            this.maxOutliers = max;
            return this;
        }

        Builder maxRound(int max) {
            this.maxRound = max;
            return this;
        }

        Builder singleOutlierDetector(SingleOutlierDetector<SarimaModel> sod) {
            this.sod=sod;
            return this;
        }

        OutliersDetectionModuleImpl build() {
            return new OutliersDetectionModuleImpl(sod, processor, maxRound, maxOutliers, cv, mvx);
        }
    }

    private final int maxRound, maxOutliers;
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final SingleOutlierDetector sod;
    private final IRegArimaProcessor<SarimaModel> processor;
    private final double cv;
    private final boolean mvx;

    private RegArimaModel<SarimaModel> regarima;
    private double[] tstats;
    private int round;
    // festim = true if the model has to be re-estimated
    private boolean rflag_, backw_, exit_, firstEstimation;
    private int[] lastremoved;
    private DoubleSequence coeff, res;
    //

    private OutliersDetectionModuleImpl(final SingleOutlierDetector sod, final IRegArimaProcessor<SarimaModel> processor,
            final int maxOutliers, final int maxRound, final double cv, final boolean mvx) {
        this.sod = sod;
        this.processor = processor;
        this.maxOutliers = maxOutliers;
        this.maxRound = maxRound;
        this.mvx = mvx;
        this.cv = cv;
    }

    @Override
    public void setBounds(int start, int end) {
        sod.setBounds(start, end);
    }

    @Override
    public void prepare(int n) {
        sod.prepare(n);
    }

    @Override
    public void exclude(int pos, int type) {
        sod.exclude(pos, type);
    }

    /**
     * @return the outliers_
     */
    @Override
    public int[][] getOutliers() {
        return outliers.toArray(new int[outliers.size()][]);
    }

    IOutlier.IOutlierFactory getFactory(int i) {
        return sod.getOutlierFactory(i);
    }

    String[] outlierTypes() {
        ArrayList<IOutlier.IOutlierFactory> factories = sod.getFactories();
        String[] types = new String[factories.size()];
        for (int i = 0; i < types.length; ++i) {
            types[i] = factories.get(i).getCode();
        }
        return types;
    }

    @Override
    public boolean process(RegArimaModel<SarimaModel> initialModel) {
        clear();
        int n = initialModel.getY().length();
        regarima = initialModel;
        double max;
        try {
            do {
                if (!estimateModel()) {
                    return false;
                }
                boolean search = true;
                if (backw_) {
                    search = verifyModel();
                    if (exit_) {
                        break;
                    }
                }
                if (search) {
                    if (!sod.process(regarima)) {
                        break;
                    }
                    round++;
                    max = sod.getMaxTStat();
                    if (Math.abs(max) < cv) {
                        break;
                    }
                    int type = sod.getMaxOutlierType();
                    int pos = sod.getMaxOutlierPosition();
                    addOutlier(pos, type, sod.coeff(pos, type));
                    if (outliers.size() == maxOutliers) {
                        break;
                    }
                }
            } while (round < maxRound);

            // we should remove non signigicant outlier (witouht re-estimation)
            if (round == maxRound || outliers.size() == maxOutliers) {
                estimateModel();
            }
            firstEstimation = false;

            while (!verifyModel()) {
                estimateModel();
            }

            return true;
        } catch (RuntimeException err) {
            return false;
        }
    }

    private boolean estimateModel() {
        // step 1 Initial values by OLS
        SarimaModel sarima = regarima.arima();
        SarimaSpecification spec = sarima.specification();
        RegArmaModel<SarimaModel> dm = regarima.differencedModel();
        LinearModel lm = dm.asLinearModel();
        if (rflag_) {
            if (lm.getVariablesCount() > 0) {
                Ols ols = new Ols();
                LeastSquaresResults lsr = ols.compute(lm);
                if (lsr == null) {
                    return false;
                }
                res = lm.calcResiduals(lsr.getCoefficients());
            } else {
                res = lm.getY();
            }

        } else if (coeff != null) {
            res = lm.calcResiduals(coeff);
        } else {
            res = lm.getY();
        }
        boolean stable = true;
        rflag_ = false;

        if (firstEstimation) {
            SarmaSpecification dspec = spec.doStationary();
            if (spec.getParametersCount() != 0) {
                HannanRissanen hr = HannanRissanen.builder().build();
                if (hr.process(res, dspec)) {
                    SarimaModel hrmodel = hr.getModel();
                    SarimaModel stmodel = SarimaMapping.stabilize(hrmodel);
                    stable = stmodel == hrmodel;
                    if (stable || mvx || round == 0) {
                        regarima = RegArimaModel.of(regarima,
                                SarimaModel.builder(spec)
                                        .parameters(stmodel.parameters())
                                        .build());
                    } else {
                        rflag_ = true;
                        stable = true;
                    }
                }
            }
            if ((mvx || !stable) && firstEstimation) {
                return optimizeModel();
            }
        }
        if (lm.getVariablesCount() > 0) {
            updateLikelihood(ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regarima), spec.getParametersCount());
        }

        return true;
    }

    private boolean optimizeModel() {
        RegArimaEstimation<SarimaModel> estimation = processor.optimize(regarima);
        regarima = estimation.getModel();
        updateLikelihood(estimation.getConcentratedLikelihood(), estimation.getNparams());
        return true;
    }

    private void updateLikelihood(ConcentratedLikelihood likelihood, int nhp) {
        coeff = likelihood.coefficients();
        tstats = likelihood.tstats(nhp, true);
    }

    private void clear() {
        rflag_ = true;
        outliers.clear();
        round = 0;
        lastremoved = null;
        coeff = null;
        tstats = null;
        firstEstimation = true;
        backw_ = false;
        exit_ = false;
        res = null;
        // festim = true if the model has to be re-estimated
    }

    /**
     * Backward procedure (without re-estimation of the model)
     *
     * @param exit
     * @return True means that the model was not modified
     */
    private boolean verifyModel() {
        firstEstimation = true;
        if (outliers.isEmpty()) {
            return true;
        }
        /*double[] t = m_model.computeLikelihood().getTStats(true,
         m_model.getArma().getParametersCount());*/
        int nx0 = regarima.getVariablesCount() - outliers.size();
        int imin = 0;
        for (int i = 1; i < outliers.size(); ++i) {
            if (Math.abs(tstats[i + nx0]) < Math.abs(tstats[imin + nx0])) {
                imin = i;
            }
        }

        if (Math.abs(tstats[nx0 + imin]) >= cv) {
            return true;
        }
        backw_ = false;
        firstEstimation = false;
        int[] toremove = outliers.get(imin);
        sod.allow(toremove[0], toremove[1]);
        removeOutlier(imin);
        if (lastremoved != null) {
            if (Arrays.equals(toremove, lastremoved)) {
                exit_ = true;
            }
        }
        lastremoved = toremove;
        return false;
    }

    private void addOutlier(int pos, int type, double c) {
        addOutlier(pos, type);
        double[] tmp;
        if (coeff == null) {
            coeff = DoubleSequence.of(c);
        } else {
            tmp = new double[coeff.length() + 1];
            coeff.copyTo(tmp, 0);
            tmp[coeff.length()] = c;
            coeff = DoubleSequence.ofInternal(tmp);
        }
        backw_ = true;
    }

    private void addOutlier(int pos, int type) {
        int[] o = new int[]{pos, type};
        outliers.add(o);
        double[] xo = new double[regarima.getObservationsCount()];
        DataBlock XO = DataBlock.ofInternal(xo);
        sod.factory(type).fill(pos, XO);
        regarima = regarima.toBuilder().addX(XO).build();
        sod.exclude(pos, type);
    }

    /**
     *
     * @param model
     * @return
     */
    private void removeOutlier(int idx) {
        //
        int opos = regarima.getVariablesCount() - outliers.size() + idx;
        regarima = regarima.toBuilder().removeX(opos).build();
        outliers.remove(idx);
        double[] tmp;
        if (coeff.length() == 1) {
            coeff = null;
        } else {
            if (regarima.isMean()) {
                ++opos;
            }
            tmp = new double[coeff.length() - 1];
            for (int i = 0; i < opos; ++i) {
                tmp[i] = coeff.get(i);
            }
            for (int i = opos + 1; i < coeff.length(); ++i) {
                tmp[i - 1] = coeff.get(i);
            }
            coeff = DoubleSequence.ofInternal(tmp);
        }
    }

    /**
     *
     * @return
     */
    List<IOutlier.IOutlierFactory> factories() {
        return Collections.unmodifiableList(sod.getFactories());
    }

}
