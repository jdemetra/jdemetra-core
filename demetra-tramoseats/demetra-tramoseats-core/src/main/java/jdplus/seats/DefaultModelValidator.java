/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.seats;

import demetra.math.Complex;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author palatej
 */
public class DefaultModelValidator implements IModelValidator {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        public static final double DEF_XL = .95;
        public static final double DEF_EPS = .0001;
        private double xl = DEF_XL;
        private double eps = DEF_EPS;

        /**
         * abs of inverse MA roots which are lower than xl are set to xl
         *
         * @param xl
         * @return
         */
        public Builder xl(double xl) {
            this.xl = xl;
            return this;
        }

        /**
         * Only used with xl == 1. Roots which are near 1 are set to 1
         * (deterministic component).
         * To be used with the Kalman smoother only.
         *
         * @param eps
         * @return
         */
        public Builder urTolerance(double eps) {
            this.eps = eps;
            return this;
        }

        public DefaultModelValidator build() {
            return new DefaultModelValidator(xl, eps);
        }

    }

    private final double xl;
    private final double eps;

    private SarimaModel newModel;

    /**
     *
     * @param xl
     */
    private DefaultModelValidator(double xl, double eps) {
        this.xl = xl;
        this.eps = eps;
    }

    /**
     *
     * @return
     */
    @Override
    public SarimaModel getNewModel() {
        return newModel;
    }

    /**
     * @return the xl
     */
    public double getXl() {
        return xl;
    }

    private boolean stabilizeMA(double ur, double[] p) {
        if (p.length == 1) {
            double q = p[0];
            if (q < -ur) {
                p[0] = -ur;
                return true;
            } else if (q > ur) {
                p[0] = ur;
                return true;
            } else {
                return false;
            }
        } else {
            Polynomial P = Polynomial.valueOf(1.0, p);
            boolean changed = false;
            // FIXME: Arrays2.copyOf() might be useless here
            Complex[] roots = P.roots();
            for (int i = 0; i < roots.length; ++i) {
                Complex root = roots[i];
                double q = 1 / roots[i].abs();
                if (q > ur) {
                    changed = true;
                    roots[i] = root.times(q / ur);
                }
            }
            if (!changed) {
                return false;
            }
            Polynomial ptmp = Polynomial.fromComplexRoots(roots);
            ptmp = ptmp.divide(ptmp.get(0));
            for (int i = 0; i < p.length; ++i) {
                p[i] = ptmp.get(i + 1);
            }
            return true;
        }
    }

    /**
     *
     * @param model The model to be validated
     * @param info
     * @return True if the current model is valid. If false, a new model can be
     * retrieved
     * through the getNewModel method
     */
    @Override
    public boolean validate(SarimaModel model) {
        newModel = model;
        boolean smp = simplifyModel();
        boolean ma = changeMA();
        boolean ar = changeAR();
        return !smp && !ma && !ar;
    }

    /**
     *
     * @param info
     * @return
     */
    private boolean changeAR() {
        // could be changed...
        return false;
    }

    private boolean fixMaUnitRoots() {
        boolean changed = false;
        double ur = 1 - eps;
        double[] q = newModel.theta(), bq = newModel.btheta();
        if (bq.length > 0) {
            double sur = Math.pow(ur, newModel.getFrequency());
            double bth = bq[0];
            if (bth < -sur) {
                bq[0] = -1;
                changed = true;
            } else if (bth > sur) {
                bq[0] = 1;
                changed = true;
            }
        }
        if (q.length == 1) {
            double th = q[0];
            if (th < -ur) {
                q[0] = -1;
                changed = true;
            } else if (th > ur) {
                q[0] = -1;
                changed = true;
            }
        } else if (q.length > 1) {
            Polynomial Q = Polynomial.valueOf(1, q);
            Complex[] roots = Q.roots();
            boolean qchanged = false;
            for (int i = 0; i < roots.length; ++i) {
                double l = roots[i].abs();
                if (l < ur) {
                    qchanged = true;
                    roots[i] = roots[i].div(l);
                }
            }
            if (qchanged) {
                Q = Polynomial.fromComplexRoots(roots);
                for (int i = 0; i < q.length; ++i) {
                    q[i] = Q.get(i + 1);
                }
                changed = true;
            }
        }
        if (changed) {
            newModel = newModel.toBuilder()
                    .theta(q)
                    .btheta(bq)
                    .build();
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param info
     * @return
     */
    private boolean changeMA() {
        if (xl < 1) {
            boolean rslt = false;
            double[] q = newModel.theta(), bq = newModel.btheta();
            if (stabilizeMA(xl, q)) {
                rslt = true;
            }
            if (stabilizeMA(xl, bq)) {
                rslt = true;
            }
            if (rslt) {
                newModel = newModel.toBuilder()
                        .theta(q)
                        .btheta(bq)
                        .build();
                return true;
            } else {
                return false;
            }
        } else {
            if (fixMaUnitRoots()) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean simplifyModel() {
        if (canSimplify(newModel.phi()) || canSimplify(newModel.bphi())
                || canSimplify(newModel.theta()) || canSimplify(newModel.btheta())) {
            newModel = newModel.toBuilder()
                    .adjustOrders(true)
                    .build();
            return true;
        } else {
            return false;
        }
    }

    private boolean canSimplify(double[] p) {
        return p.length > 0 && Math.abs(p[p.length - 1]) < SarimaModel.SMALL;
    }
}
