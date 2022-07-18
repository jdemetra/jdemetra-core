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
public class VarLocalLinearTrend {
    
    public StateComponent of(double[] lstd, double[] sstd, double lscale, double sscale) {
        Data data = new Data(lstd, sstd, lscale, sscale);
        return new StateComponent(new Initialization(), new Dynamics(data));
    }
    
    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }
    
    static class Data {
        
        final double lscale, sscale, lscale2, sscale2;
        final double[] lstd, sstd;
        
        Data(double[] lstd, double[] sstd, double lscale, double sscale) {
            this.lscale = lscale;
            this.lscale2 = lscale * lscale;
            this.lstd = lstd;
            this.sscale = sscale;
            this.sscale2 = sscale * sscale;
            this.sstd = sstd;
        }
        
        double svar(int pos) {
            if (sstd != null && pos < sstd.length) {
                double q = sstd[pos] * sscale;
                return q * q;
            } else {
                return sscale2;
            }
        }
        
        double sstd(int pos) {
            return (sstd != null && pos < sstd.length) ? sscale * sstd[pos] : sscale;
        }
        
        double lvar(int pos) {
            if (lstd != null && pos < lstd.length) {
                double q = lstd[pos] * lscale;
                return q * q;
            } else {
                return lscale2;
            }
        }
        
        double lstd(int pos) {
            return (lstd != null && pos < lstd.length) ? lscale * lstd[pos] : lscale;
        }
    }
    
    public static class Initialization implements ISsfInitialization {
        
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
//            if (data.lv > 0) {
//                pf0.set(0, 0, data.lv);
//            }
//            if (data.sv > 0) {
//                pf0.set(1, 1, data.sv);
//            }
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
            return false;
        }
        
        @Override
        public boolean areInnovationsTimeInvariant() {
            return false;
        }
        
        @Override
        public int getInnovationsDim() {
            int n = 0;
            if (data.lscale > 0) {
                ++n;
            }
            if (data.sscale > 0) {
                ++n;
            }
            return n;
        }
        
        @Override
        public void V(int pos, FastMatrix qm) {
            if (data.lscale > 0) {
                qm.set(0, 0, data.lvar(pos));
            }
            if (data.sscale > 0) {
                qm.set(1, 1, data.svar(pos));
            }
        }
        
        @Override
        public boolean hasInnovations(int pos) {
            return data.lscale != 0 || data.sscale != 0;
        }
        
        @Override
        public void S(int pos, FastMatrix s) {
            s.set(0, 0, data.lstd(pos));
            s.set(1, 1, data.sstd(pos));
        }
        
        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            x.add(0, data.lstd(pos) * u.get(0));
            x.add(1, data.sstd(pos) * u.get(1));
        }
        
        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            xs.set(0, data.lstd(pos) * x.get(0));
            xs.set(1, data.sstd(pos) * x.get(1));
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
            p.add(0, 0, data.lvar(pos));
            p.add(1, 1, data.svar(pos));
        }
        
    }
}
