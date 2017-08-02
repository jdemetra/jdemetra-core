/*
 * Copyright 2016 National Bank of Belgium
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
/*
 */
package demetra.ucarima.ssf;

import demetra.arima.ArimaModel;
import demetra.arima.ssf.SsfArima;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.implementations.CompositeDynamics;
import demetra.ssf.implementations.Measurement;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.Ssf;
import demetra.ucarima.UcarimaModel;


/**
 *
 * @author Jean Palate
 */
public class SsfUcarima extends Ssf {

    public static SsfUcarima of(final UcarimaModel ucm) {
        ucm.simplify();
        ISsfDynamics[] dyn = new ISsfDynamics[ucm.getComponentsCount()];
        int[] pos = new int[dyn.length];
        pos[0] = 0;
        int dim = 0;
        for (int i = 0; i < dyn.length; ++i) {
            ArimaModel cmp = ucm.getComponent(i);
            dyn[i] = cmp.isStationary() ? new SsfArima.StDynamics(cmp)
                    : new SsfArima.SsfArimaDynamics(cmp);
            pos[i] = dim;
            dim += dyn[i].getStateDim();
        }
        return new SsfUcarima(ucm, new CompositeDynamics(dyn), Measurement.create(dim, pos), pos);
    }

    private final UcarimaModel ucm;
    private final int[] cmpPos;

    private SsfUcarima(final UcarimaModel ucm, ISsfDynamics dyn, ISsfMeasurement m, int[] cmpPos) {
        super(dyn, m);
        this.ucm = ucm;
        this.cmpPos=cmpPos;
    }

    public UcarimaModel getUcarimaModel() {
        return ucm;
    }
    
    public int getComponentPosition(int cmp){
        return cmpPos[cmp];
    }
}
