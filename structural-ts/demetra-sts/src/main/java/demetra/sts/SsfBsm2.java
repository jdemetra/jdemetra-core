/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
 /*
 */
package demetra.sts;

import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;

/**
 *
 * @author Jean Palate
 */
public class SsfBsm2 extends Ssf {

    private SsfBsm2(Bsm2Initialization initialization,Bsm2Dynamics dynamics, ISsfMeasurement measurement) {
        super(initialization, dynamics, measurement);
    }

    /**
     *
     */
    private static int[] calcCmpsIndexes(BasicStructuralModel model) {
        int n = 0;
        if (model.cVar >= 0) {
            ++n;
        }
        if (model.lVar >= 0) {
            ++n;
        }
        if (model.seasVar >= 0) {
            ++n;
        }
        int[] cmps = new int[n];
        int i = 0, j = 0;
        if (model.cVar >= 0) {
            cmps[i++] = j;
            j += 2;
        }
        if (model.lVar >= 0) {
            cmps[i++] = j++;
        }
        if (model.sVar >= 0) {
            ++j;
        }
        if (model.seasVar >= 0) {
            cmps[i] = j;
        }
        return cmps;
    }

    public static SsfBsm2 of(BasicStructuralModel model) {
        int[] idx = calcCmpsIndexes(model);
        SsfBsm.BsmData data=new SsfBsm.BsmData(model);
        Bsm2Initialization initialization = new Bsm2Initialization(data);
        Bsm2Dynamics dynamics = new Bsm2Dynamics(data);
        ISsfMeasurement measurement = Measurement.create(idx);
        if (initialization.isValid()) {
            return new SsfBsm2(initialization, dynamics, measurement);
        } else {
            return null;
        }
    }

    public static class Bsm2Initialization implements ISsfInitialization {

        private final SsfBsm.BsmData data;

                public Bsm2Initialization(SsfBsm.BsmData data) {
            this.data=data;
        }

        @Override
        public boolean isValid() {
            if (data.freq == 1 && data.seasVar >= 0) {
                return false;
            }
            return data.lVar >= 0 || data.sVar >= 0 || data.cVar >= 0;
        }

        @Override
        public int getStateDim() {
            int r = 0;
            if (data.cVar >= 0) {
                r += 2;
            }
            if (data.lVar >= 0) {
                ++r;
            }
            if (data.sVar >= 0) {
                ++r;
            }
            if (data.seasVar >= 0) {
                r += data.freq - 1;
            }
            return r;
        }

         @Override
        public boolean isDiffuse() {
            return data.lVar >= 0 || data.seasVar >= 0;
        }

        @Override
        public int getDiffuseDim() {
            int r = 0;
            if (data.lVar >= 0) {
                ++r;
            }
            if (data.sVar >= 0) {
                ++r;
            }
            if (data.seasVar >= 0) {
                r += data.freq - 1;
            }
            return r;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            int sdim = getStateDim();
            int istart = 0;
            if (data.cVar >= 0) {
                istart += 2;
            }
            int iend = sdim;
            for (int i = istart, j = 0; i < iend; ++i, ++j) {
                b.set(i, j, 1);
            }
        }

        @Override
        public void Pi0(Matrix p) {
            int sdim = getStateDim();
            int istart = 0;
            if (data.cVar >= 0) {
                istart += 2;
            }
            int iend = sdim;
            for (int i = istart; i < iend; ++i) {
                p.set(i, i, 1);
            }
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix p) {
            int i = 0;
            if (data.cVar > 0) {
                double q = data.cVar / (1 - data.cDump * data.cDump);
                p.set(i, i, q);
                ++i;
                p.set(i, i, q);
                ++i;
            }
            if (data.lVar >= 0) {
                if (data.lVar != 0) {
                    p.set(i, i, data.lVar);
                }
                ++i;
            }
            if (data.sVar >= 0) {
                if (data.sVar != 0) {
                    p.set(i, i, data.sVar);
                }
                ++i;
            }
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    p.set(i, i, data.seasVar);
                } else {
                    int j = data.tsvar.getRowsCount();
                    p.extract(i, j, i, j).copy(data.tsvar);
                }
            }
        }

   }
    
    public static class Bsm2Dynamics implements ISsfDynamics {

        private final SsfBsm.BsmData data;

                public Bsm2Dynamics(SsfBsm.BsmData data) {
            this.data=data;
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
            int nr = 0;
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy || data.seasModel == SeasonalModel.Crude) {
                    ++nr;
                } else {
                    nr += data.freq - 1;
                }
            }
            if (data.cVar > 0) {
                nr += 2;
            }
            if (data.lVar > 0) {
                ++nr;
            }
            if (data.sVar > 0) {
                ++nr;
            }
            return nr;
        }

        @Override
        public void V(int pos, Matrix v) {
            int i = 0;
            if (data.cVar >= 0) {
                v.set(i, i, data.cVar);
                ++i;
                v.set(i, i, data.cVar);
                ++i;
            }
            if (data.lVar >= 0) {
                if (data.lVar != 0) {
                    v.set(i, i, data.lVar);
                }
                ++i;
            }
            if (data.sVar >= 0) {
                if (data.sVar != 0) {
                    v.set(i, i, data.sVar);
                }
                ++i;
            }
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    v.set(i, i, data.seasVar);
                } else {
                    int j = data.tsvar.getRowsCount();
                    v.extract(i, j, i, j).copy(data.tsvar);
                }
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, Matrix s) {
            int i = 0, j = 0;
            if (data.cVar > 0) {
                double ce = Math.sqrt(data.cVar);
                s.set(i++, j++, ce);
                s.set(i++, j++, ce);
            } else if (data.cVar == 0) {
                i += 2;
            }
            if (data.lVar > 0) {
                s.set(i++, j++, Math.sqrt(data.lVar));
            } else if (data.lVar == 0) {
                ++i;
            }
            if (data.sVar > 0) {
                s.set(i++, j++, Math.sqrt(data.sVar));
            } else if (data.sVar == 0) {
                ++i;
            }
            if (data.seasVar > 0) {
                switch (data.seasModel) {
                    case Dummy:
                        s.set(i, j, Math.sqrt(data.seasVar));
                        break;
                    case Crude:
                        s.extract(i, data.freq - 1, j,  1).set(Math.sqrt(data.seasVar));
                        break;
                    default:
                        s.extract(i, data.freq - 1, j, data.freq - 1).copy(data.ltsvar);
                        break;
                }
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            int i = 0, j = 0;
            if (data.cVar > 0) {
                double ce = Math.sqrt(data.cVar);
                x.add(i++, u.get(j++) * ce);
                x.add(i++, u.get(j++) * ce);
            } else if (data.cVar == 0) {
                i += 2;
            }
            if (data.lVar > 0) {
                x.add(i++, u.get(j++) * Math.sqrt(data.lVar));
            } else if (data.lVar == 0) {
                ++i;
            }
            if (data.sVar > 0) {
                x.add(i++, u.get(j++) * Math.sqrt(data.sVar));
            } else if (data.sVar == 0) {
                ++i;
            }
            if (data.seasVar > 0) {
                switch (data.seasModel) {
                    case Dummy:
                        x.add(i, u.get(j) * Math.sqrt(data.seasVar));
                        break;
                    case Crude:
                        x.range(i, i + data.freq - 1).add(Math.sqrt(data.seasVar) * u.get(j));
                        break;
                    default:
                        x.range(i, i + data.freq - 1).addProduct(data.ltsvar.rowsIterator(), u.range(j, j + data.freq - 1));
                        break;
                }
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            int i = 0, j = 0;
            if (data.cVar > 0) {
                double ce = Math.sqrt(data.cVar);
                xs.set(j++, x.get(i++) * ce);
                xs.set(j++, x.get(i++) * ce);
            } else if (data.cVar == 0) {
                i += 2;
            }
            if (data.lVar > 0) {
                xs.set(j++, x.get(i++) * Math.sqrt(data.lVar));
            } else if (data.lVar == 0) {
                ++i;
            }
            if (data.sVar > 0) {
                xs.set(j++, x.get(i++) * Math.sqrt(data.lVar));
            } else if (data.sVar == 0) {
                ++i;
            }
            if (data.seasVar > 0) {
                switch (data.seasModel) {
                    case Dummy:
                        xs.set(j, x.get(i) * Math.sqrt(data.seasVar));
                        break;
                    case Crude:
                        xs.set(j, x.range(i, i + data.freq - 1).sum() * Math.sqrt(data.seasVar));
                        break;
                    default:
                        xs.range(j, j + data.freq - 1).product(x.range(i, i + data.freq - 1), data.ltsvar.columnsIterator());
                        break;
                }
            }
        }

        @Override
        public void T(int pos, Matrix tr) {
            int i = 0;
            if (data.cVar >= 0) {
                tr.set(i, i, data.ccos);
                tr.set(i + 1, i + 1, data.ccos);
                tr.set(i, i + 1, data.csin);
                tr.set(i + 1, i, -data.csin);
                i += 2;
            }
            if (data.lVar >= 0) {
                tr.set(i, i, 1);
                if (data.sVar >= 0) {
                    tr.set(i, i + 1, 1);
                    ++i;
                    tr.set(i, i, 1);
                }
                ++i;
            }
            if (data.seasVar >= 0) {
                Matrix seas = tr.extract(i, data.freq - 1, i, data.freq - 1);
                seas.row(data.freq - 2).set(-1);
                seas.subDiagonal(1).set(1);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            int i0 = 0;
            if (data.cVar >= 0) {
                double a = x.get(i0), b = x.get(i0 + 1);
                x.set(i0, a * data.ccos + b * data.csin);
                x.set(i0 + 1, -a * data.csin + b * data.ccos);
                i0 += 2;
            }
            if (data.lVar >= 0) {
                if (data.sVar >= 0) {
                    x.add(i0, x.get(i0 + 1));
                    i0 += 2;
                } else {
                    ++i0;
                }
            }
            if (data.seasVar >= 0) {
                DataBlock ex = x.extract(i0, data.freq - 1, 1);
                ex.bshiftAndNegSum();
            }
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int i0 = 0;
            if (data.cVar >= 0) {
                double a = x.get(i0), b = x.get(i0 + 1);
                x.set(i0, a * data.ccos - b * data.csin);
                x.set(i0 + 1, a * data.csin + b * data.ccos);
                i0 += 2;

            }
            if (data.lVar >= 0) {
                if (data.sVar >= 0) {
                    x.add(i0 + 1, x.get(i0));
                    i0 += 2;
                } else {
                    ++i0;
                }
            }
            if (data.seasVar >= 0) {
                int imax = i0 + data.freq - 2;
                double xs = x.get(imax);
                for (int i = imax; i > i0; --i) {
                    x.set(i, x.get(i - 1) - xs);
                }
                x.set(i0, -xs);
            }
        }

        @Override
        public void addV(int pos, Matrix p) {
            int i = 0;
            if (data.cVar >= 0) {
                p.add(i, i, data.cVar);
                ++i;
                p.add(i, i, data.cVar);
                ++i;
            }
            if (data.lVar >= 0) {
                if (data.lVar != 0) {
                    p.add(i, i, data.lVar);
                }
                ++i;
            }
            if (data.sVar >= 0) {
                if (data.sVar != 0) {
                    p.add(i, i, data.sVar);
                }
                ++i;
            }
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    p.add(i, i, data.seasVar);
                } else {
                    int j = data.tsvar.getRowsCount();
                    p.extract(i, j, i, j).add(data.tsvar);
                }
            }

        }

    }
}
