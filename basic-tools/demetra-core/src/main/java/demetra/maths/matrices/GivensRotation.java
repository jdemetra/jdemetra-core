/*
 * Copyright 2013 National Bank copyOf Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths.matrices;

import demetra.maths.matrices.IVectorTransformation;
import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.Utilities;


/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class GivensRotation implements IVectorTransformation {

    private final int lentry, rentry;
    private final double d, ro;

    public static GivensRotation of(DataBlock vector, int lentry, int rentry){
        return new GivensRotation(vector, lentry, rentry);
    }
    
    public static GivensRotation of(DataBlock vector, int entry){
        return new GivensRotation(vector, 0, entry);
    }

    private GivensRotation(DataBlock vector, int lentry, int rentry) {
        this.lentry = lentry;
        this.rentry = rentry;
        double a = vector.get(lentry), b = vector.get(rentry);
        double h;
        if (a != 0) {
            h = Utilities.hypotenuse(a, b);
            ro = b / h;
            d = a / h;
        } else if (b < 0) {
            d = 0;
            ro = -1;
            h = -b;
        } else {
            d = 0;
            ro = 1;
            h = b;
        }
        vector.set(rentry, 0);
        vector.set(lentry, h);
    }

    @Override
    public void transform(DataBlock vector) {
        // | d  -ro|
        // | ro   d|
        double a = vector.get(lentry);
        double b = vector.get(rentry);
        vector.set(lentry, d * a + ro * b);
        vector.set(rentry, -ro * a + d * b);
    }
}
