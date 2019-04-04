/*
 * Copyright 2015 National Bank of Belgium
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
package demetra.ssf.univariate;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeq;
import demetra.data.Seq;

/**
 *
 * @author Jean Palate
 */
public interface ISsfData extends DoubleSeq{


     /**
     *
     * @param pos
     * @return
     */
    boolean isMissing(int pos);

    /**
     *
     * @return
     */
    boolean hasData();

    
    default int getObsCount(){
        int nm=0, n=length();
        for (int i=0; i<n; ++i){
            if (isMissing(i))
                ++nm;
        }
        return n-nm;
    }
 
    default boolean hasMissingValues(){
        int n=length();
        for (int i=0; i<n; ++i){
            if (isMissing(i))
                return true;
        }
        return false;
    }
}
