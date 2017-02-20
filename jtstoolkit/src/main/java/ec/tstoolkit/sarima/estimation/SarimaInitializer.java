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
package ec.tstoolkit.sarima.estimation;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.sarima.SarmaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaInitializer implements IarimaInitializer {

    private static double EPS = 1e-9;

    private boolean failhr_, usedefault_ = true, stabilize_ = true;
    private DataBlock dy_;
    private static final DefaultSarimaInitializer defInitializer
            = new DefaultSarimaInitializer();

    /**
     *
     * @return
     */
    public boolean isfailHR() {
        return failhr_;
    }

    public boolean isStabilizing() {
        return stabilize_;
    }

    public boolean isUsingDefaultIfFailed() {
        return usedefault_;
    }

    public void useDefaultIfFailed(boolean usedef) {
        usedefault_ = usedef;
    }

    public void setFailHR(boolean value) {
        failhr_ = value;
    }

    public void setStabilizing(boolean value) {
        stabilize_ = value;
    }

    public DataBlock differencedResiduals() {
        return dy_;
    }

    /**
     * Initialize the parameters of a given RegArima model. The initialization
     * procedure is the following. If the regression model contains variables,
     * an initial set of residuals is computed by ols. If the ols routine fails,
     * null is returned.
     *
     * @param regs The initial model
     * @return The seasonal stationary arma model that contains the initial
     * parameters
     */
    @Override
    public SarimaModel initialize(RegArimaModel<SarimaModel> regs) {
        SarimaModel sarima = regs.getArima();
        SarimaSpecification spec = sarima.getSpecification();
        SarmaSpecification dspec = spec.doStationary();
        try {
            if (spec.getParametersCount() == 0) {
                return new SarimaModel(dspec);
            }
            dy_ = null;

            HannanRissanen hr = new HannanRissanen();
            if (regs.getDModel().getVarsCount() > 0) {
                Ols ols = new Ols();
                if (!ols.process(regs.getDModel())) {
                    return (SarimaModel) sarima.stationaryTransformation().stationaryModel;
                }
                dy_ = ols.getResiduals();
            } else {
                dy_ = regs.getDModel().getY();
            }
            if (Math.sqrt(dy_.ssq() / dy_.getLength()) < EPS) {
                SarimaModel rslt = new SarimaModel(dspec);
                rslt.setDefault(0, 0);
                return rslt;
            }
            
            if (!hr.process(dy_, dspec)) {
                if (failhr_) {
                    return null;
                } else if (usedefault_) {
                    return defInitializer.doDefaultModel(dspec);
                }
            }
            SarimaModel m = hr.getModel();
            if (!stabilize_) {
                return m;
            }
            if (SarimaMapping.stabilize(m) && failhr_) {
                return null;
            } else {
                return m;
            }
        } catch (BaseException ex) {
            if (failhr_) {
                return null;
            }
            SarimaModel arma = (SarimaModel) sarima.stationaryTransformation().stationaryModel;
            if (usedefault_) {
                arma.setDefault();
            }
            return arma;
        }
    }
}
