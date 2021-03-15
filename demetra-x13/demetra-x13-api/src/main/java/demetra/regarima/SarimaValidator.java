/*
* Copyright 2019 National Bank of Belgium
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
package demetra.regarima;

import demetra.timeseries.regression.modelling.SarimaSpec;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class SarimaValidator implements SarimaSpec.Validator {

    public static final int MAXP = 6, MAXD = 2, MAXQ = 6, MAXBP = 1, MAXBD = 1, MAXBQ = 1;

    public static final SarimaValidator VALIDATOR = new SarimaValidator();

    @Override
    public void checkP(int value) {
        if (value > MAXP) {
            throw new RegArimaException("P must be <= " + Integer.toString(MAXP));
        }
    }

    @Override
    public void checkD(int value) {
        if (value > MAXD) {
            throw new RegArimaException("D must be <= " + Integer.toString(MAXD));
        }
    }

    @Override
    public void checkQ(int value) {
        if (value > MAXQ) {
            throw new RegArimaException("Q must be <= " + Integer.toString(MAXQ));
        }
    }

    @Override
    public void checkBp(int value) {
        if (value > MAXBP) {
            throw new RegArimaException("BP must be <= " + Integer.toString(MAXBP));
        }
    }

    @Override
    public void checkBd(int value) {
        if (value > MAXBD) {
            throw new RegArimaException("BD must be <= " + Integer.toString(MAXBD));
        }
    }

    @Override
    public void checkBq(int value) {
        if (value > MAXBQ) {
            throw new RegArimaException("BQ must be <= " + Integer.toString(MAXBQ));
        }
    }
}
