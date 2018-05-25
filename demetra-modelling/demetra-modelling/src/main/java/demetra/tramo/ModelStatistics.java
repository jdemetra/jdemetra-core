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
package demetra.tramo;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.likelihood.LikelihoodStatistics;
import demetra.regarima.regular.PreprocessingModel;
import java.util.Arrays;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.Value
@lombok.Builder
public class ModelStatistics {

    private int outliersCount;
    private int observationsCount;
    private int effectiveObservationsCount;
    private double bic;
    private double se;
    private double ljungBox;
    private double ljungBoxPvalue;
    private double seasonalLjungBox;
    private double seasonalLjungBoxPvalue;
    private double skewnessAbsvalue;
    private double skewnessPvalue;
    private boolean stableMean, stableVariance;
    
    private static ModelStatisticsBuilder builder(){
        return new ModelStatisticsBuilder();
    }
    
    public int getStabilityScore(){
        int s=0;
        if (! stableMean)
            s=1;
        if (! stableVariance)
            s+=2;
        return s;
    }

    public static ModelStatistics of(PreprocessingModel m) {
        LikelihoodStatistics stats = m.getEstimation().getStatistics();
        DoubleSequence e = m.getEstimation().getConcentratedLikelihood().e();
        return builder()
                .outliersCount((int) m.getDescription().variables().filter(var->var.isOutlier(false)).count())
                .observationsCount(stats.getObservationsCount())
                .effectiveObservationsCount(stats.getEffectiveObservationsCount())
                .bic(stats.getBICC())
                .se(Math.sqrt(stats.getSsqErr() / (stats.getEffectiveObservationsCount() - stats.getEstimatedParametersCount() + 1)))
                .build();
        
//        outliersCount = m.description.getOutliers().size();
//        observationsCount = m.description.getY().length;
//        RegArimaEstimation<SarimaModel> est = new RegArimaEstimation<>(m.estimation.getRegArima(),
//                m.estimation.getLikelihood());
//        int nhp = m.description.getArimaComponent().getFreeParametersCount();
//        LikelihoodStatistics stats = est.statistics(nhp, 0);
//        bic = stats.BICC;
//        se = Math.sqrt(stats.SsqErr / (stats.effectiveObservationsCount - stats.estimatedParametersCount + 1));
//        effectiveObservationsCount = stats.effectiveObservationsCount;
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
//                seasonalLjungBox = lbs.getValue();
//                seasonalLjungBoxPvalue = lbs.getPValue();
//            } else {
//                seasonalLjungBox = 0;
//                seasonalLjungBoxPvalue = 0;
//            }
//        } else {
//            seasonalLjungBox = 0;
//            seasonalLjungBoxPvalue = 0;
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
//        stableVariance=samevar;
    }

 }
