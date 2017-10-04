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
package demetra.arima.regarima.internal;

import demetra.arima.IArimaModel;
import demetra.design.Immutable;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
@Immutable
public class RegArmaEstimation<S extends IArimaModel> {

    private final RegArmaModel<S> model;
    private final double objective;
    private final boolean converged;
    private final double[] score;
    private final Matrix information;
    private final int ndf;

    RegArmaEstimation(final RegArmaModel<S> model, final double objective, final boolean converged, final double[] score,
            final Matrix information, final int ndf) {
        this.model = model;
        this.objective = objective;
        this.converged = converged;
        this.score = score;
        this.information=information;
        this.ndf=ndf;
    }

    /**
     * @return the model
     */
    public RegArmaModel<S> getModel() {
        return model;
    }

    /**
     * @return the objective
     */
    public double getObjective() {
        return objective;
    }

    /**
     * @return the converged
     */
    public boolean isConverged() {
        return converged;
    }
    
    public int getDegreesOfFreedom(){
        return ndf;
    }

    /**
     * @return the curvature
     */
    public Matrix information() {
        return information.deepClone();
    }

    /**
     * @return the score
     */
    public double[] score() {
        return score.clone();
    }

}
