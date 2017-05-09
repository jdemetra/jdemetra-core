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
package demetra.ssf.dk;

import demetra.data.DataBlock;
import demetra.ssf.univariate.IFilteringResults;

/**
 *
 * @author Jean Palate
 */
public interface IBaseDiffuseFilteringResults extends IFilteringResults{
    /**
     *
     * @param pos
     */
    void close(int pos);

    
    default double diffuseNorm2(int pos) {
        return Double.NaN;
    }

    default DataBlock Mi(int pos) {
        return null;
    }
   
    default int getEndDiffusePosition(){
        return -1;
    };
}
