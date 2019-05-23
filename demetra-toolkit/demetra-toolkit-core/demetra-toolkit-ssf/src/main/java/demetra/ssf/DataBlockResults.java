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
package demetra.ssf;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;

/**
 *
 * @author Jean Palate
 */
public class DataBlockResults {

    DataBlockStorage data;
    int start;

    /**
     *
     */
    public DataBlockResults() {
    }

    /**
     *
     */
    public void clear() {
        data = null;
    }


    /**
     *
     * @return
     */
    public int getDim() {
        return data.getDim();
    }

    /**
     *
     * @param dim
     * @param start
     * @param end
     */
    public void prepare(final int dim, final int start, final int end) {
        clear();
        this.start=start;
        data = new DataBlockStorage(dim, end - start);
    }
    
    /**
     *
     * @param t
     * @return
     */
    public DataBlock datablock(final int t) {
        if (data == null || t < start) {
            return null;
        } else {
            return data.block(t - start);
        }
    }

    public DataBlock item(int idx){
        return data.item(idx);
    }
    
    public void save(final int t, final DataBlock P) {
        int st = t - start;
        if (st < 0) {
            return;
        }
        int capacity=data.getCapacity();
        if (capacity<=st){
            data.resize(capacity<<1);
        }
        data.save(st, P);
    }
    
    public int getCurrentSize(){
        return data.getCurrentSize();
    }

    /**
     *
     * @return
     */
    public int getStartSaving() {
        return start;
    }

    public boolean isInitialized() {
        return data.getDim()>0;
    }
    
    public void rescale(double factor){
        data.rescale(factor);
    }

}
