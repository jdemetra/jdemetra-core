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
package demetra.x12;

import demetra.design.Development;
import demetra.modelling.RegressionTestSpec;
import demetra.modelling.TransformationType;
import demetra.modelling.regression.TradingDaysType;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegArimaSpec{

    public static final RegArimaSpec RGDISABLED, RG0, RG1, RG2, RG3, RG4, RG5;
 
    private static final String SMETHOD = "RG";

    static {

        RGDISABLED = new RegArimaSpec();
        RG0 = new RegArimaSpec();
        RG1 = new RegArimaSpec();
        RG2 = new RegArimaSpec();
        RG3 = new RegArimaSpec();
        RG4 = new RegArimaSpec();
        RG5 = new RegArimaSpec();

        RGDISABLED.getBasic().setPreprocessing(false);

        TransformSpec tr = new TransformSpec();
        tr.setFunction(TransformationType.Auto);

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
        o.add("AO");
        o.add("TC");
        o.add("LS");

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
    private BasicSpec basic;
    private TransformSpec transform;
    private RegressionSpec regression;
    private OutlierSpec outliers;
    private AutoModelSpec automdl;
    private ArimaSpec arima;
    private EstimateSpec estimate;

    public static final RegArimaSpec[] allSpecifications() {
        return new RegArimaSpec[]{RG0, RG1, RG2, RG3, RG4, RG5};
    }

    public RegArimaSpec() {
        basic = new BasicSpec();
        transform = new TransformSpec();
        estimate = new EstimateSpec();
        automdl = new AutoModelSpec(false);
        outliers = new OutlierSpec();
        arima = new ArimaSpec();
        regression = new RegressionSpec();
    }
    
    public RegArimaSpec(RegArimaSpec other){
        basic=new BasicSpec(other.basic);
        transform=new TransformSpec(other.transform);
        estimate=new EstimateSpec(other.estimate);
        automdl=new AutoModelSpec(other.automdl);
        outliers=new OutlierSpec(other.outliers);
        arima=new ArimaSpec(other.arima);
        regression=new RegressionSpec(other.regression);
    }

    public BasicSpec getBasic() {
        return basic;
    }

    /**
     * @return the outliers_
     */
    public OutlierSpec getOutliers() {
        return outliers;
    }

    /**
     * @param value
     */
    public void setOutliers(@Nonnull OutlierSpec value) {
        this.outliers = value;
    }

    /**
     * @return the automdl_
     */
    public AutoModelSpec getAutoModel() {
        return automdl;
    }

    /**
     * @param automdl
     */
    public void setAutoModel(@Nonnull AutoModelSpec automdl) {
        this.automdl = automdl;
        this.automdl.setEnabled(true);
    }

    /**
     * @return the arima_
     */
    public ArimaSpec getArima() {
        return arima;
    }

    /**
     * @param arima
     */
    public void setArima(@Nonnull ArimaSpec arima) {
         this.arima = arima;
        automdl.setEnabled(false);
    }

    /**
     * @return the transform_
     */
    public TransformSpec getTransform() {
        return transform;
    }

    /**
     * @param value
     */
    public void setTransform(@Nonnull TransformSpec value) {
         this.transform = value;
    }

    /**
     * @return the regression_
     */
    public RegressionSpec getRegression() {
        return regression;
    }

    /**
     * @param value
     */
    public void setRegression(@Nonnull RegressionSpec value) {
         this.regression = value;
    }

    /**
     * @return the estimate_
     */
    public EstimateSpec getEstimate() {
        return estimate;
    }

    /**
     * @param value the estimate_ to set
     */
    public void setEstimate(@Nonnull EstimateSpec value) {
         this.estimate = value;
    }

    public boolean isUsingAutoModel() {
        return automdl.isEnabled();
    }

    public void setUsingAutoModel(boolean enableAutoModel) {
        automdl.setEnabled(enableAutoModel);
    }

    public static enum Default {

        NONE, RG0, RG1, RG2, RG3, RG4, RG5;
    }

    public static boolean isSystem(RegArimaSpec spec) {
        return spec == RGDISABLED || spec == RG0 || spec == RG1 || spec == RG2
                || spec == RG3 || spec == RG4 || spec == RG5;
    }

    public static RegArimaSpec matchSystem(RegArimaSpec spec) {
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
        return this == obj || (obj instanceof RegArimaSpec && equals((RegArimaSpec) obj));
    }

    private boolean equals(RegArimaSpec spec) {
        if (isUsingAutoModel() != spec.isUsingAutoModel()) {
            return false;
        }
        if (!isUsingAutoModel() && !Objects.equals(spec.arima, arima)) {
            return false;
        }
        if (isUsingAutoModel() && !Objects.equals(spec.automdl, automdl)) {
            return false;
        }
        return Objects.equals(spec.basic, basic)
                && Objects.equals(spec.transform, transform)
                && Objects.equals(spec.regression, regression)
                && Objects.equals(spec.outliers, outliers)
                && Objects.equals(spec.estimate, estimate);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + basic.hashCode();
        hash = 23 * hash + transform.hashCode();
        hash = 23 * hash + regression.hashCode();
        hash = 23 * hash + outliers.hashCode();
        if (isUsingAutoModel()) {
            hash = 23 * hash + automdl.hashCode();
        } else {
            hash = 23 * hash + arima.hashCode();
        }
        hash = 23 * hash + estimate.hashCode();
        return hash;
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

    public static RegArimaSpec fromString(String name) {
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
                return new RegArimaSpec();
        }
    }
}
