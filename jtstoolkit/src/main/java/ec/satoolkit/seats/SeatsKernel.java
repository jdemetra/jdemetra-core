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

import ec.satoolkit.ISeriesDecomposer;
import static ec.satoolkit.seats.IArimaDecomposer.MODEL_DECOMPOSER;
import ec.satoolkit.seats.SeatsSpecification.ApproximationMode;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeatsKernel implements ISeriesDecomposer {

    /**
     *
     */
    public static final String COMPONENTS = "components", DECOMPOSITION = "decomposition";
    private ISeatsToolkit toolkit;

    /**
     * @return the toolkit
     */
    public ISeatsToolkit getToolkit() {
        return toolkit;
    }

    @Override
    public SeatsResults process(final TsData s) {
        InformationSet info = new InformationSet();
        if (toolkit == null) {
            toolkit = SeatsToolkit.create(new SeatsSpecification());
        }
        SeatsContext context = toolkit.getContext();
        SeatsModel model = toolkit.getModelBuilder().build(s, info, context);
        // step 1. Validate the current model;
        validate(model, info, context);
        IModelApproximator approximator = toolkit.getModelApproximator();
        approximator.pretest(model, info, context);
        approximator.startApproximation();
        // step 2. Try to decompose the model
        IArimaDecomposer decomposer = toolkit.getModelDecomposer();
        UcarimaModel ucm = null;
        int nround = 0;
        while (++nround <= 10) {
            ucm = decomposer.decompose(model, info, context);
            if (ucm == null && nround == 1) {
                addWarning(NON_DECOMPOSABLE, model, context);
            }
            if (ucm != null || context.getApproximationMode() == ApproximationMode.None) {
                break;
            }
            if (!approximator.approximate(model, info, context)) {
//                info.addLog(DECOMPOSITION, "Approximation failed");
                break;
            } else {
                model.setChanged(true);
//                info.addLog(DECOMPOSITION, model.getSarima().getSpecification());
            }
        }
        if (ucm == null) {
            throw new SeatsException(SeatsException.ERR_DECOMP);
        }
        SeatsResults results = new SeatsResults();
        results.model = model;
        results.decomposition = ucm;

        results.initialComponents = toolkit.getComponentsEstimator().decompose(model, ucm, info, context);
        results.finalComponents = toolkit.getBiasCorrector().correct(results.initialComponents, info, context);
        results.info_ = info;
        results.addProcessingInformation(context.processingLog);
        return results;
    }

    /**
     * @param toolkit the toolkit to set
     */
    public void setToolkit(ISeatsToolkit toolkit) {
        this.toolkit = toolkit;
    }

    private void validate(SeatsModel model, InformationSet info,
            SeatsContext context) {
        IModelValidator validator = toolkit.getModelValidator();
        ModelStatus status = validator.validate(model.getSarima(), info);
        if (status == ModelStatus.Invalid) {
            throw new SeatsException(SeatsException.ERR_MODEL);
        } else if (status == ModelStatus.Changed) {
            model.setModel(validator.getNewModel());
            model.setCutOff(true);
            info.addWarning("Model adjusted to boundaries");
            addWarning(CUT_OFF, model, context);
        }
    }

    private void addWarning(String msg, SeatsModel model, SeatsContext context) {
        if (context.processingLog != null) {
            context.processingLog.add(ProcessingInformation.warning(MODEL_DECOMPOSER,
                    DefaultModelDecomposer.class.getName(), msg, model.getSarima().clone()));
        }
    }

    private final String NON_DECOMPOSABLE = "Non decomposable model", CUT_OFF = "Parametes cut off";
}
