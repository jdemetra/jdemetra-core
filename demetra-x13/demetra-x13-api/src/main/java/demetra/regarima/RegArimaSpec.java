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
package demetra.regarima;

import demetra.modelling.regarima.SarimaSpec;
import demetra.design.Development;
import demetra.design.LombokWorkaround;
import demetra.modelling.RegressionTestSpec;
import demetra.modelling.TransformationType;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.TradingDaysType;
import demetra.util.Validatable;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value

@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "buildWithoutValidation")
public final class RegArimaSpec implements Validatable<RegArimaSpec> {

    private static final RegArimaSpec DEFAULT = RegArimaSpec.builder().build();

    private BasicSpec basic;
    private TransformSpec transform;
    private RegressionSpec regression;
    private OutlierSpec outliers;
    private AutoModelSpec autoModel;
    private SarimaSpec arima;
    private EstimateSpec estimate;

    @LombokWorkaround
    public static Builder builder() {
        SarimaSpec arima = SarimaSpec.builder()
                .validator(SarimaValidator.VALIDATOR)
                .airline()
                .build();
        return new Builder()
                .basic(BasicSpec.builder().build())
                .transform(TransformSpec.builder().build())
                .estimate(EstimateSpec.builder().build())
                .autoModel(AutoModelSpec.builder().build())
                .outliers(OutlierSpec.builder().build())
                .arima(arima)
                .regression(RegressionSpec.builder().build());
    }

    public boolean isUsingAutoModel() {
        return autoModel.isEnabled();
    }

    @Override
    public RegArimaSpec validate() throws IllegalArgumentException {
        basic.validate();
        transform.validate();
        regression.validate();
        outliers.validate();
        autoModel.validate();
        estimate.validate();
        return this;
    }

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    public static class Builder implements Validatable.Builder<RegArimaSpec> {

        public Builder usingAutoModel(boolean enableAutoModel) {
            this.autoModel = autoModel.toBuilder().enabled(enableAutoModel).build();
            return this;
        }

        public Builder arima(@NonNull SarimaSpec sarima) {
            this.arima = sarima;
            if (this.autoModel == null) {
                this.autoModel = AutoModelSpec.builder().build();
            }
            return this;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Default specifications">
    public static final RegArimaSpec RGDISABLED, RG0, RG1, RG2, RG3, RG4, RG5;

    public static final RegArimaSpec[] allSpecifications() {
        return new RegArimaSpec[]{RG0, RG1, RG2, RG3, RG4, RG5};
    }

    static {
        RGDISABLED = RegArimaSpec.builder()
                .basic(BasicSpec.builder().preProcessing(false).build())
                .build();

        TransformSpec tr = TransformSpec.builder()
                .function(TransformationType.Auto)
                .build();

        EasterSpec easter = EasterSpec.builder()
                .easterSpec(true)
                .build();

        TradingDaysSpec wd = TradingDaysSpec.builder()
                .type(TradingDaysType.WorkingDays)
                .lengthOfPeriodTime(LengthOfPeriodType.LeapYear)
                .test(RegressionTestSpec.Remove)
                .build();

        TradingDaysSpec td = TradingDaysSpec.builder()
                .type(TradingDaysType.TradingDays)
                .lengthOfPeriodTime(LengthOfPeriodType.LeapYear)
                .test(RegressionTestSpec.Remove)
                .build();

        RegressionSpec rwd = RegressionSpec.builder()
                .easter(easter)
                .tradingDays(wd)
                .build();

        RegressionSpec rtd = RegressionSpec.builder()
                .easter(easter)
                .tradingDays(td)
                .build();

        OutlierSpec o = OutlierSpec.builder()
                .type(SingleOutlierSpec.builder().type("AO").build())
                .type(SingleOutlierSpec.builder().type("TC").build())
                .type(SingleOutlierSpec.builder().type("LS").build())
                .build();

        RG0 = RegArimaSpec.builder()
                .build();

        RG1 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .build();

        RG2 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rwd)
                .build();
        RG3 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .usingAutoModel(true)
                .build();
        RG4 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rwd)
                .usingAutoModel(true)
                .build();

        RG5 = RegArimaSpec.builder()
                .transform(tr)
                .outliers(o)
                .regression(rtd)
                .usingAutoModel(true)
                .build();
    }
    //</editor-fold>
}
