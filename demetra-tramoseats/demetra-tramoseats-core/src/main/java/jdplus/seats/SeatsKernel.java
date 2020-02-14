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
package jdplus.seats;

import demetra.data.DoubleSeq;
import demetra.design.Development;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.sa.SeriesDecomposition;
import demetra.seats.SeatsException;
import jdplus.ucarima.UcarimaModel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class SeatsKernel {

    public static final String SEATS = "Seats";
    public static final String MODEL = "modelling", VALIDATION = "validation", DECOMPOSITION = "decomposition",
            ESTIMATION = "estimation", BIAS = "bias correction";

    private final SeatsToolkit toolkit;

    /**
     * @return the toolkit
     */
    public SeatsToolkit getToolkit() {
        return toolkit;
    }

    public SeatsKernel(@NonNull SeatsToolkit toolkit) {
        this.toolkit = toolkit;
    }

    public SeatsResults process(final DoubleSeq series, int period, ProcessingLog log) {
        log.push(SEATS);
        // step 0. Build the model
        SeatsModel model=buildModel(series, period, log);
        // step 1. Validate the current model;
        validate(model, log);
        // step 2. Try to decompose the model
        decomposeModel(model, log);
        // step 3. Computation of the components
        estimateComponents(model, log);
        // step 4. Bias correction
        biasCorrection(model, log);
        log.pop();
        return results(model);
    }
    
    private SeatsModel buildModel(DoubleSeq series, int period, ProcessingLog log){
        log.push(MODEL);
        SeatsModel model = toolkit.getModelBuilder().build(series, period);
        model.setCurrentModel(model.getOriginalModel());
        log.pop();
        return model;
    }

    private void validate(SeatsModel model, ProcessingLog log) {
        log.push(VALIDATION);
        IModelValidator validator = toolkit.getModelValidator();
        if (!validator.validate(model.getCurrentModel())) {
            model.setCurrentModel(validator.getNewModel());
            log.info(CUT_OFF);
        }
        log.pop();
    }

    private final String NON_DECOMPOSABLE = "Non decomposable model",
            CUT_OFF = "Arima parameters cut off",
            APPROXIMATION = "Model replaced by an approximation",
            NOISY = "Noisy model used";


    private void decomposeModel(SeatsModel model, ProcessingLog log) {
        log.push(DECOMPOSITION);
        IModelApproximator approximator = toolkit.getModelApproximator();
        IModelDecomposer decomposer = toolkit.getModelDecomposer();
        UcarimaModel ucm = null;
        int nround = 0;
        while (++nround <= 10) {
            log.step("Canonical decomposition");
            ucm = decomposer.decompose(model.getCurrentModel(), model.getOriginalModel().getFrequency());
            if (ucm == null && nround == 1) {
                log.warning(NON_DECOMPOSABLE);
            }
            if (ucm != null || approximator == null) {
                break;
            }
            if (!approximator.approximate(model)) {
                break;
            } else {
                log.step(APPROXIMATION, model.getCurrentModel().orders());
            }
        }
        if (ucm == null) {
            throw new SeatsException(SeatsException.ERR_DECOMP);
        }
        if (!ucm.getModel().equals(model.getCurrentModel())) {
            log.warning(NOISY);
        }
        model.setUcarimaModel(ucm);

        log.pop();
    }

    private void estimateComponents(SeatsModel model, ProcessingLog log) {
        log.step(ESTIMATION);
        IComponentsEstimator componentsEstimator = toolkit.getComponentsEstimator();
        model.setInitialComponents(componentsEstimator.decompose(model));
    }

    private void biasCorrection(SeatsModel model, ProcessingLog log) {
        log.step(BIAS);
        IBiasCorrector bias = toolkit.getBiasCorrector();
        if (bias != null) {
            bias.correctBias(model);
        }
    }
    
    private SeatsResults results(SeatsModel model){
        return new SeatsResults(model.getOriginalModel(), model.getCurrentModel(), 
                model.isMeanCorrection(), model.getUcarimaModel(),
                model.getInitialComponents(), model.getFinalComponents());
        
    }
}
