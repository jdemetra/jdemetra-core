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


/**
 *
 * @author Jean Palate
 */
//public class SimpleDfmMapping implements IParametricMapping<IMSsf> {
//
//    private final DynamicFactorModel template;
//    // [0, nml[ loadings
//    // [nml, nml+nm[ meas. variance (square roots)
//    // [nml+nm, nml+nm+nb*nb*nl[ var parameters 
//    private final int np;
//    private final int nml, nm, nb, nl;
//    private final int l0, mv0, v0;
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
//    public SimpleDfmMapping(DynamicFactorModel model) {
//        template = model.clone();
//        template.normalize();
//        template.getTransition().covar.set(0);
//        template.getTransition().covar.diagonal().set(1);
//        nb = template.getFactorsCount();
//        Matrix vp = template.getTransition().varParams;
//        for (int i = 0; i < vp.getColumnsCount(); ++i) {
//            if (i % template.getTransition().nlags != 0) {
//                vp.column(i).set(0);
//            }
//        }
//        nl = 1;
//        nm = template.getMeasurementsCount() - 1;
//        // measurement: all loadings, all var
//        // vparams
//        // covar
//        int p = 0;
//        int n = 0;
//        for (DynamicFactorModel.MeasurementDescriptor desc : template.getMeasurements()) {
//            for (int j = 0; j < nb; ++j) {
//                if (!Double.isNaN(desc.coeff[j])) {
//                    ++n;
//                }
//            }
//        }
//        nml = n;
//        l0 = 0;
//        mv0 = nml;
//        p += nml + nm;
//        v0 = p;
//        p += nl * nb * nb;
//        np = p;
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
//    public IMSsf map(IReadDataBlock p) {
//        DynamicFactorModel m = template.clone();
//        IReadDataBlock l = loadings(p);
//        IReadDataBlock mv = mvars(p);
//        int i0 = 0, j0 = 0;
//        if (l != null) {
//            int n = 0;
//            for (DynamicFactorModel.MeasurementDescriptor desc : m.getMeasurements()) {
//                for (int j = 0; j < nb; ++j) {
//                    if (!Double.isNaN(desc.coeff[j])) {
//                        desc.coeff[j] = l.get(i0++);
//                    }
//                }
//                if (n > 0) {
//                    double x = mv.get(j0++);
//                    desc.var = x * x;
//                }
//                ++n;
//            }
//        }
//        IReadDataBlock vp = vparams(p);
//        if (vp != null) {
//            Matrix t = m.getTransition().varParams;
//            int mnl = template.getTransition().nlags;
//            i0 = 0;
//            for (int i = 0; i < nb; ++i) {
//                for (int j = 0; j < nb; ++j) {
//                    t.set(i, j * mnl, vp.get(i0++));
//                }
//            }
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
//    public IReadDataBlock map(DynamicFactorModel m) {
//        // copy to p
//        DataBlock p = new DataBlock(np);
//        DataBlock l = loadings(p);
//        DataBlock mv = mvars(p);
//        int i0 = 0, j0 = 0;
//        if (l != null) {
//            int n = 0;
//            for (DynamicFactorModel.MeasurementDescriptor desc : m.getMeasurements()) {
//                for (int j = 0; j < nb; ++j) {
//                    if (!Double.isNaN(desc.coeff[j])) {
//                        l.set(i0++, desc.coeff[j]);
//                    }
//                }
//                if (n > 0) {
//                    mv.set(j0++, Math.sqrt(desc.var));
//                }
//                ++n;
//            }
//        }
//        DataBlock vp = vparams(p);
//        if (vp != null) {
//            Matrix t = m.getTransition().varParams;
//            int mnl = template.getTransition().nlags;
//            i0 = 0;
//            for (int i = 0; i < nb; ++i) {
//                for (int j = 0; j < nb; ++j) {
//                    vp.set(i0++, t.get(i, j * mnl));
//                }
//            }
//        }
//        return p;
//    }
//
//    @Override
//    public boolean checkBoundaries(IReadDataBlock inparams) {
//        IReadDataBlock vp = vparams(inparams);
//        if (vp != null) {
//            int i0 = 0;
//            for (int i = 0; i < nb; ++i) {
//                for (int j = 0; j < nb; ++j) {
//                    if (Math.abs(vp.get(i0++)) > .99) {
//                        return false;
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public double epsilon(IReadDataBlock inparams, int idx) {
//        return 1e-6;
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
//        return ParamValidation.Valid;
//    }
//
//    public void validate(DynamicFactorModel model) {
//        Matrix m = model.getTransition().varParams;
//        Matrix vp = m.clone();
//        m.set(0);
//        int l = model.getTransition().nlags;
//        for (int i = 0; i < nb; ++i) {
//            double r = vp.get(i, i * l);
//            if (Math.abs(r) > 1) {
//                r = Math.signum(r) * Math.min(.99, 1 / Math.abs(r));
//            }
//            m.set(i, i * l, r);
//        }
//    }
//
//    @Override
//    public String getDescription(int idx) {
//        return PARAM + idx;
//    }
//
//}
