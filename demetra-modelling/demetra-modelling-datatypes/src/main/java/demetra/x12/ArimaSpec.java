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
package demetra.x12;

import demetra.regarima.DefaultArimaSpec;

/**
 *
 * @author Jean Palate
 */
public class ArimaSpec extends DefaultArimaSpec {

    public static final int MAXP = 6, MAXD = 2, MAXQ = 6, MAXBP = 1, MAXBD = 1, MAXBQ = 1;

    public ArimaSpec() {
    }

    public ArimaSpec(ArimaSpec other) {
        super(other);
    }

    @Override
    public void setP(int value) {
        if (value > MAXP) {
            throw new X12Exception("P must be <= " + Integer.toString(MAXP));
        }
        super.setP(value);
    }

    @Override
    public void setD(int value) {
        if (value > MAXD) {
            throw new X12Exception("D must be <= " + Integer.toString(MAXD));
        }
        super.setD(value);
    }

    @Override
    public void setQ(int value) {
        if (value > MAXQ) {
            throw new X12Exception("Q must be <= " + Integer.toString(MAXQ));
        }
        super.setQ(value);
    }

    @Override
    public void setBp(int value) {
        if (value > MAXBP) {
            throw new X12Exception("BP must be <= " + Integer.toString(MAXBP));
        }
        super.setBp(value);
    }

    @Override
    public void setBd(int value) {
        if (value > MAXBD) {
            throw new X12Exception("BD must be <= " + Integer.toString(MAXBD));
        }
        super.setBd(value);
    }

    @Override
    public void setBq(int value) {
        if (value > MAXBQ) {
            throw new X12Exception("BQ must be <= " + Integer.toString(MAXBQ));
        }
        super.setBq(value);
    }
}
