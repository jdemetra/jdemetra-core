/*
* Copyright 2013 National Bank of Belgium
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


package ec.tstoolkit.ssf;

import ec.tstoolkit.utilities.Jdk6;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class DefaultCompositeModel implements ICompositeModel {

    private final ISsf[] ssfs_;

    public DefaultCompositeModel(ISsf... ssfs){
        ssfs_ = ssfs.clone();
    }

    public DefaultCompositeModel(List<ISsf> ssfs){
        ssfs_ = Jdk6.Collections.toArray(ssfs, ISsf.class);
    }

    @Override
    public int getComponentsCount() {
        return ssfs_.length;
    }

    @Override
    public ISsf getComponent(int iCmp) {
        return ssfs_[iCmp];
    }

    @Override
    public double getWeight(int iCmp, int pos) {
        return 1;
    }

    @Override
    public boolean hasConstantWeights() {
        return true;
    }

}
