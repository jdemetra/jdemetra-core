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

package ec.tstoolkit.maths.matrices;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class GivensRotation implements IVectorTransformation {

    private final int lentry_, rentry_;
    private final double d_, ro_;

    public GivensRotation(DataBlock vector, int lentry, int rentry) {
        lentry_ = lentry;
        rentry_ = rentry;
        double a = vector.get(lentry), b = vector.get(rentry);
        double h;
        if (a != 0) {
            h = ElementaryTransformations.hypotenuse(a, b);
            ro_ = b / h;
            d_ = a / h;
        } else {
            d_ = 0;
            ro_ = 1;
            h = b;
        }
        vector.set(rentry, 0);
        vector.set(lentry, h);
    }

    public GivensRotation(DataBlock vector, int entry) {
        this(vector, 0, entry);
    }

    @Override
    public void transform(DataBlock vector) {
        // | d  -ro|
        // | ro   d|
        double a = vector.get(lentry_);
        double b = vector.get(rentry_);
        vector.set(lentry_, d_ * a + ro_ * b);
        vector.set(rentry_, -ro_ * a + d_ * b);
    }
}
