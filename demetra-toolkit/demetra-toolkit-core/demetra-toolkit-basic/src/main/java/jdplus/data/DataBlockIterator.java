/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.data;

import demetra.design.Development;
import java.util.Iterator;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Release)
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
