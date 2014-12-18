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

import ec.tstoolkit.arima.ArimaException;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.SarimaFixedMapping;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TramoModelEstimator extends RegArimaEstimator {

    public static final double TRAMO_EPS = 1e-4;

     public TramoModelEstimator(IParametricMapping<SarimaModel> mapping) {
        super(mapping);
        eps_=TRAMO_EPS;
        super.setStartingPoint(StartingPoint.HannanRissanen);
    }

     @Override
    protected void computepvar(GlsSarimaMonitor monitor, RegArimaEstimation<SarimaModel> rslt) {

        int n = rslt.likelihood.getDegreesOfFreedom(true, mapping_.getDim());
        Matrix information = monitor.getObservedInformation(n);
        if (information == null) {
            return;
        }
        pcov_ = SymmetricMatrix.inverse(information);
        if (pcov_ == null) {
            return;
        }
        // inflate pcov_, if need be
        if (monitor.getMapping() instanceof SarimaFixedMapping) {
            SarimaFixedMapping mapping = (SarimaFixedMapping) monitor.getMapping();
            pcov_ = mapping.expandCovariance(pcov_);
        }
        calcDefVar(pcov_, n, rslt.model.getArima());

    }

    private int findFree(int pos, boolean[] fixed) {
        if (fixed == null) {
            return pos;
        } else {
            int cur = 0;
            while (cur < fixed.length) {
                if (!fixed[cur]) {
                    if (pos == 0) {
                        return cur;
                    } else {
                        --pos;
                    }
                }
                ++cur;
            }
            return -1;
        }
    }

    private void calcDefVar(final Matrix var, final double denom,
            final SarimaModel arima) {

        try {
            boolean[] fixed = null;
            if (mapping_ instanceof SarimaFixedMapping) {
                fixed = ((SarimaFixedMapping) mapping_).getFixedItems();
            }

            SarimaSpecification spec = arima.getSpecification();
            int np = spec.getParametersCount();
            int nf = 0;
            if (fixed != null) {
                for (int i = 0; i < fixed.length; ++i) {
                    if (fixed[i]) {
                        ++nf;
                    }
                }
            }

            // no free parameter
            if (np - nf == 0) {
                return;
            }
            // only 1 free parameter
            if (np - nf == 1) {
                int idx = findFree(0, fixed);
                double p = arima.getParameter(idx);
                var.set(idx, idx, (1 - p * p) / denom);
                return;
            }
            if (spec.isAirline(true) && nf == 0
                    && (Math.abs(arima.theta(1)) >= .9 || Math.abs(arima.btheta(1)) >= .9)) {
                double q = arima.theta(1), bq = arima.btheta(1);
                Matrix curv = new Matrix(2, 2);
                curv.set(0, 0, 1 / (1 - q * q));
                curv.set(1, 1, 1 / (1 - bq * bq));
                double qc = Math.pow(-q, spec.getFrequency() - 1);
                curv.set(0, 1, qc / (1 - q * qc * bq)); // -q*qc = (-q)**freq
                curv.set(1, 0, curv.get(0, 1));
                curv.mul(denom);

                Matrix newvar = SymmetricMatrix.inverse(curv);
                var.set(0, 0, newvar.get(0, 0));
                var.set(1, 0, newvar.get(1, 0));
                var.set(0, 1, var.get(1, 0));
                var.set(1, 1, newvar.get(1, 1));
                return;
            }

//
//        if (spec.getP() == 0 && spec.getBP() == 0 && spec.getQ() == 1
//                && spec.getBQ() == 1) {
//        } else if (spec.getBP() <= 1 && spec.getBQ() == 1) {
//            double bq = arima.btheta(1);
//            if (Math.abs(bq) >= 0.9) {
//                // index of bq
//                int iqs = spec.getP() + spec.getBP() + spec.getQ();
//                // case BP == 0
//                if (spec.getBP() == 0) {
//                    if (fixed == null || !fixed[iqs]) {
//                        var.set(iqs, iqs, (1 - bq * bq) / denom);
//                        for (int i = 0; i < var.getRowsCount(); ++i) {
//                            if (i != iqs) {
//                                var.set(i, iqs, 0);
//                                var.set(iqs, i, 0);
//                            }
//                        }
//                    }
//                } else // BP=1
//                {
//                    double bp = arima.bphi(1);
//                    // index of bp
//                    int ips = spec.getP();
//                    if (fixed == null || !fixed[iqs]) {
//                        var.set(iqs, iqs, (1 - bq * bq) * (1 - bp * bq)
//                                * (1 - bp * bq) / (denom * (bp - bq) * (bp - bq)));
//                        var.set(ips, iqs, -(1 - bp * bq) * (1 - bp * bp)
//                                * (1 - bq * bq) / (denom * (bp - bq) * (bp - bq)));
//                        var.set(iqs, ips, var.get(ips, iqs));
//                        for (int i = 0; i < var.getRowsCount(); ++i) {
//                            if (i != iqs && i != ips) {
//                                var.set(i, iqs, 0);
//                                var.set(iqs, i, 0);
//                            }
//                        }
//                    }
//                }
//            }
//        } 

//        int icur=spec.getP()+spec.getQ()+spec.getBP();
//        if (! fixed[icur] && spec.getP() <= 1 && spec.getQ() == 1 && Math.abs(arima.))
//        if () {
//            double q = arima.theta(1);
//            if (Math.abs(q) >= 0.9) {
//                // index of q
//                int iq = spec.getP() + spec.getBP();
//                // case P == 0
//                if (spec.getP() == 0) {
//                    if (fixed == null || !fixed[iq]) {
//                        var.set(iq, iq, (1 - q * q) / denom);
//                        for (int i = 0; i < var.getRowsCount(); ++i) {
//                            if (i != iq) {
//                                var.set(i, iq, 0);
//                                var.set(iq, i, 0);
//                            }
//                        }
//                    }
//                } else // P=1
//                {
//                    double p = arima.phi(1);
//                    // index of p=0
//                    if (fixed == null || !fixed[iq]) {
//                        var.set(iq, iq, (1 - q * q) * (1 - p * q) * (1 - p * q)
//                                / (denom * (p - q) * (p - q)));
//                        var.set(0, iq, -(1 - p * q) * (1 - p * p) * (1 - q * q)
//                                / (denom * (p - q) * (p - q)));
//                        var.set(iq, 0, var.get(0, iq));
//                        for (int i = 1; i < var.getRowsCount(); ++i) {
//                            if (i != iq) {
//                                var.set(i, iq, 0);
//                                var.set(iq, i, 0);
//                            }
//                        }
//                    }
//                }
//            }
//            return;
//        }
            if (np - nf == 2 && spec.getP() + spec.getQ() == 1
                    && spec.getBP() + spec.getBQ() == 1
                    && (1 - Math.abs(var.get(0, 1))
                    / Math.sqrt(var.get(0, 0) * var.get(1, 1)) <= .01)) {
                var.set(0, 1, 0);
                var.set(1, 0, 0);
            }
        } catch (ArimaException | MatrixException err) {
            String msg = err.getMessage();
        }
    }
}
