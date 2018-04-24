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
package demetra.modelling.regression;

import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
public class OutlierDefinition {

    private @lombok.NonNull String code;
    private @lombok.NonNull LocalDateTime position;
    private Double coefficient;

    public boolean isFixed() {
        return coefficient != null;
    }   
    
    public OutlierDefinition withCoefficient(Double c){
        return new OutlierDefinition(code, position, c);
    }
}
