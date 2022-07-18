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
package jdplus.ssf;

/**
 * Basic state component, composed of an initialization and of a dynamics
 * @author Jean Palate
 */
@lombok.AllArgsConstructor
@lombok.experimental.FieldDefaults(makeFinal=true, level=lombok.AccessLevel.PRIVATE)
@lombok.Getter
@lombok.experimental.Accessors(fluent=true)
public class StateComponent {
    
    @lombok.NonNull
    ISsfInitialization initialization;
    @lombok.NonNull
    ISsfDynamics dynamics;
    
    public int dim(){
        return initialization.getStateDim();
    }
}
