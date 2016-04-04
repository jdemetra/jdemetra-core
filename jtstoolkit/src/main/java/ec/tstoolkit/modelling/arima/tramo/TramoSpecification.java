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
package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.implementation.TramoProcessingFactory;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestType;
import ec.tstoolkit.modelling.arima.*;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec.AutoMethod;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.OutliersFactory;
import ec.tstoolkit.timeseries.simplets.AverageInterpolator;
import ec.tstoolkit.utilities.Arrays2;
import java.util.Map;
import java.util.Objects;

/**
 * Defines the specifications for the Tramo pre-processing, which is
 * an(automatic)regarima modelling
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Temporary)
public class TramoSpecification implements Cloneable, IRegArimaSpecification {

    private static final double CVAL = 1.96;
    final static OutliersFactory fac = new OutliersFactory(true);
    //public static TramoSpecification Default;
    public static final TramoSpecification TR0, TR1, TR2, TR3, TR4, TR5, TRfull;
    private static final String SMETHOD = "TR";
    public static final String TRANSFORM = "transform",
            AUTOMDL = "automdl", ARIMA = "arima",
            REGRESSION = "regression", OUTLIER = "outlier", ESTIMATE = "esimate";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        EstimateSpec.fillDictionary(InformationSet.item(prefix, ESTIMATE), dic);
        TransformSpec.fillDictionary(InformationSet.item(prefix, TRANSFORM), dic);
        AutoModelSpec.fillDictionary(InformationSet.item(prefix, AUTOMDL), dic);
        ArimaSpec.fillDictionary(InformationSet.item(prefix, ARIMA), dic);
        OutlierSpec.fillDictionary(InformationSet.item(prefix, OUTLIER), dic);
        RegressionSpec.fillDictionary(InformationSet.item(prefix, REGRESSION), dic);
    }

    static {
        TR0 = new TramoSpecification();
        TR1 = new TramoSpecification();
        TR2 = new TramoSpecification();
        TR3 = new TramoSpecification();
        TR4 = new TramoSpecification();
        TR5 = new TramoSpecification();
        TRfull = new TramoSpecification();

        TransformSpec tr = new TransformSpec();
        tr.setFunction(DefaultTransformationType.Auto);

        EasterSpec e = new EasterSpec();
        e.setOption(EasterSpec.Type.Standard);
        e.setTest(true);
        //e.setEasterIncluded(true);

        TradingDaysSpec wd = new TradingDaysSpec();
        wd.setTradingDaysType(TradingDaysType.WorkingDays);
        wd.setLeapYear(true);
        wd.setRegressionTestType(RegressionTestType.Separate_T);

        TradingDaysSpec td = new TradingDaysSpec();
        td.setTradingDaysType(TradingDaysType.TradingDays);
        td.setLeapYear(true);
        td.setRegressionTestType(RegressionTestType.Separate_T);

        TradingDaysSpec dc = td.clone();
        dc.setAutomatic(true);
        EasterSpec ec = e.clone();
        ec.setOption(EasterSpec.Type.IncludeEaster);

        RegressionSpec rwd = new RegressionSpec();
        CalendarSpec cwd = new CalendarSpec();
        cwd.setEaster(e);
        cwd.setTradingDays(wd);
        rwd.setCalendar(cwd);
        RegressionSpec rtd = new RegressionSpec();
        CalendarSpec ctd = new CalendarSpec();
        ctd.setEaster(e);
        ctd.setTradingDays(td);
        rtd.setCalendar(ctd);
        RegressionSpec rc = new RegressionSpec();
        CalendarSpec cc = new CalendarSpec();
        cc.setEaster(ec);
        cc.setTradingDays(dc);
        rc.setCalendar(cc);

        OutlierSpec o = new OutlierSpec();
        o.add(OutlierType.AO);
        o.add(OutlierType.TC);
        o.add(OutlierType.LS);

        TR1.setTransform(tr);
        TR1.setOutliers(o);

        TR2.setTransform(tr);
        TR2.setOutliers(o);
        TR2.setRegression(rwd);

        TR3.setTransform(tr);
        TR3.setOutliers(o);
        TR3.setUsingAutoModel(true);

        TR4.setTransform(tr);
        TR4.setOutliers(o);
        TR4.setRegression(rwd);
        TR4.setUsingAutoModel(true);

        TR5.setTransform(tr);
        TR5.setOutliers(o);
        TR5.setRegression(rtd);
        TR5.setUsingAutoModel(true);

        TRfull.setTransform(tr);
        TRfull.setOutliers(o);
        TRfull.setRegression(rc);
        TRfull.setUsingAutoModel(true);
    }

    public static final TramoSpecification[] allSpecifications() {
        return new TramoSpecification[]{TR0, TR1, TR2, TR3, TR4, TR5, TRfull};
    }

    /**
     * Creates a new default specification. No transformation, no regression
     * variables, no outliers detection, default airline model (without mean).
     */
    public TramoSpecification() {
        transform_ = new TransformSpec();
        estimate_ = new EstimateSpec();
        automdl_ = new AutoModelSpec();
        outlier_ = new OutlierSpec();
        arima_ = new ArimaSpec();
        arima_.setMean(true);
        regression_ = new RegressionSpec();
    }

    @Override
    public TramoSpecification clone() {
        try {
            TramoSpecification spec = (TramoSpecification) super.clone();
            spec.transform_ = transform_.clone();
            spec.arima_ = arima_.clone();
            spec.automdl_ = automdl_.clone();
            spec.regression_ = regression_.clone();
            spec.outlier_ = outlier_.clone();
            spec.estimate_ = estimate_.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    /**
     * Gets the specifications related to the transformation of the original
     * series. Related TRAMO options: LAM, FCT, UNITS
     *
     * @return The transform specifications
     */
    public TransformSpec getTransform() {
        return transform_;
    }

    /**
     * Sets the specifications related to the transformation of the original
     * series.
     *
     * @param value The new transform specifications. Should not be null
     */
    public void setTransform(TransformSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(TRANSFORM);
        }
        transform_ = value;
    }

    /**
     * Gets the specifications related to the automatic ARIMA modelling (AMI).
     * The AutoModelSpec and the ArimaSpec are mutually exclusive
     * specifications: Related TRAMO options: IDIF, INIC, UB1, UB2, CANCEL,
     * TSIG, PR, PCR
     *
     * @return The AMI specifications
     */
    public AutoModelSpec getAutoModel() {
        return automdl_;
    }

    /**
     * Sets the specifications related to the automatic ARIMA modelling (AMI).
     *
     * @param value The new AMI. Should not be null.
     */
    public void setAutoModel(AutoModelSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(AUTOMDL);
        }
        automdl_ = value;
        automdl_.setEnabled(true);
    }

    /**
     * Checks that the AMI is enabled
     *
     * @return true if the AMI spec is enabled, false otherwise.
     */
    public boolean isUsingAutoModel() {
        return automdl_.isEnabled();
    }

    /**
     * Enables/disables the AMI.
     *
     * @param enableAutoModel
     */
    public void setUsingAutoModel(boolean enableAutoModel) {
        automdl_.setEnabled(enableAutoModel);
    }

    /**
     * Gets the predefined Arima specification. The AutoModel and the Arima
     * properties are mutually exclusive specifications: Related TRAMO options:
     * P, D, Q, BP, BD, BQ, IMEAN, JPR, JPS, JQR, JQS, INIT
     *
     * @return
     */
    public ArimaSpec getArima() {
        return arima_;
    }

    /**
     * Sets a predefined Arima specification. The AMI is disabled.
     *
     * @param value The new Arima specifications
     */
    public void setArima(ArimaSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(ARIMA);
        }
        arima_ = value;
        setUsingAutoModel(false);
    }

    /**
     * Gets options related to the estimation routine Related TRAMO options:
     * TOL, TYPE
     *
     * @return
     */
    public EstimateSpec getEstimate() {
        return estimate_;
    }

    /**
     * Sets new options for the estimation routine
     *
     * @param value The new options
     */
    public void setEstimate(EstimateSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(ESTIMATE);
        }
        estimate_ = value;
    }

    /**
     * Gets the options for the automatic outliers detection Related TRAMO
     * options: IATIP, AIO, TC, VA
     */
    public OutlierSpec getOutliers() {
        return outlier_;
    }

    /**
     * Sets the options for the automatic outliers detection.
     *
     * @param value The new specifications.
     */
    public void setOutliers(OutlierSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(OUTLIER);
        }
        outlier_ = value;
    }

    /**
     * Gets the specifications for the regression model (including calendar
     * effects).
     *
     * @return The specifications for the regression model.
     */
    public RegressionSpec getRegression() {
        return regression_;
    }

    /**
     * Sets the specifications for the regression model.
     *
     * @param value The new specifications.
     */
    public void setRegression(RegressionSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(REGRESSION);
        }
        regression_ = value;
    }
    private ArimaSpec arima_;
    private TransformSpec transform_;
    private AutoModelSpec automdl_;
    private EstimateSpec estimate_;
    private OutlierSpec outlier_;
    private RegressionSpec regression_;

    private IPreprocessingModule makeLogLevel(TransformSpec transform_) {
        if (transform_.getFunction() != DefaultTransformationType.Auto) {
            return null;
        }
        LogLevelTest ll = new LogLevelTest();
        ll.setLogPreference(Math.log(transform_.getFct()));
        return ll;
    }

    private IOutliersDetectionModule makeOutliers(OutlierSpec o, AutoModelSpec am) {
        OutlierType[] types = o.getTypes();
        if (Arrays2.isNullOrEmpty(types)) {
            return null;
        }
        OutliersDetector detector = new OutliersDetector();
        for (int i = 0; i < types.length; ++i) {
            detector.addOutlierFactory(fac.getFactory(types[i]));
        }
        detector.setCriticalValue(o.getCriticalValue());
        detector.useEML(o.isEML());
        detector.setSpan(o.getSpan());
        if (am != null) {
            detector.setPc(am.getPc());
        }
        return detector;
    }

    private FinalEstimator makeFinalEstimator(EstimateSpec estimate, double tsig) {
        FinalEstimator estimator = new FinalEstimator();
        estimator.setEpsilon(estimate.getTol());
        estimator.setUr(estimate.getUbp());
        estimator.setTsig(tsig);
        return estimator;
    }

    private void makeAutoModelling(TramoProcessor tramo, AutoModelSpec automdl, TradingDaysSpec td) {
        if (automdl.isEnabled()) {
            DifferencingModule diff = new DifferencingModule();
            diff.setCancel(automdl.getCancel());
            diff.setUB1(automdl.getUb1());
            diff.setUB2(automdl.getUb2());
            tramo.controllers.add(new RegularUnderDifferencingTest());
            tramo.controllers.add(new SeasonalUnderDifferencingTest());
            if (automdl.isAcceptDefault()) {
                tramo.setFal(true);
            }
            if (automdl.isAmiCompare()) {
                tramo.controllers.add(new ModelBenchmarking());
            }
            if (td.isAutomatic()) {
                tramo.controllers.add(new TDController(td.getProbabibilityForFTest()));
            }
            tramo.controllers.add(new SeasonalUnderDifferencingTest2());
            tramo.controllers.add(new RegularUnderDifferencingTest2());
            tramo.differencing = diff;
            tramo.autoModelling = new ArmaModule();
        } else if (td.isAutomatic()) {
            tramo.controllers.add(new TDController(td.getProbabibilityForFTest()));
        }

    }

    private SeasonalityDetector makeSeas() {
        return new SeasonalityDetector();
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add("algorithm", TramoProcessingFactory.DESCRIPTOR);
        InformationSet tinfo = transform_.write(verbose);
        if (tinfo != null) {
            specInfo.add(TRANSFORM, tinfo);
        }
        InformationSet arimainfo = arima_.write(verbose);
        if (arimainfo != null) {
            specInfo.add(ARIMA, arimainfo);
        }
        InformationSet amiinfo = automdl_.write(verbose);
        if (amiinfo != null) {
            specInfo.add(AUTOMDL, amiinfo);
        }
        InformationSet outlierinfo = outlier_.write(verbose);
        if (outlierinfo != null) {
            specInfo.add(OUTLIER, outlierinfo);
        }
        InformationSet estimateinfo = estimate_.write(verbose);
        if (estimateinfo != null) {
            specInfo.add(ESTIMATE, estimateinfo);
        }
        InformationSet reginfo = regression_.write(verbose);
        if (reginfo != null) {
            specInfo.add(REGRESSION, reginfo);
        }
        return specInfo;
    }

    @Override
    public boolean read(InformationSet info) {
        InformationSet tinfo = info.getSubSet(TRANSFORM);
        if (tinfo != null) {
            boolean tok = transform_.read(tinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet oinfo = info.getSubSet(OUTLIER);
        if (oinfo != null) {
            boolean tok = outlier_.read(oinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet ainfo = info.getSubSet(ARIMA);
        if (ainfo != null) {
            boolean tok = arima_.read(ainfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet amiinfo = info.getSubSet(AUTOMDL);
        if (amiinfo != null) {
            boolean tok = automdl_.read(amiinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet einfo = info.getSubSet(ESTIMATE);
        if (einfo != null) {
            boolean tok = estimate_.read(einfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet rinfo = info.getSubSet(REGRESSION);
        if (rinfo != null) {
            boolean tok = regression_.read(rinfo);
            if (!tok) {
                return false;
            }
        }
        return true;
    }

    public static enum Default {

        TR0, TR1, TR2, TR3, TR4, TR5, TRfull;

        public static Default valueOfIgnoreCase(String name) {
            if (name.equalsIgnoreCase("TRfull")) {
                return TRfull;
            } else {
                String N = name.toUpperCase();
                return valueOf(N);
            }
        }
    }

    /**
     * Creates the Tramo pre-processor routine that corresponds to one of the
     * default options.
     *
     * @param option The default specification.
     * @return A new pre-processor.
     */
    public static IPreprocessor defaultPreprocessor(Default option) {
        switch (option) {
            case TR0:
                return TR0.build();
            case TR1:
                return TR1.build();
            case TR2:
                return TR2.build();
            case TR3:
                return TR3.build();
            case TR4:
                return TR4.build();
            case TR5:
                return TR5.build();
            case TRfull:
                return TRfull.build();
            default:
                return null;
        }
    }

    private void initMainRoutine(TramoProcessor tramo) {
        if (automdl_ != null) {
            tramo.setPcr(automdl_.getPcr());
            tramo.setTsig_(automdl_.getTsig());
            tramo.setDfm(automdl_.isAcceptDefault());
        }
    }

    public IPreprocessor build() {
        return build(null);
    }

    /**
     * Creates the Tramo pre-processor corresponding to this specifications
     *
     * @param context
     * @return A new Tramo pre-processor.
     */
    public IPreprocessor build(ProcessingContext context) {
        TramoProcessor tramo = new TramoProcessor();
        // span
        tramo.estimationSpan = estimate_.getSpan();
        // model builder
        tramo.builder = new TramoModelBuilder(this, context);

        // scaling
        tramo.scaling = new UnitSeriesScaling();

        // missing
        tramo.missing = new AverageInterpolator();

        tramo.seas = makeSeas();

        // tests
        tramo.loglevelTest = makeLogLevel(transform_);
        //seas = new SeasonalityDetector2();
        TradingDaysSpec td = regression_.getCalendar().getTradingDays();
        boolean join = td.isAutomatic() || td.getRegressionTestType() == RegressionTestType.Joint_F;

        //
        //Gianluca Added the new RegressionTestTD Test !!!!
        //
        switch (td.getAutomaticMethod()) {
            case FTest:
                tramo.regressionTest = new RegressionTestTD2(td.getProbabibilityForFTest());
                break;
            case WaldTest:
                tramo.regressionTest = new RegressionTestTD(td.getProbabibilityForFTest());
                break;
            default:
                tramo.regressionTest = new RegressionVariablesTest(join);
        }
        //
        //
        //

        tramo.regressionTest2 = new RegressionVariablesTest2(CVAL, CVAL);
//        tramo.regressionTest2 = new RegressionVariablesController(td.getProbabibilityForFTest(), join);
        tramo.regressionTest3 = new RegressionVariablesTest2(CVAL, automdl_.getTsig());
        tramo.outliers = makeOutliers(outlier_, automdl_);
        makeAutoModelling(tramo, automdl_, td);
//        if (td.getAutomaticMethod() != AutoMethod.Unused) {
//            tramo.controllers.add(new TDController(td.getProbabibilityForFTest())); // NO EFFECT
//        }
        tramo.finalizer = makeFinalEstimator(estimate_, automdl_.getTsig());

        initMainRoutine(tramo);
        return tramo;
    }

//    public static boolean isDefault(TramoSpecification spec) {
//        return Default != null && Default.equals(spec);
//    }
//
    /**
     * Checks that specifications are one of the pre-defined ones.
     *
     * @param spec The inspected specifications
     * @return true if the specifications object is one of the pre-defined ones.
     */
    public static boolean isSystem(TramoSpecification spec) {
        return spec == TR0 || spec == TR1 || spec == TR2
                || spec == TR3 || spec == TR4 || spec == TR5 || spec == TRfull;
    }

    /**
     * Checks that specifications correspond to one of the pre-defined ones. It
     * can be a pre-defined object or it can be equal to a pre-defined object.
     *
     * @param spec The inspected specifications
     * @return true if the specifications object corresponds to the pre-defined
     * ones.
     * @param spec
     * @return
     */
    public static TramoSpecification matchSystem(TramoSpecification spec) {
        if (spec == TR0 || spec == TR1 || spec == TR2
                || spec == TR3 || spec == TR4 || spec == TR5 || spec == TRfull) {
            return spec;
        }
        if (spec.equals(TR0)) {
            return TR0;
        }
        if (spec.equals(TR1)) {
            return TR1;
        } else if (spec.equals(TR2)) {
            return TR2;
        } else if (spec.equals(TR3)) {
            return TR3;
        } else if (spec.equals(TR4)) {
            return TR4;
        } else if (spec.equals(TR5)) {
            return TR5;
        } else if (spec.equals(TRfull)) {
            return TRfull;
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TramoSpecification && equals((TramoSpecification) obj));
    }

    private boolean equals(TramoSpecification spec) {
        if (isUsingAutoModel() != spec.isUsingAutoModel()) {
            return false;
        }
        if (!isUsingAutoModel() && !Objects.equals(spec.arima_, arima_)) {
            return false;
        }
        if (isUsingAutoModel() && !Objects.equals(spec.automdl_, automdl_)) {
            return false;
        }
        return Objects.equals(spec.transform_, transform_)
                && Objects.equals(spec.regression_, regression_)
                && Objects.equals(spec.outlier_, outlier_)
                && Objects.equals(spec.estimate_, estimate_);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        if (isUsingAutoModel()) {
            hash = 11 * hash + automdl_.hashCode();
        } else {
            hash = 11 * hash + arima_.hashCode();
        }
        hash = 11 * hash + transform_.hashCode();
        hash = 11 * hash + estimate_.hashCode();
        hash = 11 * hash + outlier_.hashCode();
        hash = 11 * hash + regression_.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        if (this == TR0) {
            return "TR0";
        }
        if (this == TR1) {
            return "TR1";
        }
        if (this == TR2) {
            return "TR2";
        }
        if (this == TR3) {
            return "TR3";
        }
        if (this == TR4) {
            return "TR4";
        }
        if (this == TR5) {
            return "TR5";
        }
        if (equals(TR0)) {
            return "TR0";
        }
        if (equals(TR1)) {
            return "TR1";
        }
        if (equals(TR2)) {
            return "TR2";
        }
        if (equals(TR3)) {
            return "TR3";
        }
        if (equals(TR4)) {
            return "TR4";
        }
        if (equals(TR5)) {
            return "TR5";
        }
        if (equals(TRfull)) {
            return "TRfull";
        }
        return SMETHOD;
    }

    public String toLongString() {
        String s = toString();
        if (SMETHOD.equals(s)) {
            return s;
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("TR[").append(s).append(']');
            return builder.toString();
        }
    }
    ///////////////////////////////////////////////////////////////////////////
}
