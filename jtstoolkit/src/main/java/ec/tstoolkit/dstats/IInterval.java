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
package ec.tstoolkit.dstats;

/**
 * Represents a numeric interval with an upper and a lower bound
 */
public interface IInterval {
    /**
     * Gets the lower bound
     * 
     * @return
     */
    double getLBound();

    /**
     * Returns Upper - Lower
     * 
     * @return
     */
    double getLength();

    /**
     * Gets the upper bound
     * 
     * @return
     */
    double getUBound();

    /**
     * Returns true if LE .EQ. UB
     * 
     * @return
     */
    boolean isEmpty();

    /**
     * Returns true if LB .LE. UB. False otherwise
     * 
     * @return
     */
    boolean isValid();

    /**
     * Sets the lower bound
     * 
     * @param lBound
     */
    void setLBound(double lBound);

    /**
     * Sets the upper bound
     * @param uBound
     */
    void setUBound(double uBound);
    
    /**
     * Checks that the given point is in the interval
     * @param pt
     * @return 
     */
    boolean contains(double pt);
}
