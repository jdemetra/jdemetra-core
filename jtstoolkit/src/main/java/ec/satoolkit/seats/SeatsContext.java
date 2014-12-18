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
package ec.satoolkit.seats;

import ec.satoolkit.seats.SeatsSpecification.ApproximationMode;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.design.Development;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeatsContext {

    private SeatsSpecification.ApproximationMode changeModel;

    private boolean logTransformed;

    private IModelEstimator estimator;

    /**
     * @since 1.5.4
     */
    public final List<ProcessingInformation> processingLog;

    /**
     *
     */
    @Deprecated
    public SeatsContext() {
        changeModel = ApproximationMode.Legacy;
        processingLog = new ArrayList<>();
    }

    /**
     *
     * @param changeModel
     * @param logTransformed
     */
    public SeatsContext(ApproximationMode changeModel, boolean logTransformed) {
        this.changeModel = changeModel;
        this.logTransformed = logTransformed;
        processingLog = new ArrayList<>();
    }

    /**
     *
     * @param changeModel
     * @param logTransformed
     * @param uselog
     */
    public SeatsContext(ApproximationMode changeModel, boolean logTransformed, boolean uselog) {
        this.changeModel = changeModel;
        this.logTransformed = logTransformed;
        if (uselog) {
            processingLog = new ArrayList<>();
        } else {
            processingLog = null;
        }
    }

    /**
     * @return the estimator
     */
    public IModelEstimator getEstimator() {
        return estimator;
    }

    /**
     * @return the changeModel
     */
    public ApproximationMode getApproximationMode() {
        return changeModel;
    }

    /**
     * @return the logTransformed
     */
    public boolean isLogTransformed() {
        return logTransformed;
    }

    /**
     * @param changeModel the changeModel to set
     */
    public void setApproximationMode(ApproximationMode changeModel) {
        this.changeModel = changeModel;
    }

    /**
     * @param estimator the estimator to set
     */
    public void setEstimator(IModelEstimator estimator) {
        this.estimator = estimator;
    }

    /**
     * @param logTransformed the logTransformed to set
     */
    public void setLogTransformed(boolean logTransformed) {
        this.logTransformed = logTransformed;
    }
}
