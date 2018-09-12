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
package demetra.ucarima;

import demetra.maths.Complex;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.UnitRoots;

/**
 * 
 * @author Jean Palate
 */
public class SeasonalSelector extends AbstractRootSelector {

    private double m_k = 0.5;

    private double m_epsphi = Math.PI / 90; // (== 2 degrees)

    private int frequency;

    /**
	 *
	 */
    public SeasonalSelector() {
	frequency = 12;
    }

    /**
     * 
     * @param freq
     */
    public SeasonalSelector(final int freq) {
	frequency = freq;
    }

    /**
     * 
     * @param freq
     * @param epsphi
     */
    public SeasonalSelector(final int freq, final double epsphi) {
	frequency = freq;
	m_epsphi = epsphi;
    }

    @Override
    public boolean accept(final Complex root) {
	if (Math.abs(root.getIm()) < 1e-6) {
	    if (1/root.getRe() < -m_k)
		return true;
	    else
		return false;
	}

	double pi = 2 * Math.PI / frequency;
	double arg = Math.abs(root.arg());
        double eps=m_epsphi/180*Math.PI;
	for (int i = 1; i <= frequency / 2; ++i) {
	    if (Math.abs(pi * i - arg) <= eps)
		return true;
	}
	return false;
    }

    /**
     * 
     * @return
     */
    public int getFrequency()
    {
	return frequency;
    }

    /**
     * 
     * @return
     */
    public double getK() {
	return m_k;
    }

    /**
     * @return Tolerance in radian
     */
    public double getTolerance() {
	return m_epsphi;
    }

    /**
     * 
     * @param value
     */
    public void setFrequency(final int value) {
	frequency = value;
    }

    /**
     * 
     * @param value
     */
    public void setK(final double value) {
	m_k = value;
    }

    /**
     * 
     * @param value
     */
    public void setTolerance(final double value) {
	m_epsphi = value;
    }

    @Override
    public boolean selectUnitRoots(Polynomial p) {
        // remove (1_B)
        selected=Polynomial.ONE;
        notSelected=p;
        if (frequency == 1)
            return false;
        Polynomial S=UnitRoots.S(frequency, 1);
        do{
            Polynomial.Division div = Polynomial.divide(notSelected, S);
            if ( div.isExact() ){
                selected=selected.times(S);
                notSelected=div.getQuotient();
            }else
                break;
        }while (p.degree()>=S.degree());
        return selected.degree()>0;
    }

}
