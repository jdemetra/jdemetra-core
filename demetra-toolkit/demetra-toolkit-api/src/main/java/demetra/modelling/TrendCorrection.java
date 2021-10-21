/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.modelling;

import demetra.data.DoubleSeq;
import demetra.design.Algorithm;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TrendCorrection {

    private final TrendCorrectionLoader.Factory FACTORY = new TrendCorrectionLoader.Factory();

    public void seFactory(Factory factory) {
        FACTORY.set(factory);
    }

    public Factory getFactory() {
        return FACTORY.get();
    }

    public DoubleSeq linearTrendCorrection(DoubleSeq seq) {
        return getFactory().linearTrendCorrection(seq);
    }

    public DoubleSeq hpCycle(DoubleSeq seq, double cycleLength) {
        return getFactory().hpCycle(seq, cycleLength);
    }

    public DifferencingResult differencing(DoubleSeq input, int period, int delta, boolean mean) {
        return getFactory().differencing(input, period, delta, mean);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    public static interface Factory {

        DoubleSeq linearTrendCorrection(DoubleSeq seq);

        DoubleSeq hpCycle(DoubleSeq seq, double cycleLength);

        DifferencingResult differencing(DoubleSeq input, int period, int delta, boolean mean);
    }

}
