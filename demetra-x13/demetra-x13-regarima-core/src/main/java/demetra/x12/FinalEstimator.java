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
package demetra.x12;

import demetra.data.DataBlock;
import demetra.data.DoubleSeq;
import demetra.design.Development;
import demetra.maths.functions.IParametricMapping;
import static demetra.maths.linearfilters.FilterUtility.checkRoots;
import demetra.regarima.regular.IModelEstimator;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.RegSarimaProcessor;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import static demetra.maths.linearfilters.FilterUtility.checkRoots;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FinalEstimator implements IModelEstimator {

    private final double tsig = 1;
    private static final int MAXD = 2, MAXBD = 1;
    private final double eps;

    public FinalEstimator(double eps) {
        this.eps = eps;
    }

    @Override
    public boolean estimate(RegArimaModelling context) {

        int niter = 0;
        do {
            try {
                IParametricMapping<SarimaModel> mapping = context.getDescription().getArimaComponent().defaultMapping();
                int ndim = mapping.getDim();
                RegSarimaProcessor processor = RegSarimaProcessor.builder()
                        .precision(eps)
//                        .startingPoint(RegSarimaProcessor.StartingPoint.Multiple)
                        .build();
                context.estimate(processor);
                if (ndim == 0) {
                    return true;
                }
                int itest = test(context);
                if (itest == 0) {
                    return true;
                } else if (itest > 1) {
                    return false;
                }
            } catch (RuntimeException err) {
                return false;
            }
        } while (niter++ < 5);
        return false;
    }

    private int test(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        double cval = tsig;
        int nz = desc.getDomain().getLength();
        double cmin = nz <= 150 ? .15 : .1;
        double cmod = .95;
        double bmin = 999;

        SarimaModel m = desc.arima();
        SarimaSpecification spec = m.specification();
        DoubleSeq pm = m.parameters();
        int start = 0, len = spec.getP();
        boolean dpr = checkRoots(pm.extract(start, len), 1 / cmod);// (m.RegularAR.Roots,
        start += len;
        len = spec.getBp();
        boolean dps = checkRoots(pm.extract(start, len), 1 / cmod);// SeasonalAR.Roots,
        start += len;
        len = spec.getQ();
        boolean dqr = checkRoots(pm.extract(start, len), 1 / cmod);// RegularMA.Roots,
        start += len;
        len = spec.getBq();
        boolean dqs = checkRoots(pm.extract(start, len), 1 / cmod);// SeasonalMA.Roots,
        if (!dpr && !dps && !dqr && !dqs) {
            return 0; // nothing to do
        }
        int cpr = 0, cps = 0, cqr = 0, cqs = 0;
        double tmin = cval;
        DataBlock diag = context.getEstimation().getParametersCovariance().diagonal();

        int k = -1;
        if (dpr) {
            k += spec.getP();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    ++cpr;
                    bmin = t;
                }
            }
        }
        if (dps) {
            k += spec.getBp();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cps;
                        bmin = t;
                        cpr = 0;
                    }
                }
            }
        }
        if (dqr) {
            k += spec.getQ();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cqr;
                        bmin = t;
                        cpr = 0;
                        cps = 0;
                    }
                }
            }
        }
        if (dqs) {
            k += spec.getBq();
            double v = Math.abs(pm.get(k));
            double s = diag.get(k);
            if (s > 0) {
                double t = v / Math.sqrt(s);
                if (t < tmin && v < cmin) {
                    if (bmin > t) {
                        ++cqs;
                        cpr = 0;
                        cps = 0;
                        cqr = 0;
                    }
                }
            }
        }

        int nnsig = cpr + cps + cqr + cqs;
        if (nnsig == 0) {
            return 0;
        }

        context.setEstimation(null);
        // reduce the orders
        if (cpr > 0) {
            spec.setP(spec.getP() - cpr);
        } else if (cps > 0) {
            spec.setBp(spec.getBp() - cps);
        } else if (cqr > 0) {
            spec.setQ(spec.getQ() - cqr);
        } else if (cqs > 0) {
            spec.setBq(spec.getBq() - cqs);
        }

        desc.setSpecification(spec);
        return nnsig;
    }

}
