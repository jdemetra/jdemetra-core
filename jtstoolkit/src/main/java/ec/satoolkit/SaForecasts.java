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

package ec.satoolkit;

import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class SaForecasts implements ISaSpecification, Cloneable {

    public static final String METHOD = "saforecasts";
    private boolean m_mul;
    private TsData m_seas, m_cal;
    private ISaSpecification m_method;

    public SaForecasts(ISaSpecification method, ISaResults rslt) {
        m_mul = rslt.getSeriesDecomposition().getMode() == DecompositionMode.Multiplicative;
        //TSData s=rslt.Series(ComponentType.Seasonal, ComponentInformation.Value);
        //TSData fs=rslt.Decomposition.Series(ComponentType.Seasonal, ComponentInformation.Forecast);
        //if (s != null && fs != null)
        //    m_seas = s.Update(fs);
        //TSData c=rslt.Series(ComponentType.CalendarEffect, ComponentInformation.Value);
        //TSData fc=rslt.Series(ComponentType.CalendarEffect, ComponentInformation.Forecast);
        //if (c != null && fc != null)
        //    m_cal = c.Update(fc);
        m_method = method;
    }

    @Override
    public SaForecasts clone() {
        try {
            SaForecasts sf = (SaForecasts) super.clone();
            sf.m_method = m_method.clone();
            return sf;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean read(InformationSet info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    @Override
//    public void fillDictionary(String prefix, List<String> dic) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public String toLongString() {
        return METHOD;
    }
}
