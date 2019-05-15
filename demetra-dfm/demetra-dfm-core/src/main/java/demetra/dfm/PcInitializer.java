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
package demetra.dfm;

import java.util.Arrays;
import jd.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
//public class PcInitializer implements IDfmInitializer {
//
//    public static final double DEF_NS = .80;
//
//    private Matrix data_, datac_;
//    private demetra.pca.PrincipalComponents[] pc_;
//    private TsDomain idom_;
//    private double ns_ = DEF_NS;
//
//    public TsDomain getEstimationDomain() {
//        return idom_;
//    }
//
//    public void setEstimationDomain(TsDomain dom) {
//        idom_ = dom;
//    }
//
//    /**
//     * Gets the minimal percentage of non missing values for determining the
//     * time span of the principal components estimation.
//     *
//     * @return A value in ]0,1]
//     */
//    public double getNonMissingThreshold() {
//        return ns_;
//    }
//
//    public void setNonMissingThreshold(double val) {
//        ns_ = val;
//    }
//
//    @Override
//    public boolean initialize(DynamicFactorModel model, TsInformationSet input) {
//        DynamicFactorModel nmodel = model.clone();
//        clear();
//        if (!computeMatrix(input)) {
//            return false;
//        }
//        if (!computePrincipalComponents(nmodel)) {
//            return false;
//        }
//        if (!computeVar(nmodel)) {
//            return false;
//        }
//        if (!computeLoadings(nmodel)) {
//            return false;
//        }
//        if (nmodel.isValid()) {
//            model.copy(nmodel);
//        }
//        return true;
//    }
//
//    public Matrix getData() {
//        return data_;
//    }
//
//    public Matrix getInterpolatedData() {
//        return datac_;
//    }
//
//    public PrincipalComponents getPrincipalComponents(int block) {
//        return pc_[block];
//    }
//
//    private void clear() {
//        data_ = null;
//        datac_ = null;
//        pc_ = null;
//    }
//
//    private boolean computeMatrix(TsInformationSet input) {
//        TsDomain domain = idom_;
//        if (domain == null) {
//            domain = searchDomain(input);
//        }
//        data_ = input.generateMatrix(domain);
//        datac_ = new Matrix(data_.getRowsCount(), data_.getColumnsCount());
//        AverageInterpolator interpolator = new AverageInterpolator();
//        for (int i = 0; i < data_.getColumnsCount(); ++i) {
//            TsData c = new TsData(domain.getStart(), data_.column(i));
//            if (interpolator.interpolate(c, null)) {
//                datac_.column(i).copy(c);
//            }
//        }
//        return true;
//    }
//
//    private boolean computePrincipalComponents(DynamicFactorModel model) {
//        int nb = model.getFactorsCount();
//        pc_ = new PrincipalComponents[nb];
//        for (int i = 0; i < nb; ++i) {
//            Matrix x = prepareDataForComponent(model, i);
//            pc_[i] = new PrincipalComponents();
//            pc_[i].process(x);
//        }
//        return true;
//    }
//
//    /**
//     * Creates the data used for the computation of the principal components
//     * analysis
//     *
//     * @param model
//     * @param cmp The considered factor
//     * @return
//     */
//    private Matrix prepareDataForComponent(DynamicFactorModel model, int cmp) {
//        // Keep only the concerned series
//        int np = 0;
//        for (DynamicFactorModel.MeasurementDescriptor desc : model.getMeasurements()) {
//            if (!Double.isNaN(desc.coeff[cmp])) {
//                ++np;
//            }
//        }
//        Matrix m = new Matrix(datac_.getRowsCount(), np);
//        // Copy the series and correct them by the effect of the previous factors
//        np = 0; // the position of the series in the matrix
//        int s = 0; // its position in the model
//        for (DynamicFactorModel.MeasurementDescriptor desc : model.getMeasurements()) {
//            if (!Double.isNaN(desc.coeff[cmp])) {
//                m.column(np).copy(datac_.column(s));
//                for (int j = 0; j < cmp; ++j) {
//                    if (!Double.isNaN(desc.coeff[j])) {
//                        SingularValueDecomposition svd = pc_[j].getSvd();
//                        double scaling=pc_[j].getScaling();
//                        double l = -svd.S()[0] * svd.V().get(searchPos(model, s, j), 0)/scaling;
//                        m.column(np).addAY(l, svd.U().column(0));
//                    }
//                }
//                ++np;
//            }
//            ++s;
//        }
//        return m;
//    }
//
//    /**
//     * Searches the position of variable v in the singular value decomposition k
//     *
//     * @param model
//     * @param v
//     * @param k
//     * @return
//     */
//    private int searchPos(DynamicFactorModel model, int v, int k) {
//        int s = -1, q = 0;
//        for (DynamicFactorModel.MeasurementDescriptor desc : model.getMeasurements()) {
//            if (!Double.isNaN(desc.coeff[k])) {
//                ++s;
//            }
//            if (q == v) {
//                break;
//            }
//            ++q;
//        }
//        return s;
//    }
//
//    private boolean computeVar(DynamicFactorModel model) {
//        DynamicFactorModel.TransitionDescriptor tr = model.getTransition();
//        int nl = tr.nlags, nb = model.getFactorsCount();
//        DataBlock[] f = new DataBlock[nb];
//        DataBlock[] e = new DataBlock[nb];
//        Matrix M = new Matrix(data_.getRowsCount() - nl, nl * nb);
//        int c = 0;
//        for (int i = 0; i < nb; ++i) {
//            DataBlock cur = pc_[i].getFactor(0);
//            f[i] = cur.drop(nl, 0);
//            for (int j = 1; j <= nl; ++j) {
//                M.column(c++).copy(cur.drop(nl - j, j));
//            }
//        }
//        RegModel regmodel = new RegModel();
//        for (int j = 0; j < M.getColumnsCount(); ++j) {
//            regmodel.addX(M.column(j));
//        }
//        for (int i = 0; i < nb; ++i) {
//            regmodel.setY(f[i]);
//            Ols ols = new Ols();
//            if (!ols.process(regmodel)) {
//                return false;
//            }
//            tr.varParams.row(i).copyFrom(ols.getLikelihood().getB(), 0);
//            e[i] = ols.getResiduals();
//        }
//
//        for (int i = 0; i < nb; ++i) {
//            for (int j = 0; j <= i; ++j) {
//                tr.covar.set(i, j, DescriptiveStatistics.cov(e[i].getData(), e[j].getData(), 0));
//            }
//        }
//        SymmetricMatrix.fromLower(tr.covar);
//        return true;
//    }
//
//    private boolean computeLoadings(DynamicFactorModel model) {
//        // creates the matrix of factors
//        DynamicFactorModel.TransitionDescriptor tr = model.getTransition();
//        int nb = model.getFactorsCount(), blen = model.getBlockLength();
//        Matrix M = new Matrix(data_.getRowsCount() - (blen - 1), nb * blen);
//        int c = 0;
//        for (int i = 0; i < nb; ++i) {
//            DataBlock cur = pc_[i].getFactor(0);
//            for (int j = 0; j < blen; ++j) {
//                M.column(c++).copy(cur.drop(blen - 1 - j, j));
//            }
//        }
//        int v = 0;
//        for (DynamicFactorModel.MeasurementDescriptor desc : model.getMeasurements()) {
//            DataBlock y = datac_.column(v++).drop(blen - 1, 0);
//            if (y.isZero()) {
//                desc.var = 1;
//            } else {
//                RegModel regmodel = new RegModel();
//                regmodel.setY(y);
//                for (int j = 0; j < nb; ++j) {
//                    if (!Double.isNaN(desc.coeff[j])) {
//                        double[] x = new double[y.getLength()];
//                        int s = j * blen, l = desc.type.getLength();
//                        for (int r = 0; r < x.length; ++r) {
//                            x[r] = desc.type.dot(M.row(r).extract(s, l));
//                        }
//                        regmodel.addX(new DataBlock(x));
//                    }
//                }
//                Ols ols = new Ols();
//                if (ols.process(regmodel)) {
//                    double[] b = ols.getLikelihood().getB();
//                    for (int i = 0, j = 0; j < nb; ++j) {
//                        if (!Double.isNaN(desc.coeff[j])) {
//                            desc.coeff[j] = b[i++];
//                        }
//                    }
//                    desc.var = ols.getLikelihood().getSigma();
//                } else {
//                    desc.var = 1;
//                }
//            }
//        }
//        return true;
//    }
//
//    private TsDomain searchDomain(TsInformationSet input) {
//        int n = input.getSeriesCount();
//        Day[] start = new Day[n];
//        Day[] end = new Day[n];
//        for (int i = 0; i < n; ++i) {
//            TsDomain cur = input.series(i).cleanExtremities().getDomain();
//            start[i] = cur.getStart().firstday();
//            end[i] = cur.getLast().lastday();
//        }
//        Arrays.sort(start);
//        Arrays.sort(end);
//        TsPeriodSelector sel = new TsPeriodSelector();
//        int t = (int) ((n - 1) * ns_);
//        sel.between(start[t], end[n - 1 - t]);
//        return input.getCurrentDomain().select(sel);
//    }
//
//}
