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
package demetra.regarima.ami;

import demetra.design.Development;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ModelStatistics {

//    public final int outliers;
//    public final int nz;
//    public final int neffective;
//    public final double bic;
//    public final double se;
//    public final double ljungBox;
//    public final double ljungBoxPvalue;
//    public final double seasLjungBox;
//    public final double seasLjungBoxPvalue;
//    public final double skewnessAbsvalue;
//    public final double skewnessPvalue;
//    public final boolean stableMean, stableVar;
//    
//    public int getStabilityScore(){
//        int s=0;
//        if (! stableMean)
//            s=1;
//        if (! stableVar)
//            s+=2;
//        return s;
//    }
//
//    public ModelStatistics(PreprocessingModel m) {
//        outliers = m.description.getOutliers().size();
//        nz = m.description.getY().length;
//        RegArimaEstimation<SarimaModel> est = new RegArimaEstimation<>(m.estimation.getRegArima(),
//                m.estimation.getLikelihood());
//        int nhp = m.description.getArimaComponent().getFreeParametersCount();
//        LikelihoodStatistics stats = est.statistics(nhp, 0);
//        bic = stats.BICC;
//        se = Math.sqrt(stats.SsqErr / (stats.effectiveObservationsCount - stats.estimatedParametersCount + 1));
//        neffective = stats.effectiveObservationsCount;
//        SarimaSpecification spec = est.model.getArima().getSpecification();
//        int n = TramoProcessor.calcLBLength(spec.getFrequency());
//
//        //ReadDataBlock res = new ReadDataBlock(est.fullResiduals());
//        ReadDataBlock res = new ReadDataBlock(est.likelihood.getResiduals());
//        int nres = res.getLength();
//        LjungBoxTest lb = new LjungBoxTest();
//        lb.setHyperParametersCount(nhp);
//        lb.setK(n);
//        lb.test(res);
//        if (lb.isValid()) {
//            ljungBox = lb.getValue();
//            ljungBoxPvalue = lb.getPValue();
//        } else {
//            ljungBox = 0;
//            ljungBoxPvalue = 0;
//        }
//
//        SkewnessTest sk = new SkewnessTest();
//        sk.test(res);
//        if (sk.isValid()) {
//            skewnessPvalue = sk.getPValue();
//            skewnessAbsvalue = Math.abs(sk.getValue());
//        } else {
//            skewnessAbsvalue = 0;
//            skewnessPvalue = 0;
//        }
//        if (spec.getFrequency() > 1) {
//            LjungBoxTest lbs = new LjungBoxTest();
//            lbs.setK(2);
//            lbs.setLag(spec.getFrequency());
//            lbs.usePositiveAc(true);
//            lbs.test(res);
//            if (lbs.isValid()) {
//                seasLjungBox = lbs.getValue();
//                seasLjungBoxPvalue = lbs.getPValue();
//            } else {
//                seasLjungBox = 0;
//                seasLjungBoxPvalue = 0;
//            }
//        } else {
//            seasLjungBox = 0;
//            seasLjungBoxPvalue = 0;
//        }
//        // Stability tests
//        StabilityTest stfull = new StabilityTest();
//        stfull.process(res);
//        boolean samemean = stfull.isSameMean();
//        boolean samevar = stfull.isSameVariance();
//        int nlast = 10 * spec.getFrequency();
//        if (nlast < nres) {
//            StabilityTest stlast = new StabilityTest();
//            stlast.process(res.rextract(nres - nlast, nlast));
//            boolean samelmean = stlast.isSameMean();
//            boolean samelvar = stlast.isSameVariance();
//            if ((!samemean && samelmean) || (samemean==samelmean && !samevar && samelvar)){
//                samemean=samelmean;
//                samevar=samelvar;
//            } 
//        }
//        stableMean=samemean;
//        stableVar=samevar;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("nz=").append(nz).append((System.lineSeparator()));
//        builder.append("effective nobs=").append(neffective).append((System.lineSeparator()));
//        builder.append("outliers=").append(outliers).append((System.lineSeparator()));
//        builder.append("bic=").append(bic).append((System.lineSeparator()));
//        builder.append("se=").append(se).append((System.lineSeparator()));
//        builder.append("Q=").append(ljungBox).append(" (pvalue=").append(ljungBoxPvalue).append((")\r\n"));
//        builder.append("Qs=").append(seasLjungBox).append(" (pvalue=").append(seasLjungBoxPvalue);
//
//        return builder.toString();
//    }
}
