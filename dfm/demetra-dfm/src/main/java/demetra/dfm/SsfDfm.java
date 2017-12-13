/*
 * Copyright 2013-2014 National Bank of Belgium
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
package demetra.dfm;

import demetra.var.VarDescriptor;
import demetra.var.VarDynamics;
import demetra.ssf.multivariate.MultivariateSsf;
import demetra.var.VarInitialization;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SsfDfm  {

    public MultivariateSsf of(VarDescriptor vdesc, MeasurementDescriptor[] mdesc) {
        int nlx = vdesc.getLagsCount();
        for (int i=0; i<mdesc.length; ++i){
            int n=mdesc[i].getType().getLength();
            if (nlx<n)
                nlx=n;
        }
        return of(vdesc, mdesc, nlx);
    }

    public MultivariateSsf of(VarDescriptor vdesc, MeasurementDescriptor[] mdesc, int nlx) {
        int nf = vdesc.getVariablesCount();
        VarInitialization initialization = new VarInitialization(nf*nlx, null);
        VarDynamics dyn = VarDynamics.of(vdesc, nlx);
        DfmMeasurements m = DfmMeasurements.from(nf, nlx, mdesc);
        return new MultivariateSsf(initialization, dyn, m);
    }

}
