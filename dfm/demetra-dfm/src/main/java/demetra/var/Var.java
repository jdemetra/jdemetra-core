/*
 * Copyright 2016-2017 National Bank of Belgium
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
package demetra.var;

import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.multivariate.MultivariateSsf;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class Var {

    MultivariateSsf of(VarDescriptor desc) {
        VarDynamics dynamics = VarDynamics.of(desc);
        VarInitialization initialization = new VarInitialization(desc.getVariablesCount() * desc.getLagsCount(), null);
        ISsfMeasurements measurements = new VarMeasurements(desc.getVariablesCount(), desc.getLagsCount());
        return new MultivariateSsf(initialization, dynamics, measurements);
    }

    public static MultivariateSsf of(VarDescriptor desc, int nlags) {
        int nl = Math.max(nlags, desc.getLagsCount());
        VarDynamics dynamics = VarDynamics.of(desc);
        VarInitialization initialization = new VarInitialization(desc.getVariablesCount() * nl, null);
        ISsfMeasurements measurements = new VarMeasurements(desc.getVariablesCount(), desc.getLagsCount());
        return new MultivariateSsf(initialization, dynamics, measurements);
    }

}
