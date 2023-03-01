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
package jdplus.ssf.sts;

import jdplus.data.DataBlock;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.basic.Loading;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 * Usual local linear trend y(t)=l(t)+n(t) l(t+1)=s(t)+l(t)+u(t)
 * s(t+1)=s(t)+v(t)
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LocalLinearTrend {
    
    public int dim(){return 2;}

    public StateComponent stateComponent(double lvar, double svar) {
        Data data = new Data(lvar, svar);
        return new StateComponent(new Initialization(data), new Dynamics(data));
    }
    
    public ISsfLoading defaultLoading(){
        return Loading.fromPosition(0);
    }


    static class Data {

        final double lv, sv;

        Data(final double lv, final double sv) {
            this.lv = lv;
            this.sv = sv;
        }
    }

    public static class Initialization implements ISsfInitialization {

        private final Data data;

        Initialization(Data data) {
            this.data = data;
        }

        @Override
        public int getStateDim() {
            return 2;
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getDiffuseDim() {
            return 2;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix pf0) {
            if (data.lv > 0) {
                pf0.set(0, 0, data.lv);
            }
            if (data.sv > 0) {
                pf0.set(1, 1, data.sv);
            }
        }

        @Override
        public void Pi0(FastMatrix pi0) {
            pi0.diagonal().set(1);
        }

    }

    public static class Dynamics implements ISsfDynamics {

        private final Data data;

        Dynamics(Data data) {
            this.data = data;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            int n = 0;
            if (data.lv > 0) {
                ++n;
            }
            if (data.sv > 0) {
                ++n;
            }
            return n;
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            if (data.lv > 0) {
                qm.set(0, 0, data.lv);
            }
            if (data.sv > 0) {
                qm.set(1, 1, data.sv);
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return data.lv != 0 || data.sv != 0;
        }

        @Override
        public void S(int pos, FastMatrix s) {
            if (data.lv != 0 && data.sv != 0) {
                s.set(0, 0, Math.sqrt(data.lv));
                s.set(1, 1, Math.sqrt(data.sv));
            } else if (data.lv != 0) {
                s.set(1, 0, Math.sqrt(data.lv));
            } else if (data.sv != 0) {
                s.set(0, 1, Math.sqrt(data.sv));
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            if (data.lv != 0 && data.sv != 0) {
                x.add(0, Math.sqrt(data.lv) * u.get(0));
                x.add(1, Math.sqrt(data.sv) * u.get(1));
            } else if (data.lv != 0) {
                x.add(0, Math.sqrt(data.lv) * u.get(0));
            } else if (data.sv != 0) {
                x.add(1, Math.sqrt(data.sv) * u.get(0));
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            if (data.sv != 0 && data.lv != 0) {
                xs.set(0, Math.sqrt(data.lv) * x.get(0));
                xs.set(1, Math.sqrt(data.sv) * x.get(1));
            } else if (data.lv != 0) {
                xs.set(0, Math.sqrt(data.lv) * x.get(0));
            } else if (data.sv != 0) {
                xs.set(0, Math.sqrt(data.sv) * x.get(1));
            }
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            tr.set(0, 0, 1);
            tr.set(0, 1, 1);
            tr.set(1, 1, 1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.add(0, x.get(1));
        }

        @Override
        public void TVT(int pos, FastMatrix vm) {
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
        public void addV(int pos, FastMatrix p) {
            if (data.lv > 0) {
                p.add(0, 0, data.lv);
            }
            if (data.sv > 0) {
                p.add(1, 1, data.sv);
            }
        }

    }
}
