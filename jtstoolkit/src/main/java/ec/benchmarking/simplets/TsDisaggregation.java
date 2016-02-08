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

package ec.benchmarking.simplets;

import ec.benchmarking.DisaggregationData;
import ec.benchmarking.DisaggregationModel;
import ec.benchmarking.ssf.SsfDisaggregation;
import ec.benchmarking.ssf.SsfDisaggregationMapper;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.CoefficientEstimation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.eco.Likelihood;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.*;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.ssf.*;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * This class computes the following model: y = X b + e, agg(y)=Y
 *
 * Y, X defined agg is an aggregation function (sum, average, last, first) y, e
 * estimated e follows a ssf of type S.
 *
 * When e ~ AR(1), the model corresponds to Chow-Lin When e ~ I(1), the model
 * corresponds to Fernandez When e ~ ARIMA(1,1,0), the model corresponds to
 * Litterman Other models could be considered
 *
 * The parameters (if any) may be estimated by maximum likelihood or by
 * unconditional least squares. When the model contains regression variables
 * and/or non-stationary residuals, the likelihood may be defined following
 * several strategies: - fixed unknown parameters - diffuse parameters
 *
 * In the latter case, the diffuse likelihood is used (see De Jong).
 *
 * @param <S> The state space form of the residuals.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsDisaggregation<S extends ISsf> {

    /**
     * @return the eps_
     */
    public double getEpsilon() {
        return eps_;
    }

    /**
     * @param eps_ the eps_ to set
     */
    public void setEpsilon(double eps_) {
        this.eps_ = eps_;
    }

    /**
     * Initial step of the Kalman filter. DKF and AKF_Diffuse should lead to the
     * same results
     */
    public static enum SsfOption {

        /**
         * Diffuse Kalman filter (see Durbin-Koopman)
         */
        DKF,
        /**
         * Augmented Kalman filter, with fixed unknown initial values
         */
        AKF_Fixed,
        /**
         * Augmented Kalman filter, with diffuse initial values
         */
        AKF_Diffuse
    }
    private S ssf_, ssfRslt_;
    private IFunction fn_;
    private IFunctionInstance fnRslt_;
    private IParametricMapping<S> mapping_;
    private IFunctionMinimizer min_;
    private double eps_=1e-7;
    private boolean converged_;
    private int ndiffuseRegressors_;
    private boolean calcVar_ = false, ml_ = true, noDisturbanceSmoother_;
    private SsfOption ssfOption_ = SsfOption.DKF;
    private DisaggregationModel model_;
    private DisaggregationData data_;
    private DiffuseConcentratedLikelihood ll_;
    private Matrix information_;
    private double[] score_;
    private double[] yl_, vyl_;

    /**
     *
     */
    public TsDisaggregation() {
    }

    /**
     * Interpolation based on the augmented Kalman filter
     *
     * @param ssfdata The interpolated data (expanded)
     */
    private void AkfInterpolation(ISsfData ssfdata) {
        int[] iregs = null;
        if (data_.hEX != null && ndiffuseRegressors_ != 0) {
            int nx = ndiffuseRegressors_ < 0 ? data_.hEX.getColumnsCount()
                    : ndiffuseRegressors_;
            iregs = new int[nx];
            for (int i = 0; i < nx; ++i) {
                iregs[i] = i;
            }
        }

        SsfModel<S> ssfmodel = new SsfModel<>(ssf_, ssfdata, EX(false), iregs);
        AkfAlgorithm<S> alg = new AkfAlgorithm<>();
        alg.useDiffuseInitialization(ssfOption_ == SsfOption.AKF_Diffuse);
        alg.useML(ml_);
        if (mapping_ != null) {
            SsfFunction<S> fn = new SsfFunction<>(ssfmodel, mapping_, alg);
            fn_ = fn;
            IFunctionMinimizer min = minimizer();
            converged_ = min.minimize(fn_, fn_.evaluate(mapping_.map(ssf_)));
            SsfFunctionInstance<S> rslt = (SsfFunctionInstance<S>) min.getResult();
            fnRslt_=rslt;
            ssfRslt_ = rslt.ssf;
            ll_ = rslt.getLikelihood();
             computeInformation(fn, rslt);
        } else {
            ssfRslt_ = ssf_;
            converged_ = true;
            ll_ = alg.evaluate(ssfmodel).getLikelihood();
        }
    }

    private boolean calcInterpolatedSeries() {
        double[] yc = YC();
        if (calcVar_ == false && noDisturbanceSmoother_ == false) {
            DisturbanceSmoother dsm = new DisturbanceSmoother();
            dsm.setSsf(ssfRslt_);
            if (!dsm.process(new SsfData(yc, null))) {
                return false;
            }
            SmoothingResults srslts = dsm.calcSmoothedStates();
            yl_ = new double[yc.length];
            for (int i = 0; i < yl_.length; ++i) {
                yl_[i] = ssf_.ZX(i, srslts.A(i));
            }
            SubMatrix x = X(true);
            if (x != null) {
                DataBlock rcy = new DataBlock(yl_);
                DataBlockIterator cols = x.columns();
                DataBlock col = cols.getData();
                DataBlock b = coeff();
                do {
                    rcy.addAY(b.get(cols.getPosition()), col);
                } while (cols.next());
            }
        } else if (noDisturbanceSmoother_) {
            Smoother sm = new Smoother();
            sm.setSsf(ssfRslt_);
            SmoothingResults srslts = new SmoothingResults();
            if (!sm.process(new SsfData(yc, null), srslts)) {
                return false;
            }
            yl_ = new double[yc.length];
            for (int i = 0; i < yl_.length; ++i) {
                yl_[i] = ssf_.ZX(i, srslts.A(i));
            }
            SubMatrix x = X(true);
            if (x != null) {
                DataBlock rcy = new DataBlock(yl_);
                DataBlockIterator cols = x.columns();
                DataBlock col = cols.getData();
                DataBlock b = coeff();
                do {
                    rcy.addAY(b.get(cols.getPosition()), col);
                } while (cols.next());
            }
        } else {
            Smoother smoother = new Smoother();
            smoother.setCalcVar(calcVar_);
            smoother.setSsf(ssfRslt_);
            SmoothingResults srslts = new SmoothingResults();
            if (!smoother.process(new SsfData(yc, null), srslts)) {
                return false;
            }
            yl_ = new double[yc.length];
            vyl_ = new double[yc.length];
            double sig = ll_.getSigma();
            for (int i = 0; i < yl_.length; ++i) {
                yl_[i] = ssfRslt_.ZX(i, srslts.A(i));
                vyl_[i] = sig * ssfRslt_.ZVZ(i, srslts.P(i));
            }

            SubMatrix x = X(true);
            SubMatrix xc = EX(true);
            if (x != null) {
                int nrows = x.getRowsCount();
                Matrix xl = new Matrix(nrows, x.getColumnsCount());
                DataBlockIterator xccols = xc.columns(), xlcols = xl.columns();
                DataBlock xccol = xccols.getData(), xlcol = xlcols.getData();
                DisturbanceSmoother dsm = new DisturbanceSmoother();
                dsm.setSsf(ssfRslt_);
                DiffuseFilteringResults dfrslts = smoother.getFilteringResults();
                double[] tmp = new double[nrows];
                do {
                    xccol.copyTo(tmp, 0);
                    dfrslts.getVarianceFilter().process(
                            dfrslts.getFilteredData(), 0, tmp, null);
                    if (!dsm.process(new SsfData(tmp, null), dfrslts)) {
                        return false;
                    }
                    srslts = dsm.calcSmoothedStates();
                    for (int i = 0; i < nrows; ++i) {
                        xlcol.set(i, ssfRslt_.ZX(i, srslts.A(i)));
                    }
                } while (xccols.next() && xlcols.next());

                DataBlock rcy = new DataBlock(yl_);
                DataBlockIterator xcols = x.columns();
                DataBlock xcol = xcols.getData();
                DataBlock b = coeff();
                do {
                    rcy.addAY(b.get(xcols.getPosition()), xcol);
                } while (xcols.next());

                DataBlockIterator xrows = x.rows(), xlrows = xl.rows();
                DataBlock xrow = xrows.getData(), xlrow = xlrows.getData();
                SubMatrix bvar = coeffVar();
                do {
                    xlrow.sub(xrow);
                    vyl_[xlrows.getPosition()] += SymmetricMatrix.quadraticForm(bvar, xlrow);
                } while (xrows.next() && xlrows.next());
            }

            for (int i = 0; i < vyl_.length; ++i) {
                if (vyl_[i] < 1e-12) {
                    vyl_[i] = 0;
                }
            }
        }
        return true;
    }

    private boolean calcSmoothedSeries() {
        double[] yc = YC();
        if (calcVar_ == false && noDisturbanceSmoother_ == false) {
            DisturbanceSmoother dsm = new DisturbanceSmoother();
            ISsf ssf = new SsfDisaggregation<>(data_.FrequencyRatio, ssfRslt_);
            dsm.setSsf(ssf);
            if (!dsm.process(new SsfData(yc, null))) {
                return false;
            }
            SmoothingResults srslts = dsm.calcSmoothedStates();
            yl_ = new double[yc.length];
            for (int i = 0; i < yl_.length; ++i) {
                yl_[i] = ssf_.ZX(i, srslts.A(i).drop(1, 0));
            }
            SubMatrix x = X(true);
            if (x != null) {
                DataBlock rcy = new DataBlock(yl_);
                DataBlockIterator cols = x.columns();
                DataBlock col = cols.getData();
                DataBlock b = coeff();
                do {
                    rcy.addAY(b.get(cols.getPosition()), col);
                } while (cols.next());
            }
        } else if (noDisturbanceSmoother_) {
            Smoother sm = new Smoother();
            ISsf ssf = new SsfDisaggregation<>(data_.FrequencyRatio, ssfRslt_);
            sm.setSsf(ssf);
            SmoothingResults srslts = new SmoothingResults();

            if (!sm.process(new SsfData(yc, null), srslts)) {
                return false;
            }
            yl_ = new double[yc.length];
            for (int i = 0; i < yl_.length; ++i) {
                yl_[i] = ssf_.ZX(i, srslts.A(i).drop(1, 0));
            }
            SubMatrix x = X(true);
            if (x != null) {
                DataBlock rcy = new DataBlock(yl_);
                DataBlockIterator cols = x.columns();
                DataBlock col = cols.getData();
                DataBlock b = coeff();
                do {
                    rcy.addAY(b.get(cols.getPosition()), col);
                } while (cols.next());
            }
        } else {
            Smoother smoother = new Smoother();
            smoother.setCalcVar(calcVar_);
            ISsf ssf = new SsfDisaggregation<>(data_.FrequencyRatio, ssfRslt_);
            smoother.setSsf(ssf);
            SmoothingResults srslts = new SmoothingResults();
            if (!smoother.process(new SsfData(yc, null), srslts)) {
                return false;
            }
            yl_ = new double[yc.length];
            vyl_ = new double[yc.length];
            int dim = ssf.getStateDim();
            double sig = ll_.getSigma();
            for (int i = 0; i < yl_.length; ++i) {
                yl_[i] = ssfRslt_.ZX(i, srslts.A(i).drop(1, 0));
                vyl_[i] = sig
                        * ssfRslt_.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
            }

            SubMatrix x = X(true);
            SubMatrix xc = EX(true);
            if (x != null) {
                int nrows = x.getRowsCount();
                Matrix xl = new Matrix(nrows, x.getColumnsCount());
                DataBlockIterator xccols = xc.columns(), xlcols = xl.columns();
                DisturbanceSmoother dsm = new DisturbanceSmoother();
                dsm.setSsf(ssf);
                DiffuseFilteringResults dfrslts = smoother.getFilteringResults();
                DataBlock xccol = xccols.getData(), xlcol = xlcols.getData();
                double[] tmp = new double[nrows];
                do {
                    xccol.copyTo(tmp, 0);
                    dfrslts.getVarianceFilter().process(
                            dfrslts.getFilteredData(), 0, tmp, null);
                    if (!dsm.process(new SsfData(tmp, null), dfrslts)) {
                        return false;
                    }
                    srslts = dsm.calcSmoothedStates();
                    for (int i = 0; i < nrows; ++i) {
                        xlcol.set(i, ssfRslt_.ZX(i, srslts.A(i).drop(1, 0)));
                    }
                } while (xccols.next() && xlcols.next());

                DataBlock rcy = new DataBlock(yl_);
                DataBlockIterator xcols = x.columns();
                DataBlock xcol = xcols.getData();
                DataBlock b = coeff();
                do {
                    rcy.addAY(b.get(xcols.getPosition()), xcol);
                } while (xcols.next());

                DataBlockIterator xrows = x.rows(), xlrows = xl.rows();
                DataBlock xrow = xrows.getData(), xlrow = xlrows.getData();
                SubMatrix bvar = coeffVar();
                do {
                    xlrow.sub(xrow);
                    vyl_[xlrows.getPosition()] += SymmetricMatrix.quadraticForm(bvar, xlrow);
                } while (xrows.next() && xlrows.next());
            }

            for (int i = 0; i < vyl_.length; ++i) {
                if (vyl_[i] < 1e-12) {
                    vyl_[i] = 0;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param value
     */
    public void calculateVariance(boolean value) {
        calcVar_ = value;
    }

    private void clearResults() {
        data_ = null;
        ll_ = null;
        ssfRslt_ = null;
        yl_ = null;
        vyl_ = null;
        information_ = null;
        score_ = null;
        fnRslt_=null;
    }

    private DataBlock coeff() {
        double[] b = ll_.getB();
        if (ssfOption_ == SsfOption.DKF) {
            return new DataBlock(b);
        } else {
            return new DataBlock(b, ssf_.getNonStationaryDim(), b.length, 1);
        }
    }

    private SubMatrix coeffVar() {
        if (ssfOption_ == SsfOption.DKF) {
            return ll_.bvar().subMatrix();
        } else {
            int n = ll_.bvar().getRowsCount();
            int start = ssf_.getNonStationaryDim();
            return ll_.bvar().subMatrix(start, n, start, n);
        }
    }

    private void DKFInterpolation(ISsfData ssfdata) {
        int[] iregs = null;
        if (data_.hEX != null && ndiffuseRegressors_ != 0) {
            int nx = ndiffuseRegressors_ < 0 ? data_.hEX.getColumnsCount()
                    : ndiffuseRegressors_;
            iregs = new int[nx];
            for (int i = 0; i < nx; ++i) {
                iregs[i] = i;
            }
        }

        SsfModel<S> ssfmodel = new SsfModel<>(ssf_, ssfdata, EX(false), iregs);
        SsfAlgorithm<S> alg = new SsfAlgorithm<>();
        alg.useML(ml_);
        if (mapping_ != null) {
            SsfFunction<S> fn = new SsfFunction<>(ssfmodel, mapping_, alg);
            fn_ = fn;
            IFunctionMinimizer min = minimizer();
            converged_ = min.minimize(fn_, fn_.evaluate(mapping_.map(ssf_)));
            SsfFunctionInstance<S> rslt = (SsfFunctionInstance<S>) min.getResult();
            ssfRslt_ = rslt.ssf;
            fnRslt_=rslt;
            ll_ = rslt.getLikelihood();
            computeInformation(fn, rslt);
        } else {
            ssfRslt_ = ssf_;
            converged_ = true;
            ll_ = alg.evaluate(ssfmodel).getLikelihood();
        }
    }

    private SubMatrix EX(boolean full) {
        if (data_.hEX == null) {
            return null;
        } else if (!full) {
            int xstart = data_.hEDom.getStart().minus(data_.hDom.getStart());
            int nx = data_.hEDom.getLength(), ny = data_.hEX.getColumnsCount();
            return data_.hEX.subMatrix(xstart, xstart + nx, 0, ny);
        } else {
            return data_.hEX.subMatrix();
        }

    }

    private ISsf extendedSsf() {
        if (ssfRslt_ == null || data_ == null) {
            return null;
        }
        SsfDisaggregation<S> ssf = new SsfDisaggregation<>(data_.FrequencyRatio, ssfRslt_);
        if (data_.hEX == null) {
            return ssf;
        } else {
            return new RegSsf(ssf, EX(false));
        }
    }

    private ISsfData extendedY() {
        if (data_ == null) {
            return null;
        }
        int xstart = data_.hEDom.getStart().minus(data_.hDom.getStart());
        int nx = data_.hEDom.getLength();
        return new SsfRefData(new DataBlock(data_.hY, xstart, xstart + nx, 1),
                null);
    }

    /**
     * Gets the disaggregation model
     *
     * @return The internal disaggregation model
     */
    public DisaggregationData getData() {
        return data_;
    }

    /**
     * Gets the number of diffuse regression variables (at the beginning of the
     * regressors list).
     *
     * @return -1 if all the regressors are diffuse
     */
    public int getDiffuseRegressorsCount() {
        return ndiffuseRegressors_;
    }

    /**
     * Gets the state space form which describes the residuals (high frequency)
     *
     * @return
     */
    public S getEstimatedSsf() {
        return ssfRslt_;
    }

    // / <summary>
    // / The estimation function is the function used in the optimization
    // procedure
    // / </summary>
    /**
     *
     * @return
     */
    public IFunction getEstimationFunction() {
        return fn_;
    }

    /**
     *
     * @return
     */
    public Matrix getObservedInformation() {
        return information_;
    }

    public double[] getScore() {
        return score_;
    }

    /**
     *
     * @return
     */
    public DiffuseConcentratedLikelihood getLikelihood() {
        return ll_;
    }

    /**
     *
     * @return
     */
    public IParametricMapping<S> getMapping() {
        return mapping_;
    }

    /**
     *
     * @return
     */
    public IFunctionMinimizer getMinimizer() {
        return min_;
    }
    
    public IFunction getObjectiveFunction(){
        return fn_;
    }
    
    public IFunctionInstance getMin(){
        return fnRslt_;
    }

    /**
     *
     * @return
     */
    public DisaggregationModel getModel() {
        return model_;
    }

    /**
     *
     * @return
     */
    public TsData getFullResiduals() {
        if (ll_ == null) {
            return null;
        }
        
        Filter<S> filter=new Filter<>();
        filter.setSsf(ssfRslt_);
        // filter 
        double[] yc=YC();
        FilteringResults frslts=new FilteringResults();
        filter.process(new SsfData(yc, null), frslts);
        double[]xres=frslts.getFilteredData().data(true, true);
        TsPeriod rend = new TsPeriod(model_.getY().getFrequency());
        rend.set(data_.hEDom.getEnd().firstday());
        rend.move(-xres.length);
        TsData s= new TsData(rend, xres, true);
        return s;
    }

    /**
     *
     * @return
     */
    public TsData getSmoothedSeries() {
        if (yl_ == null) {
            return null;
        } else {
            return new TsData(data_.hDom.getStart(), yl_, true);
        }
    }

    /**
     *
     * @return
     */
    public TsData getSmoothedSeriesVariance() {
        if (vyl_ == null) {
            return null;
        } else {
            return new TsData(data_.hDom.getStart(), vyl_, true);
        }
    }

    /**
     *
     * @return
     */
    public S getSsf() {
        return ssf_;
    }

    /**
     *
     * @return
     */
    public SsfOption getSsfOption() {
        return ssfOption_;
    }

    public boolean hasConverged() {
        return converged_;
    }

    /**
     *
     * @return
     */
    public boolean isCalculatingVariance() {
        return calcVar_;
    }

    /**
     *
     * @return
     */
    public boolean isUsingDisturbanceSmoother() {
        return !noDisturbanceSmoother_;
    }

    /**
     *
     * @return
     */
    public boolean isUsingML() {
        return ml_;
    }

    private IFunctionMinimizer minimizer() {
        if (min_ != null) {
            min_.setConvergenceCriterion(eps_);
            return min_;
        }
        if (mapping_ != null && mapping_.getDim() == 1) {
            double a = mapping_.lbound(0), b = mapping_.ubound(0);
            if (!Double.isInfinite(a) && !Double.isInfinite(b)) {
                GridSearch search = new GridSearch();
                search.setBounds(a, b);
                search.setConvergenceCriterion(eps_);
                return search;
            }
        }

        return new ProxyMinimizer(new LevenbergMarquardtMethod());
    }

    /**
     *
     * @param model
     * @param domain
     * @return
     */
    public boolean process(DisaggregationModel model, TsDomain domain) {
        clearResults();
        model_ = model;
        data_ = model.data(domain, false);
        if (data_ == null) {
            return false;
        }
        ISsfData ssfdata = extendedY();
        if (model.getAggregationType() == TsAggregationType.Sum
                || model.getAggregationType() == TsAggregationType.Average) {
            if (ssfOption_ == SsfOption.DKF) {
                processDKF(ssfdata);
            } else {
                processAkf(ssfdata);
            }
            calcSmoothedSeries();
        } else {
            if (ssfOption_ == SsfOption.DKF) {
                DKFInterpolation(ssfdata);
            } else {
                AkfInterpolation(ssfdata);
            }
            calcInterpolatedSeries();
        }
        rescale();
        //reset the original data, to avoid problems
         data_ = model.data(domain, false);
        return true;
    }

    private void processAkf(ISsfData ssfdata) {
        SsfDisaggregation<S> xssf = new SsfDisaggregation<>(data_.FrequencyRatio, ssf_);
        int[] iregs = null;
        if (data_.hEX != null && ndiffuseRegressors_ != 0) {
            int nx = ndiffuseRegressors_ < 0 ? data_.hEX.getColumnsCount()
                    : ndiffuseRegressors_;
            iregs = new int[nx];
            for (int i = 0; i < nx; ++i) {
                iregs[i] = i;
            }
        }
        SsfModel<SsfDisaggregation<S>> ssfmodel = new SsfModel<>(
                xssf, ssfdata, EX(false), iregs);

        AkfAlgorithm<SsfDisaggregation<S>> alg = new AkfAlgorithm<>();
        alg.useDiffuseInitialization(ssfOption_ == SsfOption.AKF_Diffuse);
        alg.useML(ml_);
        if (mapping_ != null) {
            SsfFunction<SsfDisaggregation<S>> fn = new SsfFunction<>(
                    ssfmodel, new SsfDisaggregationMapper<>(mapping_,
                    this.data_.FrequencyRatio), alg);
            fn_ = fn;
            IFunctionMinimizer min = minimizer();
            converged_ = min.minimize(fn_, fn_.evaluate(mapping_.map(ssf_)));
            SsfFunctionInstance<SsfDisaggregation<S>> rslt = (SsfFunctionInstance<SsfDisaggregation<S>>) min.getResult();
            fnRslt_=rslt;
            ssfRslt_ = rslt.ssf.getInternalSsf();
            ll_ = rslt.getLikelihood();
            computeInformation(fn, rslt);
        } else {
            ssfRslt_ = ssf_;
            converged_ = true;
            ll_ = alg.evaluate(ssfmodel).getLikelihood();
        }

    }

    private void processDKF(ISsfData ssfdata) {
        SsfDisaggregation<S> xssf = new SsfDisaggregation<>(data_.FrequencyRatio, ssf_);
        int[] iregs = null;
        if (data_.hEX != null && ndiffuseRegressors_ != 0) {
            int nx = ndiffuseRegressors_ < 0 ? data_.hEX.getColumnsCount()
                    : ndiffuseRegressors_;
            iregs = new int[nx];
            for (int i = 0; i < nx; ++i) {
                iregs[i] = i;
            }
        }
        SsfModel<SsfDisaggregation<S>> ssfmodel = new SsfModel<>(
                xssf, ssfdata, EX(false), iregs);
        SsfAlgorithm<SsfDisaggregation<S>> alg = new SsfAlgorithm<>();
        alg.useML(ml_);
        if (mapping_ != null) {
            SsfFunction<SsfDisaggregation<S>> fn = new SsfFunction<>(
                    ssfmodel, new SsfDisaggregationMapper<>(mapping_,
                    this.data_.FrequencyRatio), alg);
            fn_ = fn;
            IFunctionMinimizer min = minimizer();
            converged_ = min.minimize(fn_, fn_.evaluate(mapping_.map(ssf_)));
            SsfFunctionInstance<SsfDisaggregation<S>> rslt = (SsfFunctionInstance<SsfDisaggregation<S>>) min.getResult();
            fnRslt_=rslt;
            ssfRslt_ = rslt.ssf.getInternalSsf();
            ll_ = rslt.getLikelihood();
            computeInformation(fn, rslt);
        } else {
            converged_ = true;
            ssfRslt_ = ssf_;
            ll_ = alg.evaluate(ssfmodel).getLikelihood();
        }
    }

    private void computeInformation(ISsqFunction fn, ISsqFunctionInstance p) {
        SsqNumericalDerivatives d=new SsqNumericalDerivatives(fn, p);
        information_ = d.getHessian().clone();
        score_ = d.getGradient();
        double obj = p.getSsqE();
        int ndf = ll_.getDegreesOfFreedom(true, mapping_ != null ? mapping_.getDim() : 0);
        information_.mul((.5 * ndf) / obj);
        for (int i = 0; i < score_.length; ++i) {
            score_[i] *= (-.5 * ndf) / obj;
        }

    }

    private void rescale() {
        double yf = 1 / data_.yfactor;
        if (model_.getAggregationType() == TsAggregationType.Average) {
            yf *= data_.FrequencyRatio;
        }
        if (yf != 1) {
            for (int i = 0; i < yl_.length; ++i) {
                yl_[i] *= yf;
            }
            if (vyl_ != null) {
                double vyf = yf * yf;
                for (int i = 0; i < yl_.length; ++i) {
                    vyl_[i] *= vyf;
                }
            }

        }
        double[] xf = data_.xfactor != null ? data_.xfactor.clone() : null;
        if (xf != null && model_.getAggregationType() == TsAggregationType.Average) {
            for (int i = 0; i < xf.length; ++i) {
                xf[i] *= data_.FrequencyRatio;
            }
        }
        if (ssfOption_ == SsfOption.DKF || ssf_.getNonStationaryDim() == 0) {
            ll_.rescale(data_.yfactor, xf);
        } else {
            int ns = ssf_.getNonStationaryDim();
            int nx = data_.xfactor == null ? 0 : data_.xfactor.length;
            double[] xfactor = new double[ns + nx];
            for (int i = 0; i < ns; ++i) {
                xfactor[i] = 1;
            }
            for (int i = 0; i < nx; ++i) {
                xfactor[i + ns] = xf[i];
            }
            ll_.rescale(data_.yfactor, xfactor);
        }
    }

    /**
     * Sets the number of diffuse regression variables, which have to be placed
     * at the beginning of the regressors list.
     *
     * @param value The number of diffuse regression variable. -1 if all the
     * regressors are diffuse.
     */
    public void setDiffuseRegressorsCount(int value) {
        ndiffuseRegressors_ = value;
    }

    /**
     * Sets
     *
     * @param value
     */
    public void setMapping(IParametricMapping<S> value) {
        mapping_ = value;
    }

    /**
     *
     * @param value
     */
    public void setMinimizer(IFunctionMinimizer value) {
        min_ = value;
    }

    /**
     *
     * @param value
     */
    public void setSsf(S value) {
        ssf_ = value;
    }

    /**
     *
     * @param value
     */
    public void setSsfOption(SsfOption value) {
        ssfOption_ = value;
    }

    /**
     *
     * @param value
     */
    public void useDisturbanceSmoother(boolean value) {
        noDisturbanceSmoother_ = !value;
    }

    /**
     *
     * @param value
     */
    public void useML(boolean value) {
        ml_ = value;
    }

    private SubMatrix X(boolean full) {
        if (data_.hX == null) {
            return null;
        } else if (!full) {
            int xstart = data_.hEDom.getStart().minus(data_.hDom.getStart());
            int nx = data_.hEDom.getLength(), ny = data_.hEX.getColumnsCount();
            return data_.hX.subMatrix(xstart, xstart + nx, 0, ny);
        } else {
            return data_.hX.subMatrix();
        }
    }

    /**
     *
     * @param i
     * @return
     */
    public CoefficientEstimation X(int i) {
        if (ll_ == null) {
            return null;
        }
        double b = ll_.getB()[i];
        int hp = mapping_ == null ? 0 : mapping_.getDim();
        double v = ll_.bser(i, true, hp);
        return new CoefficientEstimation(b, v);
    }

    private double[] YC() {
        SubMatrix x = EX(true);
        if (x != null) {
            double[] yc = data_.hY.clone();
            DataBlock rcy = new DataBlock(yc);
            DataBlockIterator cols = x.columns();
            DataBlock col = cols.getData();
            DataBlock b = coeff();
            do {
                rcy.addAY(-b.get(cols.getPosition()), col);
            } while (cols.next());
            return yc;
        } else {
            return data_.hY;
        }
    }

}
