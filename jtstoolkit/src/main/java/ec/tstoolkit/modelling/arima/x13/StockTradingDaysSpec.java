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

package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.modelling.ChangeOfRegimeSpec;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class StockTradingDaysSpec implements Cloneable, InformationSetSerializable {

    private ChangeOfRegimeSpec m_changeofregime;
    private int m_w;
    private boolean m_test;

    public StockTradingDaysSpec() {
    }

    public int getW() {
        return m_w;
    }

    public void setW(int value) {
        m_w = value;
    }

    public ChangeOfRegimeSpec getChangeOfRegime() {
        return m_changeofregime;
    }

    public void setChangeOfRegime(ChangeOfRegimeSpec value) {
        m_changeofregime = value;
    }

    public boolean isTest() {
        return m_test;
    }

    public void setTest(boolean value) {
        m_test = value;
    }

    @Override
    public StockTradingDaysSpec clone() {
        StockTradingDaysSpec rslt = new StockTradingDaysSpec();
        if (m_changeofregime != null) {
            rslt.m_changeofregime = m_changeofregime.clone();
        }
        return rslt;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof StockTradingDaysSpec && equals((StockTradingDaysSpec) obj));
    }
    
    private boolean equals(StockTradingDaysSpec other) {
        return Objects.equals(m_changeofregime, other.m_changeofregime)
                && m_test == other.m_test && m_w == other.m_w;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.m_changeofregime);
        hash = 83 * hash + this.m_w;
        hash = 83 * hash + (this.m_test ? 1 : 0);
        return hash;
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
//        dictionary(prefix, dic);
//    }
    
    public static void dictionary(String prefix, List<String> dic) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
