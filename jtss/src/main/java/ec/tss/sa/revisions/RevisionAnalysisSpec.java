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
package ec.tss.sa.revisions;

import ec.satoolkit.ISaSpecification;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.sa.EstimationPolicyType;
import ec.tstoolkit.algorithm.IProcSpecification;
import static ec.tstoolkit.algorithm.IProcSpecification.ALGORITHM;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Month;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mats Maggi
 */
public class RevisionAnalysisSpec implements IProcSpecification, Cloneable {

    public static final String LENGTH = "revisionLength";
    public static final String DELAY = "revisionDelay";
    public static final String MAIN_POLICY = "mainPolicy";
    public static final String INTERMEDIATE_POLICY = "intermediatePolicy";
    public static final String SPECIFICATION = "specification";
    public static final String REVISION_START = "revisionStartDay";
    public static final String OUTOFSAMPLE = "outofsample";
    public static final String FINAL = "final";

    public static class DayMonth {

        /**
         *
         * @param day 0-based day !!
         * @param month
         */
        public DayMonth(int day, Month month) {
            this.day = day;
            this.month = month;
        }
        public final int day;
        public final Month month;

        public static DayMonth BEG = new DayMonth(0, Month.January);

        @Override
        public String toString() {
            return String.valueOf(day) + "/" + month.toString();
        }
    }

    /**
     * Used policies: for main:
     * Complete,Outliers,LastOutliers,FreeParameters,FixedFilterDefinition, for
     * intermediate: idem + UseForecasts
     */
    private MainPolicyType mainEstimation = MainPolicyType.Complete;
    private IntermediatePolicyType intermediateEstimation = IntermediatePolicyType.FreeParameters;

    /**
     * Specification used for the processing
     */
    private ISaSpecification specification = TramoSeatsSpecification.RSA4;

    public enum MainPolicyType {

        Complete(EstimationPolicyType.Complete),
        Outliers(EstimationPolicyType.Outliers),
        LastOutliers(EstimationPolicyType.LastOutliers),
        FreeParameters(EstimationPolicyType.FreeParameters),
        FixedParameters(EstimationPolicyType.FixedParameters),
        Fixed(EstimationPolicyType.Fixed);

        private final EstimationPolicyType t;

        private MainPolicyType(EstimationPolicyType t) {
            this.t = t;
        }

        public EstimationPolicyType getType() {
            return t;
        }
    }

    public enum IntermediatePolicyType {

        Complete(EstimationPolicyType.Complete),
        Outliers(EstimationPolicyType.Outliers),
        LastOutliers(EstimationPolicyType.LastOutliers),
        FreeParameters(EstimationPolicyType.FreeParameters),
        FixedParameters(EstimationPolicyType.FixedParameters),
        Fixed(EstimationPolicyType.Fixed),
        Current(EstimationPolicyType.Current);

        private final EstimationPolicyType t;

        private IntermediatePolicyType(EstimationPolicyType t) {
            this.t = t;
        }

        public EstimationPolicyType getType() {
            return t;
        }
    }

    /**
     * Length of the revision analysis (in years)
     */
    private int analysisLength = 4;
    /**
     * Period between two re-estimations of the models (in years)
     */
    private int revisionDelay = 1;
    
    private boolean ftarget=false;

    private DayMonth revisionDay = DayMonth.BEG;
    private boolean outOfSample = true;

    public MainPolicyType getMainEstimation() {
        return mainEstimation;
    }

    public void setMainEstimation(MainPolicyType mainEstimation) {
        this.mainEstimation = mainEstimation;
    }

    public IntermediatePolicyType getIntermediateEstimation() {
        return intermediateEstimation;
    }

    public void setIntermediateEstimation(IntermediatePolicyType intermediateEstimation) {
        this.intermediateEstimation = intermediateEstimation;
    }

    public boolean isOutOfSample() {
        return this.outOfSample;
    }

    public void setOutOfSample(boolean b) {
        this.outOfSample = b;
    }

    public boolean isTargetFinal() {
        return this.ftarget;
    }

    public void setTargetFinal(boolean b) {
        ftarget = b;
    }

    public int getAnalysisLength() {
        return analysisLength;
    }

    public void setAnalysisLength(int analysisLength) {
        this.analysisLength = analysisLength;
    }

    public int getRevisionDelay() {
        return revisionDelay;
    }

    public void setRevisionDelay(int revisionDelay) {
        this.revisionDelay = revisionDelay;
    }

    public DayMonth getRevisionDay() {
        return revisionDay;
    }

    public void setRevisionDay(DayMonth revisionDay) {
        this.revisionDay = revisionDay;
    }

    public ISaSpecification getSaSpecification() {
        return specification;
    }

    public void setSaSpecification(ISaSpecification specification) {
        this.specification = specification;
    }

    @Override
    public RevisionAnalysisSpec clone() {
        RevisionAnalysisSpec cloned;
        try {
            return (RevisionAnalysisSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(ALGORITHM, RevisionAnalysisProcessor.DESCRIPTOR);

        info.set(LENGTH, analysisLength);
        info.set(DELAY, revisionDelay);

        if (mainEstimation != null) {
            info.set(MAIN_POLICY, mainEstimation.toString());
        }

        if (intermediateEstimation != null || verbose) {
            info.set(INTERMEDIATE_POLICY, intermediateEstimation.toString());
        }

        if (specification != null || verbose) {
            info.set(SPECIFICATION, specification.write(true));
        }

        if (revisionDay != null || verbose) {
            info.set(REVISION_START, revisionDay.toString());
        }

        if (outOfSample == false || verbose) {
            info.set(OUTOFSAMPLE, outOfSample);
        }
        if (ftarget == true || verbose) {
            info.set(FINAL, ftarget);
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        Integer l = info.get(LENGTH, Integer.class);
        if (l != null) {
            analysisLength = l;
        }

        Integer d = info.get(DELAY, Integer.class);
        if (d != null) {
            revisionDelay = d;
        }

        String m = info.get(MAIN_POLICY, String.class);
        if (m != null) {
            mainEstimation = MainPolicyType.valueOf(m);
        }

        String i = info.get(INTERMEDIATE_POLICY, String.class);
        if (i != null) {
            intermediateEstimation = IntermediatePolicyType.valueOf(i);
        }

        InformationSet s = info.get(SPECIFICATION, InformationSet.class);
        if (s != null) {
            if (!specification.read(s)) {
                return false;
            }
        }

        String f = info.get(REVISION_START, String.class);
        if (f != null) {
            String[] split = f.split("/");
            revisionDay = new DayMonth(Integer.parseInt(split[0]), Month.valueOf(split[1]));
        }

        Boolean b = info.get(OUTOFSAMPLE, Boolean.class);
        if (b != null) {
            this.outOfSample = b;
        }

        b = info.get(FINAL, Boolean.class);
        if (b != null) {
            this.ftarget = b;
        }
        return true;
    }

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, LENGTH), Integer.class);
        dic.put(InformationSet.item(prefix, DELAY), Integer.class);
        dic.put(InformationSet.item(prefix, MAIN_POLICY), String.class);
        dic.put(InformationSet.item(prefix, INTERMEDIATE_POLICY), String.class);
        dic.put(InformationSet.item(prefix, SPECIFICATION), InformationSet.class);
        dic.put(InformationSet.item(prefix, REVISION_START), String.class);
        dic.put(InformationSet.item(prefix, OUTOFSAMPLE), Boolean.class);
        dic.put(InformationSet.item(prefix, FINAL), Boolean.class);
    }

}
