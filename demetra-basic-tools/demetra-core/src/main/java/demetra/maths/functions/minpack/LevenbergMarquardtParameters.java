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

package demetra.maths.functions.minpack;

/**
 *
 * @author Jean Palate
 */
public class LevenbergMarquardtParameters implements Cloneable{
    
    @Override
    public LevenbergMarquardtParameters clone(){
        try {
            return (LevenbergMarquardtParameters) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
    
    public static final double DEF_EPS=1e-10, DEF_STEPFACTOR=100;
    public static final int DEF_MAXITER=200;

    public double ftol = DEF_EPS;
    public double xtol = DEF_EPS;
    public double gtol = DEF_EPS;
    public double stepfactor = DEF_STEPFACTOR;
    public int maxiter = DEF_MAXITER;
}
