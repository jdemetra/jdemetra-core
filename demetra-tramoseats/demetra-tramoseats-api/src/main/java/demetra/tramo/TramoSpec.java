/*
 * Copyright 2019 National Bank of Belgium
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

import nbbrd.design.Development;
import nbbrd.design.LombokWorkaround;
import demetra.modelling.TransformationType;
import demetra.arima.SarimaSpec;
import demetra.timeseries.regression.RegressionTestType;
import demetra.timeseries.regression.TradingDaysType;
import demetra.util.Validatable;
import lombok.NonNull;

/**
 * Defines the specifications for the Tramo pre-processing, which is
 * an(automatic)regarima modelling
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class TramoSpec implements Validatable<TramoSpec> {

    public static final TramoSpec DEFAULT = TramoSpec.builder().build();

    /**
     * Gets the predefined Arima specification. The AutoModel and the Arima
     * properties are mutually exclusive specifications: Related TRAMO options:
     * P, D, Q, BP, BD, BQ, IMEAN, JPR, JPS, JQR, JQS, INIT.
     *
     * @return the Arima specifications
     */
    @lombok.NonNull
    private SarimaSpec arima;

    /**
     * Gets the specifications related to the transformation of the original
     * series. Related TRAMO options: LAM, FCT, UNITS.
     * -- SETTER --
     * Sets the specifications related to the transformation of the original
     * series.
     *
     * @return The transform specifications
     * @param transform The new transform specifications. Should not be null
     */
    @lombok.NonNull
    private TransformSpec transform;

    /**
     * Gets the specifications related to the automatic ARIMA modelling (AMI).
     * The AutoModelSpec and the ArimaSpec are mutually exclusive
     * specifications: Related TRAMO options: IDIF, INIC, UB1, UB2, CANCEL,
     * TSIG, PR, PCR.
     *
     * @return The AMI specifications
     */
    @lombok.NonNull
    private AutoModelSpec autoModel;

    /**
     * Gets options related to the estimation routine Related TRAMO options:
     * TOL, TYPE.
     * -- SETTER --
     * Sets new options for the estimation routine.
     *
     * @param estimate The new options
     * @return
     */
    @lombok.NonNull
    private EstimateSpec estimate;

    /**
     * Gets the options for the automatic outliers detection Related TRAMO
     * options: IATIP, AIO, TC, VA
     * -- SETTER --
     * Sets the options for the automatic outliers detection.
     *
     * @return the options for automatic outliers detection.
     * @param outliers The new specifications.
     */
    @lombok.NonNull
    private OutlierSpec outliers;

    /**
     * Gets the specifications for the regression model (including calendar
     * effects).
     * -- SETTER --
     * Sets the specifications for the regression model.
     *
     * @param regression The new regression specifications.
     * @return The specifications for the regression model.
     */
    @lombok.NonNull
    private RegressionSpec regression;

    /**
     * Creates a new default specification builder. No transformation, no
     * regression
     * variables, no outliers detection, default airline model (without mean).
     *
     * @return the builder initialized with default parameters
     */
    @LombokWorkaround
    public static Builder builder() {
        SarimaSpec sarima = SarimaSpec.airline();
        RegressionSpec regs = RegressionSpec.builder()
                .mean(true)
                .build();
        return new Builder()
                .transform(TransformSpec.builder().build())
                .estimate(EstimateSpec.builder().build())
                .autoModel(AutoModelSpec.builder().build())
                .outliers(OutlierSpec.builder().build())
                .arima(sarima)
                .regression(regs);
    }

    @Override
    public TramoSpec validate() throws IllegalArgumentException {
        autoModel.validate();
        estimate.validate();
        outliers.validate();
        regression.validate();
        transform.validate();
        return this;
    }

    /**
     * Checks that the AMI is enabled
     *
     * @return true if the AMI spec is enabled, false otherwise.
     */
    public boolean isUsingAutoModel() {
        return autoModel.isEnabled();
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<TramoSpec> {

        /**
         * Enables/disables the AMI.
         *
         * @param enableAutoModel
         */
        public Builder usingAutoModel(boolean enableAutoModel) {
            this.autoModel = autoModel.toBuilder().enabled(enableAutoModel).build();
            return this;
        }

        /**
         * Sets a predefined Sarima specification. The AMI is disabled.
         *
         * @param sarima new Sarima spec
         * @return
         */
        public Builder arima(@NonNull SarimaSpec sarima) {
            this.arima = sarima;
            if (this.autoModel == null) {
                this.autoModel = AutoModelSpec.builder().build();
            }

            return this;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Default Specifications">
    public static final TramoSpec TR0, TR1, TR2, TR3, TR4, TR5, TRfull;

    public static final TramoSpec[] allSpecifications() {
        return new TramoSpec[]{TR0, TR1, TR2, TR3, TR4, TR5, TRfull};
    }

    static {
        TR0 = TramoSpec.builder().build();

        TransformSpec tr = TransformSpec.builder()
                .function(TransformationType.Auto)
                .build();

        EasterSpec e = EasterSpec.builder()
                .type(EasterSpec.Type.Standard)
                .test(true)
                .build();

        TradingDaysSpec wd = TradingDaysSpec.td(TradingDaysType.WorkingDays, 
                true, RegressionTestType.Separate_T);

        TradingDaysSpec td = TradingDaysSpec.td(TradingDaysType.TradingDays,
                true, RegressionTestType.Separate_T);

        TradingDaysSpec dc = TradingDaysSpec.automatic(TradingDaysSpec.AutoMethod.FTest, 
                TradingDaysSpec.DEF_PFTD);
        
         EasterSpec ec = e.toBuilder()
                .type(EasterSpec.Type.IncludeEaster)
                .test(true)
                .build();

        CalendarSpec cwd = CalendarSpec.builder()
                .easter(e)
                .tradingDays(wd)
                .build();
        RegressionSpec rwd = RegressionSpec.builder()
                .mean(true)
                .calendar(cwd)
                .build();

        OutlierSpec o = OutlierSpec.builder()
                .ao(true)
                .tc(true)
                .ls(true).build();

        TR1 = TramoSpec.builder()
                .transform(tr)
                .outliers(o)
                .build();

        TR2 = TramoSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rwd)
                .build();

        TR3 = TramoSpec.builder()
                .transform(tr)
                .outliers(o)
                .usingAutoModel(true)
                .build();

        TR4 = TramoSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rwd)
                .usingAutoModel(true)
                .build();

        CalendarSpec ctd = CalendarSpec.builder()
                .easter(e)
                .tradingDays(td)
                .build();

        RegressionSpec rtd = RegressionSpec.builder()
                .calendar(ctd)
                .build();

        TR5 = TramoSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rtd)
                .usingAutoModel(true)
                .build();

        CalendarSpec cc = CalendarSpec.builder()
                .easter(ec)
                .tradingDays(dc)
                .build();
        RegressionSpec rc = RegressionSpec.builder()
                .calendar(cc)
                .build();
        TRfull = TramoSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rc)
                .usingAutoModel(true)
                .build();
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
                throw new TramoException();
        }
    } 
    //</editor-fold>
}
