/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.highfreq.regarima;

import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;

/**
 *
 * @author PALATEJ
 */
public class ArimaComputer {
    
    private final double eps;
    private final boolean exactDerivatives;
    
    public ArimaComputer(double eps, boolean exactDerivatives){
        this.eps=eps;
        this.exactDerivatives=exactDerivatives;
    }
    
    public <S extends IArimaModel> RegArimaEstimation<S> process(final RegArimaModel<S> regarima, IArimaMapping<S> mapping){
        
        GlsArimaProcessor finalProcessor = GlsArimaProcessor.builder(IArimaModel.class)
                .precision(eps)
                .computeExactFinalDerivatives(exactDerivatives)
                .build();
        return finalProcessor.process(regarima, mapping);
    }
}
