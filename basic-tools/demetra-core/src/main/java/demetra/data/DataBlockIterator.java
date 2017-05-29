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
package demetra.data;

import java.util.Iterator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class DataBlockIterator implements Iterator<DataBlock> {

    private final DataBlock data;
    private final int end, inc;
    private int pos;

    protected DataBlockIterator(final DataBlock start, int niter, int inc) {
        this.data = start;
        end = niter;
        this.inc = inc;
        pos = 0;
    }

    @Override
    public boolean hasNext() {
        return pos < end;
    }

    @Override
    public DataBlock next() {
        pos++;
        data.slide(inc);
        return data;
    }
    
    public void begin(){
        reset(0);
    }

     /**
     * Reset the iterator at position newpos
     *
     * @param newpos The new position. 0 corresponds to begin()
     * @return
     */
    public void reset(final int newpos) {
        int del = pos - newpos;
        if (del != 0) {
            del *= inc;
            data.slide(-del);
            pos = newpos;
        }
    }

    public void reset() {
        reset(0);
    }
}
