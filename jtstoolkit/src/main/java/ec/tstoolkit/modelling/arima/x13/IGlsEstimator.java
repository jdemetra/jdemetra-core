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

package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.arima.estimation.IRegArimaProcessor;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ISsqFunctionMinimizer;
import ec.tstoolkit.maths.realfunctions.ProxyMinimizer;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.estimation.IterativeGlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.SarimaFixedMapping;
import ec.tstoolkit.sarima.estimation.SarimaInitializer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class IGlsEstimator implements IRegArimaProcessor<SarimaModel> {

    public static final double DEF_EPS = 1e-7;
    private double eps_ = DEF_EPS;
    private boolean ml_ = true;
    private boolean usedefault = true;
    private Matrix pcov_;
    private final IParametricMapping<SarimaModel> mapping_;
    private ISsqFunctionMinimizer min_;

    public ISsqFunctionMinimizer getMinimizer() {
        return min_;
    }

    public void setMinimizer(ISsqFunctionMinimizer fmin) {
        min_ = fmin;
    }

    public boolean isMaximumLikelihood() {
        return ml_;
    }

    public boolean isUsingDefaultIfFailed() {
        return usedefault;
    }

    public void setUsingDefaultIfFailed(boolean usedef) {
        usedefault = usedef;
    }

    public void setMaximumLikelihood(boolean ml) {
        ml_ = ml;
    }

    public IGlsEstimator(IParametricMapping<SarimaModel> mapping) {
        //DogLegMethod min=new DogLegMethod();
        //LevenbergMarquardtMinimizer min= new LevenbergMarquardtMinimizer();
        LevenbergMarquardtMethod min = new LevenbergMarquardtMethod();
        //min.setStrict(true);
        min_ = min;
        mapping_ = mapping;
        
    }

    @Override
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs) {

        if (mapping_.getDim() == 0) {
            return compute(regs);
        }
        // use Hannan-Rissanen if there isn't fixed coefficients
        SarimaModel start=null;
        if (mapping_.getDim() == regs.getArima().getParametersCount()) {
            SarimaInitializer initializer = new SarimaInitializer();
            initializer.useDefaultIfFailed(this.usedefault);
            start = initializer.initialize(regs);
        }
        return optimize(regs, start);

        // initilaize using HannanRissanen
    }

    @Override
    public double getPrecision() {
        return eps_;
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs) {
        if (mapping_.getDim() == 0) {
            return compute(regs);
        }
        return optimize(regs, regs.getArma());
    }

    private RegArimaEstimation<SarimaModel> compute(RegArimaModel<SarimaModel> regs) {
        return new RegArimaEstimation<>(regs, regs.computeLikelihood());
    }

    private RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs, SarimaModel start) {
        IterativeGlsSarimaMonitor monitor = new IterativeGlsSarimaMonitor();
        monitor.useLogLikelihood(false);
        monitor.setMinimizer(new ProxyMinimizer(min_));
        monitor.setMapping(mapping_);
        monitor.setPrecision(eps_);
        //       monitor.useMaximumLikelihood(ml_);
        RegArimaEstimation<SarimaModel> rslt = monitor.optimize(regs, start);
        if (rslt == null) {
            return null;
        }
        // if (monitor.hasConverged()) {
        computepvar(monitor, rslt);
        return rslt;
//
//        } else {
//            return null;
//        }
    }

    @Override
    public void setPrecision(double value) {
        eps_ = value;
    }

    private void computepvar(IterativeGlsSarimaMonitor monitor, RegArimaEstimation<SarimaModel> rslt) {
        Matrix curvature = monitor.getCurvature();
        double svar = monitor.getMinimum() / (rslt.likelihood.getDegreesOfFreedom(false, 0));
        if (curvature == null) {
            return;
        }
        pcov_ = SymmetricMatrix.inverse(curvature);
        if (pcov_ == null) {
            return;
        }
        pcov_.mul(svar*2);
        // inflate pcov_, if need be
        if (monitor.getMapping() instanceof SarimaFixedMapping) {
            SarimaFixedMapping mapping = (SarimaFixedMapping) monitor.getMapping();
            pcov_ = mapping.expandCovariance(pcov_);
        }
    }

    Matrix getParametersCovariance() {
        return pcov_;
    }
}
