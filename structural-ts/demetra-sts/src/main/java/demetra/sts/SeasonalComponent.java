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
package demetra.sts;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.Ssf;
import demetra.linearsystem.ILinearSystemSolver;

/**
 *
 * @author Jean Palate
 */
public class SeasonalComponent {

    private static Matrix tsvar(int freq) {
        int n = freq - 1;
        Matrix M = Matrix.make(n, freq);
        M.diagonal().set(1);
        M.column(n).set(-1);
        Matrix O = SymmetricMatrix.XXt(M);
        ILinearSystemSolver.robustSolver().solve(O, M);
        Matrix H = Matrix.make(freq, n);
        // should be improved
        for (int i = 0; i < freq; ++i) {
            double z = 2 * Math.PI * (i + 1) / freq;
            for (int j = 0; j < n / 2; ++j) {
                H.set(i, 2 * j, Math.cos((j + 1) * z));
                H.set(i, 2 * j + 1, Math.sin((j + 1) * z));
            }
            if (n % 2 == 1) {
                H.set(i, n - 1, Math.cos((freq / 2) * z));
            }
        }
        Matrix QH = M.times(H);
        Matrix Z = SymmetricMatrix.XXt(QH);
        Z.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);

        return Z;
    }

    /**
     *
     * @param freq
     * @return
     */
    private static synchronized Matrix tsVar(int freq) {
        switch (freq) {
            case 12:
                if (VTS12 == null) {
                    VTS12 = tsvar(12);
                }
                return VTS12.deepClone();
            case 4:
                if (VTS4 == null) {
                    VTS4 = tsvar(4);
                }
                return VTS4.deepClone();
            case 2:
                if (VTS2 == null) {
                    VTS2 = tsvar(2);
                }
                return VTS2.deepClone();
            case 3:
                if (VTS3 == null) {
                    VTS3 = tsvar(3);
                }
                return VTS3.deepClone();
            case 6:
                if (VTS6 == null) {
                    VTS6 = tsvar(6);
                }
                return VTS6.deepClone();
            default:
                return tsvar(freq);
        }
    }

    private static Matrix hsvar(int freq) {
        Matrix m = Matrix.square(freq - 1);
        m.set(-1.0 / freq);
        m.diagonal().add(1);
        return m;
    }

    private static synchronized Matrix hslVar(int freq) {
        switch (freq) {
            case 12:
                if (LHS12 == null) {
                    LHS12 = hsvar(12);
                    SymmetricMatrix.lcholesky(LHS12);
                }
                return LHS12.deepClone();
            case 4:
                if (LHS4 == null) {
                    LHS4 = hsvar(4);
                    SymmetricMatrix.lcholesky(LHS4);
                }
                return LHS4.deepClone();
            case 2:
                if (LHS2 == null) {
                    LHS2 = hsvar(2);
                    SymmetricMatrix.lcholesky(LHS2);
                }
                return LHS2.deepClone();
            case 3:
                if (LHS3 == null) {
                    LHS3 = hsvar(3);
                    SymmetricMatrix.lcholesky(LHS3);
                }
                return LHS3.deepClone();
            case 6:
                if (LHS6 == null) {
                    LHS6 = hsvar(6);
                    SymmetricMatrix.lcholesky(LHS6);
                }
                return LHS6.deepClone();
            default:
                Matrix lhs = hsvar(freq);
                SymmetricMatrix.lcholesky(lhs);
                return lhs;
        }
    }

    public static Matrix tsVar(SeasonalModel seasModel, final int freq) {
        if (seasModel == SeasonalModel.Trigonometric) {
            return tsVar(freq);
        } else {
            int n = freq - 1;
            Matrix Q = Matrix.square(n);
            if (null != seasModel) // Dummy
            {
                switch (seasModel) {
                    case Dummy:
                        Q.set(n - 1, n - 1, 1);
                        break;
                    case Crude:
                        Q.set(1);
                        //Q.set(0, 0, freq * var);
                        break;
                    case HarrisonStevens:
                        double v = 1.0 / freq;
                        Q.set(-v);
                        Q.diagonal().add(1);
                        break;
                    default:
                        break;
                }
            }
            return Q;
        }
    }

    private static synchronized Matrix tslVar(int freq) {
        switch (freq) {
            case 12:
                if (LVTS12 == null) {
                    LVTS12 = tsvar(12);
                    SymmetricMatrix.lcholesky(LVTS12);
                    LVTS12.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                }
                return LVTS12.deepClone();
            case 4:
                if (LVTS4 == null) {
                    LVTS4 = tsvar(4);
                    LVTS4.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                    SymmetricMatrix.lcholesky(LVTS4);
                }
                return LVTS4.deepClone();
            case 2:
                if (LVTS2 == null) {
                    LVTS2 = tsvar(2);
                    LVTS2.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                    SymmetricMatrix.lcholesky(LVTS2);
                }
                return LVTS2.deepClone();
            case 3:
                if (LVTS3 == null) {
                    LVTS3 = tsvar(3);
                    SymmetricMatrix.lcholesky(LVTS3);
                    LVTS3.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                }
                return LVTS3.deepClone();
            case 6:
                if (LVTS6 == null) {
                    LVTS6 = tsvar(6);
                    SymmetricMatrix.lcholesky(LVTS6);
                    LVTS6.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                }
                return LVTS6.deepClone();
            default:
                Matrix var = tsvar(freq);
                SymmetricMatrix.lcholesky(var);
                var.apply(x -> Math.abs(x) < 1e-12 ? 0 : x);
                return var;
        }
    }

    public static Matrix tslVar(SeasonalModel seasModel, final int freq) {
        switch (seasModel) {
            case Trigonometric:
                return tslVar(freq);
            case HarrisonStevens:
                return hslVar(freq);
            default:
                int n = freq - 1;
                Matrix Q = Matrix.square(n);
                if (null != seasModel) // Dummy
                {
                    switch (seasModel) {
                        case Dummy:
                            Q.set(n - 1, n - 1, 1);
                            break;
                        case Crude:
                            Q.set(1);
                            //Q.set(0, 0, freq * var);
                            break;
                        default:
                            break;
                    }
                }
                return Q;
        }
    }

    private static Matrix VTS2, VTS3, VTS4, VTS6, VTS12;
    private static Matrix LVTS2, LVTS3, LVTS4, LVTS6, LVTS12, LHS2, LHS3, LHS4, LHS6, LHS12;

    public static ISsf create(final SeasonalModel model, final double seasVar, final int period) {
        return new Ssf(new Dynamics(model, seasVar, period), Measurement.create(period - 1, 1));
    }

    public static ISsf harrisonStevens(final int period, final double v) {
        return new Ssf(new HarrisonStevensDynamics(period, v), Measurement.circular(period));
    }

    public static ISsf harrisonStevens(final double[] var) {
        return new Ssf(new HarrisonStevensDynamics(var), Measurement.circular(var.length));
    }

    public static class Dynamics implements ISsfDynamics {

        private final SeasonalModel seasModel;
        private final double seasVar;
        private final int freq;
        private final Matrix tsvar, lvar;

        public Dynamics(final SeasonalModel model, final double seasVar, final int freq) {
            this.seasVar = seasVar;
            this.seasModel = model;
            this.freq = freq;
            if (seasVar > 0) {
                tsvar = tsVar(seasModel, freq);
                tsvar.mul(seasVar);
                if (model != SeasonalModel.Crude && model != SeasonalModel.Dummy) {
                    lvar = tslVar(seasModel, freq);
                    lvar.mul(std());
                } else {
                    lvar = null;
                }
            } else {
                tsvar = null;
                lvar = null;
            }
        }

        private double std() {
            if (seasVar == 0 || seasVar == 1) {
                return seasVar;
            } else {
                return Math.sqrt(seasVar);
            }
        }

        @Override
        public int getStateDim() {
            return freq - 1;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            if (seasVar > 0) {
                if (seasModel == SeasonalModel.Dummy || seasModel == SeasonalModel.Crude) {
                    return 1;
                } else {
                    return freq - 1;
                }
            } else {
                return 0;
            }
        }

        @Override
        public void V(int pos, Matrix v) {
            if (seasVar > 0) {
                if (seasModel == SeasonalModel.Dummy) {
                    v.set(0, 0, seasVar);
                } else {
                    v.copy(tsvar);
                }
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return seasModel != SeasonalModel.Fixed;
        }

        @Override
        public void S(int pos, Matrix s) {
            if (null != seasModel) switch (seasModel) {
                case Crude:
                    s.set(std());
                    break;
                case Dummy:
                    s.set(freq - 1, 0, std());
                    break;
                default:
                    s.copy(lvar);
                    break;
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            if (null != seasModel) switch (seasModel) {
                case Crude:
                    x.add(std() * u.get(0));
                    break;
                case Dummy:
                    x.add(freq - 1, std() * u.get(0));
                    break;
                default:
                    x.addProduct(lvar.rowsIterator(), u);
                    break;
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            if (null != seasModel) switch (seasModel) {
                case Crude:
                    xs.set(0, std() * x.sum());
                    break;
                case Dummy:
                    xs.set(0, std() * x.get(freq - 1));
                    break;
                default:
                    xs.product(x, lvar.columnsIterator());
                    break;
            }
        }

//        @Override
//        public void addSX(int pos, DataBlock x, DataBlock y) {
//            y.add(x);
//        }
//
        @Override
        public void T(int pos, Matrix tr) {
            if (seasVar >= 0) {
                tr.row(freq - 2).set(-1);
                tr.subDiagonal(1).set(1);
            }
        }

        @Override
        public boolean isDiffuse() {
            return seasVar >= 0;
        }

        @Override
        public int getNonStationaryDim() {
            return freq - 1;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void Pi0(Matrix p) {
            p.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix p) {
            V(0, p);
            return true;
        }

        @Override
        public void TX(int pos, DataBlock x) {
            x.bshiftAndNegSum();
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int imax = freq - 2;
            double xs = x.get(imax);
            for (int i = imax; i > 0; --i) {
                x.set(i, x.get(i - 1) - xs);
            }
            x.set(0, -xs);

        }

        @Override
        public void addV(int pos, Matrix p) {
            switch (seasModel) {
                case Fixed:
                    return;
                case Dummy:
                    p.add(0, 0, seasVar);
                    break;
                default:
                    p.add(tsvar);
                    break;
            }
        }

    }

    public static class HarrisonStevensDynamics implements ISsfDynamics {

        private final int period;
        private final double[] var;
        private final Matrix V;

        public HarrisonStevensDynamics(final int period, final double v) {
            this.period = period;
            var = null;
            V = Matrix.square(period - 1);
            V.set(-1.0 / period);
            V.diagonal().add(1);
            V.mul(v);
        }

        public HarrisonStevensDynamics(final double[] var) {
            period = var.length;
            this.var = var.clone();
//            Matrix C = new Matrix(period - 1, period);
//            C.set(-1.0 / period);
//            C.diagonal().add(1);
//            Matrix D = Matrix.diagonal(var);
//            V = SymmetricMatrix.quadraticFormT(D, C);
            DataBlock xvar = DataBlock.ofInternal(var);
            V = Matrix.square(period - 1);
//            V.diagonal().copyFrom(var, 0);
//            V.add(xvar.sum() / (period * period));
//            for (int i = 0; i < period - 1; ++i) {
//                for (int j = 0; j < period - 1; ++j) {
//                    V.add(i, j, -(var[i] + var[j]) / period);
//                }
//            }
            double mvar = xvar.sum() / (period * period);
            double dp = 2.0 / period;
            for (int i = 0; i < period - 1; ++i) {
                V.set(i, i, var[i] * (1 - dp) + mvar);
                for (int j = 0; j < i; ++j) {
                    V.set(i, j, mvar - (var[i] + var[j]) / period);
                }
            }
            SymmetricMatrix.fromLower(V);
        }

        public double[] getVariances() {
            return var;
        }

        @Override
        public int getStateDim() {
            return period - 1;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean isValid() {
            return period > 1;
        }

        @Override
        public int getInnovationsDim() {
            return period - 1;
        }

        @Override
        public void V(int pos, Matrix qm) {
            if (V != null) {
                qm.copy(V);
            }
        }

//        @Override
//        public boolean hasS() {
//            return false;
//        }
//
        @Override
        public boolean hasInnovations(int pos) {
            return V != null;
        }

//        @Override
//        public void Q(int pos, Matrix qm) {
//            qm.copy(V.subMatrix());
//        }
//
//        @Override
//        public void S(int pos, Matrix sm) {
//        }
//
        @Override
        public void S(int pos, Matrix s) {
            //TODO
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            //TODO
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            //TODO
        }
//        @Override
//        public void addSX(int pos, DataBlock x, DataBlock y) {
//            y.add(x);
//        }
//

        @Override
        public void T(int pos, Matrix tr) {
            tr.diagonal().set(1);
        }

        @Override
        public boolean isDiffuse() {
            return true;
        }

        @Override
        public int getNonStationaryDim() {
            return period - 1;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public void Pi0(Matrix b) {
            b.diagonal().set(1);
        }

        @Override
        public boolean a0(DataBlock a0) {
            return true;
        }

        @Override
        public boolean Pf0(Matrix pf0) {
            V(0, pf0);
            return true;
        }

        @Override
        public void TX(int pos, DataBlock x) {
        }

        @Override
        public void XT(int pos, DataBlock x) {
        }

        @Override
        public void addV(int pos, Matrix p) {
            p.add(V);
        }

    }
}
