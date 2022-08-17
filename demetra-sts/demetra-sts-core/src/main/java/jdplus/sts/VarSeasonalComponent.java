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
package jdplus.sts;

import demetra.sts.SeasonalModel;
import jdplus.data.DataBlock;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.basic.Loading;
import jdplus.ssf.ISsfInitialization;
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class VarSeasonalComponent {

    public StateComponent of(final SeasonalModel model, final int period, final double[] std, double scale) {
        SeasonalModel cmodel = scale == 0 ? SeasonalModel.Fixed : model;
        Data data = new Data(cmodel, period, std, scale);
        return new StateComponent(new Initialization(data), new Dynamics(data));
    }

    public ISsfLoading defaultLoading() {
        return Loading.fromPosition(0);
    }

    public ISsfLoading harrisonStevensLoading(final int period) {
        return Loading.circular(period);
    }

    static class Data {

        private final SeasonalModel seasModel;
        private final double scale;
        private final double[] std;
        private final int period;
        private final FastMatrix tsvar, lvar;

        Data(final SeasonalModel model, final int period, final double[] std, double scale) {
            this.scale = scale;
            this.std = std;
            this.seasModel = model;
            this.period = period;
            if (scale > 0) {
                tsvar = SeasonalComponent.tsVar(seasModel, period);
                if (model != SeasonalModel.Crude && model != SeasonalModel.Dummy) {
                    lvar = SeasonalComponent.tslVar(seasModel, period);
                } else {
                    lvar = null;
                }
            } else {
                tsvar = null;
                lvar = null;
            }
        }

        double var(int pos) {
            if (pos < std.length) {
                double q = std[pos] * scale;
                return q * q;
            } else {
                return scale * scale;
            }
        }

        double std(int pos) {
            return pos < std.length ? std[pos] * scale : scale;
        }
    }

    static class Initialization implements ISsfInitialization {

        private final Data data;

        public Initialization(final Data data) {
            this.data = data;
        }

        @Override
        public int getStateDim() {
            return data.period - 1;
        }

        @Override
        public boolean isDiffuse() {
            return data.scale >= 0;
        }

        @Override
        public int getDiffuseDim() {
            return data.period - 1;
        }

        @Override
        public void diffuseConstraints(FastMatrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void Pi0(FastMatrix p) {
            p.diagonal().set(1);
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(FastMatrix p) {
        }
    }

    static class Dynamics implements ISsfDynamics {

        private final Data data;

        public Dynamics(final Data data) {
            this.data = data;
        }

        @Override
        public boolean isTimeInvariant() {
            return data.scale == 0;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return data.scale == 0;
        }

        @Override
        public int getInnovationsDim() {
            if (data.scale > 0) {
                if (data.seasModel == SeasonalModel.Dummy
                        || data.seasModel == SeasonalModel.Crude) {
                    return 1;
                } else {
                    return data.period - 1;
                }
            } else {
                return 0;
            }
        }

        @Override
        public void V(int pos, FastMatrix v) {
            if (data.scale > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    v.set(0, 0, data.var(pos));
                } else {
                    v.setAY(data.var(pos), data.tsvar);
                }
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return data.seasModel != SeasonalModel.Fixed;
        }

        @Override
        public void S(int pos, FastMatrix s) {
            if (null != data.seasModel) {
                switch (data.seasModel) {
                    case Crude:
                        s.set(data.std(pos));
                        break;
                    case Dummy:
                        s.set(data.period - 2, data.period - 2, data.std(pos));
                        break;
                    default:
                        s.setAY(data.std(pos), data.lvar);
                        break;
                }
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            switch (data.seasModel) {
                case Crude:
                    x.add(data.std(pos) * u.get(0));
                    break;
                case Dummy:
                    x.add(0, data.std(pos) * u.get(0));
                    break;
                default:
                    x.addAProduct(data.std(pos), data.lvar.rowsIterator(), u);
                    break;
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            switch (data.seasModel) {
                case Crude:
                    xs.set(0, data.std(pos) * x.sum());
                    break;
                case Dummy:
                    xs.set(0, data.std(pos) * x.get(data.period - 2));
                    break;
                default:
                    xs.AProduct(data.std(pos), x, data.lvar.columnsIterator());
                    break;
            }
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            if (data.scale >= 0) {
                tr.row(data.period - 2).set(-1);
                tr.subDiagonal(1).set(1);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.fshiftAndNegSum();
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int imax = data.period - 2;
            double xs = x.get(0);
            for (int i = 0; i < imax; ++i) {
                x.set(i, x.get(i + 1) - xs);
            }
            x.set(imax, -xs);
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            switch (data.seasModel) {
                case Fixed:
                    return;
                case Dummy:
                    p.add(data.period - 2, data.period - 2, data.var(pos));
                    break;
                case Crude:
                    p.add(data.var(pos));
                    break;
                default:
                    p.addAY(data.var(pos), data.tsvar);
                    break;
            }
        }
    }

}
