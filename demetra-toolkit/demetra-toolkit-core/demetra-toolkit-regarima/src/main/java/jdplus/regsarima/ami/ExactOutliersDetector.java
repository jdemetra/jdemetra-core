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
package jdplus.regsarima.ami;

import jdplus.data.DataBlock;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.outlier.SingleOutlierDetector;
import jdplus.sarima.SarimaModel;
import jdplus.modelling.regression.IOutlierFactory;
import java.util.ArrayList;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.regarima.ami.GenericOutliersDetection;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ExactOutliersDetector implements GenericOutliersDetection<SarimaModel> {

    static int DEF_MAXROUND = 100;
    static int DEF_MAXOUTLIERS = 50;
    static final double EPS = 1e-5;


    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(ExactOutliersDetector.class)
    public static class Builder {

        private double cv;
        private IRegArimaProcessor<SarimaModel> processor;
        private int maxOutliers = DEF_MAXOUTLIERS;
        private int maxRound = DEF_MAXROUND;
        private SingleOutlierDetector<SarimaModel> sod;

        private Builder() {
        }

        public Builder singleOutlierDetector(SingleOutlierDetector<SarimaModel> sod) {
            this.sod = sod;
            return this;
        }

        public Builder criticalValue(double cv) {
            this.cv = cv;
            return this;
        }

        public Builder processor(IRegArimaProcessor<SarimaModel> processor) {
            this.processor = processor;
            return this;
        }

        public Builder maxOutliers(int max) {
            this.maxOutliers = max;
            return this;
        }

        public Builder maxRound(int max) {
            this.maxRound = max;
            return this;
        }

        public ExactOutliersDetector build() {
            return new ExactOutliersDetector(sod, cv, processor, maxRound, maxOutliers);
        }
    }

    private final int maxRound, maxOutliers;
    private RegArimaModel<SarimaModel> regarima;
    private final ArrayList<int[]> outliers = new ArrayList<>(); // Outliers : (position, type)
    private final SingleOutlierDetector sod;
    private final IRegArimaProcessor<SarimaModel> processor;
    private final double cv;
    private double[] tstats;
    private int round;
    private boolean changed;

    private ExactOutliersDetector(final SingleOutlierDetector sod, final double cv, final IRegArimaProcessor<SarimaModel> processor,
            final int maxOutliers, final int maxRound) {
        this.sod = sod;
        this.cv = cv;
        this.processor = processor;
        this.maxOutliers = maxOutliers;
        this.maxRound = maxRound;
    }

    @Override
    public boolean process(RegArimaModel<SarimaModel> initialModel, IArimaMapping<SarimaModel> mapping) {
        changed = false;
        regarima = initialModel;
        estimateModel(mapping, true);
        execute(mapping);
        return changed;
    }

    @Override
    public void prepare(int n) {
        sod.prepare(n);
    }

    @Override
    public void setBounds(int start, int end) {
        sod.setBounds(start, end);
    }

    @Override
    public void exclude(int pos, int type) {
        sod.exclude(pos, type);
    }

    public void exclude(int pos) {
        int n = this.getOutlierFactoriesCount();
        for (int i = 0; i < n; ++i) {
            sod.exclude(pos, i);
        }
    }

    /**
     * @return the outliers_
     */
    @Override
    public int[][] getOutliers() {
        return outliers.toArray(new int[outliers.size()][]);
    }
    
    public RegArimaModel<SarimaModel> getRegArima(){
        return regarima;
    }

    public IOutlierFactory getFactory(int i) {
        return sod.getOutlierFactory(i);
    }

    public String[] outlierTypes() {
        IOutlierFactory[] factories = sod.getOutliersFactories();
        String[] types = new String[factories.length];
        for (int i = 0; i < types.length; ++i) {
            types[i] = factories[i].getCode();
        }
        return types;
    }

    private void addNewOutlier(int pos, int type) {
        int[] o = new int[]{pos, type};
        outliers.add(o);
        double[] xo = new double[regarima.getObservationsCount()];
        DataBlock XO = DataBlock.of(xo);
        sod.getOutlierFactory(type).fill(pos, XO);
        regarima = regarima.toBuilder().addX(XO).build();
        sod.exclude(pos, type);
        changed = true;
    }

    public void addOutlier(int pos, int type) {
        int[] o = new int[]{pos, type};
        outliers.add(o);
        sod.exclude(pos, type);
        
     }

    /**
     *
     */
    public void clear() {
        outliers.clear();
        regarima = null;
    }

    private void execute(IArimaMapping<SarimaModel> mapping) {
        double max;
        round = 0;

        do {
            if (!sod.process(regarima)) {
                break;
            }
            max = sod.getMaxTStat();
            if (Math.abs(max) > cv) {
                round++;
                int type = sod.getMaxOutlierType();
                int pos = sod.getMaxOutlierPosition();
                addNewOutlier(pos, type);
                if (!estimateModel(mapping, true)) {
                    outliers.remove(outliers.size() - 1);
                    estimateModel(mapping, false);
                    break;
                }
            } else {
                break;// no outliers to remove...
            }
        } while (round < maxRound && outliers.size() < maxOutliers);

        while (!verifymodel()) {
            if (!estimateModel(mapping, false)) {
                break;
            }
        }
    }

    public double getCritivalValue() {
        return cv;
    }

    public int getMaxIter() {
        return maxRound;
    }

    /**
     *
     * @return
     */
    public RegArimaModel<SarimaModel> getModel() {
        return regarima;
    }

    /**
     *
     * @return
     */
    public int getOutlierFactoriesCount() {
        return sod.getOutlierFactoriesCount();
    }

    /**
     *
     * @return
     */
    public int getOutliersCount() {
        return outliers.size();
    }

    private boolean estimateModel(IArimaMapping<SarimaModel> mapping, boolean full) {

        RegArimaEstimation<SarimaModel> estimation = full ? processor.process(regarima, mapping) : processor.optimize(regarima, mapping);
        if (estimation == null) {
            return false;
        }
        regarima = estimation.getModel();
        tstats = estimation.getConcentratedLikelihood().tstats(0, false);
        return true;
    }

    /**
     *
     * @return
     */
    public boolean isInitialized() {
        return regarima != null;
    }

    private void removeOutlier(int idx) {
        int opos = regarima.getXCount()- outliers.size() + idx;
        regarima = regarima.toBuilder().removeX(opos).build();
        outliers.remove(idx);
        changed = true;
    }

    private boolean verifymodel() {
        if (regarima == null) {
            return true;
        }
        if (outliers.isEmpty()) {
            return true; // no outlier detected
        }
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
        int[] toremove = outliers.get(imin);
        sod.allow(toremove[0], toremove[1]);
        removeOutlier(imin);
        return false;
    }

}
