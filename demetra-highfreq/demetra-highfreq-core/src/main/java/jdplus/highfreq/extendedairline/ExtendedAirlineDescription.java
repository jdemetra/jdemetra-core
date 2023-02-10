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
package jdplus.highfreq.extendedairline;

import demetra.data.DoubleSeq;
import demetra.highfreq.ExtendedAirlineSpec;
import jdplus.arima.ArimaModel;
import jdplus.arima.estimation.IArimaMapping;
import jdplus.highfreq.regarima.ArimaDescription;

/**
 *
 * @author palatej
 */
@lombok.Getter
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
public class ExtendedAirlineDescription implements ArimaDescription<ArimaModel>{
    
    private final ExtendedAirlineSpec spec;
    private final ExtendedAirlineMapping mapping;
    private final ArimaModel model;
    
    public ExtendedAirlineDescription(ExtendedAirlineSpec spec){
        this.spec=spec;
        this.mapping=ExtendedAirlineMapping.of(spec);
        this.model=mapping.getDefault();
    }

    @Override
    public ArimaModel arima() {
        return model;
    }

    @Override
    public IArimaMapping mapping() {
        return mapping;
    }

    @Override
    public ArimaDescription withParameters(DoubleSeq p) {
        ArimaModel nmodel = mapping.map(p);
        ExtendedAirlineSpec nspec = spec.withFreeParameters(p);
        return new ExtendedAirlineDescription(nspec, mapping, nmodel);
    }

}
