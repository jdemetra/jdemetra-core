/*
 * Copyright 2023 National Bank of Belgium
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

import demetra.data.DoubleSeq;
import jdplus.arima.IArimaModel;
import jdplus.arima.estimation.IArimaMapping;

/**
 *
 * @author palatej
 * @param <S>
 */
public interface ArimaDescription<S extends IArimaModel> {
    S arima();
    IArimaMapping<S> mapping();
    
    default S defaultModel(){
        return mapping().getDefault();
    }
    
    ArimaDescription<S> withParameters(DoubleSeq p);

    default int freeParametersCount(){
        return mapping().getDim();
    }
    
    default int differencingOrders(){
        return arima().getNonStationaryArOrder();
    }
}
