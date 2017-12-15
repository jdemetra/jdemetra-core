/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.dfm;

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.dfm.internal.SsfDfm;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.var.VarDescriptor;
import demetra.var.VarSpecification;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */

public class DynamicFactorModel  {

    private int nlx;
    private VarDescriptor vdesc;
    private ISsfInitialization.Type initialization = ISsfInitialization.Type.Unconditional;
    private List<MeasurementDescriptor> mdesc = new ArrayList<>();
    private Matrix V0;

    /**
     * Creates a new dynamic factors model
     *
     * @param nlags The number of lags for each factors (in [t, t-c[) that has to be
     * integrated in the model
     * @param nf The number of factors
     */
    public DynamicFactorModel(int nlags) {
        nlx = nlags;
    }

    public void rescaleVariances(double cvar) {
        for (MeasurementDescriptor m : mdesc) {
            m.rescaleVariance(cvar);
        }
        vdesc.rescaleVariance(cvar);
        if (V0 != null) {
            V0.mul(cvar);
        }
    }

    /**
     * Rescale the model so that the variances of the transition shocks are
     * equal to 1. The method divides each factor by the standard deviation of
     * the corresponding transition shock and updates the different coefficients
     * accordingly.
     */
    public void normalize() {
        // scaling factors
        int nl = vdesc.getLagsCount(), nf=vdesc.getVariablesCount();
        double[] w = new double[nf];
        vdesc.getInnovationsVariance().diagonal().copyTo(w, 0);
        for (int i = 0; i < nf; ++i) {
            w[i] = Math.sqrt(w[i]);
        }
        if (V0 != null) {
            for (int i = 0; i < nf; ++i) {
                for (int j = 0; j < nf; ++j) {
                    V0.extract(i * nlx, nlx, j * nlx, nlx).mul(1 / (w[i] * w[j]));
                }
            }
        }
        // covar
        for (int i = 0; i < nf; ++i) {
            if (w[i] != 0) {
                vdesc.getInnovationsVariance().set(i, i, 1);
                for (int j = 0; j < i; ++j) {
                    if (w[j] != 0) {
                        vdesc.getInnovationsVariance().mul(i, j, 1 / (w[i] * w[j]));
                    }
                }
            }
        }
        SymmetricMatrix.fromLower(vdesc.getInnovationsVariance());
        // varParams
        for (int i = 0; i < nf; ++i) {
            if (w[i] != 0) {
                DataWindow range = vdesc.getVarMatrix().row(i).left();
                for (int j = 0; j < nf; ++j) {
                    if (w[j] != 0 && i != j) {
                        range.next(nl).mul(w[j] / w[i]);
                    }
                }
            }
        }
        // loadings
        for (MeasurementDescriptor desc : mdesc) {
            for (int i = 0; i < nf; ++i) {
                if (desc.isUsed(i)) {
                    desc.rescaleCoefficient(i, w[i]);
                }
            }
        }
    }

    /**
     * Rescale the model so that the variance of the transition is I. The method
     * pre-multiplies the factor by the inverse of the Cholesky factor of the
     * covariance matrix of the transition innovations. The different
     * coefficients are updated accordingly
     *
     * @throws A DfmException is thrown when the loadings are not compatible
     * with the triangular transformation implied by Cholesky
     */
    public void lnormalize() {
        int nf=vdesc.getVariablesCount(), nl=vdesc.getLagsCount();
        if (vdesc.getInnovationsVariance().isIdentity())
            return;
        Matrix L = vdesc.getInnovationsVariance().deepClone();
        SymmetricMatrix.lcholesky(L);
        // L contains the Cholesky factor

        // transform the loadings
        // y = C*f + e <-> y = (C*L)*L^-1*f+e
        // B = C*L
        // loadings
        for (MeasurementDescriptor desc : mdesc) {
            double[] c = desc.getCoefficients();
            for (int i = 0; i < nf; ++i) {
                double z = 0;
                boolean nd = false;
                for (int j = i; j < nf; ++j) {
                    if (!Double.isNaN(c[j])) {
                        if (nd) {
                            throw new DfmException("Unsupported model");
                        }
                        z += c[j] * L.get(j, i);
                    } else {
                        nd = true;
                    }
                }
                if (desc.isUsed(i)) {
                    desc.setCoefficient(i, z);
                }
            }
        }
        // transform the var
        // f(t) = A f(t-1) + u(t)
        //L^-1*f(t) = L^-1*A*L*L^-1* f(t-1) + e(t)
        // C=L^-1*A*L <-> LC=AL
        
        for (int i = 1; i <= nl; ++i) {
            Matrix A = vdesc.getA(i);
            // AL
            LowerTriangularMatrix.lmul(L, A);
            // LC = (AL)
            LowerTriangularMatrix.rsolve(L, A);
            vdesc.setA(i, A);
        }
        vdesc.getInnovationsVariance().set(0);
        vdesc.getInnovationsVariance().diagonal().set(1);
        if (V0 != null) {
            // L^-1*V*L^-1' =W <-> L(WL')=V <-> LX=V, WL'=X or LW'=X'
            Matrix V0 = Matrix.square(nf * nlx);
            for (int i = 0; i < nlx; ++i) {
                for (int j = 0; j <nlx; ++j) {
                    Matrix t = Matrix.square(nf);
                    for (int k = 0; k < nf; ++k) {
                        for (int l = 0; l < nf; ++l) {
                            t.set(k, l, this.V0.get(k * nlx + i, l * nlx + j));
                        }
                    }
                    LowerTriangularMatrix.rsolve(L, t);
                    LowerTriangularMatrix.rsolve(L, t.transpose());
                    for (int k = 0; k < nf; ++k) {
                        for (int l = 0; l < nf; ++l) {
                            V0.set(k * nlx + i, l * nlx + j, t.get(k, l));
                        }
                    }
                }
            }
            this.V0=V0;
        }
    }

//    @Override
//    public DynamicFactorModel clone() {
//        try {
//            DynamicFactorModel m = (DynamicFactorModel) super.clone();
//            VarDescriptor td = new VarDescriptor(nf_, vdesc.nlags);
//            td.innovationCovariance.copy(vdesc.covar);
//            td.varParams.copy(vdesc.varParams);
//            m.vdesc = td;
//            m.mdesc = new ArrayList<>();
//            for (MeasurementDescriptor md : mdesc) {
//                m.mdesc.add(new MeasurementDescriptor(
//                        md.type, md.coeff.clone(), md.var));
//            }
//            if (V0 != null) {
//                m.V0 = V0.clone();
//            }
//            return m;
//        } catch (CloneNotSupportedException ex) {
//            throw new AssertionError();
//        }
//    }
//
    /**
     * Copies the parameters of a given model in this object
     *
     * @param m The model being copied
     * @return True if the models have the same structure and can be copied
     * false otherwise. Models have the same structure means that they have: -
     * same VAR structure (number of factors, number of lags) - same number of
     * measurement equations
     */
    public boolean copy(DynamicFactorModel m) {
        int nf=vdesc.getVariablesCount(), nl=vdesc.getLagsCount();
        if (nf != m.vdesc.getVariablesCount()
                || nl != m.vdesc.getLagsCount()
                || mdesc.size() != m.mdesc.size()) {
            return false;
        }
        vdesc.copy(m.vdesc);
        for (int i = 0; i < mdesc.size(); ++i) {
            MeasurementDescriptor s = m.mdesc.get(i),
                    t = mdesc.get(i);
            t.copy(s);
        }
        if (m.V0 != null) {
            V0 = m.V0.deepClone();
        } else {
            V0 = null;
        }
        return true;
    }

//    /**
//     * Compacts the factors of a given models
//     *
//     * @param from The first factor to merge
//     * @param to The last factor (included) to merge
//     * @return A new model is returned. It should be re-estimated.
//     */
//    public DynamicFactorModel compactFactors(int from, int to) {
//        if (from < 0 || to < from || to >= nf_) {
//            return null;
//        }
//        if (to == from) {
//            return clone();
//        }
//        int nc = to - from;
//        DynamicFactorModel m = new DynamicFactorModel(nlx, nf_ - nc);
//        TransitionDescriptor td = new TransitionDescriptor(nf_ - nc, vdesc.nlags);
//        m.vdesc = td;
//        m.vdesc.covar.diagonal().set(1);
//        for (MeasurementDescriptor md : mdesc) {
//            double[] ncoeff = new double[nf_ - nc];
//            for (int i = 0; i < from; ++i) {
//                ncoeff[i] = md.coeff[i];
//            }
//            for (int i = to + 1; i < nf_; ++i) {
//                ncoeff[i - nc] = md.coeff[i];
//            }
//            boolean used = false;
//            for (int i = from; i <= to; ++i) {
//                if (!Double.isNaN(md.coeff[i])) {
//                    used = true;
//                    break;
//                }
//            }
//            if (!used) {
//                ncoeff[from] = Double.NaN;
//            }
//            m.mdesc.add(new MeasurementDescriptor(
//                    md.type, ncoeff, 1));
//        }
//        return m;
//    }
//
    /**
     * The number of lags for each factor
     *
     * @return
     */
    public int getBlockLength() {
        return nlx;
    }

    /**
     * The number of factors
     *
     * @return
     */
    public int getFactorsCount() {
        return vdesc.getVariablesCount();
    }

    /**
     * Changes the number of lags of each factor that is included in the model
     *
     * @param c The size of each block of factors (lags in [t, t-c[ belong to
     * the model). c should larger or equal to the number of lags in the
     * transition equation.
     * @throws DfmException is thrown when the model is invalid (see above)
     */
    public void setBlockLength(int c) throws DfmException {
        if (vdesc != null && c < vdesc.getLagsCount()) {
            throw new DfmException(DfmException.INVALID_MODEL);
        }
        nlx = c;
    }

    /**
     * Sets a new descriptor for the transition equation (VAR model)
     *
     * @param desc The descriptor of the transition equation
     * @throws DfmException is thrown when the model is invalid
     */
    public void setTransition(VarDescriptor desc) throws DfmException {
        if ( nlx < desc.getLagsCount()) {
            throw new DfmException(DfmException.INVALID_MODEL);
        }
        vdesc = desc;
    }

    /**
     *
     * @return
     */
    public VarDescriptor getVarDescriptor() {
        return vdesc;
    }

    /**
     *
     * @return
     */
    public List<MeasurementDescriptor> getMeasurements() {
        return Collections.unmodifiableList(mdesc);
    }

    /**
     *
     * @param desc
     */
    public void addMeasurement(MeasurementDescriptor desc) {
        mdesc.add(desc);
    }

    public void clearMeasurements() {
        mdesc.clear();
    }

    /**
     *
     * @return
     */
    public IMultivariateSsf ssfRepresentation() {
        return SsfDfm.of(vdesc, mdesc.toArray(new MeasurementDescriptor[mdesc.size()]), nlx, V0);
    }

    /**
     *
     * @return
     */
    public int getMeasurementsCount() {
        return mdesc.size();
    }

    /**
     *
     * @param init
     */
    public void setInitialization(ISsfInitialization.Type init) {
        initialization = init;
        if (initialization != ISsfInitialization.Type.UserDefined) {
            V0 = null;
        }
    }

    /**
     *
     * @return
     */
    public ISsfInitialization.Type getInitialization() {
        return initialization;
    }

    /**
     *
     * @param v0
     */
    public void setInitialCovariance(Matrix v0) {
        V0 = v0.deepClone();
        initialization = ISsfInitialization.Type.UserDefined;
    }

//    /**
//     *
//     * @return True if the model has been changed
//     */
//    public boolean validate() {
//        boolean rslt = false;
//        DfmMapping mapping = new DfmMapping(this);
//        if (!mapping.checkBoundaries(mapping.parameters())) {
//            // set default values for the VAR matrix
//            tdesc_.varParams.set(0);
//            for (int j = 0; j < nf_; ++j) {
//                tdesc_.varParams.set(j, j * tdesc_.nlags, AR_DEF);
//            }
//            rslt = true;
//        }
//
//        Matrix v = this.tdesc_.covar.clone();
//        try {
//            SymmetricMatrix.lcholesky(v);
//            return rslt;
//        } catch (MatrixException err) {
//            DataBlock d = v.diagonal().deepClone();
//            this.tdesc_.covar.set(0);
//            this.tdesc_.covar.diagonal().copy(d);
//            return true;
//        }
//
//    }
//
    public void setDefault() {
        vdesc.setDefault();
        for (MeasurementDescriptor m : this.mdesc) {
            m.setDefault();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Loadings").append("\r\n");
        for (MeasurementDescriptor m : mdesc) {
            builder.append(m).append("\r\n");
        }
        builder.append("VAR").append("\r\n");
        builder.append(vdesc.getVarMatrix());
        builder.append(vdesc.getInnovationsVariance());
        return builder.toString();
    }

}
