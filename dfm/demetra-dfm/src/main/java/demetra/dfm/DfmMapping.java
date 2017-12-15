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
package demetra.dfm;


///**
// *
// * @author Jean Palate
// */
//public class DfmMapping implements IDfmMapping {
//
//    static final double EPS = 1e-5;
//
//    private final DynamicFactorModel template;
//    // [0, nml[ loadings
//    // [nml, nml+nm[ meas. variance (square roots)
//    // [nml+nm, nml+nm+nb*nb*nl[ var parameters 
//    // [nml+nb*nb*nl+nm, nml+nb*nb*nl+nm+nb*(nb-1)/2[ trans. covariance (cholesky factor), by row 
//    private final int np;
//    private final int nml, nm, nb, nl;
//    private final int l0, mv0, v0, tv0;
//    private final int ivmax;
//    private final double vmax;
//    private final int[] mmax;
//    private final double[] fmax;
//
//    private IReadDataBlock loadings(IReadDataBlock p) {
//        return l0 < 0 ? null : p.rextract(l0, nml);
//    }
//
//    private IReadDataBlock vparams(IReadDataBlock p) {
//        return v0 < 0 ? null : p.rextract(v0, nb * nb * nl);
//    }
//
//    private IReadDataBlock mvars(IReadDataBlock p) {
//        return mv0 < 0 ? null : p.rextract(mv0, nm);
//    }
//
//    private IReadDataBlock tvars(IReadDataBlock p) {
//        return tv0 < 0 ? null : p.rextract(tv0, nb * (nb + 1) / 2);
//        // return tv0 < 0 ? null : p.rextract(tv0, nb);
//    }
//
//    private DataBlock loadings(DataBlock p) {
//        return l0 < 0 ? null : p.extract(l0, nml);
//    }
//
//    private DataBlock vparams(DataBlock p) {
//        return v0 < 0 ? null : p.extract(v0, nb * nb * nl);
//    }
//
//    private DataBlock mvars(DataBlock p) {
//        return mv0 < 0 ? null : p.extract(mv0, nm);
//    }
//
//    private DataBlock tvars(DataBlock p) {
////        return tv0 < 0 ? null : p.extract(tv0, nb );
//        return tv0 < 0 ? null : p.extract(tv0, nb * (nb + 1) / 2);
//    }
//
//    private void mtvar(Matrix v, IReadDataBlock tv) {
//        int i0 = 0;
//        Matrix tmp = new Matrix(nb, nb);
//        for (int i = 0; i < nb; ++i) {
//            DataBlock x = tmp.row(i).range(0, i + 1);
//            x.copy(tv.rextract(i0, i + 1));
//            i0 += i + 1;
//        }
//        SymmetricMatrix.XXt(tmp.subMatrix(), v.subMatrix());
////        v.set(0);
////        DataBlock d=v.diagonal();
////        d.copy(tv);
////        d.square();
//    }
//
//    public DfmMapping(DynamicFactorModel model) {
//        this(model, false, false);
//    }
//
//    public DfmMapping(DynamicFactorModel model, final boolean mfixed, final boolean tfixed) {
//        template = model.clone();
//        nb = template.getFactorsCount();
//        nl = template.getTransition().nlags;
//        // measurement: all loadings, all var
//        // vparams
//        // covar
//        int p = 0;
//        if (mfixed) {
//            nml = 0;
//            nm = 0;
//            l0 = -1;
//            mv0 = -1;
//            mmax = null;
//            fmax = null;
//            ivmax = -1;
//            vmax = 0;
//            v0 = 0;
//            tv0 = nb * nb * nl;
//            p = nb * nb * nl + nb * (nb + 1) / 2;
//        } else {
//            int n = 0, m = 0;
//            int iv = -1;
//            double v = 0;
//            for (MeasurementDescriptor desc : template.getMeasurements()) {
//                if (desc.var > v) {
//                    v = desc.var;
//                    iv = m;
//                }
//                for (int i = 0; i < nb; ++i) {
//                    if (!Double.isNaN(desc.coeff[i])) {
//                        ++n;
//                    }
//                }
//                ++m;
//            }
//            l0 = 0;
//            ivmax = iv;
//            vmax = v;
//            nm = template.getMeasurementsCount() - 1;
//            if (tfixed) {
//                nml = n;
//                mmax = null;
//                fmax = null;
//                mv0 = nml;
//                v0 = -1;
//                tv0 = -1;
//                p = nm + nml;
//            } else {
//                nml = n - nb;
//                mmax = new int[nb];
//                for (int i = 0; i < nb; ++i) {
//                    mmax[i] = -1;
//                }
//                n = 0;
//                fmax = new double[nb];
//                for (MeasurementDescriptor desc : template.getMeasurements()) {
//                    for (int j = 0; j < nb; ++j) {
//                        double f = desc.coeff[j];
//                        if (!Double.isNaN(f) && (mmax[j] < 0 || Math.abs(f) > fmax[j])) {
//                            mmax[j] = n;
//                            fmax[j] = f;
//                        }
//                    }
//                    ++n;
//                }
//                for (int i = 0; i < nb; ++i) {
//                    if (fmax[i] == 0) {
//                        fmax[i] = 1;
//                    }
//                }
//                mv0 = nml;
//                //         p = tv0 + nb;
//                p += nml + nm;
//                v0 = p;
//                tv0 = p + nb * nb * nl;
//                p = tv0 + nb * (nb + 1) / 2;
//            }
//        }
//        np = p;
//
//    }
//
//    public DataBlock getDefault() {
//        DataBlock p = new DataBlock(np);
//        if (mv0 >= 0) {
//            mvars(p).set(1);
//        }
//        //loadings(p).set(.1);
//        return p;
//    }
//
//    @Override
//    public IReadDataBlock parameters() {
//        return map(template);
//    }
//
//    @Override
//    public IMSsf map(IReadDataBlock p) {
//        DynamicFactorModel m = template.clone();
//        IReadDataBlock l = loadings(p);
//        IReadDataBlock mv = mvars(p);
//        int i0 = 0, j0 = 0;
//        if (l != null) {
//            int n = 0;
//            for (MeasurementDescriptor desc : m.getMeasurements()) {
//                for (int k = 0; k < nb; ++k) {
//                    if (!Double.isNaN(desc.coeff[k])) {
//                        if (mmax == null || n != mmax[k]) {
//                            desc.coeff[k] = l.get(i0++);
//                        } else {
//                            desc.coeff[k] = fmax[k];
//                        }
//                    }
//                }
//                if (n == ivmax) {
//                    desc.var = vmax;
//                } else {
//                    double x = mv.get(j0++);
//                    desc.var = x * x;
//                }
//                ++n;
//            }
//        }
//        IReadDataBlock tv = tvars(p), vp = vparams(p);
//        if (tv != null) {
//            Matrix v = m.getTransition().covar;
//            Matrix t = m.getTransition().varParams;
//            mtvar(v, tv);
//            vp.copyTo(t.internalStorage(), 0);
//        }
//        return m.ssfRepresentation();
//    }
//
//    @Override
//    public IReadDataBlock map(IMSsf mssf) {
//        DynamicFactorModel.Ssf ssf = (DynamicFactorModel.Ssf) mssf;
//        DynamicFactorModel m = ssf.getModel();
//        return map(m);
//    }
//
//    @Override
//    public IReadDataBlock map(DynamicFactorModel m) {
//        // copy to p
//        DataBlock p = new DataBlock(np);
//        DataBlock l = loadings(p);
//        DataBlock mv = mvars(p);
//        int i0 = 0, j0 = 0;
//        if (l != null) {
//            int n = 0;
//            for (MeasurementDescriptor desc : m.getMeasurements()) {
//                for (int k = 0; k < nb; ++k) {
//                    if (!Double.isNaN(desc.coeff[k]) && (mmax == null || n != mmax[k])) {
//                        l.set(i0++, desc.coeff[k]);
//                    }
//                }
//                if (n != ivmax) {
//                    mv.set(j0++, Math.sqrt(desc.var));
//                }
//                ++n;
//            }
//        }
//        DataBlock tv = tvars(p), vp = vparams(p);
//        if (tv != null) {
//            Matrix v = m.getTransition().covar.clone();
//            SymmetricMatrix.lcholesky(v);
//            i0 = 0;
//            for (int i = 0; i < nb; ++i) {
//                tv.extract(i0, i + 1).copy(v.row(i).range(0, i + 1));
//                i0 += i + 1;
//            }
////            tv.copy(m.getTransition().covar.diagonal());
////            tv.sqrt();
//            Matrix t = m.getTransition().varParams;
//            vp.copyFrom(t.internalStorage(), 0);
//        }
//        return p;
//    }
//
//    @Override
//    public boolean checkBoundaries(IReadDataBlock inparams) {
//        // check the stability of VAR
//        try {
//            IReadDataBlock vp = vparams(inparams);
//            if (vp == null) {
//                return true;
//            }
//        // s=(f0,t f1,t f2,t f0,t-1 f1,t-1 f2,t-1 ...f0,t-l+1 f1,t-l+1 f2,t-l+1)
//            //    |x00 x10 x20   
//            // T =|...
//            // T =|1   0   0
//            //    |0   1   0
//            //    |...
//            Matrix Q = new Matrix(nb * nl, nb * nl);
//            for (int i = 0, i0 = 0; i < nb; ++i) {
//                for (int l = 0; l < nl; ++l, i0 += nb) {
//                    DataBlock c = Q.column(l * nb + i).range(0, nb);
//                    c.copy(vp.rextract(i0, nb));
//                }
//            }
//            Q.subDiagonal(-nb).set(1);
//            IEigenSystem es = EigenSystem.create(Q, false);
//            Complex[] ev = es.getEigenValues();
//            return max(ev) < 1;
//        } catch (MatrixException err) {
//            return false;
//        }
////        return true;
//    }
//
//    static double max(Complex[] v) {
//        if (v == null || v.length == 0) {
//            return 0;
//        }
//        double m = v[0].abs();
//        for (int i = 1; i < v.length; ++i) {
//            double cur = v[i].abs();
//            if (cur > m) {
//                m = cur;
//            }
//        }
//        return m;
//    }
//
//    @Override
//    public double epsilon(IReadDataBlock inparams, int idx) {
//        return inparams.get(idx) > 0 ? -EPS : EPS;
//    }
//
//    @Override
//    public int getDim() {
//        return np;
//    }
//
//    @Override
//    public double lbound(int idx) {
//        return -Double.MAX_VALUE;
//    }
//
//    @Override
//    public double ubound(int idx) {
//        return Double.MAX_VALUE;
//    }
//
//    @Override
//    public ParamValidation validate(IDataBlock ioparams) {
//        return checkBoundaries(ioparams) ? ParamValidation.Valid : ParamValidation.Invalid;
//    }
//
//    @Override
//    public String getDescription(int idx) {
//        return PARAM + idx;
//    }
//}
