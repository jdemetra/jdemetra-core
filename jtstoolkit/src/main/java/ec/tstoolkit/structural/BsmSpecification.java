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
package ec.tstoolkit.structural;

import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BsmSpecification implements IProcSpecification, Cloneable {

    public static final String OPTIMIZER = "optimizer", TOL = "tol", DREGS = "diffuseregs",
            MSPEC = "modelspec";

    public static enum Optimizer {

        LevenbergMarquardt, MinPack, LBFGS
    }

    public static final double DEF_TOL = 1e-9;
    public static final Optimizer DEF_OPT = Optimizer.LevenbergMarquardt;
    public static final boolean DEF_DREGS = false;

    private ModelSpecification mspec_;
    private boolean dregs_ = DEF_DREGS;
    private double tol_ = DEF_TOL;
    private Optimizer opt_ = DEF_OPT;

    public BsmSpecification() {
        mspec_ = new ModelSpecification();
    }

    public Optimizer getOptimizer() {
        return opt_;
    }

    public void setOptimizer(Optimizer opt) {
        opt_ = opt;
    }

    public double getPrecision() {
        return tol_;
    }

    public void setPrecision(double tol) {
        tol_ = tol;
    }

    public boolean isDiffuseRegressors() {
        return dregs_;
    }

    public void setDiffuseRegressors(boolean dregs) {
        dregs_ = dregs;
    }

    public ModelSpecification getModelSpecification() {
        return mspec_;
    }

    public void setModelSpecification(ModelSpecification mspec) {
        mspec_ = mspec;
    }

    @Override
    public BsmSpecification clone() {
        try {
            BsmSpecification spec = (BsmSpecification) super.clone();
            spec.mspec_ = mspec_.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (tol_ != DEF_TOL || verbose) {
            info.set(TOL, tol_);
        }
        if (opt_ != DEF_OPT || verbose) {
            info.set(OPTIMIZER, opt_.name());
        }
        if (dregs_ != DEF_DREGS || verbose) {
            info.set(DREGS, dregs_);
        }
        InformationSet mspec = mspec_.write(verbose);
        if (mspec != null) {
            info.set(MSPEC, mspec);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        if (info == null) {
            return true;
        }
        Double tol = info.get(TOL, Double.class);
        if (tol != null) {
            tol_ = tol;
        }
        String opt = info.get(OPTIMIZER, String.class);
        if (opt != null) {
            opt_ = Optimizer.valueOf(opt);
        }
        Boolean dregs = info.get(DREGS, Boolean.class);
        if (dregs != null) {
            dregs_ = dregs;
        }
        return mspec_.read(info.getSubSet(MSPEC));
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof BsmSpecification && equals((BsmSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.dregs_ ? 1 : 0);
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.tol_) ^ (Double.doubleToLongBits(this.tol_) >>> 32));
        hash = 67 * hash + Objects.hashCode(this.opt_);
        return hash;
    }

    private boolean equals(BsmSpecification spec) {
        return spec.dregs_ == dregs_ && spec.opt_ == opt_ && spec.tol_ == tol_
                && Objects.deepEquals(spec.mspec_, mspec_);
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, OPTIMIZER), String.class);
        dic.put(InformationSet.item(prefix, TOL), Double.class);
        dic.put(InformationSet.item(prefix, DREGS), Boolean.class);
        ModelSpecification.fillDictionary(InformationSet.item(prefix, MSPEC), dic);
    }

}
