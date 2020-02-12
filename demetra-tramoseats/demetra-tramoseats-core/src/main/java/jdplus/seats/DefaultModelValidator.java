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

import demetra.arima.SarimaOrders;
import demetra.math.Complex;
import jdplus.data.DataBlock;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author palatej
 */
public class DefaultModelValidator implements IModelValidator {

    public static final double DEF_XL = .95;
    private double xl = DEF_XL;
    public static final double DEF_EPS = .0001;
    private double eps = DEF_EPS;
    private SarimaModel newModel;
    
        

    /**
     *
     */
    public DefaultModelValidator() {
    }

    /**
     *
     * @param xl
     */
    public DefaultModelValidator(double xl) {
        this.xl = xl;
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

    private boolean maStabilize(double ur, DataBlock p, int start, int n) {
        if (n == 1) {
            double q = p.get(start);
            if (q < -ur) {
                p.set(start, -ur);
                return true;
            } else if (q > ur) {
                p.set(start, ur);
                return true;
            } else {
                return false;
            }
        } else {
            double[] P = Polynomial.Doubles.fromDegree(n);
            P[0] = 1;
            for (int i = 0; i < n; ++i) {
                P[i + 1] = p.get(start + i);
            }
            boolean changed = false;
            // FIXME: Arrays2.copyOf() might be useless here
            Complex[] roots = Arrays2.copyOf(Polynomial.of(P).roots());
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
            for (int i = 0; i < n; ++i) {
                p.set(start + i, ptmp.get(i + 1));
            }
            return true;
        }
    }

    /**
     * @param xl the xl to set
     */
    public void setXl(double xl) {
        this.xl = xl;
    }

    /**
     *
     * @param model
     * @param info
     * @return
     */
    @Override
    public ModelStatus validate(SarimaModel model, InformationSet info) {
        newModel = model;
        ModelStatus smp = simplifyModel(info);
        ModelStatus ma = validateMA(info);
        if (ma == ModelStatus.Invalid) {
            return ModelStatus.Invalid;
        }
        ModelStatus ar = validateAR(info);
        if (ar == ModelStatus.Invalid) {
            return ModelStatus.Invalid;
        }
        if (smp == ModelStatus.Changed || ar == ModelStatus.Changed || ma == ModelStatus.Changed) {
            return ModelStatus.Changed;
        } else {
            return ModelStatus.Valid;
        }
    }

    /**
     *
     * @param info
     * @return
     */
    protected ModelStatus validateAR(InformationSet info) {
        // could be changed...
        return ModelStatus.Valid;
    }

    protected boolean fixMaUnitRoots(SarimaModel arima) {
        SarimaOrders spec = arima.orders();
        boolean changed = false;
        double ur = 1 - eps;
        if (spec.getBQ() > 0) {

            double sur = Math.pow(ur, arima.getFrequency());
            double bth = arima.btheta(1);
            if (bth < -sur) {
                changed = true;
                arima.setBTheta(1, -1);
            } else if (bth > sur) {
                changed = true;
                arima.setBTheta(1, 1);
            }
        }
        if (spec.getQ() == 1) {
            double th = arima.theta(1);
            if (th < -ur) {
                changed = true;
                arima.setTheta(1, -1);
            } else if (th > ur) {
                changed = true;
                arima.setTheta(1, 1);
            }
        } else if (spec.getQ() > 1) {
            Polynomial q = arima.getRegularMA();
            Complex[] roots = q.roots();
            boolean qchanged = false;
            for (int i = 0; i < roots.length; ++i) {
                double l = roots[i].abs();
                if (l < ur) {
                    qchanged = true;
                    roots[i] = roots[i].div(l);
                }
            }
            if (qchanged) {
                q = Polynomial.fromComplexRoots(roots);
                for (int i = 1; i <= spec.getQ(); ++i) {
                    arima.setTheta(i, q.get(i) / q.get(0));
                }
            }
        }
        return changed;
    }

    /**
     *
     * @param info
     * @return
     */
    private ModelStatus validateMA(SarimaModel.Builder nmodel) {
        if (xl < 1) {
            SarimaOrders spec = nmodel newModel.orders();
            boolean rslt = false;
            DataBlock p = new DataBlock(newModel.getParameters());
            if (spec.getQ() > 0
                    && maStabilize(this.xl, p, spec.getP() + spec.getBP(), spec
                            .getQ())) {
                rslt = true;
            }
            if (spec.getBQ() > 0
                    //&& maStabilize(Math.pow(xl, spec.getFrequency()), p, spec
                    // 22/7/2014. Questionable correction
                    && maStabilize(xl, p, spec
                            .getP()
                            + spec.getBP() + spec.getQ(), spec.getBQ())) {
                rslt = true;
            }
            if (rslt) {
                newModel.setParameters(p);
                return ModelStatus.Changed;
            } else {
                return ModelStatus.Valid;
            }
        } else {
            if (fixMaUnitRoots(newModel)) {
                return ModelStatus.Changed;
            } else {
                return ModelStatus.Valid;
            }
        }
    }

    protected ModelStatus simplifyModel(InformationSet info) {
        if (newModel.adjustSpecification()) {
            return ModelStatus.Changed;
        } else {
            return ModelStatus.Valid;
        }
    }
}
