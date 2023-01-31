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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarmaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ArmaModule {

    static final double NO_BIC = 99999;

    /**
     *
     */
    public static class RegArmaBic implements Comparable<RegArmaBic> {

        //private final RegArimaEstimation<SarimaModel> m_est;
        private final double m_bic;
        private final SarmaSpecification m_spec;

        /**
         *
         * @param data
         * @param spec
         * @param eps
         */
        public RegArmaBic(final DataBlock data, final SarmaSpecification spec, double eps) {
            GlsSarimaMonitor monitor = new GlsSarimaMonitor();
            monitor.setMinimizer(new ProxyMinimizer(new LevenbergMarquardtMethod()));
            monitor.setPrecision(eps);
            monitor.useLogLikelihood(false);
            m_spec = spec.clone();
            RegArimaModel<SarimaModel> model = new RegArimaModel<>(new SarimaModel(spec), data);
            RegArimaEstimation<SarimaModel> est = monitor.process(model);
            if (est != null && monitor.hasConverged()) {
                m_bic = est.statistics(spec.getParametersCount(), 0).BIC2;
            } else {
                m_bic = NO_BIC;
            }
        }

        @Override
        public int compareTo(RegArmaBic o) {
            return Double.compare(m_bic, o.m_bic);
        }

        /**
         *
         * @return
         */
        public double getBIC() {
            return m_bic;
        }

        /**
         *
         * @return
         */
        public SarmaSpecification getSpecification() {
            return m_spec;
        }
    }

    private boolean balanced_, mixed_ = true;
    private RegArmaBic[] m_est;
    private boolean m_bforced = false;
    private int m_nmod = 5;
    private double eps_ = 1e-5;

    public double getEpsilon() {
        return eps_;
    }

    public void setEpsilon(double val) {
        eps_ = val;
    }

    public void setBalanced(boolean balanced) {
        balanced_ = balanced;
    }

    public boolean isBalanced() {
        return balanced_;
    }

    public void setMixed(boolean mixed) {
        mixed_ = mixed;
    }

    public boolean isMixed() {
        return mixed_;
    }

    /**
     *
     */
    public ArmaModule() {
    }

    /**
     *
     * @param nmod
     */
    public ArmaModule(final int nmod) {
        m_nmod = nmod;
    }

    /**
     *
     */
    public void clear() {
        m_est = null;
    }

    /**
     *
     * @return
     */
    public int getCount() {
        return m_est == null ? 0 : m_est.length;
    }

    /**
     *
     * @return
     */
    public RegArmaBic[] getPreferedModels() {
        return m_est;
    }

    /**
     *
     * @return
     */
    public boolean isMA1Forced() {
        return m_bforced;
    }

    private void merge(final RegArmaBic[] mods) {
        if (m_est == null) {
            return;
        }
        int gmod = mods.length;
        int nmax = getCount();
        if (nmax > gmod) {
            nmax = gmod;
        }
        // insert the new specifications in the old one
        for (int i = 0, icur = 0; i < nmax && icur < gmod; ++i) {
            double bic = m_est[i].getBIC();
            for (int j = icur; j < gmod; ++j) {
                if (mods[j] == null) {
                    mods[j] = m_est[i];
                    icur = j + 1;
                    break;
                } else if (mods[j].getSpecification().equals(m_est[i].getSpecification())) {
                    icur = j + 1;
                    break;
                } else if (mods[j].getBIC() > bic) {
                    for (int k = gmod - 1; k > j; --k) {
                        mods[k] = mods[k - 1];
                    }
                    mods[j] = m_est[i];
                    icur = j + 1;
                    break;
                }

            }
        }
    }

    /**
     *
     * @param d
     * @param bd
     * @param npass
     * @return
     */
    public SarmaSpecification select(IReadDataBlock data, final int d, final int bd) {
        int idmax = m_nmod;
        while (m_est[idmax - 1].getBIC() == NO_BIC) {
            --idmax;
        }
        SarmaSpecification spec = m_est[0].getSpecification();
        int nr1 = spec.getP() + spec.getQ(), ns1 = spec.getBP() + spec.getBQ();
        int nrr1 = Math.abs(spec.getP() + d - spec.getQ());
        int nss1 = Math.abs(spec.getBP() + bd - spec.getBQ());
        double bmax = m_est[idmax - 1].getBIC() - m_est[0].getBIC();
        if (bmax < 0.003) {
            bmax = 0.0625;
        } else if (bmax < 0.03) {
            bmax = .25;
        } else {
            bmax = 1;
        }
        double vc11 = 0.01 * bmax;
        double vc2 = 0.0025 * bmax;
        double vc22 = 0.0075 * bmax;

        int idpref = 0;
        int icmod = 0;
        for (int i = 1; i < idmax; ++i) {
            SarmaSpecification cur = m_est[i].getSpecification();
            int nr2 = cur.getP() + cur.getQ(), ns2 = cur.getBP() + cur.getBQ();
            int nrr2 = Math.abs(cur.getP() + d - cur.getQ());
            int nss2 = Math.abs(cur.getBP() + bd - cur.getBQ());
            double dbic = m_est[i].getBIC() - m_est[idpref].getBIC();
            int chk = 0;
            if ((nrr2 < nrr1 || nss2 < nss1) && nr1 == nr2 && ns1 == ns2 && dbic <= vc11 && balanced_) {
                chk = 1;
            } else if (nrr2 < nrr1 && nr2 <= nr1 && ns2 == ns1 && dbic <= vc2
                    && cur.getP() > 0 && cur.getQ() > 0 && balanced_) {
                chk = 2;
            } else if (((nrr2 == 0 && nrr2 < nrr1 && d > 0) || (nss2 == 0
                    && nss2 < nss1 && bd > 0))
                    && nr1 == nr2 && ns1 == ns2 && dbic <= vc11 && balanced_) {
                chk = 3;
            } else if (nrr2 == 0 && nss2 == 0 && dbic < vc2 && balanced_) {
                chk = 4;
            } else if (nr2 > nr1 && nrr2 == 0 && ns2 == ns1 && dbic < vc2 && balanced_) {
                chk = 5;
            } else if (ns2 > ns1 && nss2 == 0 && nr2 == nr1 && dbic < vc2 && balanced_) {
                chk = 6;
            } else if (ns2 < ns1 && ns2 > 0 && nr2 == nr1 && nss2 == 0 && dbic < vc2 && balanced_) {
                chk = 7;
            } else if (i == 1 && nr1 == 0 && nr2 == 1 && ns2 == ns1 && dbic < vc2) {
                chk = 8;
            } else if (nr2 < nr1 && nr2 > 0 && ns2 == ns1 && dbic < vc2) {
                chk = 9;
            } else if (ns2 < ns1 && ns2 > 0 && nr2 == nr1 && dbic < vc2) {
                chk = 10;
            } else if (cur.getP() < spec.getP() && cur.getQ() == spec.getQ()
                    && nr2 > 0 && ns2 == ns1 && dbic < vc22) {
                chk = 11;
            }
            if (chk > 0) {
                ++icmod;
                double dc = m_est[i].getBIC() - m_est[0].getBIC();
                vc11 -= dc;
                vc2 -= dc;
                vc22 -= dc;
                nr1 = nr2;
                ns1 = ns2;
                nrr1 = nrr2;
                nss1 = nss2;
                idpref = i;
                spec = cur.clone();
            }
        }
        if (spec.getParametersCount() == 0) {
            if (idpref < m_nmod - 1) {
                return m_est[idpref + 1].getSpecification().clone();
            }
        }

        return m_est[idpref].getSpecification().clone();

    }

//    /**
//     *
//     * @param maxspec
//     * @return
//     */
//    public void sort(final DataBlock data, final SarmaSpecification maxspec) {
//        int nspecs = (maxspec.getP() + 1) * (maxspec.getQ() + 1)
//                * (maxspec.getBP() + 1) * (maxspec.getBQ() + 1);
//        SarmaSpecification[] specs = new SarmaSpecification[nspecs];
//        for (int p = 0, i = 0; p <= maxspec.getP(); ++p) {
//            for (int q = 0; q <= maxspec.getQ(); ++q) {
//                for (int bp = 0; bp <= maxspec.getBP(); ++bp) {
//                    for (int bq = 0; bq <= maxspec.getBQ(); ++bq) {
//                        SarmaSpecification spec = new SarmaSpecification();
//                        spec.setFrequency(maxspec.getFrequency());
//                        spec.setP(p);
//                        spec.setQ(q);
//                        spec.setBP(bp);
//                        spec.setBQ(bq);
//                        specs[i++] = spec;
//                    }
//                }
//            }
//        }
//
//        sort(data, specs);
//    }
//
    /**
     *
     * @param data
     * @param specs
     * @return
     */
    public int sort(final DataBlock data, final SarmaSpecification[] specs) {
        m_est = new RegArmaBic[specs.length];
        for (int i = 0; i < specs.length; ++i) {
            m_est[i] = new RegArmaBic(data, specs[i], eps_);
        }

        Arrays.sort(m_est);
        for (int i = m_est.length; i > 0; --i) {
            if (m_est[i - 1].getBIC() != NO_BIC) {
                return i;
            }
        }
        return 0;
    }

    /**
     *
     * @param data
     * @param maxspec
     * @param d
     * @param bd
     * @param npass
     * @return
     */
    public SarmaSpecification select(final DataBlock data, final int freq,
            final int rmax, int smax, final int d, final int bd) {
        clear();
        // step I

        SarmaSpecification spec = new SarmaSpecification();
        SarmaSpecification cur = new SarmaSpecification();

        m_est = new RegArmaBic[m_nmod];

        spec.setFrequency(freq);
        if (freq == 2) {
            spec.setP(1);
        } else {
            spec.setP(3);
        }
        spec.setQ(0);

        int nmax = 0;
        List<SarmaSpecification> lspecs0 = new ArrayList<>();
        if (freq != 1) {
            for (int bp = 0, i = 0; bp <= smax; ++bp) {
                for (int bq = 0; bq <= smax; ++bq) {
                    if (mixed_ || (bp == 0 || bq == 0)) {
                        spec.setBP(bp);
                        spec.setBQ(bq);
                        lspecs0.add(spec.clone());
                    }
                }
            }
            SarmaSpecification[] specs0 = lspecs0.toArray(new SarmaSpecification[lspecs0.size()]);

            ArmaModule step0 = new ArmaModule();
            step0.setEpsilon(eps_);
            nmax = step0.sort(data, specs0);
            if (0 == nmax) {
                return null;
            }
            cur = step0.m_est[0].getSpecification().clone();
        } else {
            cur = spec.clone();
        }

        List<SarmaSpecification> lspecs1 = new ArrayList<>();
        for (int p = 0; p <= rmax; ++p) {
            for (int q = 0; q <= rmax; ++q) {
                if (mixed_ || (p == 0 || q == 0)) {
                    cur.setP(p);
                    cur.setQ(q);
                    lspecs1.add(cur.clone());
                }
            }
        }
        SarmaSpecification[] specs1 = lspecs1.toArray(new SarmaSpecification[lspecs1.size()]);

        ArmaModule step1 = new ArmaModule();
        step1.setEpsilon(eps_);
        nmax = step1.sort(data, specs1);
        if (0 == nmax) {
            return null;
        }

        ArmaModule step2 = null;

        cur = step1.m_est[0].getSpecification().clone();
        step1.merge(m_est);

        int spmax = smax, sqmax = smax;
        if (bd == 1) {
            spmax = 0;
        }
        if (freq != 1) {
            List<SarmaSpecification> lspecs2 = new ArrayList<>();
            for (int bp = 0, i = 0; bp <= spmax; ++bp) {
                for (int bq = 0; bq <= sqmax; ++bq) {
                    if (mixed_ || (bp == 0 || bq == 0)) {
                        cur.setBP(bp);
                        cur.setBQ(bq);
                        lspecs2.add(cur.clone());
                    }
                }
            }
            SarmaSpecification[] specs2 = lspecs2.toArray(new SarmaSpecification[lspecs2.size()]);

            step2 = new ArmaModule();
            step2.setEpsilon(eps_);
            if (0 == step2.sort(data, specs2)) {
                return null;
            }
            step2.merge(m_est);
        } else {
            step2 = step1;
        }
        if (freq == 1) {
            if (m_est[0].getSpecification().getParametersCount() == 0) {
                return m_est[1].getSpecification().clone();
            } else {
                return m_est[0].getSpecification().clone();
            }
        } else {
            return select(data, d, bd);
        }
    }
}
