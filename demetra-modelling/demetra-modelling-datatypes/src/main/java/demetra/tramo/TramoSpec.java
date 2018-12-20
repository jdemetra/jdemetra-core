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

import demetra.design.Development;
import demetra.modelling.TransformationType;
import demetra.modelling.regression.RegressionTestType;
import demetra.modelling.regression.TradingDaysType;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Defines the specifications for the Tramo pre-processing, which is
 * an(automatic)regarima modelling
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Temporary)
public class TramoSpec {

    //public static TramoSpecification Default;
    public static final TramoSpec TR0, TR1, TR2, TR3, TR4, TR5, TRfull;

    static {
        TR0 = new TramoSpec();
        TR1 = new TramoSpec();
        TR2 = new TramoSpec();
        TR3 = new TramoSpec();
        TR4 = new TramoSpec();
        TR5 = new TramoSpec();
        TRfull = new TramoSpec();

        TransformSpec tr = new TransformSpec();
        tr.setFunction(TransformationType.Auto);

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

        TradingDaysSpec dc = new TradingDaysSpec(td);
        dc.setAutomatic(true);
        EasterSpec ec = new EasterSpec(e);
        ec.setOption(EasterSpec.Type.IncludeEaster);
        ec.setTest(true);

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
        o.add("AO");
        o.add("TC");
        o.add("LS");

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

    public static final TramoSpec[] allSpecifications() {
        return new TramoSpec[]{TR0, TR1, TR2, TR3, TR4, TR5, TRfull};
    }

    /**
     * Creates a new default specification. No transformation, no regression
     * variables, no outliers detection, default airline model (without mean).
     */
    public TramoSpec() {
        transform = new TransformSpec();
        estimate = new EstimateSpec();
        automdl = new AutoModelSpec(false);
        outlier = new OutlierSpec();
        arima = new ArimaSpec();
        arima.airlineWithMean();
        regression = new RegressionSpec();
    }

    public TramoSpec(TramoSpec other) {
        transform=new TransformSpec(other.transform);
        estimate=new EstimateSpec(other.estimate);
        automdl=new AutoModelSpec(other.automdl);
        outlier=new OutlierSpec(other.outlier);
        arima=new ArimaSpec(other.arima);
        regression=new RegressionSpec(other.regression);
    }

    /**
     * Gets the specifications related to the transformation of the original
     * series. Related TRAMO options: LAM, FCT, UNITS
     *
     * @return The transform specifications
     */
    public TransformSpec getTransform() {
        return transform;
    }

    /**
     * Sets the specifications related to the transformation of the original
     * series.
     *
     * @param value The new transform specifications. Should not be null
     */
    public void setTransform(@Nonnull TransformSpec value) {
        transform = value;
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
        return automdl;
    }

    /**
     * Sets the specifications related to the automatic ARIMA modelling (AMI).
     *
     * @param value The new AMI. Should not be null.
     */
    public void setAutoModel(@Nonnull AutoModelSpec value) {
        automdl = value;
        automdl.setEnabled(true);
    }

    /**
     * Checks that the AMI is enabled
     *
     * @return true if the AMI spec is enabled, false otherwise.
     */
    public boolean isUsingAutoModel() {
        return automdl.isEnabled();
    }

    /**
     * Enables/disables the AMI.
     *
     * @param enableAutoModel
     */
    public void setUsingAutoModel(boolean enableAutoModel) {
        automdl.setEnabled(enableAutoModel);
    }

    /**
     * Gets the predefined Arima specification. The AutoModel and the Arima
     * properties are mutually exclusive specifications: Related TRAMO options:
     * P, D, Q, BP, BD, BQ, IMEAN, JPR, JPS, JQR, JQS, INIT
     *
     * @return
     */
    public ArimaSpec getArima() {
        return arima;
    }

    /**
     * Sets a predefined Arima specification. The AMI is disabled.
     *
     * @param value The new Arima specifications
     */
    public void setArima(@Nonnull ArimaSpec value) {
        arima = value;
        setUsingAutoModel(false);
    }

    /**
     * Gets options related to the estimation routine Related TRAMO options:
     * TOL, TYPE
     *
     * @return
     */
    public EstimateSpec getEstimate() {
        return estimate;
    }

    /**
     * Sets new options for the estimation routine
     *
     * @param value The new options
     */
    public void setEstimate(@Nonnull EstimateSpec value) {
        estimate = value;
    }

    /**
     * Gets the options for the automatic outliers detection Related TRAMO
     * options: IATIP, AIO, TC, VA
     * @return 
     */
    public OutlierSpec getOutliers() {
        return outlier;
    }

    /**
     * Sets the options for the automatic outliers detection.
     *
     * @param value The new specifications.
     */
    public void setOutliers(@Nonnull OutlierSpec value) {
         outlier = value;
    }

    /**
     * Gets the specifications for the regression model (including calendar
     * effects).
     *
     * @return The specifications for the regression model.
     */
    public RegressionSpec getRegression() {
        return regression;
    }

    /**
     * Sets the specifications for the regression model.
     *
     * @param value The new specifications.
     */
    public void setRegression(@Nonnull RegressionSpec value) {
         regression = value;
    }
    private ArimaSpec arima;
    private TransformSpec transform;
    private AutoModelSpec automdl;
    private EstimateSpec estimate;
    private OutlierSpec outlier;
    private RegressionSpec regression;

    /**
     * Checks that specifications are one of the pre-defined ones.
     *
     * @param spec The inspected specifications
     * @return true if the specifications object is one of the pre-defined ones.
     */
    public static boolean isSystem(TramoSpec spec) {
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
    public static TramoSpec matchSystem(TramoSpec spec) {
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
        return this == obj || (obj instanceof TramoSpec && equals((TramoSpec) obj));
    }

    private boolean equals(TramoSpec spec) {
        if (isUsingAutoModel() != spec.isUsingAutoModel()) {
            return false;
        }
        if (!isUsingAutoModel() && !Objects.equals(spec.arima, arima)) {
            return false;
        }
        if (isUsingAutoModel() && !Objects.equals(spec.automdl, automdl)) {
            return false;
        }
        return Objects.equals(spec.transform, transform)
                && Objects.equals(spec.regression, regression)
                && Objects.equals(spec.outlier, outlier)
                && Objects.equals(spec.estimate, estimate);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        if (isUsingAutoModel()) {
            hash = 11 * hash + automdl.hashCode();
        } else {
            hash = 11 * hash + arima.hashCode();
        }
        hash = 11 * hash + transform.hashCode();
        hash = 11 * hash + estimate.hashCode();
        hash = 11 * hash + outlier.hashCode();
        hash = 11 * hash + regression.hashCode();
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
        return "TR";
    }

    public String toLongString() {
        String s = toString();
        if ("TR".equals(s)) {
            return s;
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("TR[").append(s).append(']');
            return builder.toString();
        }
    }

    public static TramoSpec fromString(String name) {
        switch (name) {
            case "TR0":
                return TR0;
            case "TR1":
                return TR1;
            case "TR2":
                return TR2;
            case "TR3":
                return TR3;
            case "TR4":
                return TR4;
            case "TR5":
                return TR5;
            case "TRfull":
                return TRfull;
            default:
                return new TramoSpec();
        }
    }
}
