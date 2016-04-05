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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.realfunctions.IFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.SsfAlgorithm;
import ec.tstoolkit.ssf.SsfComposite;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfFunction;
import ec.tstoolkit.ssf.SsfFunctionInstance;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.Arrays2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class MixedAirlineMonitor {

    private TsData m_series;
    private int m_best = -1;
    private HashSet<String> m_computed = new HashSet<>();

    public boolean process(final TsData series, final MaSpecification spec) {
        if (series == null) {
            return false;
        }
        m_series = series;
        m_computed.clear();
        m_models.clear();
        m_best = -1;
        int freq = series.getFrequency().intValue();
        MixedAirlineModel m = new MixedAirlineModel();
        m.setNoisyPeriods(Arrays2.EMPTY_INT_ARRAY);
        if (spec.airline != null) {
            m.setAirline(spec.airline);
        } else {
            m.setFrequency(freq);
        }
        MixedEstimation rslt = estimate(m);
        if (rslt == null)
            return false;
        SarimaModel airline=rslt.model.getAirline();
        m_models.add(rslt);
        double refll = rslt.ll.getLogLikelihood() + 2;

        m_best = 0;
        if (spec.noisyPeriods != null || spec.allPeriods) {
            m = new MixedAirlineModel();
            m.setAirline(airline);
            if (spec.allPeriods) {
                int[] np = new int[freq];
                for (int i = 0; i < freq; ++i) {
                    np[i] = i;

                }
                m.setNoisyPeriods(np);
            } else {
                m.setNoisyPeriods(spec.noisyPeriods);
                
            }
            rslt = estimate(m);
            if (rslt != null) {
                m_models.add(rslt);
                double ll = rslt.ll.getLogLikelihood();
                if (ll > refll) {
                    m_best = 1;
                } else {
                    m_best = 0;
                }
                refll = ll;
                return true;
            } else {
                return false;
            }

        } else {
            boolean switched;

            int iter = 0;
            boolean[] noisy = new boolean[freq];
            do {
                switched = false;
                for (int i = 0; i < freq; ++i) {
                    noisy[i] = !noisy[i];
                    m = new MixedAirlineModel();
                    m.setAirline(airline.clone());
                    int[] noisyPeriods = buildNoisyPeriods(noisy);
                    boolean success = false;
                    if (noisyPeriods != null) {
                        m.setNoisyPeriods(noisyPeriods);
                        String name = m.toString();
                        if (!m_computed.contains(name)) {
                            m_computed.add(name);
                            rslt = estimate(m);
                            if (rslt != null) {
                                m_models.add(rslt);
                                double ll = rslt.ll.getLogLikelihood();
                                if (ll > refll) {
                                    m_best = m_models.size() - 1;
                                    refll = ll;
                                    switched = true;
                                    success = true;
                                }
                            }
                        }
                    }
                    if (!success) {
                        noisy[i] = !noisy[i];
                    }
                }
            } while (iter++ <= 5 && switched);

//            do {
//                switched = false;
//                int ibest=-1;
//                for (int i = 0; i < freq; ++i) {
//                    noisy[i] = !noisy[i];
//                    m = new MixedAirlineModel();
//                    m.setAirline(airline.clone());
//                    int[] noisyPeriods = buildNoisyPeriods(noisy);
//                    if (noisyPeriods != null) {
//                        m.setNoisyPeriods(noisyPeriods);
//                        String name = m.toString();
//                        if (!m_computed.contains(name)) {
//                            m_computed.add(name);
//                            rslt = estimate(m);
//                            if (rslt != null) {
//                                m_models.add(rslt);
//                                double ll = rslt.ll.getLogLikelihood();
//                                if (ll > refll) {
//                                    m_best = m_models.size() - 1;
//                                    ibest=i;
//                                    refll = ll;
//                                    switched = true;
//                                }
//                            }
//                        }
//                    }
//                    noisy[i] = !noisy[i];
//                }
//                if (ibest>=0)
//                noisy[ibest]=!noisy[ibest];
//            } while (iter++ <= 5 && switched);
            
            return true;
        }
    }

    public MixedEstimation getBestModel() {
        if (m_best < 0) {
            return null;
        }
        return m_models.get(m_best);
    }

    public int getBestModelPosition() {
        return m_best;
    }

    private MixedEstimation estimate(MixedAirlineModel model) {
        try {
            if (model.getNoisyPeriods().length > 0) {
                model.setNoisyPeriodsVariance(1);
            }
            IFunctionMinimizer fmin = new ProxyMinimizer(new LevenbergMarquardtMethod());
//            IFunctionMinimizer fmin = new ProxyMinimizer(new ec.tstoolkit.maths.realfunctions.minpack.LevenbergMarquardtMinimizer()); 
            MixedAirlineMapper mapper = new MixedAirlineMapper(model);
            SsfFunction<ISsf> fn = buildFunction(model, mapper);

            boolean converged = fmin.minimize(fn, fn.evaluate(mapper.map(model.makeSsf())));
            SsfFunctionInstance<ISsf> rfn = (SsfFunctionInstance<ISsf>) fmin.getResult();

            MixedEstimation rslt = new MixedEstimation();
            rslt.ll = rfn.getLikelihood();
            SsfComposite c = (SsfComposite) rfn.ssf;
            rslt.model = ((MixedAirlineCompositeModel) c.getCompositeModel()).toModel();
            rslt.model.stabilize();
            return rslt;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private SsfFunction<ISsf> buildFunction(
            MixedAirlineModel model, MixedAirlineMapper mapper) {
        SsfData data = new SsfData(m_series.internalStorage(), null);
        SsfAlgorithm<ISsf> alg = new SsfAlgorithm<>();
        // alg.useSsq(ssq);
        SsfFunction<ISsf> eval = new SsfFunction<>(
                new SsfModel(model.makeSsf(), data, null, null), mapper, alg, false, true);
        return eval;
    }
    private ArrayList<MixedEstimation> m_models = new ArrayList<>();

    private int[] buildNoisyPeriods(boolean[] noisy) {
        int n = 0;
        for (int i = 0; i < noisy.length; ++i) {
            if (noisy[i]) {
                ++n;
            }
        }
        if (n == 0) {
            return null;
        }
        int[] p = new int[n];
        for (int i = 0, j = 0; j < n; ++i) {
            if (noisy[i]) {
                p[j++] = i;
            }
        }
        return p;
    }

    public static class MixedEstimation {

        public DiffuseConcentratedLikelihood ll;
        public MixedAirlineModel model;
    }

    public List<MixedEstimation> getAllResults() {
        return m_models;
    }
}
