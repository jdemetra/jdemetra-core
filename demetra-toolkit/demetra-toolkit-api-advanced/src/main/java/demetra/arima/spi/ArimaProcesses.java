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

import demetra.arima.ArimaProcess;
import demetra.arima.SarimaProcess;
import demetra.arima.UcarimaProcess;
import demetra.design.Algorithm;
import demetra.design.ServiceDefinition;
import demetra.util.ServiceLookup;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class ArimaProcesses {

    private final AtomicReference<Processor> PROCESSOR = ServiceLookup.firstMutable(Processor.class);
    
    public void setProcessor(Processor processor) {
        PROCESSOR.set(processor);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }
    
    public IntToDoubleFunction autoCovarianceFunction(ArimaProcess process){
        return PROCESSOR.get().autoCovarianceFunction(process);
    }

    public DoubleUnaryOperator pseudoSpectrum(ArimaProcess process){
        return PROCESSOR.get().pseudoSpectrum(process);
    }

    public IntToDoubleFunction piWeights(ArimaProcess process){
        return PROCESSOR.get().piWeights(process);
    }

    public IntToDoubleFunction psiWeights(ArimaProcess process){
        return PROCESSOR.get().psiWeights(process);
    }
        
    public ArimaProcess plus(ArimaProcess left, ArimaProcess right){
        return PROCESSOR.get().plus(left, right);
    }

    public ArimaProcess minus(ArimaProcess left, ArimaProcess right){
        return PROCESSOR.get().minus(left, right);
    }
        
    public ArimaProcess plus(ArimaProcess left, double noise){
        return PROCESSOR.get().plus(left, noise);
    }

    public ArimaProcess minus(ArimaProcess left, double noise){
        return PROCESSOR.get().minus(left, noise);
    }
       
    public ArimaProcess convert(SarimaProcess sarima){
        return PROCESSOR.get().convert(sarima);
    }
        
    public UcarimaProcess doCanonical(UcarimaProcess ucarima){
        return PROCESSOR.get().doCanonical(ucarima);
    }
    
    @ServiceDefinition
    @Algorithm
    public static interface Processor {

        IntToDoubleFunction autoCovarianceFunction(ArimaProcess process);

        DoubleUnaryOperator pseudoSpectrum(ArimaProcess process);

        IntToDoubleFunction piWeights(ArimaProcess process);

        IntToDoubleFunction psiWeights(ArimaProcess process);
        
        ArimaProcess plus(ArimaProcess left, ArimaProcess right);

        ArimaProcess minus(ArimaProcess left, ArimaProcess right);
        
        ArimaProcess plus(ArimaProcess left, double noise);

        ArimaProcess minus(ArimaProcess left, double noise);
       
        ArimaProcess convert(SarimaProcess sarima);
        
        UcarimaProcess doCanonical(UcarimaProcess ucarima);

    }
}
