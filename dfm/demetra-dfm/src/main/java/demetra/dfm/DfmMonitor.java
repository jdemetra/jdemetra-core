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
package demetra.dfm;

//import ec.tstoolkit.timeseries.information.TsInformationSet;
//import ec.tstoolkit.maths.matrices.Matrix;
//import ec.tstoolkit.mssf2.IMSsf;
//import ec.tstoolkit.mssf2.MFilteringResults;
//import ec.tstoolkit.mssf2.MSmoother;
//import ec.tstoolkit.mssf2.MSmoothingResults;
//import ec.tstoolkit.mssf2.MultivariateSsfData;
//import ec.tstoolkit.timeseries.simplets.TsData;
//
///**
// *
// * @author Jean Palate
// */
//public class DfmMonitor {
//
//    private IDfmInitializer initializer_;
//    private IDfmEstimator estimator_;
//    private IDfmProcessor processor_ = new DfmProcessor();
//
//    /**
//     * Creates a new monitor for a given model
//     *
//     * @param model The model used by the monitor.
//     * @throws DfmException An exception is thrown when the given model is null
//     * or invalid
//     */
//    public DfmMonitor() throws DfmException {
//    }
//
//    /**
//     * @return the initializer
//     */
//    public IDfmInitializer getInitializer() {
//        return initializer_;
//    }
//
//    /**
//     * @param initializer_ the initializer_to set
//     */
//    public void setInitializer(IDfmInitializer initializer) {
//        this.initializer_ = initializer;
//    }
//
//    /**
//     * @return the estimator
//     */
//    public IDfmEstimator getEstimator() {
//        return estimator_;
//    }
//
//    /**
//     * @param estimator the estimator to set
//     */
//    public void setEstimator(IDfmEstimator estimator) {
//        this.estimator_ = estimator;
//    }
//
//    /**
//     * @return the processor
//     */
//    public IDfmProcessor getProcessor() {
//        return processor_;
//    }
//
//    /**
//     * @param processor the processor to set
//     */
//    public void setProcessor(IDfmProcessor processor) {
//        this.processor_ = processor;
//    }
//
//    public boolean process(DynamicFactorModel model, TsData[] input) {
//        TsInformationSet info = new TsInformationSet(input);
//        if (initializer_ != null) {
//            if (!initializer_.initialize(model, info)) {
//                return false;
//            }
//        }
//        if (estimator_ != null) {
//            if (!estimator_.estimate(model, info)) {
//                return false;
//            }
//        }
//        if (processor_ != null) {
//            if (!processor_.process(model, info)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public MSmoothingResults getSmoothingResults() {
//        return processor_ != null ? processor_.getSmoothingResults() : null;
//    }
//    public MFilteringResults getFilteringResults() {
//        return processor_ != null ? processor_.getFilteringResults(): null;
//    }
//}
