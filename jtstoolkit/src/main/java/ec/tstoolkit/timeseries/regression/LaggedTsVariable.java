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
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LaggedTsVariable implements ITsModifier {
    // / <summary>
    // / firstlag less then lastlag !!!
    // / positive lags mean past, negative lags mean future !!!
    // / </summary>
    // / <param name="var"></param>
    // / <param name="firstlag">First lag. Usually 0 or 1</param>
    // / <param name="lastlag">Last lag</param>

    private final int m_firstlag, m_lastlag;
    private ITsVariable var;

    /**
     * 
     * @param var
     * @param firstlag
     * @param lastlag
     */
    public LaggedTsVariable(ITsVariable var, int firstlag, int lastlag) {
        if (lastlag < firstlag) {
            throw new TsException("Invalid lags");
        }
        this.var = var;
        m_firstlag = firstlag;
        m_lastlag = lastlag;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        int nvar = var.getDim();
        for (int i = m_firstlag, k = 0; i <= m_lastlag; ++i, k+=nvar) {
            var.data(domain.move(-i), data.subList(k, k+nvar));
        }
    }

    @Override
    public TsDomain getDefinitionDomain() {
        TsDomain dom = var.getDefinitionDomain();
        if (dom == null) {
            return null;
        }
        return new TsDomain(dom.getStart().plus(m_lastlag), dom.getLength()
                - m_lastlag + m_firstlag);
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return var.getDefinitionFrequency();
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(var.getDescription());
        if (m_firstlag > 0) {
            builder.append("[-").append(m_firstlag).append(" : ");
        }
        else if (m_firstlag < 0) {
            builder.append("[+").append(-m_firstlag).append(" : ");
        }
        if (m_lastlag > 0) {
            builder.append('-').append(m_lastlag).append(']');
        }
        else if (m_lastlag < 0) {
            builder.append('+').append(-m_lastlag).append(']');
        }
        return builder.toString();
    }

    @Override
    public int getDim() {
        return getLagsCount() * var.getDim();
    }

    /**
     * 
     * @return
     */
    public int getFirstLag() {
        return m_firstlag;
    }

    @Override
    public String getItemDescription(int idx) {
        int nvar = var.getDim();

        int lag = m_firstlag + idx / nvar;

        StringBuilder builder = new StringBuilder();
        builder.append(var.getItemDescription(idx % nvar));
        if (lag > 0) {
            builder.append(" [-").append(lag).append(']');
        }
        else if (lag < 0) {
            builder.append(" [+").append(-lag).append(']');
        }
        return builder.toString();
    }

    /**
     * 
     * @return
     */
    public int getLagsCount() {
        return m_lastlag - m_firstlag + 1;
    }

    /**
     * 
     * @return
     */
    public int getLastLag() {
        return m_lastlag;
    }

    @Override
    public ITsVariable getVariable() {
        return var;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        // check common domain
        int n = domain.getLength() + m_firstlag - m_lastlag;
        if (n <= 0) {
            return false;
        }
        return var.isSignificant(new TsDomain(domain.getStart().minus(
                m_firstlag), n));
    }
}
