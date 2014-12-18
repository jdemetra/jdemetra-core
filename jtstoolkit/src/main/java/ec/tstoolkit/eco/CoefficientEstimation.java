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

import java.text.DecimalFormat;

/**
 * 
 * @author Jean Palate
 */
public class CoefficientEstimation {

    private final double m_ser, m_val;

    /**
     * 
     * @param val
     * @param ser
     */
    public CoefficientEstimation(final double val, final double ser) {
        m_val = val;
        m_ser = ser;
    }

    /**
     * 
     * @return
     */
    public double getStdev() {
        return m_ser;
    }

    /**
     * 
     * @return
     */
    public double getTStat() {
        return m_val / m_ser;
    }

    /**
     * 
     * @return
     */
    public double getValue() {
        return m_val;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        DecimalFormat df4 = new DecimalFormat("0.0000");
        DecimalFormat dg6 = new DecimalFormat("0.######");
        builder.append(dg6.format(m_val));
        if (m_ser != 0) {
            builder.append(" (").append(df4.format(getTStat())).append(')');
        }
        return builder.toString();
    }
}
