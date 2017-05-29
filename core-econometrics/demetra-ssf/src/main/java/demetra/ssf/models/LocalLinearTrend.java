/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.models;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.Ssf;

/**
 * Usual local linear trend y(t)=l(t)+n(t) l(t+1)=s(t)+l(t)+u(t)
 * s(t+1)=s(t)+v(t)
 *
 * @author Jean Palate
 */
public class LocalLinearTrend extends Ssf {

    private final double lv, sv, nv;

    public LocalLinearTrend(double lvar, double svar, double nvar) {
        super(new Dynamics(lvar, svar), Measurement.create(2, 0, nvar));
        lv = lvar;
        sv = svar;
        nv = nvar;
    }

    public double getVariance() {
        return lv;
    }

    public double getSlopeVariance() {
        return sv;
    }

    public double getNoiseVariance() {
        return nv;
    }

    public static class Dynamics implements ISsfDynamics {

        private final double lvar, svar;

        public Dynamics(double var, double svar) {
            lvar = var;
            this.svar = svar;
        }

        public double getVariance() {
            return lvar;
        }

        public double getSlopeVariance() {
            return svar;
        }

        @Override
        public int getStateDim() {
            return 2;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return lvar >= 0 && svar >= 0;
        }

        @Override
        public int getInnovationsDim() {
            int n = 0;
            if (lvar > 0) {
                ++n;
            }
            if (svar > 0) {
                ++n;
            }
            return n;
        }

        @Override
        public void V(int pos, Matrix qm) {
            if (lvar > 0) {
                qm.set(0, 0, lvar);
            }
            if (svar > 0) {
                qm.set(1, 1, svar);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return lvar != 0 || svar != 0;
        }

//        @Override
//        public void Q(int pos, Matrix qm) {
//            int i = 0;
//            if (lvar > 0) {
//                qm.set(0, 0, lvar);
//                i = 1;
//            }
//            if (svar > 0) {
//                qm.set(i, i, svar);
//            }
//        }
//
//        @Override
//        public void S(int pos, Matrix sm) {
//            if (svar == 0 && lvar != 0) {
//                sm.set(1, 0, 1);
//            } else if (svar != 0 && lvar == 0) {
//                sm.set(0, 0, 1);
//            }
//        }
        @Override
        public void S(int pos, Matrix s) {
            if (svar != 0 && lvar != 0) {
                s.set(0, 0, Math.sqrt(lvar));
                s.set(1, 1, Math.sqrt(svar));
            } else if (lvar != 0) {
                s.set(1, 0, Math.sqrt(lvar));
            } else if (svar != 0) {
                s.set(0, 1, Math.sqrt(svar));
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            if (svar != 0 && lvar != 0) {
                x.add(0, Math.sqrt(lvar) * u.get(0));
                x.add(1, Math.sqrt(svar) * u.get(1));
            } else if (lvar != 0) {
                x.add(0, Math.sqrt(lvar) * u.get(0));
            } else if (svar != 0) {
                x.add(1, Math.sqrt(svar) * u.get(0));
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            if (svar != 0 && lvar != 0) {
                xs.set(0, Math.sqrt(lvar) * x.get(0));
                xs.set(1, Math.sqrt(svar) * x.get(1));
            } else if (lvar != 0) {
                xs.set(0, Math.sqrt(lvar) * x.get(0));
            } else if (svar != 0) {
                xs.set(0, Math.sqrt(svar) * x.get(1));
            }
        }
//        @Override
//        public void addSX(int pos, DataBlock x, DataBlock y) {
//            if (svar == 0 && lvar != 0) {
//                y.add(1, x.get(0));
//            } else if (svar != 0 && lvar == 0) {
//                y.add(0, x.get(0));
//            } else
//            y.add(x);
//        }
//

        @Override
        public void T(int pos, Matrix tr) {
            tr.set(0, 0, 1);
            tr.set(0, 1, 1);
            tr.set(1, 1, 1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return 2;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            V(0,pf0);
            return true;
        }

        @Override
        public void Pi0(Matrix pi0) {
            pi0.diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.add(0, x.get(1));
        }

        @Override
        public void TVT(int pos, Matrix vm) {
            double v01 = vm.get(0, 1), v11 = vm.get(1, 1);
            vm.add(0, 0, 2 * v01 + v11);
            vm.add(0, 1, v11);
            vm.add(1, 0, v11);
        }

        @Override
        public void XT(int pos, DataBlock x) {
            x.add(1, x.get(0));
        }

        @Override
        public void addV(int pos, Matrix p) {
            if (lvar > 0) {
                p.add(0, 0, lvar);
            }
            if (svar > 0) {
                p.add(1, 1, svar);
            }
        }

    }
}
