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

package ec.tstoolkit.arima.special;

import ec.tstoolkit.arima.estimation.GlsArimaMonitor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.SubArrayOfInt;
import ec.tstoolkit.data.TableOfInt;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.HannanRissanen;
import ec.tstoolkit.sarima.estimation.SarimaMapping;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.List;

/**
 * The GeneralizedAirlineMonitor monitors the estimation and the decomposition
 * of the generalized airline model for given series and specifications.
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class GeneralizedAirlineMonitor {

    private GaSpecification m_spec = new GaSpecification();
    private RegArimaEstimation<GeneralizedAirlineModel>[] m_rslts;
    private TsData m_series;
    private TsVariableList m_regs;
    private DataBlock m_starthr, m_startairline;
    private static final double m_urbound = .98;
    private boolean m_bfreeestimation = true;
    private boolean m_mean;
    private final HashMap<Long, RegArimaEstimation<GeneralizedAirlineModel>> m_smodel = new HashMap<>();
    private static final int m_nsel = 5;

    /**
     * Estimates the generalized airline model for a given series.
     *
     * @param series The analyzed series.
     * @param saregs
     * @return True if at least one model has been successfully estimated
     */
    public boolean process(final TsData series, final TsVariableList saregs) {
        if (series == null)
            return false;
        m_smodel.clear();
        m_rslts = null;
        m_series = series;
        m_regs = saregs;
        int freq = m_series.getFrequency().intValue();

        RegArimaModel<GeneralizedAirlineModel> regs = buildModel(null);

        if (m_spec.getEstimationMode() == GaSpecification.EstimationMode.Exhaustive) {
            return exhaustiveEstimation(freq, regs);
        } else if (m_spec.getEstimationMode() == GaSpecification.EstimationMode.Iterative) {
            return recursiveEstimation(freq, regs);
        } else {
            return selectiveEstimation(freq, regs);
        }
    }

    private boolean selectiveEstimation(final int freq,
            final RegArimaModel<GeneralizedAirlineModel> regs) {
        m_rslts = new RegArimaEstimation[1];
        if (!calcInitialValues(freq, regs)) {
            return false;
        }
        TableOfInt C = m_spec.generateParameters(freq);
        if (C == null || C.getRowsCount() == 0) {
            return true;
        }

        // compute derivatives
        double[] derivative = new double[freq / 2];
        int[] c = new int[freq / 2];
        for (int i = 0; i < c.length; ++i) {
            c[i] = 1;
        }
        double ll = m_rslts[0].likelihood.getLogLikelihood();
        double[] val = m_rslts[0].model.getArima().getCoefficients();
        double[] np = new double[3];
        np[0] = val[0];
        np[1] = val[1];
        double h = 0.001;
        int df = m_rslts[0].likelihood.getN()
                - m_rslts[0].likelihood.getNx();
        for (int i = 0; i < freq / 2; ++i) {
            c[i] = 2;
            np[2] = val[1] - h;
            GeneralizedAirlineModel gairline2 = new GeneralizedAirlineModel(
                    freq, np, SubArrayOfInt.create(c));
            regs.setArima(gairline2);
            double ll1 = regs.computeLikelihood().getLogLikelihood();
            if (1 - np[2] < h) {
                derivative[i] = (ll - ll1) / (h * df);
            } else {
                gairline2.setParameter(2, val[1] + h);
                double ll2 = regs.computeLikelihood().getLogLikelihood();
                derivative[i] = (ll2 - ll1) / (2 * h * df);
            }
            c[i] = 1;
        }

        int nparams = m_spec.isFreeZeroFrequencyParameter() ? 4 : 3;
        double[] drank = new double[C.getRowsCount()];
        for (int i = 0; i < drank.length; ++i) {
            int nr = 0;
            double d0 = 0, d1 = 0;
            for (int j = 0; j < C.getColumnsCount(); ++j) {
                if (C.get(i, j) == nparams - 1) {
                    d0 += derivative[j];
                    ++nr;
                } else {
                    d1 += derivative[j];
                }
            }
            d0 /= Math.sqrt(nr);
            d1 /= Math.sqrt(C.getColumnsCount() - nr);
            drank[i] = Math.abs(d0 - d1);
        }

        double[] tmp = new double[drank.length];
        for (int i = 0; i < tmp.length; ++i) {
            tmp[i] = Math.abs(drank[i]);
        }
        java.util.Arrays.sort(tmp);
        int nsel = Math.min(tmp.length / 3, m_nsel);
        if (nsel <= 0) {
            nsel = 1;
        }
        double threshold = Math.max(tmp[tmp.length - nsel],
                tmp[tmp.length - 1] / 3);

        java.util.ArrayList<RegArimaEstimation<GeneralizedAirlineModel>> rtmp = new java.util.ArrayList<>();

        for (int i = 0; i < C.getRowsCount(); ++i) {
            try {
                if (Math.abs(drank[i]) >= threshold) {
                    rtmp.add(estimate(freq, regs, C.row(i)));
                }
            } catch (Exception e) {
            }
        }

        RegArimaEstimation<GeneralizedAirlineModel>[] rslts = new RegArimaEstimation[rtmp.size() + 1];
        rslts[0] = m_rslts[0];
        int ir = 1;
        for (RegArimaEstimation<GeneralizedAirlineModel> r : rtmp) {
            rslts[ir++] = r;
        }
        m_rslts = rslts;
        return true;
    }

    private boolean recursiveEstimation(final int freq,
            final RegArimaModel<GeneralizedAirlineModel> regs) {
        int freq2 = freq / 2;
        int nm = 1;
        for (int i = 1, j = freq2; i <= m_spec.getMaxFrequencyGroup() && j > 0; ++i, --j) {
            if (i >= m_spec.getMinFrequencyGroup()) {
                nm += j;
            }
        }
        m_rslts = new RegArimaEstimation[nm + 1];
        if (!calcInitialValues(freq, regs)) {
            return false;
        }
        int[] C = new int[freq2];
        int istart = m_spec.isFreeZeroFrequencyParameter() ? 2 : 1;
        for (int i = 0; i < freq2; ++i) {
            C[i] = istart;
        }
        int ridx = 0;
        for (int i = 1; i <= m_spec.getMaxFrequencyGroup(); ++i) {
            ridx = RCalc(i >= m_spec.getMinFrequencyGroup(), ridx, freq, regs,
                    SubArrayOfInt.create(C));
        }
        return true;
    }

    private int RCalc(final boolean save, int ridx, final int freq,
            final RegArimaModel<GeneralizedAirlineModel> regs,
            final SubArrayOfInt C) {
        int q = m_spec.isFreeZeroFrequencyParameter() ? 3 : 2;
        int jbest = -1;
        double ll = Double.MIN_VALUE;
        for (int j = 0; j < C.getLength(); ++j) {
            if (C.get(j) != q) {
                C.set(j, q);
                RegArimaEstimation<GeneralizedAirlineModel> est = estimate(
                        freq, regs, C);
                if (save) {
                    m_rslts[++ridx] = est;
                }
                if (est != null) {
                    double curll = est.likelihood.getLogLikelihood();
                    if (jbest < 0 || curll >= ll) {
                        jbest = j;
                        ll = curll;
                    }
                }
                C.set(j, q - 1);
            }
        }
        if (jbest >= 0) {
            C.set(jbest, q);
        }
        return ridx;
    }

    private boolean exhaustiveEstimation(final int freq,
            final RegArimaModel<GeneralizedAirlineModel> regs) {
        TableOfInt C = m_spec.generateParameters(freq);
        int n = C.getRowsCount() + 1;
        // FIXME: problem with generics
        m_rslts = new RegArimaEstimation[n];

        if (!calcInitialValues(freq, regs)) {
            return false;
        }

        for (int i = 1; i < m_rslts.length; ++i) {
            try {
                m_rslts[i] = estimate(freq, regs, C.row(i - 1));
            } catch (Exception e) {
            }
        }
        return true;
    }

    // build the regarima model
    private RegArimaModel<GeneralizedAirlineModel> buildModel(final int[] c) {
        int np = 2;
        if (c != null) {
            np = m_spec.isFreeZeroFrequencyParameter() ? 4 : 3;
        }
        GeneralizedAirlineModel model = new GeneralizedAirlineModel(m_series.getFrequency().intValue(),
                np, c);
        RegArimaModel<GeneralizedAirlineModel> regModel = new RegArimaModel<>(model, new DataBlock(m_series.internalStorage()));
        regModel.setMeanCorrection(m_mean);
        if (m_regs != null) {
            List<DataBlock> X = m_regs.all().data(m_series.getDomain());
            for (DataBlock x : X) {
                regModel.addX(x);
            }
        }
        return regModel;
    }

    private RegArimaEstimation<GeneralizedAirlineModel> estimate(
            final int freq, final RegArimaModel<GeneralizedAirlineModel> regs,
            final SubArrayOfInt c) {
        int nparams = m_spec.isFreeZeroFrequencyParameter() ? 4 : 3;
        // Estimate first the free model...
        if (m_bfreeestimation) {
            GeneralizedAirlineModel gairline = new GeneralizedAirlineModel(
                    freq, nparams, c);
            RegArimaEstimation<GeneralizedAirlineModel> e = estimate(regs,
                    gairline);
            if (e != null
                    && e.likelihood != null
                    && (m_rslts[0] == null || e.likelihood.getLogLikelihood() >= m_rslts[0].likelihood.getLogLikelihood())) {
                return e;
            }
        }

        if (nparams == 3) {
            return estimate3(freq, regs, c);
        } else {
            return estimate4(freq, regs, c);
        }
    }

    private RegArimaEstimation<GeneralizedAirlineModel> estimate3(
            final int freq, final RegArimaModel<GeneralizedAirlineModel> regs,
            final SubArrayOfInt c) {
        RegArimaEstimation<GeneralizedAirlineModel> est = estimate3(freq, regs,
                m_startairline, c, true);
        if (est == null
                || est.likelihood == null
                || est.likelihood.getLogLikelihood() < m_rslts[0].likelihood.getLogLikelihood()) {
            return estimate3(freq, regs, m_startairline, c, false);
        } else {
            return est;
        }
    }

    private RegArimaEstimation<GeneralizedAirlineModel> estimate3(
            final int freq, final RegArimaModel<GeneralizedAirlineModel> regs,
            final DataBlock p, final SubArrayOfInt c, boolean checkur) {
        RegArimaEstimation<GeneralizedAirlineModel> rslt;
        long ckey = GeneralizedAirlineModel.CKey(c);
        if (!checkur && m_smodel.containsKey(ckey)) {
            m_smodel.remove(ckey);
        }

        rslt = m_smodel.get(ckey);
        if (rslt == null) {
            GeneralizedAirlineModel gairline = new GeneralizedAirlineModel(
                    freq, 3, p.get(0), p.get(1), c);
            if (checkur) {
                gairline.checkRoots(m_urbound);
            }
            regs.setArima(gairline);
            rslt = estimate(regs, gairline);
            if (!checkur) {
                m_smodel.put(ckey, rslt);
            }
        }
        return rslt;
    }

    private RegArimaEstimation<GeneralizedAirlineModel> estimate4(
            final int freq, final RegArimaModel<GeneralizedAirlineModel> regs,
            final SubArrayOfInt c) {
        // estimates first the 3 parameters model.
        DataBlock pinit = null;

        int[] C3 = new int[c.getLength()];
        SubArrayOfInt c3 = SubArrayOfInt.create(C3);
        c3.copy(c);

        int nr = 0;
        for (int i = 0; i < c3.getLength(); ++i) {
            c3.set(i, c3.get(i) - 1);
            if (c3.get(i) == 0) {
                ++nr;
            }
        }

        if (c.getLength() % 2 == 0 && nr == c.getLength() / 2) {
            RegArimaEstimation<GeneralizedAirlineModel> g31 = estimate3(freq,
                    regs, c3);
            for (int i = 0; i < c3.getLength(); ++i) {
                if (c3.get(i) == 1) {
                    c3.set(i, 0);
                } else {
                    c3.set(i, 1);
                }
            }
            RegArimaEstimation<GeneralizedAirlineModel> g32 = estimate3(freq,
                    regs, c3);
            if (g31 != null && g32 != null && g31.likelihood != null
                    && g32.likelihood != null) {
                if (g31.likelihood.getLogLikelihood() > g32.likelihood.getLogLikelihood()) {
                    pinit = new DataBlock(g31.model.getArima().getParameters());
                } else {
                    pinit = new DataBlock(g32.model.getArima().getParameters());
                }
            } else if (g31 != null) {
                pinit = new DataBlock(g31.model.getArima().getParameters());
            } else if (g32 != null) {
                pinit = new DataBlock(g32.model.getArima().getParameters());
            }
        } else {
            RegArimaEstimation<GeneralizedAirlineModel> g3 = estimate3(freq,
                    regs, c3);
            if (g3 != null
                    && g3.likelihood != null
                    && g3.likelihood.getLogLikelihood() > m_rslts[0].likelihood.getLogLikelihood()) {
                pinit = new DataBlock(g3.model.getArima().getCoefficients());
            }
        }

        RegArimaEstimation<GeneralizedAirlineModel> est = estimate4(freq, regs,
                pinit, c, true);
        if (est == null
                || est.likelihood == null
                || est.likelihood.getLogLikelihood() < m_rslts[0].likelihood.getLogLikelihood()) {
            return estimate4(freq, regs, pinit, c, false);
        } else {
            return est;
        }
    }

    private RegArimaEstimation<GeneralizedAirlineModel> estimate4(
            final int freq, final RegArimaModel<GeneralizedAirlineModel> regs,
            final IReadDataBlock p, final SubArrayOfInt c,
            final boolean checkur) {
        double[] xp = new double[4];
        if (p != null) {
            double p0 = p.get(0), p1 = p.get(1);
            if (p0 >= m_urbound) {
                p0 = m_urbound;
            }
            if (p1 >= m_urbound) {
                p1 = m_urbound;
            }
            xp[0] = -(p0 + p1);
            xp[1] = p0 * p1;

            xp[2] = p.get(1);
            xp[3] = p.get(2);
        } else {
            double p0 = m_startairline.get(0), p1 = m_startairline.get(1);
            if (p0 >= m_urbound) {
                p0 = m_urbound;
            }
            if (p1 >= m_urbound) {
                p1 = m_urbound;
            }
            xp[0] = -(p0 + p1);
            xp[1] = p0 * p1;

            xp[2] = m_startairline.get(1);
            xp[3] = m_startairline.get(1);
        }

        GeneralizedAirlineModel gairline = new GeneralizedAirlineModel(freq,
                xp, c);
        if (checkur) {
            gairline.checkRoots(m_urbound);
        }
        regs.setArima(gairline);
        return estimate(regs, gairline);
    }

    private RegArimaEstimation<GeneralizedAirlineModel> estimate(
            final RegArimaModel<GeneralizedAirlineModel> regs,
            GeneralizedAirlineModel gairline) {
        try {
            boolean ok = false;
            GlsArimaMonitor<GeneralizedAirlineModel> monitor =
                    new GlsArimaMonitor<>();
            monitor.setMinimizer(new ProxyMinimizer(new LevenbergMarquardtMethod()));
            RegArimaEstimation<GeneralizedAirlineModel> est = null;
            do {
                regs.setArima(gairline);
                GeneralizedAirlineMapper mapper = new GeneralizedAirlineMapper(gairline);
                monitor.setMapping(mapper);
                est = monitor.process(regs);
                ok = monitor.hasConverged();
                if (est == null) {
                    return null;
                }
                gairline = est.model.getArima();
                mapper.setStrict(true);
                DataBlock p = new DataBlock(mapper.map(gairline));
                if (mapper.validate(p) == ParamValidation.Changed) {
                    gairline = mapper.map(p);
                    est.model.setArima(gairline);
                }
            } while ((m_spec.isFixingUnitRoots() && gairline.fixUnitRoots(ok ? .001 : 0.01))
                    && gairline.getParametersCount() > 0);
            return est;
//            // RegArimaFunction
//            SsqFunction<RegArimaModel<GeneralizedAirlineModel>, DefaultLikelihoodEvaluation<ConcentratedLikelihood>> fn = new SsqFunction<RegArimaModel<GeneralizedAirlineModel>, DefaultLikelihoodEvaluation<ConcentratedLikelihood>>();
//            GlsArima<GeneralizedAirlineModel> gls = new GlsArima<GeneralizedAirlineModel>();
//            fn.setAlgorithm(gls);
//            boolean ok = false;
//            // RegArimaFunctionInstance
//            SsqFunctionInstance<RegArimaModel<GeneralizedAirlineModel>, DefaultLikelihoodEvaluation<ConcentratedLikelihood>> ifn = null;
//            do {
//                ParametersSet p = new ParametersSet(gairline.getParameters());
//                regs.setArima(gairline);
//                fn.setExemplar(regs);
//                GeneralizedAirlineDomain domain = new GeneralizedAirlineDomain(
//                        gairline);
//                fn.setDomain(domain);
//                if (m_spec.isUsingBFGS()) {
//                    BFGS bfgs = new BFGS();
//                    IRealFunction llfn = fn;
//                    ok = bfgs.minimize(llfn, p);
//                    ifn = (SsqFunctionInstance<RegArimaModel<GeneralizedAirlineModel>, DefaultLikelihoodEvaluation<ConcentratedLikelihood>>) bfgs
//                            .getResult();
//                } else {
//                    QRMarquardt qm = new QRMarquardt();
//                    qm.setSsqConvergenceCriterion(1e-9);
//                    ISsqRealFunction llfn = fn;
//                    ok = qm.ssqMinimize(llfn, p);
//                    ifn = (SsqFunctionInstance<RegArimaModel<GeneralizedAirlineModel>, DefaultLikelihoodEvaluation<ConcentratedLikelihood>>) qm
//                            .getSsqResult();
//                }
//                if (ifn == null)
//                    return null;
//
//                gairline = ifn.getInstance().getArima();
//                domain.setStrict(true);
//                p = new ParametersSet(gairline.getParameters());
//                if (domain.validate(p) == ParamValidation.Changed)
//                    gairline.setParameters(p);
//            } while ((m_spec.isFixingUnitRoots() && gairline
//                    .fixUnitRoots(ok ? .001 : 0.01))
//                    && gairline.getParametersCount() > 0);
//
//
//            RegArimaEstimation<GeneralizedAirlineModel> e = new RegArimaEstimation<GeneralizedAirlineModel>();
//            e.setLikelihood(ifn.getEvaluation().getLikelihood());
//            e.setModel(ifn.getInstance());
//            return e;

        } catch (Exception e) {
            return null;
        }

    }

    private boolean calcInitialValues(final int freq,
            final RegArimaModel<GeneralizedAirlineModel> regs) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.airline();
        SarimaModel arima = new SarimaModel(spec);
        HannanRissanen hr = new HannanRissanen();
        boolean ok = false;
        if (regs.getVarsCount() > 0) {
            Ols ols = new Ols();
            ols.process(regs.getDModel());
            ok = hr.process(ols.getResiduals(), spec.doStationary());
        } else {
            ok = hr.process(regs.getDModel().getY(), spec.doStationary());
        }
        if (ok) {
            arima.setParameters(hr.getModel().getParameters());
            SarimaMapping.stabilize(arima);
        } else {
            DataBlock start = new DataBlock(2);
            start.set(0, -.95);
            start.set(1, -.95);
            arima.setParameters(start);
        }
        // calc airline
        m_starthr = new DataBlock(arima.getParameters());
        m_starthr.set(0, -m_starthr.get(0));
        double sq = -m_starthr.get(1);
        if (sq < 0) {
            sq = 0.2;
        }
        m_starthr.set(1, Math.pow(sq, 1.0 / freq));
        GeneralizedAirlineModel gairline = new GeneralizedAirlineModel(freq, 2,
                m_starthr.get(0), m_starthr.get(1), null);
        m_rslts[0] = estimate(regs, gairline);

        if (m_rslts[0] == null) {
            return false;
        }
        m_startairline = new DataBlock(m_rslts[0].model.getArima().getCoefficients());
        return true;
    }

    public int searchBestEstimation() {
        if (m_rslts == null) {
            return -1;
        }
        double c = Double.MAX_VALUE;
        int imax = -1;
        for (int i = 0; i < m_rslts.length; ++i) {
            if (m_rslts[i] != null) {
                GeneralizedAirlineModel ga = m_rslts[i].model.getArima();
                ConcentratedLikelihood ll = m_rslts[i].likelihood;
                if (ga != null && ll != null) {
                    int np = ga.getType() + ll.getNx();
                    double cur = 0;
                    if (m_spec.getCriterion() == GaSpecification.Criterion.AIC) {
                        cur = ll.AIC(np);
                    } else {
                        cur = ll.BIC(np);
                    }
                    if (cur < c) {
                        imax = i;
                        c = cur;
                    }
                }
            }
        }
        return imax;
    }

    public RegArimaEstimation<GeneralizedAirlineModel> result(final int idx) {
        if (m_rslts == null) {
            return null;
        }
        return m_rslts[idx];
    }

    public RegArimaEstimation<GeneralizedAirlineModel> getBestResult() {
        if (m_rslts == null) {
            return null;
        }
        int imax = searchBestEstimation();
        if (imax == -1) {
            return null;
        } else {
            return result(imax);
        }
    }

    public int getResultsCount() {
        return (m_rslts == null) ? 0 : m_rslts.length;
    }

    public int getValidResultsCount() {
        if (m_rslts == null) {
            return 0;
        } else {
            int n = 0;
            for (int i = 0; i < m_rslts.length; ++i) {
                if (m_rslts[i] != null) {
                    ++n;
                }
            }
            return n;
        }
    }

    /*public RegArimaSADecomposition SAResult(final int idx) {
    GeneralizedAirlineModelEstimation e = result(idx);
    if (e == null)
    return null;
    UCModel ucm = e.getEstimation().getModel().getArima().UCModel(
    m_spec.getURBound());
    if (ucm == null)
    return null;
    else {
    boolean bmean = e.getDefinition().getStochasticComponent().isMean();
    RegArimaSADecomposition sa = new RegArimaSADecomposition(
    e.getDefinition().getSeries().isLog() ? DecompositionType.Multiplicative
    : DecompositionType.Additive, ucm, bmean);
    TS lseries = e.linearizedSeries(false);
    if (e.getDefinition().getSeries().isLog())
    lseries.getValues().log();
    sa.getComponents().addSeries(lseries,
    ec.seasonaladjustment.ComponentType.Series,
    ec.seasonaladjustment.ComponentInformation.Value);
    KalmanEstimation kf = new KalmanEstimation();
    if (!kf.estimate(sa.getComponents(), ucm, bmean))
    return null;
    sa.complete(e, null);
    return sa;
    }
    }*/
    public GaSpecification getSpecification() {
        return m_spec;
    }

    public void setSpecification(final GaSpecification value) {
        m_spec = value.clone();
    }

    public boolean isFreeEstimation() {
        return m_bfreeestimation;
    }

    public void setFreeEstimation(final boolean value) {
        m_bfreeestimation = value;
    }

    public boolean isMeanCorrection() {
        return m_mean;
    }

    public void setMeanCorrection(final boolean value) {
        m_mean = value;
    }
}
