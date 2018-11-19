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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.arima.estimation.ArmaKF;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.ProcessingResult;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;
import ec.tstoolkit.sarima.estimation.HannanRissanen;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ArmaModule implements IPreprocessingModule {

    // returns the first inic value that can be estimated
    static int comespa(final int freq, final int n, final int inic, final int d, final int bd, final boolean seas) {
        for (int i = inic; i > 0; --i) {
            if (checkespa(freq, n, i, d, bd, seas)) {
                return i;
            }
        }
        return 0;
    }

    static boolean checkespa(final int freq, final int nz, final int inic, final int d, final int bd, final boolean seas) {
        SarimaSpecification spec = checkmaxspec(freq, inic, d, bd, seas);
        if (TramoProcessor.autlar(nz, spec) < 0) {
            return false;
        }
        int n = nz - spec.getP() - spec.getFrequency() * spec.getBP();
        spec.setP(0);
        spec.setBP(0);
        return TramoProcessor.autlar(n, spec) >= 0;
    }

    static SarimaSpecification calcmaxspec(final int freq, final int inic, final int d,
            final int bd, final boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(d);
        if (seas) {
            spec.setBD(bd);
        }
        switch (inic) {
            case 1:
                spec.setP(1);
                spec.setQ(1);
                if (seas) {
                    spec.setBP(1);
                    spec.setBQ(1);
                }
                break;
            case 2:
                spec.setP(2);
                spec.setQ(2);
                if (seas) {
                    spec.setBP(1);
                    spec.setBQ(1);
                }
                break;
            case 3:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    spec.setBP(1);
                    spec.setBQ(1);
                }
                break;
            case 4:
                spec.setP(3);
                spec.setQ(3);
                spec.setBP(2);
                spec.setBQ(2);
                break;
        }
//        if (inic <= 3 && bd == 1) {
//            spec.setBP(0);
//        }
        return spec;
    }

    static SarimaSpecification checkmaxspec(final int freq, final int inic, final int d,
            final int bd, final boolean seas) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(d);
        if (seas) {
            spec.setBD(bd);
        }
        switch (inic) {
            case 1:
                spec.setP(1);
                spec.setQ(1);
                if (seas) {
                    if (bd == 0) {
                        spec.setBP(1);
                    }
                    spec.setBQ(1);
                }
                break;
            case 2:
                spec.setP(2);
                spec.setQ(2);
                if (seas) {
                    if (bd == 0) {
                        spec.setBP(1);
                    }
                    spec.setBQ(1);
                }
                break;
            case 3:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    if (bd == 0) {
                        spec.setBP(1);
                    }
                    spec.setBQ(1);
                }
                break;
            case 4:
                spec.setP(3);
                spec.setQ(3);
                if (seas) {
                    spec.setBP(2);
                    spec.setBQ(2);
                }
                break;
        }
//        if (inic <= 3 && bd == 1) {
//            spec.setBP(0);
//        }
        return spec;
    }

    private HRBic[] m_hrs;
    private boolean m_bforced = false;
    private final int m_nmod;
    private static final int NMOD = 5;
    private boolean acceptwn = false;

    @Override
    public ProcessingResult process(ModellingContext context) {
        SarimaSpecification curspec = context.description.getSpecification();
        int m;
        switch (curspec.getFrequency()){
            case 2:
                m=1;
                break;
            case 3:
                m=2;
                break;
            default:
                m=3;
        }
        int inic = comespa(curspec.getFrequency(), context.description.getEstimationDomain().getLength(),
                m, curspec.getD(), curspec.getBD(), context.hasseas);
        if (inic == 0) {
            curspec.airline(context.hasseas);
            context.description.setSpecification(curspec);
            context.estimation = null;
            return ProcessingResult.Changed;
        }
        SarimaSpecification maxspec = calcmaxspec(context.description.getFrequency(),
                inic, curspec.getD(), curspec.getBD(), context.hasseas);
        DataBlock res = context.description.getOlsResiduals();
        HannanRissanen hr = tramo(res, maxspec.doStationary(), maxspec.getD(), maxspec.getBD(), context.hasseas);
        for (int i = 0; i < m_hrs.length; ++i) {
            addModelInfo(context, m_hrs[i]);
        }
        if (hr == null) {
            context.description.setAirline(context.hasseas);
            context.estimation = null;
            return ProcessingResult.Failed;
        }
        SarmaSpecification rsltSpec = hr.getSpec();
        if (rsltSpec.equals(curspec.doStationary())) {
            return ProcessingResult.Unchanged;
        }
        curspec.copy(rsltSpec);
        context.description.setSpecification(curspec);
        context.estimation = null;
        return ProcessingResult.Changed;
    }

    /**
     *
     */
    public static class HRBic implements Comparable<HRBic> {

        private final HannanRissanen m_hr;
        private final double m_bic;

        /**
         *
         * @param hr
         */
        public HRBic(final HannanRissanen hr) {
            m_hr = hr;
            ArmaKF fkf = new ArmaKF(hr.getModel());
            m_bic = fkf.fastProcessing(hr.getData(), hr.getSpec().getParametersCount());
        }

        @Override
        public int compareTo(HRBic o) {
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
        public HannanRissanen getHR() {
            return m_hr;
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
     * @return
     */
    public HRBic[] getPreferedModels() {
        return m_hrs;
    }

    /**
     *
     * @param idx
     * @return
     */
    public HannanRissanen HR(final int idx) {
        return m_hrs[idx].getHR();
    }

    /**
     *
     * @return
     */
    public boolean isMA1Forced() {
        return m_bforced;
    }

    private void merge(final HRBic[] mods) {
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
            HannanRissanen hrcur = m_hrs[i].getHR();
            double bic = m_hrs[i].getBIC();
            for (int j = icur; j < gmod; ++j) {
                if (mods[j] == null) {
                    mods[j] = m_hrs[i];
                    icur = j + 1;
                    break;
                } else if (mods[j].getHR().getSpec().equals(hrcur.getSpec())) {
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
    public HannanRissanen select(IReadDataBlock data, final int d, final int bd) {
        int idmax = m_nmod;
        while (m_hrs[idmax - 1] == null || m_hrs[idmax - 1].getHR() == null && idmax > 0) {
            --idmax;
        }
        if (idmax == 0) {
            return null;
        } else if (idmax == 1) {
            return m_hrs[0].getHR();
        }
        SarmaSpecification spec = m_hrs[0].getHR().getSpec();
        int nr1 = spec.getP() + spec.getQ(), ns1 = spec.getBP() + spec.getBQ();
        int nrr1 = Math.abs(spec.getP() + d - spec.getQ());
        int nss1 = Math.abs(spec.getBP() + bd - spec.getBQ());
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
            SarmaSpecification cur = m_hrs[i].getHR().getSpec();
            int nr2 = cur.getP() + cur.getQ(), ns2 = cur.getBP() + cur.getBQ();
            int nrr2 = Math.abs(cur.getP() + d - cur.getQ());
            int nss2 = Math.abs(cur.getBP() + bd - cur.getBQ());
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
                return m_hrs[idpref + 1].getHR();
            }
        }
        return m_hrs[idpref].getHR();

    }

    /**
     *
     * @param data
     * @param maxspec
     * @return
     */
    public int sort(final IReadDataBlock data, final SarmaSpecification maxspec) {
        int nspecs = (maxspec.getP() + 1) * (maxspec.getQ() + 1)
                * (maxspec.getBP() + 1) * (maxspec.getBQ() + 1);
        SarmaSpecification[] specs = new SarmaSpecification[nspecs];
        for (int p = 0, i = 0; p <= maxspec.getP(); ++p) {
            for (int q = 0; q <= maxspec.getQ(); ++q) {
                for (int bp = 0; bp <= maxspec.getBP(); ++bp) {
                    for (int bq = 0; bq <= maxspec.getBQ(); ++bq) {
                        SarmaSpecification spec = new SarmaSpecification();
                        spec.setFrequency(maxspec.getFrequency());
                        spec.setP(p);
                        spec.setQ(q);
                        spec.setBP(bp);
                        spec.setBQ(bq);
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
    public int sort(final IReadDataBlock data, final SarmaSpecification[] specs) {
        m_hrs = null;
        HRBic[] hrs = new HRBic[specs.length];
        int n = 0;
        for (int i = 0; i < specs.length; ++i) {
            HannanRissanen hr = new HannanRissanen();
            if (hr.process(data, specs[i])) {
                SarimaModel m = hr.getModel();
                if (!m.adjustSpecification() && m.isStable(true)) {
                    HRBic hrbic = new HRBic(hr);
                    hrs[n++] = hrbic;
                }
            }
        }
        if (n == 0) {
            return 0;
        }

        m_hrs = new HRBic[n];
        for (int i = 0; i < n; ++i) {
            m_hrs[i] = hrs[i];
        }
        Arrays.sort(m_hrs);
        return n;
    }

    private SarmaSpecification getPreferredSpecification() {
        if (m_hrs.length == 1) {
            return m_hrs[0].m_hr.getSpec().clone();
        }
        int idx = 0;
        while (idx < m_hrs.length && m_hrs[idx].m_hr.getSpec().getParametersCount() == 0) {
            ++idx;
        }
        return m_hrs[idx].m_hr.getSpec().clone();
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
    public HannanRissanen tramo(final IReadDataBlock data,
            final SarmaSpecification maxspec, final int d, final int bd, final boolean seas) {
        clear();
        // step I
        int gpr = maxspec.getP(), gqr = maxspec.getQ(), gps = maxspec.getBP(), gqs = maxspec.getBQ();

        SarmaSpecification[] specs = new SarmaSpecification[(gps + 1)
                * (gqs + 1)];
        SarmaSpecification spec = new SarmaSpecification();
        SarmaSpecification cur;

        m_hrs = new HRBic[m_nmod];

        spec.setFrequency(maxspec.getFrequency());
        spec.setP(3);
        spec.setQ(0);

        int nmax;

        if (seas) {
            for (int bp = 0, i = 0; bp <= gps; ++bp) {
                for (int bq = 0; bq <= gqs; ++bq) {
                    spec.setBP(bp);
                    spec.setBQ(bq);
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
            if (spec.getP() <= maxspec.getP()) {
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
                    cur.setBP(bp);
                    cur.setBQ(bq);
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
            if (m_hrs[1] != null && m_hrs[0].getHR().getSpec().getParametersCount() == 0 && !acceptwn) {
                return m_hrs[1].getHR();
            } else {
                return m_hrs[0].getHR();
            }
        } else {
            return select(data, d, bd);
        }
    }

    private void addModelInfo(ModellingContext context, HRBic hr) {
//        if (context.processingLog != null && hr != null) {
//            StringBuilder builder = new StringBuilder();
//            SarmaSpecification spec = hr.getHR().getSpec();
//            builder.append(spec).append(": Bic = ").append(hr.m_bic);
//            context.processingLog.add(ProcessingInformation.info(ARMA,
//                    ArmaModule.class.getName(), builder.toString(), null));
//        }
    }

    private static final String ARMA = "Arma identification";
}
