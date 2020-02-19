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

import demetra.design.Development;
import demetra.seats.SeatsSpec;
import jdplus.regsarima.GlsSarimaProcessor;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SeatsToolkit {

    public static SeatsToolkit of(SeatsSpec spec) {
        DefaultModelValidator validator = DefaultModelValidator.builder()
                .xl(spec.getModelSpec().getXlBoundary())
                .build();

        DefaultModelEstimator estimator = new DefaultModelEstimator(validator, GlsSarimaProcessor.PROCESSOR);

        DefaultModelDecomposer decomposer = new DefaultModelDecomposer(spec.getDecompositionSpec());

        IModelApproximator approximator;
        switch (spec.getDecompositionSpec().getApproximationMode()) {
            case None:
                approximator = null;
                break;
            default:
                approximator = new DefaultModelApproximator(estimator);
        }

        IComponentsEstimator cmpEstimator;
        switch (spec.getComponentsSpec().getMethod()) {
            case KalmanSmoother:
                cmpEstimator = new KalmanEstimator();
                break;
            case McElroyMatrix:
                cmpEstimator = new MatrixEstimator();
                break;
            default:
                cmpEstimator = new WienerKolmogorovEstimator();
                break;
        }

        IBiasCorrector bias;
        switch (spec.getBiasSpec().getBiasCorrection()) {
            case Legacy:
                bias = new DefaultBiasCorrector(true);
                break;
            default:
                bias = new DefaultBiasCorrector(false);
        }

        return builder()
                .modelBuilder((s, p) -> SeatsModel.of(s, p, spec))
                .modelValidator(validator)
                .modelApproximator(approximator)
                .modelDecomposer(decomposer)
                .componentsEstimator(cmpEstimator)
                .biasCorrector(bias)
                .build();

    }

    private IModelBuilder modelBuilder;

    private IModelValidator modelValidator;

    private IModelApproximator modelApproximator;

    private IModelDecomposer modelDecomposer;

    private IComponentsEstimator componentsEstimator;

    private IBiasCorrector biasCorrector;

//    /**
//     *
//     * @param spec
//     * @return
//     */
//    public static SeatsToolkit create(SeatsSpecification spec) {
//        DefaultModelValidator validator = new DefaultModelValidator();
//        validator.setXl(spec.getXlBoundary());
//
//        SeatsContext context = new SeatsContext(spec.getApproximationMode(), spec.isLog());
//        SeatsToolkit toolkit = new SeatsToolkit(context);
//        context.setEstimator(new DefaultModelEstimator(validator));
//
//        DefaultModelBuilder builder = new DefaultModelBuilder(spec.getArima());
//        toolkit.modelBuilder = builder;
//
//        toolkit.modelValidator = validator;
//
//        DefaultModelApproximator3 approximator = new DefaultModelApproximator3();
//        toolkit.modelApproximator = approximator;
//
//        DefaultModelDecomposer decomposer = new DefaultModelDecomposer(spec.getApproximationMode() == ApproximationMode.Noisy);
//        decomposer.setEpsphi(spec.getSeasTolerance());
//        decomposer.setRmod(spec.getTrendBoundary());
//        decomposer.setSmod(spec.getSeasBoundary());
//        decomposer.setSmod1(spec.getSeasBoundary1());
//        toolkit.modelDecomposer = decomposer;
//
//        IComponentsEstimator cmpEstimator;
//        switch (spec.getMethod()) {
//            case KalmanSmoother:
//                cmpEstimator = new KalmanEstimator();
//                break;
//            case McElroyMatrix:
//                cmpEstimator = new MatrixEstimator();
//                break;
//            default:
//                cmpEstimator = new WienerKolmogorovEstimator();
//                break;
//        }
//        toolkit.componentsEstimator = cmpEstimator;
//
//        DefaultBiasCorrector corrector = new DefaultBiasCorrector();
//        toolkit.biasCorrector = corrector;
//        return toolkit;
//    }
//
//    private final SeatsContext context;
//
//    private IModelBuilder modelBuilder;
//
//    private IModelValidator modelValidator;
//
//    private IModelApproximator modelApproximator;
//
//    private IArimaDecomposer modelDecomposer;
//
//    private IComponentsEstimator componentsEstimator;
//
//    private IBiasCorrector biasCorrector;
//
//    SeatsToolkit(SeatsContext context) {
//        this.context = context;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public IBiasCorrector getBiasCorrector() {
//        return biasCorrector;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public IComponentsEstimator getComponentsEstimator() {
//        return componentsEstimator;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public SeatsContext getContext() {
//        return context;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public IModelApproximator getModelApproximator() {
//        return modelApproximator;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public IArimaDecomposer getModelDecomposer() {
//        return modelDecomposer;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public IModelValidator getModelValidator() {
//        return modelValidator;
//    }
//
//    @Override
//    public IModelBuilder getModelBuilder() {
//        return modelBuilder;
//    }
}
