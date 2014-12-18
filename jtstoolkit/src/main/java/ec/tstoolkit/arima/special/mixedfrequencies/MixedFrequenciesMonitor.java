/*
 * Copyright 2014 National Bank of Belgium
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
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.benchmarking.Cumulator;
import ec.benchmarking.ssf.SsfDisaggregation;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.arima.StationaryTransformation;
import ec.tstoolkit.arima.estimation.AnsleyFilter;
import ec.tstoolkit.arima.special.EasterSpec;
import ec.tstoolkit.arima.special.RegressionSpec;
import ec.tstoolkit.arima.special.TradingDaysSpec;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.DifferenceStationaryModelHelper;
import ec.tstoolkit.eco.DifferenceStationaryModelHelper.LikelihoodFunction;
import ec.tstoolkit.eco.DifferenceStationaryModelHelper.LikelihoodFunctionInstance;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ISsqFunction;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.DefaultArimaSpec;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.ssf.DiffuseFilteringResults;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfAlgorithm;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfFunction;
import ec.tstoolkit.ssf.SsfFunctionInstance;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.timeseries.DataType;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.DiffConstant;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequenciesMonitor {

    private TsData ls_, hs_, le_, he_, si_, esi_;
    private int c_;
    private MixedFrequenciesSpecification spec_;
    private TsDomain hdomain_, edomain_;
    private final ProcessingContext context_;
    private TsVariableList x_;
    private Matrix X_, J_, pcov_;
    private SarimaModel arima0_, arima_;
    private ConcentratedLikelihood ll_;
    private ISsqFunction fn_;

    public MixedFrequenciesMonitor() {
        context_ = ProcessingContext.getActiveContext();
    }

    public MixedFrequenciesMonitor(ProcessingContext context) {
        context_ = context;
    }

    public boolean process(final TsData lseries, final TsData hseries, final MixedFrequenciesSpecification spec) {
        clear();
        TsFrequency lf = lseries.getFrequency(), hf = hseries.getFrequency();
        if (lf == hf) {
            return false;
        }
        if (lf.intValue() < hf.intValue()) {
            ls_ = lseries;
            hs_ = hseries;
        } else {
            ls_ = hseries;
            hs_ = lseries;
        }
        spec_ = spec;
        if (!computeDomains()) {
            return false;
        }
        buildRegression();
        computeX();
        calcInitialModel();
        if (spec_.getEstimate().getMethod() == EstimateSpec.Method.KalmanFilter) {
            return estimateSsf();
        } else {
            return estimateMatrix();
        }
    }

    public void calcInterpolation() {
        if (spec_.getEstimate().getMethod() == EstimateSpec.Method.KalmanFilter) {
            ssfInterpolate();
        } else {
            matrixInterpolation();
        }

    }

    public TsData getInterpolatedSeries() {
        if (si_ == null) {
            calcInterpolation();
        }
        return si_;
    }

    public TsData getInterpolationErrors() {
        if (esi_ == null) {
            calcInterpolation();
        }
        return esi_;
    }

    public SarimaModel getArima() {
        return arima_;
    }

    public ConcentratedLikelihood getLikelihood() {
        return ll_;
    }

    public TsVariableList getRegression() {
        buildRegression();
        return x_;
    }

    public Matrix getX() {
        return X_;
    }

    public Matrix getJ() {
        return J_;
    }

    public TsDomain getEstimationDomain() {
        return edomain_;
    }

    public TsData getHighFreqInput() {
        return hs_;
    }

    public TsData getLowFreqInput() {
        return ls_;
    }

    public TsData getHighFreqData() {
        return he_;
    }

    public TsData getLowFreqData() {
        return le_;
    }

    public int getFrequenciesRatio() {
        return c_;
    }

    Matrix getParametersCovariance() {
        return pcov_;
    }

    private void clear() {
        ls_ = null;
        hs_ = null;
        spec_ = null;
        hdomain_ = null;
        edomain_ = null;
        x_ = null;
        X_ = null;
        J_ = null;
        arima_ = null;
        arima0_ = null;
        fn_ = null;
        ll_ = null;
        si_ = null;
        esi_ = null;
    }

    public ISsqFunction getFunction() {
        return fn_;
    }

    private void calcInitialModel() {
        arima0_ = spec_.getArima().getArima(edomain_.getFrequency().intValue());
        if (!spec_.getArima().hasFreeParameters()) {
            return;
        }
        arima0_.setDefault();
        TramoSpecification spec = TramoSpecification.TR0.clone();
        // Log
        if (spec_.getBasic().isLog()) {
            spec.getTransform().setFunction(DefaultTransformationType.Log);
        }
        // Arima
        ec.tstoolkit.modelling.arima.tramo.ArimaSpec tarima = spec.getArima();
        DefaultArimaSpec sarima = spec_.getArima();
        tarima.setPhi(sarima.getPhi());
        tarima.setTheta(sarima.getTheta());
        tarima.setBPhi(sarima.getBPhi());
        tarima.setBTheta(sarima.getBTheta());
        tarima.setD(sarima.getD());
        tarima.setBD(sarima.getBD());
        tarima.setMean(sarima.isMean());
        // Calendar
        TradingDaysSpec std = spec_.getRegression().getTradingDays();
        ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec ttd = spec.getRegression().getCalendar().getTradingDays();
        if (std.isUsed()) {
            ttd.setTradingDaysType(ttd.getTradingDaysType());
            ttd.setLeapYear(std.isLeapYear());
            ttd.setStockTradingDays(std.getStockTradingDays());
            if (std.getHolidays() != null) {
                ttd.setHolidays(std.getHolidays());
            } else if (std.getUserVariables() != null) {
                std.setUserVariables(ttd.getUserVariables());
            }
        }
        EasterSpec se = spec_.getRegression().getEaster();
        ec.tstoolkit.modelling.arima.tramo.EasterSpec te = spec.getRegression().getCalendar().getEaster();
        if (se.isUsed()) {
            te.setDuration(se.getDuration());
            te.setOption(se.getOption());
        }
        // Regression
        RegressionSpec sreg = spec_.getRegression();
        ec.tstoolkit.modelling.arima.tramo.RegressionSpec treg = spec.getRegression();
        treg.setInterventionVariables(sreg.getInterventionVariables());
        treg.setOutliers(sreg.getOutliers());
        treg.setRamps(sreg.getRamps());
        treg.setUserDefinedVariables(sreg.getUserDefinedVariables());

        IPreprocessor preprocessor = spec.build();
        try {
            PreprocessingModel model = preprocessor.process(hs_, null);
            if (model != null) {
                arima0_ = model.estimation.getArima();
            }
        } catch (Exception err) {
        }
    }

    private boolean computeDomains() {
        TsFrequency hf = hs_.getFrequency(), lf = ls_.getFrequency();
        c_ = hf.ratio(lf);
        TsDomain hdom = hs_.getDomain(), ldom = ls_.getDomain().changeFrequency(hf, true);
        if (!hdom.intersection(ldom).isEmpty()) {
            return false; // Domains are not disjoint
        }
        hdomain_ = hdom.union(ldom).select(spec_.getBasic().getSpan());
        if (hdomain_.intersection(hdom).isEmpty() || hdomain_.intersection(ldom).isEmpty()) {
            return false;
        }
        edomain_ = hdomain_.select(spec_.getEstimate().getSpan());
        TsPeriodSelector sel = new TsPeriodSelector();
        sel.between(edomain_.getStart().firstday(), edomain_.getLast().lastday());
        he_ = hs_.select(sel);
        le_ = ls_.select(sel);
        return !edomain_.intersection(hdom).isEmpty() && !edomain_.intersection(ldom).isEmpty();
    }

    private void computeX() {
        if (x_.isEmpty()) {
            return;
        }
        X_ = x_.all().matrix(edomain_);
    }

    private void buildRegression() {
        x_ = new TsVariableList();
        if (spec_.getArima().isMean()) {
            x_.add(new DiffConstant(spec_.getArima().getSpecification(edomain_.getFrequency().intValue()).getDifferencingFilter(),
                    edomain_.getStart().firstday()));
        }
        spec_.getRegression().fill(x_, edomain_.getFrequency(), context_);
    }

    private DataBlock buildSsfY() {
        TsFrequency hf = hs_.getFrequency(), lf = ls_.getFrequency();
        TsData h = hs_.fittoDomain(edomain_);
        TsDomain ldom = ls_.getDomain(), hdom = hs_.getDomain();
        int l0 = edomain_.search(ldom.getStart().firstPeriod(hf));
        int l1 = edomain_.search(ldom.getEnd().firstPeriod(hf));
        if (l0 < 0) {
            l0 = 0;
        }
        l0 += c_ - 1;
        if (l1 < 0) {
            l1 = edomain_.getLength();
        }
        int i0 = ldom.search(edomain_.getStart().firstday());
        if (i0 < 0) {
            i0 = 0;
        }
        int h0 = edomain_.search(hdom.getStart()), h1 = edomain_.search(hdom.getEnd());
        if (h0 < 0) {
            h0 = 0;
        }
        if (h1 < 0) {
            h1 = edomain_.getLength();
        }
        boolean log = spec_.getBasic().isLog();
        boolean flow = spec_.getBasic().getDataType() == DataType.Flow;
        DataBlock y = new DataBlock(log ? h.log() : h);
        if (flow) {
            Cumulator cumul = new Cumulator(c_);
            cumul.transform(y.range(h0, h1));
        }
        if (log) {
            double cl = Math.log(c_);
            for (int j = l0, i = i0; j < l1; j += c_, ++i) {
                double cur = Math.log(ls_.get(i));
                if (flow) {
                    y.set(j, c_ * (cur - cl));
                } else {
                    y.set(j, cur);
                }
            }
        } else {
            for (int j = l0, i = i0; j < l1; j += c_, ++i) {
                y.set(j, ls_.get(i));
            }
        }
        return y;
    }

    private Matrix buildSsfX() {
        if (X_ != null) {
            Matrix x = X_.clone();
            if (spec_.getBasic().getDataType() == DataType.Flow) {
                TsFrequency hf = hs_.getFrequency(), lf = ls_.getFrequency();
                int c = hf.ratio(lf);
                Cumulator cumul = new Cumulator(c);
                DataBlockIterator cols = x.columns();
                DataBlock col = cols.getData();
                do {
                    cumul.transform(col);
                } while (cols.next());
            }
            return x;
        } else {
            return null;
        }
    }

    private boolean estimateSsf() {
        DataBlock yc = buildSsfY();
        Matrix Xc = buildSsfX();
        if (spec_.getBasic().getDataType() == DataType.Flow) {
            return calcDisaggSsf(yc, Xc);
        } else {
            return calcSsf(yc, Xc);
        }
    }

    private boolean estimateMatrix() {
        TsFrequency hf = hs_.getFrequency(), lf = ls_.getFrequency();
        int c = hf.ratio(lf);
        TsDomain ldom = ls_.getDomain(), hdom = hs_.getDomain();
        TsPeriodSelector sel = new TsPeriodSelector();
        sel.between(edomain_.getStart().firstday(), edomain_.getLast().lastday());
        int l0 = edomain_.search(ldom.getStart().firstPeriod(hf));
        int l1 = edomain_.search(ldom.getEnd().firstPeriod(hf));
        if (l0 < 0) {
            l0 = 0;
        }
        if (l1 < 0) {
            l1 = edomain_.getLength();
        }
        TsData tsl = ls_.select(sel);
        int h0 = edomain_.search(hdom.getStart()), h1 = edomain_.search(hdom.getEnd());
        if (h0 < 0) {
            h0 = 0;
        }
        if (h1 < 0) {
            h1 = edomain_.getLength();
        }
        TsData tsh = hs_.select(sel);
        int d = spec_.getArima().getD() + spec_.getArima().getBD() * edomain_.getFrequency().intValue();
        int n = edomain_.getLength();
        int m = tsl.getObsCount() + tsh.getObsCount();

        int[] initials = DifferenceStationaryModelHelper.searchDefaultInitialValues(tsh, d);
        if (initials == null) {
            return false;
        }
        DataBlock y = new DataBlock(m);
        J_ = new Matrix(m, n);
        boolean log = spec_.getBasic().isLog();
        boolean flow = spec_.getBasic().getDataType() == DataType.Flow;
        double lc = log ? Math.log(c) : 0, cf = log ? 1.0 / c : 1.0;

        // first, the initial values
        int r = 0;
        for (; r < d; ++r) {
            y.set(r, log ? Math.log(tsh.get(initials[r])) : tsh.get(initials[r]));
            J_.set(r, h0 + initials[r], 1);
        }
        // then, the low-freq values
        for (int i = 0, j = l0; i < tsl.getLength(); ++i, j += c) {
            if (!tsl.isMissing(i)) {
                double cur = log ? Math.log(tsl.get(i)) : tsl.get(i);
                y.set(r, flow ? cur - lc : cur);
                if (flow) {
                    for (int k = j; k < j + c; ++k) {
                        J_.set(r, k, cf);
                    }

                } else {
                    J_.set(r, j + c - 1, 1);
                }
                ++r;
            }
        }
        // then, the high-freq values
        for (int i = 0, j = h0; i < tsh.getLength(); ++i, ++j) {
            if (!tsh.isMissing(i) && !contains(initials, i)) {
                y.set(r, log ? Math.log(tsh.get(i)) : tsh.get(i));
                J_.set(r, j, 1);
                ++r;
            }
        }
        return calcMcElroy(y);
    }

    private static boolean contains(int[] inits, int c) {
        if (inits == null) {
            return false;
        }
        for (int i = 0; i < inits.length; ++i) {
            if (inits[i] == c) {
                return true;
            }
        }
        return false;
    }

    private boolean calcDisaggSsf(DataBlock yc, Matrix Xc) {
        SarimaModel arima = arima0_.clone();
        SsfArima ssf = new SsfArima(arima);
        SsfDisaggregation disagg = new SsfDisaggregation(c_, ssf);
        SsfModel<SsfDisaggregation> model = new SsfModel<>(disagg, new SsfData(yc, null), Xc != null ? Xc.subMatrix() : null, null);
        SsfAlgorithm<SsfDisaggregation> alg = new SsfAlgorithm<>();
        DisaggregationMapping mapping = new DisaggregationMapping();
        SsfFunction<SsfDisaggregation> fn = new SsfFunction<>(model, mapping, alg, false, true);
        fn_ = fn;
        if (!spec_.getArima().hasFreeParameters()) {
            arima_ = arima0_.clone();
            SsfFunctionInstance<SsfDisaggregation> rslt = (SsfFunctionInstance<SsfDisaggregation>) fn.evaluate(mapping.map(disagg));
            ll_ = rslt.getLikelihood().toConcentratedLikelihood();
        } else {
            IFunctionMinimizer min = minimizer();
            boolean converged = min.minimize(fn, fn.evaluate(mapping.map(disagg)));
            SsfFunctionInstance<SsfDisaggregation> rslt = (SsfFunctionInstance<SsfDisaggregation>) min.getResult();
            disagg = rslt.ssf;
            ssf = (SsfArima) disagg.getInternalSsf();
            arima_ = (SarimaModel) ssf.getModel();
            ll_ = rslt.getLikelihood().toConcentratedLikelihood();
        }
        // compute the interpolated series

        return true;
    }

    private void ssfInterpolate(SsfArima model, DataBlock yc, Matrix Xc) {
        Smoother smoother = new Smoother();
        smoother.setCalcVar(true);
        smoother.setSsf(model);
        SmoothingResults srslts = new SmoothingResults();
        if (!smoother.process(new SsfData(calcYc(yc, Xc), null), srslts)) {
            return;
        }
        int n = yc.getLength();
        double[] yl = new double[n];
        double[] vyl = new double[n];
        double sig = ll_.getSigma();
        for (int i = 0; i < n; ++i) {
            yl[i] = model.ZX(i, srslts.A(i));
            vyl[i] = sig * model.ZVZ(i, srslts.P(i));
        }

        if (Xc != null) {
            Matrix Xl = new Matrix(n, Xc.getColumnsCount());
            DataBlockIterator xccols = Xc.columns(), xlcols = Xl.columns();
            DataBlock xccol = xccols.getData(), xlcol = xlcols.getData();
            DisturbanceSmoother dsm = new DisturbanceSmoother();
            dsm.setSsf(model);
            DiffuseFilteringResults dfrslts = smoother.getFilteringResults();
            double[] tmp = new double[n];
            do {
                xccol.copyTo(tmp, 0);
                dfrslts.getVarianceFilter().process(
                        dfrslts.getFilteredData(), 0, tmp, null);
                if (!dsm.process(new SsfData(tmp, null), dfrslts)) {
                    return;
                }
                srslts = dsm.calcSmoothedStates();
                for (int i = 0; i < n; ++i) {
                    xlcol.set(i, model.ZX(i, srslts.A(i)));
                }
            } while (xccols.next() && xlcols.next());

            DataBlock rcy = new DataBlock(yl);
            DataBlockIterator xcols = X_.columns();
            DataBlock xcol = xcols.getData();
            DataBlock b = new DataBlock(ll_.getB());
            do {
                rcy.addAY(b.get(xcols.getPosition()), xcol);
            } while (xcols.next());

            DataBlockIterator xrows = X_.rows(), xlrows = Xl.rows();
            DataBlock xrow = xrows.getData(), xlrow = xlrows.getData();
            Matrix bvar = ll_.getBVar();
            do {
                xlrow.sub(xrow);
                vyl[xlrows.getPosition()] += SymmetricMatrix.quadraticForm(bvar, xlrow);
            } while (xrows.next() && xlrows.next());
        }

        DescriptiveStatistics ds = new DescriptiveStatistics(yl);

        for (int i = 0; i < vyl.length; ++i) {
            if (vyl[i] < ds.getVar() * 1e-12) {
                vyl[i] = 0;
            } else {
                vyl[i] = Math.sqrt(vyl[i]);
            }
        }
        si_ = new TsData(edomain_.getStart(), yl, false);
        esi_ = new TsData(edomain_.getStart(), vyl, false);
    }

    private void ssfInterpolate() {
        DataBlock yc = buildSsfY();
        Matrix Xc = buildSsfX();
        SsfArima ssf = new SsfArima(arima_);
        if (spec_.getBasic().getDataType() == DataType.Flow) {
            SsfDisaggregation disagg = new SsfDisaggregation(c_, ssf);
            ssfInterpolate(disagg, yc, Xc);
        } else {
            ssfInterpolate(ssf, yc, Xc);
        }
    }

    private void ssfInterpolate(SsfDisaggregation model, DataBlock yc, Matrix Xc) {
        ISsf issf = model.getInternalSsf();
        int dim = model.getStateDim();
        Smoother smoother = new Smoother();
        smoother.setCalcVar(true);
        smoother.setSsf(model);
        SmoothingResults srslts = new SmoothingResults();
        DataBlock ycc = calcYc(yc, Xc);
        if (!smoother.process(new SsfData(ycc, null), srslts)) {
            return;
        }
        int n = yc.getLength();
        double[] yl = new double[n];
        double[] vyl = new double[n];
        double sig = ll_.getSigma();
        for (int i = 0; i < n; ++i) {
            yl[i] = issf.ZX(i, srslts.A(i).drop(1, 0));
            vyl[i] = sig * issf.ZVZ(i, srslts.P(i).extract(1, dim, 1, dim));
        }

        if (Xc != null) {
            Matrix Xl = new Matrix(n, Xc.getColumnsCount());
            DataBlockIterator xccols = Xc.columns(), xlcols = Xl.columns();
            DataBlock xccol = xccols.getData(), xlcol = xlcols.getData();
            DisturbanceSmoother dsm = new DisturbanceSmoother();
            dsm.setSsf(model);
            DiffuseFilteringResults dfrslts = smoother.getFilteringResults();
            double[] tmp = new double[n];
            do {
                xccol.copyTo(tmp, 0);
                dfrslts.getVarianceFilter().process(
                        dfrslts.getFilteredData(), 0, tmp, null);
                if (!dsm.process(new SsfData(tmp, null), dfrslts)) {
                    return;
                }
                srslts = dsm.calcSmoothedStates();
                for (int i = 0; i < n; ++i) {
                    xlcol.set(i, issf.ZX(i, srslts.A(i).drop(1, 0)));
                }
            } while (xccols.next() && xlcols.next());

            DataBlock rcy = new DataBlock(yl);
            DataBlockIterator xcols = X_.columns();
            DataBlock xcol = xcols.getData();
            DataBlock b = new DataBlock(ll_.getB());
            do {
                rcy.addAY(b.get(xcols.getPosition()), xcol);
            } while (xcols.next());

            DataBlockIterator xrows = X_.rows(), xlrows = Xl.rows();
            DataBlock xrow = xrows.getData(), xlrow = xlrows.getData();
            Matrix bvar = ll_.getBVar();
            do {
                xlrow.sub(xrow);
                vyl[xlrows.getPosition()] += SymmetricMatrix.quadraticForm(bvar, xlrow);
            } while (xrows.next() && xlrows.next());
        }
        DescriptiveStatistics ds = new DescriptiveStatistics(yl);

        for (int i = 0; i < vyl.length; ++i) {
            if (vyl[i] < ds.getVar() * 1e-12) {
                vyl[i] = 0;
            } else {
                vyl[i] = Math.sqrt(vyl[i]);
            }
        }
        si_ = new TsData(edomain_.getStart(), yl, false);
        esi_ = new TsData(edomain_.getStart(), vyl, false);
    }

    private DataBlock calcYc(DataBlock y, Matrix Xc) {
        if (Xc != null) {
            DataBlock yc = y.deepClone();
            DataBlockIterator cols = Xc.columns();
            DataBlock col = cols.getData();
            double[] b = ll_.getB();
            do {
                yc.addAY(-b[cols.getPosition()], col);
            } while (cols.next());
            return yc;
        } else {
            return y;
        }
    }

    private boolean calcSsf(DataBlock yc, Matrix Xc) {
        SarimaModel arima = arima0_.clone();
        SsfArima ssf = new SsfArima(arima);
        SsfModel<SsfArima> model = new SsfModel<>(ssf, new SsfData(yc, null), Xc != null ? Xc.subMatrix() : null, null);
        SsfAlgorithm<SsfArima> alg = new SsfAlgorithm<>();
        Mapping mapping = new Mapping();
        SsfFunction<SsfArima> fn = new SsfFunction<>(model, mapping, alg);
        fn_ = fn;
        IFunctionMinimizer min = minimizer();
        boolean converged = min.minimize(fn, fn.evaluate(mapping.map(ssf)));
        SsfFunctionInstance<SsfArima> rslt = (SsfFunctionInstance<SsfArima>) min.getResult();
        ssf = (SsfArima) rslt.ssf;
        arima_ = (SarimaModel) ssf.getModel();
        ll_ = rslt.getLikelihood().toConcentratedLikelihood();
        // compute the interpolated series
        ssfInterpolate(ssf, yc, Xc);
        return true;
    }

    private boolean calcMcElroy(DataBlock y) {
        SarimaModel arima = arima0_.clone();
        ModelProvider provider = new ModelProvider(y, J_, X_, arima);

        SarimaMapping mapping = new SarimaMapping(arima.getSpecification(), true);
        LikelihoodFunction<ModelProvider> fn
                = new LikelihoodFunction<>(provider, mapping);
        fn.setLCompute(spec_.getEstimate().getMethod() == EstimateSpec.Method.Cholesky);
        fn_ = fn;
        if (!spec_.getArima().hasFreeParameters()) {
            LikelihoodFunctionInstance<ModelProvider> rslt = (LikelihoodFunctionInstance<ModelProvider>) fn.evaluate(mapping.map(arima));
            arima_ = arima0_.clone();
            ll_ = rslt.getLikelihood();
        } else {
            IFunctionMinimizer min = minimizer();
            boolean converged = min.minimize(fn, fn.evaluate(mapping.map(arima)));
            LikelihoodFunctionInstance<ModelProvider> rslt = (LikelihoodFunctionInstance<ModelProvider>) min.getResult();
            arima_ = mapping.map(rslt.getParameters());
            ll_ = rslt.getLikelihood();
        }
        return true;
    }

    private void matrixInterpolation() {
        LikelihoodFunction<ModelProvider> fn = (LikelihoodFunction<ModelProvider>) fn_;
        Matrix proj = fn.getHelper().computeProjections(null, fn.getModelProvider().getStationnaryCovariance(arima_.getParameters()));
        if (proj != null) {
            si_ = new TsData(edomain_.getStart(), proj.column(0));
            esi_ = new TsData(edomain_.getStart(), proj.subDiagonal(1)).sqrt();
        }

    }

    private IFunctionMinimizer minimizer() {
        LevenbergMarquardtMethod lm = new LevenbergMarquardtMethod();
        lm.setConvergenceCriterion(spec_.getEstimate().getTol());
        return new ProxyMinimizer(lm);
    }

    private class Mapping implements IParametricMapping<SsfArima> {

        private final SarimaMapping internalMapping;
        private final int c;

        public Mapping() {
            TsFrequency lf = ls_.getFrequency(), hf = hs_.getFrequency();
            c = hf.ratio(lf);
            internalMapping = new SarimaMapping(spec_.getArima().getSpecification(hf.intValue()), true);
        }

        @Override
        public SsfArima map(IReadDataBlock p) {
            SarimaModel arima = internalMapping.map(p);
            return new SsfArima(arima);
        }

        @Override
        public IReadDataBlock map(SsfArima t) {
            SarimaModel arima = (SarimaModel) t.getModel();
            return internalMapping.map(arima);
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
            return internalMapping.checkBoundaries(inparams);
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return internalMapping.epsilon(inparams, idx);
        }

        @Override
        public int getDim() {
            return internalMapping.getDim();
        }

        @Override
        public double lbound(int idx) {
            return internalMapping.lbound(idx);
        }

        @Override
        public double ubound(int idx) {
            return internalMapping.ubound(idx);
        }

        @Override
        public ParamValidation validate(IDataBlock ioparams) {
            return internalMapping.validate(ioparams);
        }

        @Override
        public String getDescription(int idx) {
            return internalMapping.getDescription(idx);
        }
    }

    private class DisaggregationMapping implements IParametricMapping<SsfDisaggregation> {

        private final SarimaMapping internalMapping;
        private final int c;

        public DisaggregationMapping() {
            TsFrequency lf = ls_.getFrequency(), hf = hs_.getFrequency();
            c = hf.ratio(lf);
            internalMapping = new SarimaMapping(spec_.getArima().getSpecification(hf.intValue()), true);
        }

        @Override
        public SsfDisaggregation map(IReadDataBlock p) {
            SarimaModel arima = internalMapping.map(p);
            return new SsfDisaggregation(c, new SsfArima(arima));
        }

        @Override
        public IReadDataBlock map(SsfDisaggregation t) {
            SsfArima ssf = (SsfArima) t.getInternalSsf();
            SarimaModel arima = (SarimaModel) ssf.getModel();
            return internalMapping.map(arima);
        }

        @Override
        public boolean checkBoundaries(IReadDataBlock inparams) {
            return internalMapping.checkBoundaries(inparams);
        }

        @Override
        public double epsilon(IReadDataBlock inparams, int idx) {
            return internalMapping.epsilon(inparams, idx);
        }

        @Override
        public int getDim() {
            return internalMapping.getDim();
        }

        @Override
        public double lbound(int idx) {
            return internalMapping.lbound(idx);
        }

        @Override
        public double ubound(int idx) {
            return internalMapping.ubound(idx);
        }

        @Override
        public ParamValidation validate(IDataBlock ioparams) {
            return internalMapping.validate(ioparams);
        }

        @Override
        public String getDescription(int idx) {
            return internalMapping.getDescription(idx);
        }
    }

    static class ModelProvider implements DifferenceStationaryModelHelper.IModelProviderEx {

        private final DataBlock y_;
        private final Matrix J_, X_;
        private final SarimaModel starima_;
        private final BackFilter diff_;

        ModelProvider(DataBlock y, Matrix J, Matrix X, SarimaModel arima) {
            y_ = y;
            J_ = J;
            X_ = X;
            StationaryTransformation sf = arima.stationaryTransformation();
            starima_ = (SarimaModel) sf.stationaryModel;
            diff_ = sf.unitRoots;
        }

        @Override
        public Matrix getStationnaryCovariance(IReadDataBlock parameters) {
            SarimaModel tmp = starima_.clone();
            tmp.setParameters(parameters);
            return tmp.covariance(J_.getColumnsCount() - diff_.getDegree()); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public BackFilter getDifferencing() {
            return diff_;
        }

        @Override
        public Matrix getTransformation() {
            return J_;
        }

        @Override
        public DataBlock getTransformedData() {
            return y_;
        }

        @Override
        public Matrix getDesignMatrix() {
            return X_;
        }

        @Override
        public Matrix getLCholesky(IReadDataBlock parameters, Matrix B) {
            SarimaModel tmp = starima_.clone();
            tmp.setParameters(parameters);
            int n = B.getColumnsCount(), m = B.getRowsCount();
            // step 1: Compute the Lcholesky factor of the banded matrix of the transformed model, 
            // as in Ansley.
            AnsleyFilter filter = new AnsleyFilter();
            filter.initialize(tmp, n);
            Matrix L = filter.getCholeskyFactor();

            // re-arrange B
            Matrix Bt = B.transpose();

            DataBlockIterator rows = Bt.columns();
            DataBlock row = rows.getData();
            Polynomial P = tmp.getStationaryAR().getPolynomial();
            if (P.getDegree() > 0) {
                do {
                    ptransform(row, P, P.getDegree());
                } while (rows.next());
                rows.begin();
            }

            // computes B x L
            do {
                ltransform(row, L);
            } while (rows.next());

            // triangularize by givens rotations
            ec.tstoolkit.maths.matrices.ElementaryTransformations.givensTriangularize(Bt.subMatrix().transpose());
            return new Matrix(Bt.subMatrix(0, m, 0, m).transpose());
        }

        /**
         * Computes b = b * P^-1 where P[[0,n[, [0,n[ ] = I and P[j-d... j, j] =
         * p(d)...1. We have : c = b * P^-1 iff c * P = b. c may be computed
         * recursively
         *
         * @param b
         * @param d
         * @param n
         */
        private void ptransform(DataBlock b, Polynomial p, int n) {
            int l = b.getLength();
            int d = p.getDegree();
            while (l > d && b.get(l - 1) == 0) {
                --l;
            }
            DataBlock w = new DataBlock(p.rextract(1, d));
            // initial steps j=l-d...l-1
            // c[l-1]=p
            for (int i = 1, j = l - 1; i < d; ++i, --j) {
                double z = b.range(j, l).dot(w.range(0, i));
                b.add(j - 1, -z);
            }
            // normal steps
            for (int j = l - d; j > n; --j) {
                double z = b.range(j, j + d).dot(w);
                b.add(j - 1, -z);
            }
            // last steps
            for (int i = 0, j = n; j > n - d; --j, ++i) {
                double z = b.range(n, n + d - i).dot(w.range(i, d));
                b.add(j - 1, -z);
            }

        }

        /**
         * Computes b*L
         *
         * @param b
         * @param L
         */
        private void ltransform(DataBlock b, Matrix L) {
            // full steps...
            int n = b.getLength(), k = L.getRowsCount();
            while (n > k && b.get(n - 1) == 0) {
                --n;
            }
            DataBlock bc = b.range(0, k);
            for (int i = 0; i < n - k; ++i) {
                b.set(i, L.column(i).dot(bc));
                bc.move(1);
            }
            for (int i = n - k, j = 0; i < n; ++i, ++j) {
                b.set(i, L.column(i).drop(0, j).dot(bc));
                bc.bshrink();
            }

        }
    }
}
