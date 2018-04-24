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
package demetra.tramo;

import demetra.arima.internal.FastKalmanFilter;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.linearmodel.LeastSquaresResults;
import demetra.linearmodel.LinearModel;
import demetra.linearmodel.Ols;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.IArmaModule;
import demetra.sarima.HannanRissanen;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.SarmaSpecification;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ArmaModule implements IArmaModule {

    // returns the first inic value that can be estimated
    static int comespa(final int freq, final int n, final int inic, final int d, final int bd, final boolean seas) {
        for (int i = inic; i > 1; --i) {
            if (checkespa(freq, n, i, d, bd, seas)) {
                return i;
            }
        }
        return 0;
    }

    static boolean checkespa(final int freq, final int nz, final int inic, final int d, final int bd, final boolean seas) {
        SarimaSpecification spec = checkmaxspec(freq, inic, d, bd, seas);
        if (TramoUtility.autlar(nz, spec) < 0) {
            return false;
        }
        int n = nz - spec.getP() - spec.getPeriod() * spec.getBp();
        spec.setP(0);
        spec.setBp(0);
        return TramoUtility.autlar(n, spec) >= 0;
    }

    static SarimaSpecification calcmaxspec(final int freq, final int inic, final int d,
            final int bd, final boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(d);
        if (seas) {
            spec.setBd(bd);
        }
        switch (inic) {
            case 1:
                spec.setP(1);
                spec.setQ(1);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
                break;
            case 2:
                spec.setP(2);
                spec.setQ(2);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
                break;
            case 3:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    spec.setBp(1);
                    spec.setBq(1);
                }
                break;
            case 4:
                spec.setP(3);
                spec.setQ(3);
                spec.setBp(2);
                spec.setBq(2);
                break;
        }
//        if (inic <= 3 && bd == 1) {
//            spec.setBp(0);
//        }
        return spec;
    }

    static SarimaSpecification checkmaxspec(final int freq, final int inic, final int d,
            final int bd, final boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(d);
        if (seas) {
            spec.setBd(bd);
        }
        switch (inic) {
            case 1:
                spec.setP(1);
                spec.setQ(1);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
                break;
            case 2:
                spec.setP(2);
                spec.setQ(2);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
                break;
            case 3:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    if (bd == 0) {
                        spec.setBp(1);
                    }
                    spec.setBq(1);
                }
                break;
            case 4:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    spec.setBp(2);
                    spec.setBq(2);
                }
                break;
        }
//        if (inic <= 3 && bd == 1) {
//            spec.setBp(0);
//        }
        return spec;
    }

    private FastBIC[] m_hrs;
    private boolean m_bforced = false;
    private final int m_nmod;
    private static final int NMOD = 5;
    private boolean acceptwn = false;

    /**
     *
     */
    static class FastBIC implements Comparable<FastBIC> {

        private final SarimaModel arma;
        private final double bic;

        /**
         *
         * @param hr
         */
        public FastBIC(final DoubleSequence data, final SarimaModel arma) {
            this.arma = arma;
            FastKalmanFilter fkf = new FastKalmanFilter(arma);
            bic = fkf.fastProcessing(data, arma.getParametersCount());
        }

        @Override
        public int compareTo(FastBIC o) {
            return Double.compare(bic, o.bic);
        }

        /**
         *
         * @return
         */
        public double getBIC() {
            return bic;
        }

        /**
         *
         * @return
         */
        public SarimaModel getArma() {
            return arma;
        }

        private SarmaSpecification getSpecification() {
            return arma == null ? null : arma.specification().doStationary();
        }
    }

    /**
     *
     */
    public ArmaModule() {
        m_nmod = NMOD;
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
        m_hrs = null;
        m_bforced = false;
    }

    public void setAcceptingWhiteNoise(boolean wn) {
        this.acceptwn = wn;
    }

    public boolean isAcceptingWhiteNoise() {
        return this.acceptwn;
    }

    /**
     *
     * @return
     */
    public int getCount() {
        return m_hrs == null ? 0 : m_hrs.length;
    }

    /**
     *
     * @param idx
     * @return
     */
    public SarimaModel Arma(final int idx) {
        return m_hrs[idx].getArma();
    }

    /**
     *
     * @return
     */
    public boolean isMA1Forced() {
        return m_bforced;
    }

    private void merge(final FastBIC[] mods) {
        if (m_hrs == null) {
            return;
        }
        int gmod = mods.length;
        int nmax = getCount();
        if (nmax > gmod) {
            nmax = gmod;
        }
        // insert the new specifications in the old one
        for (int i = 0, icur = 0; i < nmax && icur < gmod; ++i) {
            SarimaModel cur = m_hrs[i].getArma();
            SarimaSpecification curSpec = cur.specification();
            double bic = m_hrs[i].getBIC();
            for (int j = icur; j < gmod; ++j) {
                if (mods[j] == null) {
                    mods[j] = m_hrs[i];
                    icur = j + 1;
                    break;
                } else if (mods[j].getArma().specification().equals(curSpec)) {
                    icur = j + 1;
                    break;
                } else if (mods[j].getBIC() > bic) {
                    for (int k = gmod - 1; k > j; --k) {
                        mods[k] = mods[k - 1];
                    }
                    mods[j] = m_hrs[i];
                    icur = j + 1;
                    break;
                }
            }
        }
    }

    /**
     *
     * @param data
     * @param d
     * @param bd
     * @return
     */
    private SarimaModel select(DoubleSequence data, final int d, final int bd) {
        int idmax = m_nmod;
        while (m_hrs[idmax - 1] == null || m_hrs[idmax - 1].getArma() == null && idmax > 0) {
            --idmax;
        }
        if (idmax == 0) {
            return null;
        } else if (idmax == 1) {
            return m_hrs[0].getArma();
        }
        SarmaSpecification spec = m_hrs[0].getSpecification();
        int nr1 = spec.getP() + spec.getQ(), ns1 = spec.getBp() + spec.getBq();
        int nrr1 = Math.abs(spec.getP() + d - spec.getQ());
        int nss1 = Math.abs(spec.getBp() + bd - spec.getBq());
        double bmax = m_hrs[idmax - 1].getBIC() - m_hrs[0].getBIC();
        if (bmax < 0.003) {
            bmax = 0.0625;
        } else if (bmax < 0.03) {
            bmax = .25;
        } else {
            bmax = 1;
        }
        // double vc11=(d+bd <= 1) ? 0.01*bmax : 0.018*bmax;
        double vc11 = 0.01 * bmax;
        double vc2 = 0.0025 * bmax;
        double vc22 = 0.0075 * bmax;

        int idpref = 0;
        int icmod = 0;
        for (int i = 1; i < idmax; ++i) {
            SarmaSpecification cur = m_hrs[i].getSpecification();
            int nr2 = cur.getP() + cur.getQ(), ns2 = cur.getBp() + cur.getBq();
            int nrr2 = Math.abs(cur.getP() + d - cur.getQ());
            int nss2 = Math.abs(cur.getBp() + bd - cur.getBq());
            double dbic = m_hrs[i].getBIC() - m_hrs[idpref].getBIC();
            if (((nrr2 < nrr1 || nss2 < nss1) && nr1 == nr2 && ns1 == ns2 && dbic <= vc11)
                    || (nrr2 < nrr1 && nr2 <= nr1 && ns2 == ns1 && dbic <= vc2
                    && cur.getP() > 0 && cur.getQ() > 0)
                    || (((nrr2 == 0 && nrr2 < nrr1 && d > 0) || (nss2 == 0
                    && nss2 < nss1 && bd > 0))
                    && nr1 == nr2 && ns1 == ns2 && dbic <= vc11)
                    || (nrr2 == 0 && nss2 == 0 && dbic < vc2)
                    || (nr2 > nr1 && nrr2 == 0 && ns2 == ns1 && dbic < vc2)
                    || (i == 1 && nr1 == 0 && nr2 == 1 && ns2 == ns1 && dbic < vc2)
                    || (ns2 > ns1 && nss2 == 0 && nr2 == nr1 && dbic < vc2)
                    || (nr2 < nr1 && nr2 > 0 && ns2 == ns1 && dbic < vc2)
                    || (cur.getP() < spec.getP() && cur.getQ() == spec.getQ()
                    && nr2 > 0 && ns2 == ns1 && dbic < vc22)
                    || (ns2 < ns1 && ns2 > 0 && nr2 == nr1 && nss2 == 0 && dbic < vc2)
                    || (ns2 < ns1 && ns2 > 0 && nr2 == nr1 && dbic < vc2)) {
                ++icmod;
                double dc = m_hrs[i].getBIC() - m_hrs[0].getBIC();
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
                return m_hrs[idpref + 1].getArma();
            }
        }
        return m_hrs[idpref].getArma();

    }

    /**
     *
     * @param data
     * @param maxspec
     * @return
     */
    public int sort(final DoubleSequence data, final SarmaSpecification maxspec) {
        int nspecs = (maxspec.getP() + 1) * (maxspec.getQ() + 1)
                * (maxspec.getBp() + 1) * (maxspec.getBq() + 1);
        SarmaSpecification[] specs = new SarmaSpecification[nspecs];
        for (int p = 0, i = 0; p <= maxspec.getP(); ++p) {
            for (int q = 0; q <= maxspec.getQ(); ++q) {
                for (int bp = 0; bp <= maxspec.getBp(); ++bp) {
                    for (int bq = 0; bq <= maxspec.getBq(); ++bq) {
                        SarmaSpecification spec = new SarmaSpecification(maxspec.getPeriod());
                        spec.setP(p);
                        spec.setQ(q);
                        spec.setBp(bp);
                        spec.setBq(bq);
                        specs[i++] = spec;
                    }
                }
            }
        }

        return sort(data, specs);
    }

    /**
     *
     * @param data
     * @param specs
     * @return
     */
    public int sort(final DoubleSequence data, final SarmaSpecification[] specs) {
        m_hrs = null;
        FastBIC[] hrs = new FastBIC[specs.length];
        int n = 0;
        for (int i = 0; i < specs.length; ++i) {
            HannanRissanen hr = HannanRissanen.builder().build();
            if (hr.process(data, specs[i])) {
                SarimaModel m = hr.getModel();
                if (m.isStable(true)) {
                    FastBIC hrbic = new FastBIC(data, m);
                    hrs[n++] = hrbic;
                }
            }
        }
        if (n == 0) {
            return 0;
        }

        m_hrs = new FastBIC[n];
        for (int i = 0; i < n; ++i) {
            m_hrs[i] = hrs[i];
        }
        Arrays.sort(m_hrs);
        return n;
    }

    private SarmaSpecification getPreferredSpecification() {
        if (m_hrs.length == 1) {
            return m_hrs[0].arma.specification().doStationary();
        }
        int idx = 0;
        while (idx < m_hrs.length && m_hrs[idx].arma.specification().getParametersCount() == 0) {
            ++idx;
        }
        return m_hrs[idx].arma.specification().doStationary();
    }
    
    private SarmaSpecification maxSpec;
    
    public SarmaSpecification getMaxSpec(){
        return maxSpec;
    }

    public void setMaxSpec( SarmaSpecification maxSpec){
        this.maxSpec=maxSpec;
    }
    
    /**
     *
     * @param data
     * @param maxspec
     * @param d
     * @param bd
     * @param seas
     * @return
     */
    private SarimaModel tramo(final DoubleSequence data, final int period, final int d, final int bd, final boolean seas) {
        clear();
        // step I
        int gpr = maxSpec.getP(), gqr = maxSpec.getQ(), gps = maxSpec.getBp(), gqs = maxSpec.getBq();

        SarmaSpecification[] specs = new SarmaSpecification[(gps + 1)
                * (gqs + 1)];
        SarmaSpecification spec = new SarmaSpecification(period);
        SarmaSpecification cur;

        m_hrs = new FastBIC[m_nmod];

        spec.setP(3);
        spec.setQ(0);

        int nmax;

        if (seas) {
            for (int bp = 0, i = 0; bp <= gps; ++bp) {
                for (int bq = 0; bq <= gqs; ++bq) {
                    spec.setBp(bp);
                    spec.setBq(bq);
                    specs[i++] = spec.clone();
                }
            }

            ArmaModule step0 = new ArmaModule();
            nmax = step0.sort(data, specs);
            if (0 == nmax) {
                for (int i = 0; i < specs.length; ++i) {
                    specs[i].setP(1);
                    nmax = step0.sort(data, specs);
                }
                if (0 == nmax) {
                    return null;
                }
            }

            cur = step0.getPreferredSpecification();
            if (spec.getP() <= maxSpec.getP()) {
                step0.merge(m_hrs);
            }
        } else {
            cur = spec.clone();
        }

        specs = new SarmaSpecification[(gpr + 1) * (gqr + 1)];
        for (int p = 0, i = 0; p <= gpr; ++p) {
            for (int q = 0; q <= gqr; ++q) {
                cur.setP(p);
                cur.setQ(q);
                specs[i++] = cur.clone();
            }
        }

        ArmaModule step1 = new ArmaModule();
        nmax = step1.sort(data, specs);
        if (0 == nmax) {
            return null;
        }

        ArmaModule step2;

        cur = step1.getPreferredSpecification();
        step1.merge(m_hrs);

        if (seas) {
            specs = new SarmaSpecification[(gps + 1) * (gqs + 1)];
            for (int bp = 0, i = 0; bp <= gps; ++bp) {
                for (int bq = 0; bq <= gqs; ++bq) {
                    cur.setBp(bp);
                    cur.setBq(bq);
                    specs[i++] = cur.clone();
                }
            }

            step2 = new ArmaModule();
            if (0 == step2.sort(data, specs)) {
                return null;
            }
            step2.merge(m_hrs);
        }
        if (!seas) {
            if (m_hrs[1] != null && m_hrs[0].arma.getParametersCount() == 0 && !acceptwn) {
                return m_hrs[1].arma;
            } else {
                return m_hrs[0].arma;
            }
        } else {
            return select(data, d, bd);
        }
    }

//    @Override
    public SarimaSpecification process(RegArimaModel<SarimaModel> regarima, boolean seas) {
        SarimaSpecification curSpec = regarima.arima().specification();
        int inic = comespa(curSpec.getPeriod(), regarima.getObservationsCount(), 3, curSpec.getD(), curSpec.getBd(), seas);
        if (inic == 0) {
            curSpec.airline(seas);
            return curSpec;
        }
        SarimaSpecification maxspec = calcmaxspec(curSpec.getPeriod(), inic, curSpec.getD(), curSpec.getBd(), seas);
        LinearModel lm = regarima.differencedModel().asLinearModel();
        Ols ols = new Ols();
        LeastSquaresResults lsr = ols.compute(lm);
        DataBlock res = lm.calcResiduals(lsr.getCoefficients());
        setMaxSpec(maxspec.doStationary());
        SarimaModel model = tramo(res, maxspec.getPeriod(), maxspec.getD(), maxspec.getBd(), seas);
        if (model == null) {
            curSpec.airline(seas);
            return curSpec;
        } else {
            SarimaSpecification nspec = model.specification();
            nspec.setD(curSpec.getD());
            nspec.setBd(curSpec.getBd());
            return nspec;
        }
    }
    
    @Override
    public SarmaSpecification process(DoubleSequence data, int period, int d, int bd, boolean seas) {
        SarimaModel model = tramo(data, period, d, bd, seas);
        if (model == null)
            return null;
        else
            return model.specification().doStationary();
    }
}
