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
package ec.tstoolkit.eco;

/**
 * 
 * @author Jean Palate
 */
public class SingleOls {
    private double[] m_x, m_y;

    private int m_lag;

    private double m_b, m_xtx, m_xty, m_yty, m_se2;

    /**
	 *
	 */
    public SingleOls() {
    }

    private void calc() {
	if (m_xtx != 0)
	    return;
	int n = m_y.length;
	int i0 = m_lag > 0 ? m_lag : 0;
	int xn = m_x.length + m_lag;
	if (xn < n)
	    n = xn;
	for (int i = i0; i < n; ++i) {
	    double x = m_x[i - m_lag];
	    m_xtx += x * x;
	    m_xty += x * m_y[i];
	}
	m_b = m_xty / m_xtx;
    }

    private void calcse2() {
	if (m_se2 != 0)
	    return;
	calc();
	calcyty();
	m_se2 = m_yty - m_xty / m_xtx * m_xty;
    }

    private void calcyty() {
	if (m_yty != 0)
	    return;
	int n = m_y.length;
	for (int i = 0; i < n; ++i)
	    m_yty += m_y[i] * m_y[i];
    }

    private void clear() {
	m_b = m_xtx = m_xty = m_se2 = 0;
    }

    /**
     * 
     * @return
     */
    public double getCoefficient() {
	calc();
	return m_b;
    }

    /**
     * 
     * @return
     */
    public int getLag() {
	return m_lag;
    }

    /**
     * 
     * @return
     */
    public double getSer() {
	calcse2();
	return Math.sqrt(m_se2 / (m_y.length - 1));
    }

    /**
     * 
     * @return
     */
    public double getSsqErr() {
	calcse2();
	return m_se2;
    }

    /**
     * 
     * @return
     */
    public double[] getX() {
	return m_x;
    }

    /**
     * 
     * @return
     */
    public double getXtX() {
	calc();
	return m_xtx;
    }

    /**
     * 
     * @return
     */
    public double getXtY()
    {
	calc();
	return m_xty;
    }

    /**
     * 
     * @return
     */
    public double[] getY() {
	return m_y;
    }

    /**
     * 
     * @param value
     */
    public void setLag(final int value) {
	clear();
	m_lag = value;
    }

    /**
     * 
     * @param value
     */
    public void setX(final double[] value) {
	clear();
	m_x = value;
    }

    /**
     * 
     * @param value
     */
    public void setY(final double[] value) {
	clear();
	m_yty = 0;
	m_y = value;
    }

}
