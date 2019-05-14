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
package demetra.arima;

import demetra.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public interface UcarimaType {

    ArimaType getSum();
    
    default ArimaType[] getComponents(){
        ArimaType[] all = new ArimaType[size()];
        for (int i=0; i<all.length; ++i){
            all[i]=getComponent(i);
        }
        return all;
    }
    
    int size();
    
    ArimaType getComponent(int i);

}
