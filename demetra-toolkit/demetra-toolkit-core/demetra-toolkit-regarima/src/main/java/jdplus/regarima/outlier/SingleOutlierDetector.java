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
package jdplus.regarima.outlier;

import jdplus.stats.RobustStandardDeviationComputer;
import jdplus.arima.IArimaModel;
import demetra.data.DoubleList;
import jdplus.math.matrices.FastMatrix;
import jdplus.regarima.RegArimaModel;
import jdplus.modelling.regression.IOutlierFactory;
import demetra.util.TableOfBoolean;
import java.util.ArrayList;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public abstract class SingleOutlierDetector<T extends IArimaModel> {

    private final RobustStandardDeviationComputer sdevComputer;
    private IOutlierFactory[] factories;
    private double[] weights;
    protected int lbound;
    protected int ubound;
    protected FastMatrix T;
    protected FastMatrix coef;
    private RegArimaModel<T> regarima;

    private TableOfBoolean allowedTable;

    private int posMax = -1, oMax = -1;

    public boolean process(RegArimaModel<T> model) {
        if (factories == null) {
            return false;
        }
        this.regarima = model;
        clear(false);
        return calc();
    }

    /**
     *
     * @param sdevComputer Computation of the standard deviation
     */
    protected SingleOutlierDetector(RobustStandardDeviationComputer sdevComputer) {
        this.sdevComputer = sdevComputer;
    }

    /**
     *
     * @param factories Outliers factories
     * @param weights Weights of the corresponding outliers factories. Could be
     * null,
     * which means that all factories have the same weight (1)
     */
    public void setOutlierFactories(IOutlierFactory[] factories, double[] weights) {
        this.factories = factories;
        if (weights == null) {
            this.weights = new double[factories.length];
            for (int i = 0; i < this.weights.length; ++i) {
                this.weights[i] = 1;
            }
        } else {
            this.weights = weights;
        }
        clear(true);
    }

    public void setOutlierFactories(IOutlierFactory... factories) {
        this.factories = factories;
        this.weights = new double[factories.length];
        for (int i = 0; i < this.weights.length; ++i) {
            this.weights[i] = 1;
        }
        clear(true);
    }

    /**
     * @return the sdevComputer
     */
    public RobustStandardDeviationComputer getStandardDeviationComputer() {
        return sdevComputer;
    }

    /**
     *
     * @return
     */
    protected abstract boolean calc();

    /**
     *
     * @param all
     */
    protected void clear(boolean all) {
        oMax = -1;
        posMax = -1;
        if (all) {
            T = null;
            coef = null;
            allowedTable = null;
        } else if (T != null) {
            T.set(0);
            coef.set(0);
        }
    }

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    public double coefficient(int pos, int outlier) {
        return coef.get(pos, outlier);
    }

    /**
     *
     * @param pos
     * @param ioutlier
     */
    public void exclude(int pos, int ioutlier) {
        // avoid outliers outside the current range
        if (pos >= 0 && pos < allowedTable.getRowsCount()) {
            allowedTable.set(pos, ioutlier, false);
            T.set(pos, ioutlier, 0);
        }
    }

    /**
     *
     * @param pos
     * @param ioutlier
     */
    public void allow(int pos, int ioutlier) {
        // avoid outliers outside the current range
        if (pos >= 0 && pos < allowedTable.getRowsCount()) {
            allowedTable.set(pos, ioutlier, true);
            T.set(pos, ioutlier, 0);
        }
    }

    /**
     *
     * @param pos
     */
    public void exclude(int[] pos) {
        if (pos == null) {
            return;
        }
        for (int i = 0; i < pos.length; ++i) {
            for (int j = 0; j < factories.length; ++j) {
                exclude(pos[i], j);
            }
        }
    }

    /**
     *
     * @param pos
     */
    public void allow(int[] pos) {
        if (pos == null) {
            return;
        }
        for (int i = 0; i < pos.length; ++i) {
            for (int j = 0; j < factories.length; ++j) {
                allow(pos[i], j);
            }
        }
    }

    /**
     *
     * @param pos
     */
    public void exclude(int pos) {
        for (int j = 0; j < factories.length; ++j) {
            exclude(pos, j);
        }
    }

    /**
     *
     * @return
     */
    public int getLBound() {
        return lbound;
    }

    /**
     *
     * @return
     */
    public int getMaxOutlierType() {
        if (oMax == -1) {
            searchMax();
        }
        return oMax;
    }

    /**
     *
     * @return
     */
    public int getMaxOutlierPosition() {
        if (posMax == -1) {
            searchMax();
        }
        return posMax;
    }

    /**
     *
     * @return
     */
    public double getMaxTStat() {
        if (oMax == -1) {
            searchMax();
        }
        double tmax = T(posMax, oMax);
        return tmax;
    }

    /**
     *
     * @return
     */
    public int getOutlierFactoriesCount() {
        return factories.length;
    }
    
    public IOutlierFactory[] getOutliersFactories(){
        return factories;
    }

    /**
     *
     * @param i
     * @return
     */
    public IOutlierFactory getOutlierFactory(int i) {
        return factories[i];
    }

    /**
     *
     * @return
     */
    public int getUBound() {
        return ubound;
    }

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    public boolean isAllowed(int pos, int outlier) {
        return allowedTable.get(pos, outlier);
    }

    public void setBounds(int lbound, int ubound) {
        this.lbound = lbound;
        this.ubound = ubound;
    }

    /**
     *
     * @param n
     */
    public void prepare(int n) {
        lbound = 0;
        ubound = n;
        T = FastMatrix.make(n, factories.length);
        coef = FastMatrix.make(n, factories.length);
        allowedTable = new TableOfBoolean(n, factories.length);
        for (int i = 0; i < factories.length; ++i) {
            IOutlierFactory fac = factories[i];
            int jstart = fac.excludingZoneAtStart();
            int jend = n - fac.excludingZoneAtEnd();
            for (int j = jstart; j < jend; ++j) {
                allowedTable.set(j, i, true);
            }
        }
    }

    private void searchMax() {
        if (T == null) {
            return;
        }
        double max = 0;
        int imax = -1;
        double[] table = T.getStorage();
        for (int i = 0, c = 0; c < T.getColumnsCount(); ++c) {
            double w = weights[c];

            for (int r = 0; r < T.getRowsCount(); ++r, ++i) {
                double cur = Math.abs(table[i]) * w;
                if (cur > max) {
                    imax = i;
                    max = cur;
                }
            }
            if (imax == -1) {
                return;
            }
        }
        posMax = imax % T.getRowsCount();
        oMax = imax / T.getRowsCount();
    }

    /**
     *
     * @param pos
     * @param outlier
     * @param val
     */
    protected void setT(int pos, int outlier, double val) {
        T.set(pos, outlier, val);
    }

    /**
     *
     * @param pos
     * @param outlier
     * @param val
     */
    protected void setCoefficient(int pos, int outlier, double val) {
        coef.set(pos, outlier, val);
    }

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    public double T(int pos, int outlier) {
        return T.get(pos, outlier);
    }

    /**
     * @return the weights
     */
    public double getWeight(int pos) {
        return weights[pos];
    }

    /**
     * @return the lbound
     */
    public int getLbound() {
        return lbound;
    }

    /**
     * @return the ubound
     */
    public int getUbound() {
        return ubound;
    }

    /**
     * @return the T
     */
    public FastMatrix getT() {
        return T;
    }

    /**
     * @return the coef
     */
    public FastMatrix allCoefficients() {
        return coef;
    }

    void allow(int pos, String code) {
        for (int i = 0; i < factories.length; ++i) {
            IOutlierFactory outlier = factories[i];
            if (outlier.getCode().equals(code)) {
                allow(pos - lbound, i);
                return;
            }
        }
    }

    void exclude(int pos, String code) {
        for (int i = 0; i < factories.length; ++i) {
            IOutlierFactory factory = factories[i];
            if (factory.getCode().equals(code)) {
                exclude(pos - lbound, i);
                return;
            }
        }
    }

    /**
     * @return the regarima
     */
    public RegArimaModel<T> getRegArima() {
        return regarima;
    }
}
