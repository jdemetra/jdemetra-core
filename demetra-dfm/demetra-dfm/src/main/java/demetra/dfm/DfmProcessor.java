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


///**
// *
// * @author Jean Palate
// */
//public class DfmProcessor implements IDfmProcessor {
//
//    private MSmoothingResults srslts_;
//    private MFilteringResults frslts_;
//    private boolean bvar_;
//
//    private void clear() {
//        srslts_ = null;
//        frslts_ = null;
//    }
//    
//    public boolean isCalcVariance(){
//        return bvar_;
//    }
//    
//    public void setCalcVariance(boolean bvar){
//        bvar_=bvar;
//    }
//
//    /**
//     * Retrieves the smoothing results
//     *
//     * @return The Smoothing results. May by null.
//     */
//    @Override
//    public MSmoothingResults getSmoothingResults() {
//        return srslts_;
//    }
//
//    @Override
//    public MFilteringResults getFilteringResults() {
//        return frslts_;
//    }
//
//    @Override
//    public boolean process(DynamicFactorModel model, TsInformationSet input) {
//        try {
//            clear();
//            Matrix M = input.generateMatrix(null);
//            if (M.getColumnsCount() != model.getMeasurementsCount()) {
//                throw new DfmException(DfmException.INCOMPATIBLE_DATA);
//            }
//            MSmoother smoother = new MSmoother();
//            srslts_ = new MSmoothingResults();
//            smoother.setCalcVariance(bvar_);
//            IMSsf ssf = model.ssfRepresentation();
//            smoother.process(ssf, new MultivariateSsfData(M.subMatrix().transpose(), null), srslts_);
//            frslts_ = smoother.getFilteringResults();
//            return true;
//        } catch (Exception err) {
//            srslts_ = null;
//            return false;
//        }
//    }
//
//}
