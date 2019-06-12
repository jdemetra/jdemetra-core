/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.x12;

import demetra.design.BuilderPattern;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.regular.IArmaModule;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import demetra.arima.SarmaSpecification;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class ArmaModule implements IArmaModule {

    public static Builder builder() {
        return new Builder();
    }

    @BuilderPattern(ArmaModule.class)
    public static class Builder {

        private boolean wn = false;
        private boolean balanced = false, mixed = true;
        private double eps = 1e-5;

        private Builder() {
        }

        Builder balanced(boolean balanced) {
            this.balanced = balanced;
            return this;
        }

        Builder mixed(boolean mixed) {
            this.mixed = mixed;
            return this;
        }

        Builder estimationPrecision(double eps) {
            this.eps = eps;
            return this;
        }

        Builder acceptWhiteNoise(boolean ok) {
            this.wn = ok;
            return this;
        }

        ArmaModule build() {
            return new ArmaModule(this);
        }
    }


        private final boolean wn, balanced, mixed;
        private final double eps;

    private ArmaModule(Builder builder) {
        this.balanced = builder.balanced;
        this.mixed = builder.mixed;
        this.wn = builder.wn;
        this.eps = builder.eps;
    }

    private ArmaModuleImpl createModule() {
        return ArmaModuleImpl.builder()
                .acceptWhiteNoise(wn)
                .balanced(balanced)
                .mixed(mixed)
                .estimationPrecision(eps)
                .maxP(2)
                .maxQ(2)
                .maxBp(1)
                .maxBq(1)
                .build();
    }

    @Override
    public ProcessingResult process(RegArimaModelling context) {
        ModelDescription desc = context.getDescription();
        SarimaSpecification curspec = desc.getSpecification();
        DoubleSeq res = RegArimaUtility.olsResiduals(desc.regarima());
        ArmaModuleImpl impl = createModule();
        SarmaSpecification nspec = impl.process(res, curspec.getPeriod(), curspec.getD(), curspec.getBd(), desc.getAnnualFrequency()>1);
        if (nspec.equals(curspec.doStationary())) {
            return ProcessingResult.Unchanged;
        }
        curspec = SarimaSpecification.of(nspec, curspec.getD(), curspec.getBd());
        desc.setSpecification(curspec);
        context.setEstimation(null);
        return ProcessingResult.Changed;
    }

    public SarimaSpecification process(RegArimaModel<SarimaModel> regarima, boolean seas) {
        SarimaSpecification curSpec = regarima.arima().specification();
        DoubleSeq res = RegArimaUtility.olsResiduals(regarima);
        ArmaModuleImpl impl = createModule();
        SarmaSpecification spec = impl.process(res, curSpec.getPeriod(), curSpec.getD(), curSpec.getBd(), curSpec.getPeriod() > 1);
        if (spec == null) {
            curSpec.airline(seas);
            return curSpec;
        } else {
            return SarimaSpecification.of(spec, curSpec.getD(), curSpec.getBd());
        }
    }
}
