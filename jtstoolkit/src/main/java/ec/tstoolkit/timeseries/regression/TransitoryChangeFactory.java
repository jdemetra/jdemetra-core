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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TransitoryChangeFactory implements IOutlierFactory {

    private double coefficient = DEF_TCRATE;
    private boolean monthlyCoefficient;

    /**
     *
     */
    public static final double DEF_TCRATE = .7;
    
    public boolean isMonthlyCoefficient(){
        return monthlyCoefficient;
    }
    
    public void setMonthlyCoefficient(boolean mc){
        this.monthlyCoefficient=mc;
    }

    /**
     *
     * @param position
     * @return
     */
    @Override
    public TransitoryChange create(Day position) {
	return new TransitoryChange(position, coefficient, monthlyCoefficient);
    }

    /**
     *
     * @param tsdomain
     * @return
     */
    @Override
    public TsDomain definitionDomain(TsDomain tsdomain) {
	return tsdomain.drop(0, 1);
    }

    /**
     * 
     * @return
     */
    public double getCoefficient()
    {
	return coefficient;
    }

    /**
     *
     * @return
     */
    @Override
    public OutlierType getOutlierType() {
	return OutlierType.TC;
    }

    /**
     * 
     * @param value
     */
    public void setCoefficient(double value)
    {
	coefficient = value;
    }
}
