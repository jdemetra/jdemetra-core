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

package demetra.tramo;

import demetra.regarima.DefaultArimaSpec;
import demetra.design.Development;
import java.util.Map;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ArimaSpec extends DefaultArimaSpec {

    public static final int MAXP=3, MAXD=2, MAXQ=3, MAXBP=1, MAXBD=1, MAXBQ=1;

    public ArimaSpec() {
    }
    
    public ArimaSpec(ArimaSpec other){
        super(other);
    }

   @Override
    public void setP(int value) {
        if (value > MAXP) {
            throw new TramoException("P must be <= "+Integer.toString(MAXP));
        }
        super.setP(value);
    }

    @Override
    public void setD(int value) {
        if (value > MAXD) {
            throw new TramoException("D must be <= "+Integer.toString(MAXD));
        }
        super.setD(value);
    }

    @Override
    public void setQ(int value) {
        if (value > MAXQ) {
            throw new TramoException("Q must be <= "+Integer.toString(MAXQ));
        }
        super.setQ(value);
    }
     @Override
    public void setBp(int value) {
        if (value > MAXBP) {
            throw new TramoException("BP must be <= "+Integer.toString(MAXBP));
        }
        super.setBp(value);
    }

    @Override
    public void setBd(int value) {
        if (value > MAXBD) {
            throw new TramoException("BD must be <= "+Integer.toString(MAXBD));
        }
        super.setBd(value);
    }

    @Override
    public void setBq(int value) {
        if (value > MAXBQ) {
            throw new TramoException("BQ must be <= "+Integer.toString(MAXBQ));
        }
        super.setBq(value);
    }

}

