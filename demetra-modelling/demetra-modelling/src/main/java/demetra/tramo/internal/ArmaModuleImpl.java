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
package demetra.tramo.internal;

import demetra.arima.internal.FastKalmanFilter;
import demetra.data.DoubleSequence;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import demetra.design.VisibleForTesting;
import demetra.sarima.HannanRissanen;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.SarmaSpecification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
class ArmaModuleImpl {

    static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(ArmaModuleImpl.class)
    static class Builder {

        private int modelsCount = NMOD;
        private boolean wn = false;
        private int maxP = 3, maxBp = 1, maxQ = 3, maxBq = 1;

        private Builder() {
        }

        public Builder modelsCount(int n) {
            this.modelsCount = n;
            return this;
        }

        public Builder maxP(int p) {
            this.maxP = p;
            return this;
        }

        public Builder maxBp(int bp) {
            this.maxBp = bp;
            return this;
        }

        public Builder maxQ(int q) {
            this.maxQ = q;
            return this;
        }

        public Builder maxBq(int bq) {
            this.maxBq = bq;
            return this;
        }

        public Builder acceptWhiteNoise(boolean ok) {
            this.wn = ok;
            return this;
        }

        public ArmaModuleImpl build() {
            return new ArmaModuleImpl(modelsCount, wn, maxP, maxQ, maxBp, maxBq);
        }
    }

    private FastBIC[] hrModels;
    private final int nmodels;
    private static final int NMOD = 5;
    private final boolean acceptwn;
    private final int maxP, maxBp, maxQ, maxBq;

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
        FastBIC(final DoubleSequence data, final SarimaModel arma) {
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
        double getBIC() {
            return bic;
        }

        /**
         *
         * @return
         */
        SarimaModel getArma() {
            return arma;
        }

        private SarmaSpecification getSpecification() {
            return arma == null ? null : arma.specification().doStationary();
        }

        /**
         *
         * @param data
         * @param specs
         * @return
         */
        static FastBIC[] sort(final DoubleSequence data, final SarmaSpecification[] specs) {
            List<FastBIC> hrs = new ArrayList<FastBIC>();
            for (int i = 0; i < specs.length; ++i) {
                HannanRissanen hr = HannanRissanen.builder().build();
                if (hr.process(data, specs[i])) {
                    SarimaModel m = hr.getModel();
                    if (m.isStable(true)) {
                        FastBIC hrbic = new FastBIC(data, m);
                        hrs.add(hrbic);
                    }
                }
            }
            FastBIC[] ahrs = hrs.toArray(new FastBIC[hrs.size()]);
            Arrays.sort(ahrs);
            return ahrs;
        }

        static SarmaSpecification getPreferredSpecification(FastBIC[] hrs, boolean acceptwn) {
            if (hrs.length == 0) {
                return null;
            }
            if (hrs.length == 1 || acceptwn) {
                return hrs[0].arma.specification().doStationary();
            }
            int idx = 0;
            while (idx < hrs.length && hrs[idx].arma.specification().getParametersCount() == 0) {
                ++idx;
            }
            return hrs[idx].arma.specification().doStationary();
        }

        static void mergeInto(FastBIC[] candidates, FastBIC[] models) {
            int gmod = models.length;
            int nmax = candidates.length;
            if (nmax > gmod) {
                nmax = gmod;
            }
            // insert the new specifications in the old one
            for (int i = 0, icur = 0; i < nmax && icur < gmod; ++i) {
                SarimaModel cur = candidates[i].getArma();
                SarimaSpecification curSpec = cur.specification();
                double bic = candidates[i].getBIC();
                for (int j = icur; j < gmod; ++j) {
                    if (models[j] == null) {
                        models[j] = candidates[i];
                        icur = j + 1;
                        break;
                    } else if (models[j].getArma().specification().equals(curSpec)) {
                        icur = j + 1;
                        break;
                    } else if (models[j].getBIC() > bic) {
                        for (int k = gmod - 1; k > j; --k) {
                            models[k] = models[k - 1];
                        }
                        models[j] = candidates[i];
                        icur = j + 1;
                        break;
                    }
                }
            }
        }
    }

    /**
     *
     * @param nmod
     */
    private ArmaModuleImpl(final int nmod, final boolean acceptwn,
            final int maxP, final int maxQ, final int maxBp, final int maxBq) {
        nmodels = nmod;
        this.acceptwn = acceptwn;
        this.maxP = maxP;
        this.maxQ = maxQ;
        this.maxBp = maxBp;
        this.maxBq = maxBq;
    }

    /**
     *
     */
    private void clear() {
        hrModels = null;
    }

    /**
     *
     * @param idx
     * @return
     */
    SarimaModel Arma(final int idx) {
        return hrModels[idx].getArma();
    }

    /**
     *
     * @param data
     * @param d
     * @param bd
     * @return
     */
    SarmaSpecification select(DoubleSequence data, final int d, final int bd) {
        int idmax = nmodels;
        while (hrModels[idmax - 1] == null || hrModels[idmax - 1].getArma() == null && idmax > 0) {
            --idmax;
        }
        if (idmax == 0) {
            return null;
        } else if (idmax == 1) {
            return hrModels[0].getSpecification();
        }
        SarmaSpecification spec = hrModels[0].getSpecification();
        int nr1 = spec.getP() + spec.getQ(), ns1 = spec.getBp() + spec.getBq();
        int nrr1 = Math.abs(spec.getP() + d - spec.getQ());
        int nss1 = Math.abs(spec.getBp() + bd - spec.getBq());
        double bmax = hrModels[idmax - 1].getBIC() - hrModels[0].getBIC();
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
            SarmaSpecification cur = hrModels[i].getSpecification();
            int nr2 = cur.getP() + cur.getQ(), ns2 = cur.getBp() + cur.getBq();
            int nrr2 = Math.abs(cur.getP() + d - cur.getQ());
            int nss2 = Math.abs(cur.getBp() + bd - cur.getBq());
            double dbic = hrModels[i].getBIC() - hrModels[idpref].getBIC();
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
                double dc = hrModels[i].getBIC() - hrModels[0].getBIC();
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
            if (idpref < nmodels - 1) {
                return hrModels[idpref + 1].getSpecification();
            }
        }
        return hrModels[idpref].getSpecification();

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
    @VisibleForTesting
    public SarmaSpecification process(final DoubleSequence data, final int period, final int d, final int bd, final boolean seas) {
        clear();
        // step I

        SarmaSpecification[] specs = new SarmaSpecification[(maxBp + 1)
                * (maxBq + 1)];
        SarmaSpecification spec = new SarmaSpecification(period);
        SarmaSpecification cur;

        hrModels = new FastBIC[nmodels];

        spec.setP(3);
        spec.setQ(0);

        if (seas) {
            for (int bp = 0, i = 0; bp <= maxBp; ++bp) {
                for (int bq = 0; bq <= maxBq; ++bq) {
                    spec.setBp(bp);
                    spec.setBq(bq);
                    specs[i++] = spec.clone();
                }
            }

            FastBIC[] hrs0 = FastBIC.sort(data, specs);
            if (0 == hrs0.length) {
                for (int i = 0; i < specs.length; ++i) {
                    specs[i].setP(1);
                }
                hrs0 = FastBIC.sort(data, specs);
                if (0 == hrs0.length) {
                    return null;
                }
            }

            cur = FastBIC.getPreferredSpecification(hrs0, acceptwn);
            if (spec.getP() <= maxP) {
                FastBIC.mergeInto(hrs0, hrModels);
            }
        } else {
            cur = spec.clone();
        }

        specs = new SarmaSpecification[(maxP + 1) * (maxQ + 1)];
        for (int p = 0, i = 0; p <= maxP; ++p) {
            for (int q = 0; q <= maxQ; ++q) {
                cur.setP(p);
                cur.setQ(q);
                specs[i++] = cur.clone();
            }
        }

        FastBIC[] hrs1 = FastBIC.sort(data, specs);
        if (0 == hrs1.length) {
            return null;
        }

        cur = FastBIC.getPreferredSpecification(hrs1, acceptwn);
        FastBIC.mergeInto(hrs1, hrModels);

        if (seas) {
            specs = new SarmaSpecification[(maxBp + 1) * (maxBq + 1)];
            for (int bp = 0, i = 0; bp <= maxBp; ++bp) {
                for (int bq = 0; bq <= maxBq; ++bq) {
                    cur.setBp(bp);
                    cur.setBq(bq);
                    specs[i++] = cur.clone();
                }
            }

            FastBIC[] hrs2 = FastBIC.sort(data, specs);
            if (0 == hrs2.length) {
                return null;
            }
            FastBIC.mergeInto(hrs2, hrModels);
        }
        if (!seas) {
            if (hrModels[1] != null && hrModels[0].arma.getParametersCount() == 0 && !acceptwn) {
                return hrModels[1].getSpecification();
            } else {
                return hrModels[0].getSpecification();
            }
        } else {
            return select(data, d, bd);
        }
    }


}
