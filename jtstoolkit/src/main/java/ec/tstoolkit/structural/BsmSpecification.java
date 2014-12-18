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
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private ModelSpecification mspec_;
    private boolean dregs_ = false;
    private double tol_ = DEF_TOL;
    private Optimizer opt_ = Optimizer.LevenbergMarquardt;

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean read(InformationSet info) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, OPTIMIZER), String.class);
        dic.put(InformationSet.item(prefix, TOL), Double.class);
        dic.put(InformationSet.item(prefix, DREGS), Boolean.class);
        ModelSpecification.fillDictionary(InformationSet.item(prefix, MSPEC), dic);
    }

}
