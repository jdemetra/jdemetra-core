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


package ec.tstoolkit.arima.special;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class MaSpecification implements Cloneable {


    public SarimaModel airline;
    // search method
    public int[] noisyPeriods; // pre-specified periods
    public boolean allPeriods;

    @Override
    public MaSpecification clone(){
        try {
            MaSpecification spec = (MaSpecification) super.clone();
            if (airline != null) {
                spec.airline = airline.clone();
            }
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }
}
