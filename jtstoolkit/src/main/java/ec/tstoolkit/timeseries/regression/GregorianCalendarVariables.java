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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.calendars.DefaultGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class GregorianCalendarVariables implements ITradingDaysVariable, Cloneable {

    private final IGregorianCalendarProvider m_provider;
    private TradingDaysType m_dkind = TradingDaysType.None;

    public static GregorianCalendarVariables getDefault(TradingDaysType dkind) {
        GregorianCalendarVariables vars = new GregorianCalendarVariables(DefaultGregorianCalendarProvider.instance,
                dkind);
        return vars;
    }

    public GregorianCalendarVariables(IGregorianCalendarProvider provider, TradingDaysType dkind) {
        m_provider = provider;
        m_dkind = dkind;
     }

    @Override
    public GregorianCalendarVariables clone(){
        try{
            GregorianCalendarVariables cal=(GregorianCalendarVariables) super.clone();
            return cal;
        } catch (CloneNotSupportedException err){
            throw new AssertionError();
        }
     }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        m_provider.calendarData(m_dkind, domain, data);
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return null;
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return TsFrequency.Undefined;
    }

    @Override
    public String getDescription() {
        switch (m_dkind) {
            case WorkingDays:
                return "Working days";
            case TradingDays:
                return "Trading days";
            default:
                return "";
        }
    }

    @Override
    public int getDim() {
        return m_provider.count(m_dkind);
    }

    @Override
    public String getItemDescription(int idx) {
        int ntd = m_provider.count(m_dkind);
        if (idx < ntd) {
            return m_provider.getDescription(m_dkind, idx);
        } else {
            return null;
        }
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return true;
    }

    public TradingDaysType getDayOfWeek() {
        return m_dkind;
    }

    public void setDayOfWeek(TradingDaysType dtype) {
        m_dkind=dtype;
    }

    public IGregorianCalendarProvider getProvider() {
        return m_provider;
    }
}
