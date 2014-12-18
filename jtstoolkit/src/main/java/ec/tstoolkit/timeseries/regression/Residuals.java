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

import ec.tstoolkit.IDocumented;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Jean Palate
 */
public class Residuals implements IDocumented {

    public static enum Type {
        /// <summary>
        /// no information
        /// </summary>

        Undefined,
        /// <summary>
        /// One step ahead forecast error
        /// </summary>
        OneStepAHead,
        /// <summary>
        /// Maximum likelihood estimates
        /// </summary>
        MLEstimate,
        /// <summary>
        /// Residuals obtained through a QR estimates of a regression model (
        /// see for instance TRAMO)
        /// </summary>
        QR_Transformed,
        ///
        FullResiduals;
    }
    private double[] m_values;
    private TsDomain m_domain;
    private Type m_type;
    private NiidTests tests_;
    private MetaData m_metadata = new MetaData();

    public Residuals() {
    }

    public NiidTests getTests(){
        return tests_;
    }

    public Type getType() {
        return m_type;
    }

    public void setType(Type value) {
        m_type = value;
    }

    public double[] getValues() {
        return m_values;
    }

    public void setValues(double[] value) {
        m_values = value.clone();
    }

    public TsDomain getDomain() {
        return m_domain;
    }

    public void setDomain(TsDomain value) {
        m_domain = value;
    }


    public boolean calc(int nhp, int nx) {
        if (m_values == null) {
            return false;
        }
        try {
            tests_ = new NiidTests(new ReadDataBlock(m_values),
                    m_domain != null ? m_domain.getFrequency().intValue() : 0, nhp,
                    m_domain != null && m_domain.getFrequency() != TsFrequency.Yearly);
             return true;
        }
        catch (Exception err) {
            return false;
        }
    }

    public TsData getTsValues() {
        if (m_domain == null || m_values == null) {
            return null;
        }
        else {
            return new TsData(m_domain.getStart(), m_values, true);
        }

    }

    @Override
    public MetaData getMetaData() {
        return m_metadata;
    }
}
