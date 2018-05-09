///*
// * Copyright 2013 National Bank of Belgium
// *
// * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
// * by the European Commission - subsequent versions of the EUPL (the "Licence");
// * You may not use this work except in compliance with the Licence.
// * You may obtain a copy of the Licence at:
// *
// * http://ec.europa.eu/idabc/eupl
// *
// * Unless required by applicable law or agreed to in writing, software 
// * distributed under the Licence is distributed on an "AS IS" basis,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the Licence for the specific language governing permissions and 
// * limitations under the Licence.
// */
//package demetra.x12;
//
//import demetra.design.BuilderPattern;
//import demetra.design.Development;
//import demetra.modelling.regression.IEasterVariable;
//import demetra.regarima.IRegArimaProcessor;
//import demetra.regarima.ami.IRegressionModule;
//import demetra.regarima.ami.ProcessingResult;
//import demetra.regarima.ami.RegArimaUtility;
//import demetra.regarima.ami.SarimaComponent;
//import demetra.regarima.regular.AICcComparator;
//import demetra.regarima.regular.IModelComparator;
//import demetra.regarima.regular.ModelDescription;
//import demetra.regarima.regular.ModelEstimation;
//import demetra.regarima.regular.RegArimaContext;
//import demetra.sarima.GlsSarimaProcessor;
//import demetra.sarima.RegSarimaProcessor;
//import demetra.sarima.SarimaModel;
//
///**
// *
// * @author Jean Palate
// */
//@Development(status = Development.Status.Preliminary)
//public class EasterDetectionModule implements IRegressionModule {
//
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    @BuilderPattern(EasterDetectionModule.class)
//    public static class Builder {
//
//        private IEasterVariable[] easters;
//        private IModelComparator comparator = new AICcComparator(0);
//        private double eps = 1e-5;
//
//        public Builder easters(IEasterVariable[] easters) {
//            this.easters = easters;
//            return this;
//        }
//
//        public Builder estimationPrecision(double eps) {
//            this.eps = eps;
//            return this;
//        }
//
//        public Builder modelComparator(IModelComparator comparator) {
//            this.comparator = comparator;
//            return this;
//        }
//
//        public EasterDetectionModule build() {
//            return new EasterDetectionModule(this);
//        }
//    }
//
//    private final IModelComparator comparator;
//    private final IEasterVariable[] easters;
//    private final double eps;
//
//    public EasterDetectionModule(Builder builder) {
//        this.comparator = builder.comparator;
//        this.easters = builder.easters;
//        this.eps = builder.eps;
//    }
//
//
//    @Override
//    public ProcessingResult test(RegArimaContext context) {
//        ModelDescription description = context.getDescription();
//        IRegArimaProcessor<SarimaModel> processor=RegArimaUtility.processor(true, eps);
//        int n = easters.length;
//        int icur = -1;
//        ModelEstimation[] emodels = new ModelEstimation[n];
//        ModelDescription[] desc = new ModelDescription[n];
//        int nhp = mapping.getDim();
//        if (context.estimation == null) {
//            context.estimation = new ModelEstimation(context.description.buildRegArima(), context.description.getLikelihoodCorrection());
//            context.estimation.compute(monitor, nhp);
//        }
//        ModelDescription refdesc = context.description.clone();
//        ModelEstimation refest = null;
//        if (!PreprocessingModelBuilder.updateEaster(refdesc, 0)) {
//            refest = context.estimation;
//        } else {
//            refest = new ModelEstimation(refdesc.buildRegArima(), refdesc.getLikelihoodCorrection());
//            refest.compute(monitor, nhp);
//        }
//
//        for (int i = 0; i < n; ++i) {
//            desc[i] = context.description.clone();
//            if (!PreprocessingModelBuilder.updateEaster(desc[i], duration_[i])) {
//                icur = i;
//                emodels[i] = context.estimation;
//            } else {
//                try {
//                    emodels[i] = new ModelEstimation(desc[i].buildRegArima(), desc[i].getLikelihoodCorrection());
//                    emodels[i].compute(monitor, nhp);
//                } catch (Exception err) {
//
//                }
//            }
//        }
//
//        // choose best model
//        int imodel = comparer_.compare(refest, emodels);
//        if (imodel < 0) {
//            context.description = refdesc;
//            context.estimation = refest;
//        } else {
//            context.description = desc[imodel];
//            context.estimation = emodels[imodel];
//        }
//
//        return icur == imodel ? ProcessingResult.Unchanged : ProcessingResult.Changed;
//    }
//    
//    private IRegArimaProcessor<SarimaModel> processor(SarimaComponent arima){
//        int nhp=arima.getFreeParametersCount();
//        int np=arima.getParametersCount();
//        GlsSarimaProcessor.builder()
//                .precision(eps)
//                .
//    }
//
//}
