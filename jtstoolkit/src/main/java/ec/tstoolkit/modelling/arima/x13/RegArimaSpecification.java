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
package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.algorithm.implementation.RegArimaProcessingFactory;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.AICcComparator;
import ec.tstoolkit.modelling.arima.IModelEstimator;
import ec.tstoolkit.modelling.arima.IOutliersDetectionModule;
import ec.tstoolkit.modelling.arima.IPreprocessingModule;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.IRegArimaSpecification;
import ec.tstoolkit.modelling.arima.UnitSeriesScaling;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.AdditiveOutlierFactory;
import ec.tstoolkit.timeseries.regression.LevelShiftFactory;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.OutliersFactory;
import ec.tstoolkit.timeseries.regression.SeasonalOutlierFactory;
import ec.tstoolkit.timeseries.regression.TransitoryChangeFactory;
import ec.tstoolkit.timeseries.simplets.ConstInterpolator;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegArimaSpecification implements IRegArimaSpecification, Cloneable {

    final static OutliersFactory fac = new OutliersFactory(false);
    public static final RegArimaSpecification RGDISABLED, RG0, RG1, RG2, RG3, RG4, RG5;
    public static final String BASIC = "basic", TRANSFORM = "transform",
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

    private static final String SMETHOD = "RG";

    static {

        fac.register(new AdditiveOutlierFactory());
        LevelShiftFactory lfac = new LevelShiftFactory();
        lfac.setZeroEnded(true);
        fac.register(lfac);
        TransitoryChangeFactory tfac = new TransitoryChangeFactory();
        tfac.setMonthlyCoefficient(true);
        fac.register(tfac);
        SeasonalOutlierFactory sfac = new SeasonalOutlierFactory();
        sfac.setZeroEnded(true);
        fac.register(sfac);

        RGDISABLED = new RegArimaSpecification();
        RG0 = new RegArimaSpecification();
        RG1 = new RegArimaSpecification();
        RG2 = new RegArimaSpecification();
        RG3 = new RegArimaSpecification();
        RG4 = new RegArimaSpecification();
        RG5 = new RegArimaSpecification();

        RGDISABLED.getBasic().setPreprocessing(false);

        TransformSpec tr = new TransformSpec();
        tr.setFunction(DefaultTransformationType.Auto);

        MovingHolidaySpec easter = MovingHolidaySpec.easterSpec(true);

        TradingDaysSpec wd = new TradingDaysSpec();
        wd.setTradingDaysType(TradingDaysType.WorkingDays);
        wd.setLengthOfPeriod(LengthOfPeriodType.LeapYear);
        wd.setTest(RegressionTestSpec.Remove);

        TradingDaysSpec td = new TradingDaysSpec();
        td.setTradingDaysType(TradingDaysType.TradingDays);
        td.setLengthOfPeriod(LengthOfPeriodType.LeapYear);
        td.setTest(RegressionTestSpec.Remove);

        RegressionSpec rwd = new RegressionSpec();
        rwd.add(easter);
        rwd.setTradingDays(wd);
        RegressionSpec rtd = new RegressionSpec();
        rtd.add(easter);
        rtd.setTradingDays(td);

        OutlierSpec o = new OutlierSpec();
        o.add(OutlierType.AO);
        o.add(OutlierType.TC);
        o.add(OutlierType.LS);

        RG1.setTransform(tr);
        RG1.setOutliers(o);

        RG2.setTransform(tr);
        RG2.setOutliers(o);
        RG2.setRegression(rwd);

        RG3.setTransform(tr);
        RG3.setOutliers(o);
        RG3.setUsingAutoModel(true);

        RG4.setTransform(tr);
        RG4.setOutliers(o);
        RG4.setRegression(rwd);
        RG4.setUsingAutoModel(true);

        RG5.setTransform(tr);
        RG5.setOutliers(o);
        RG5.setRegression(rtd);
        RG5.setUsingAutoModel(true);
    }
    private BasicSpec basic_;
    private TransformSpec transform_;
    private RegressionSpec regression_;
    private OutlierSpec outliers_;
    private AutoModelSpec automdl_;
    private ArimaSpec arima_;
    private EstimateSpec estimate_;

    public static final RegArimaSpecification[] allSpecifications() {
        return new RegArimaSpecification[]{RG0, RG1, RG2, RG3, RG4, RG5};
    }

    public RegArimaSpecification() {
        basic_ = new BasicSpec();
        transform_ = new TransformSpec();
        estimate_ = new EstimateSpec();
        automdl_ = new AutoModelSpec();
        outliers_ = new OutlierSpec();
        arima_ = new ArimaSpec();
        regression_ = new RegressionSpec();
    }

    public BasicSpec getBasic() {
        return basic_;
    }

    /**
     * @return the outliers_
     */
    public OutlierSpec getOutliers() {
        return outliers_;
    }

    /**
     * @param value
     */
    public void setOutliers(OutlierSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(OUTLIER);
        }
        this.outliers_ = value;
    }

    /**
     * @return the automdl_
     */
    public AutoModelSpec getAutoModel() {
        return automdl_;
    }

    /**
     * @param automdl
     */
    public void setAutoModel(AutoModelSpec automdl) {
        if (automdl == null) {
            throw new java.lang.IllegalArgumentException(AUTOMDL);
        }
        automdl_ = automdl;
        automdl_.setEnabled(true);
    }

    /**
     * @return the arima_
     */
    public ArimaSpec getArima() {
        return arima_;
    }

    /**
     * @param arima
     */
    public void setArima(ArimaSpec arima) {
        if (arima == null) {
            throw new java.lang.IllegalArgumentException(ARIMA);
        }
        arima_ = arima;
        automdl_.setEnabled(false);
    }

    /**
     * @return the transform_
     */
    public TransformSpec getTransform() {
        return transform_;
    }

    /**
     * @param value
     */
    public void setTransform(TransformSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(TRANSFORM);
        }
        this.transform_ = value;
    }

    /**
     * @return the regression_
     */
    public RegressionSpec getRegression() {
        return regression_;
    }

    /**
     * @param value
     */
    public void setRegression(RegressionSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(REGRESSION);
        }
        this.regression_ = value;
    }

    /**
     * @return the estimate_
     */
    public EstimateSpec getEstimate() {
        return estimate_;
    }

    /**
     * @param value the estimate_ to set
     */
    public void setEstimate(EstimateSpec value) {
        if (value == null) {
            throw new java.lang.IllegalArgumentException(ESTIMATE);
        }
        this.estimate_ = value;
    }

    public boolean isUsingAutoModel() {
        return automdl_.isEnabled();
    }

    public void setUsingAutoModel(boolean enableAutoModel) {
        automdl_.setEnabled(enableAutoModel);
    }

    private IPreprocessingModule makeTd(RegressionSpec m_regression) {
        if (m_regression != null) {
            TradingDaysSpec td = m_regression.getTradingDays();
            if (td.isUsed() && td.getTest() != RegressionTestSpec.None) {
                CalendarEffectsDetection cl = new CalendarEffectsDetection(new AICcComparator(m_regression.getAICCDiff()));
                cl.setEpsilon(estimate_.getTol());
                return cl;
            }
        }
        return null;
    }

    private IPreprocessingModule makeEaster(RegressionSpec m_regression) {
        if (m_regression != null) {
            MovingHolidaySpec espec = m_regression.getEaster();
            if (espec != null && espec.getTest() != RegressionTestSpec.None) {
                EasterDetection cl = new EasterDetection(new AICcComparator(m_regression.getAICCDiff()));
                if (espec.getTest() != RegressionTestSpec.Add) {
                    cl.setDuration(espec.getW());
                }
                cl.setEpsilon(estimate_.getTol());
                return cl;
            }
        }
        return null;

    }

    public static enum Default {

        NONE, RG0, RG1, RG2, RG3, RG4, RG5;
    }

    public static IPreprocessor defaultPreprocessor(Default option) {
        switch (option) {
            case NONE:
                return RGDISABLED.build();
            case RG0:
                return RG0.build();
            case RG1:
                return RG1.build();
            case RG2:
                return RG2.build();
            case RG3:
                return RG3.build();
            case RG4:
                return RG4.build();
            case RG5:
                return RG5.build();
            default:
                return null;
        }

    }

    @Override
    public RegArimaSpecification clone() {
        try {
            RegArimaSpecification spec = (RegArimaSpecification) super.clone();
            spec.basic_ = basic_.clone();
            spec.transform_ = transform_.clone();
            spec.arima_ = arima_.clone();
            spec.automdl_ = automdl_.clone();
            spec.regression_ = regression_.clone();
            spec.outliers_ = outliers_.clone();
            spec.estimate_ = estimate_.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }

    }

    private IPreprocessingModule makeLogLevel(TransformSpec transform_) {
        if (transform_.getFunction() != DefaultTransformationType.Auto) {
            return null;
        }
        LogLevelTest ll = new LogLevelTest(transform_.getAICDiff());
        ll.setEpsilon(estimate_.getTol());
        // params
        return ll;
    }

    private IOutliersDetectionModule makeOutliers(OutlierSpec o) {
        if (!o.isUsed()) {
            return null;
        }
        SingleOutlierSpec[] types = o.getTypes();
        OutliersDetector detector = new OutliersDetector();
        detector.setEpsilon(estimate_.getTol());
        detector.setSpan(o.getSpan());
        for (int i = 0; i < types.length; ++i) {
            detector.addOutlierFactory(fac.getFactory(types[i].getType()));
        }
        if (automdl_ != null) {
            detector.setPc(automdl_.getPercentReductionCV());
        }
        detector.setCriticalValue(o.getDefaultCriticalValue());
        //detector.setMaxIter(o.getMaxIter());
        return detector;
    }

    private IModelEstimator makeFinalEstimator(EstimateSpec estimate_) {
        FinalEstimator estimator = new FinalEstimator();
        estimator.setEpsilon(estimate_.getTol());
        return estimator;
    }

    private void makeAutoModelling(X13Preprocessor x13, AutoModelSpec automdl) {
        if (automdl.isEnabled()) {
            AutoModel ami = new AutoModel();
            ami.setEpsilon(estimate_.getTol());
            ami.setBalanced(automdl.isBalanced());
            ami.setMixed(automdl.isMixed());
            x13.autoModelling = ami;
            x13.setCheckMu(true);
            x13.setLjungBoxLimit(automdl.getLjungBoxLimit());
            x13.setMixed(automdl.isMixed());

        } else {
//            x13.setCheckMu(automdl.isCheckMu());
            x13.setCheckMu(false);
        }

    }

    public IPreprocessor build() {
        return build(null);
    }

    public IPreprocessor build(ProcessingContext context) {
        X13Preprocessor x13 = new X13Preprocessor();
        // span
        x13.estimateSpan = estimate_.getSpan();
        // model builder
        x13.builder = new X13ModelBuilder(this, context);

        // scaling
        x13.scaling = new UnitSeriesScaling();

        // missing
        x13.missing = new ConstInterpolator();

        // tests
        x13.loglevelTest = makeLogLevel(transform_);

        x13.outliers = makeOutliers(outliers_);
        makeAutoModelling(x13, automdl_);

        x13.tdTest = makeTd(regression_);
        x13.easterTest = makeEaster(regression_);
//        x13.userTest = makeUserTest(regression_);

        x13.estimator = new X13Estimator(estimate_.getTol());
        x13.finalizer = makeFinalEstimator(estimate_);

        return x13;
    }

    public static boolean isSystem(RegArimaSpecification spec) {
        return spec == RGDISABLED || spec == RG0 || spec == RG1 || spec == RG2
                || spec == RG3 || spec == RG4 || spec == RG5;
    }

    public static RegArimaSpecification matchSystem(RegArimaSpecification spec) {
        if (spec == RGDISABLED || spec == RG0 || spec == RG1 || spec == RG2
                || spec == RG3 || spec == RG4 || spec == RG5) {
            return spec;
        }
        if (spec.equals(RGDISABLED)) {
            return RGDISABLED;
        } else if (spec.equals(RG0)) {
            return RG0;
        } else if (spec.equals(RG1)) {
            return RG1;
        } else if (spec.equals(RG2)) {
            return RG2;
        } else if (spec.equals(RG3)) {
            return RG3;
        } else if (spec.equals(RG4)) {
            return RG4;
        } else if (spec.equals(RG5)) {
            return RG5;
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof RegArimaSpecification && equals((RegArimaSpecification) obj));
    }

    private boolean equals(RegArimaSpecification spec) {
        if (isUsingAutoModel() != spec.isUsingAutoModel()) {
            return false;
        }
        if (!isUsingAutoModel() && !Objects.equals(spec.arima_, arima_)) {
            return false;
        }
        if (isUsingAutoModel() && !Objects.equals(spec.automdl_, automdl_)) {
            return false;
        }
        return Objects.equals(spec.basic_, basic_)
                && Objects.equals(spec.transform_, transform_)
                && Objects.equals(spec.regression_, regression_)
                && Objects.equals(spec.outliers_, outliers_)
                && Objects.equals(spec.estimate_, estimate_);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + basic_.hashCode();
        hash = 23 * hash + transform_.hashCode();
        hash = 23 * hash + regression_.hashCode();
        hash = 23 * hash + outliers_.hashCode();
        if (isUsingAutoModel()) {
            hash = 23 * hash + automdl_.hashCode();
        } else {
            hash = 23 * hash + arima_.hashCode();
        }
        hash = 23 * hash + estimate_.hashCode();
        return hash;
    }
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add("algorithm", RegArimaProcessingFactory.DESCRIPTOR);
        InformationSet binfo = basic_.write(verbose);
        if (binfo != null) {
            specInfo.add(BASIC, binfo);
        }
        if (!basic_.isPreprocessing()) {
            return specInfo;
        }
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
        InformationSet outlierinfo = outliers_.write(verbose);
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
        InformationSet binfo = info.getSubSet(BASIC);
        if (binfo != null) {
            boolean tok = basic_.read(binfo);
            if (!tok) {
                return false;
            }
        }
        if (!basic_.isPreprocessing()) {
            return true;
        }
        InformationSet tinfo = info.getSubSet(TRANSFORM);
        if (tinfo != null) {
            boolean tok = transform_.read(tinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet oinfo = info.getSubSet(OUTLIER);
        if (oinfo != null) {
            boolean tok = outliers_.read(oinfo);
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

    @Override
    public String toString() {
        if (this == RG0) {
            return "RG0";
        }
        if (this == RG1) {
            return "RG1";
        }
        if (this == RG2) {
            return "RG2c";
        }
        if (this == RG3) {
            return "RG3";
        }
        if (this == RG4) {
            return "RG4c";
        }
        if (this == RG5) {
            return "RG5c";
        }
        if (equals(RG0)) {
            return "RG0";
        }
        if (equals(RG1)) {
            return "RG1";
        }
        if (equals(RG2)) {
            return "RG2c";
        }
        if (equals(RG3)) {
            return "RG3";
        }
        if (equals(RG4)) {
            return "RG4c";
        }
        if (equals(RG5)) {
            return "RG5c";
        }
        return SMETHOD;
    }

    public String toLongString() {
        String s = toString();
        if (SMETHOD.equals(s)) {
            return s;
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("RG[").append(s).append(']');
            return builder.toString();
        }
    }

    public static RegArimaSpecification fromString(String name) {
        switch (name) {
            case "RG0":
                return RG0;
            case "RG1":
                return RG1;
            case "RG2c":
                return RG2;
            case "RG3":
                return RG3;
            case "RG4c":
                return RG4;
            case "RG5c":
                return RG5;
            default:
                return new RegArimaSpecification();
        }
    }
}
