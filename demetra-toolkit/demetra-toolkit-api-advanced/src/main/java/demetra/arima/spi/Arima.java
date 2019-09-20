/*
 * Copyright 2019 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package demetra.arima.spi;

import demetra.arima.ArimaModel;
import demetra.arima.ArimaType;
import demetra.arima.UcarimaModel;
import demetra.design.Algorithm;
import nbbrd.service.ServiceDefinition;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Arima {

    private final ArimaLoader.Processor PROCESSOR = new ArimaLoader.Processor();

    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public IntToDoubleFunction autoCovarianceFunction(ArimaType process) {
        return PROCESSOR.get().autoCovarianceFunction(process);
    }

    public DoubleUnaryOperator pseudoSpectrum(ArimaType process) {
        return PROCESSOR.get().pseudoSpectrum(process);
    }

    public IntToDoubleFunction piWeights(ArimaType process) {
        return PROCESSOR.get().piWeights(process);
    }

    public IntToDoubleFunction psiWeights(ArimaType process) {
        return PROCESSOR.get().psiWeights(process);
    }

    public ArimaModel plus(ArimaType left, ArimaType right) {
        return PROCESSOR.get().plus(left, right);
    }

    public ArimaModel minus(ArimaType left, ArimaType right) {
        return PROCESSOR.get().minus(left, right);
    }

    public ArimaModel plus(ArimaType left, double noise) {
        return PROCESSOR.get().plus(left, noise);
    }

    public ArimaType minus(ArimaType left, double noise) {
        return PROCESSOR.get().minus(left, noise);
    }

    public UcarimaModel doCanonical(UcarimaModel ucarima) {
        return PROCESSOR.get().doCanonical(ucarima);
    }

    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    @Algorithm
    public static interface Processor {

        IntToDoubleFunction autoCovarianceFunction(ArimaType process);

        DoubleUnaryOperator pseudoSpectrum(ArimaType process);

        IntToDoubleFunction piWeights(ArimaType process);

        IntToDoubleFunction psiWeights(ArimaType process);

        ArimaModel plus(ArimaType left, ArimaType right);

        ArimaModel minus(ArimaType left, ArimaType right);

        ArimaModel plus(ArimaType left, double noise);

        ArimaModel minus(ArimaType left, double noise);

        UcarimaModel doCanonical(UcarimaModel ucarima);

    }
}
