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
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 * Interface for the creation of outlier variable
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@ServiceDefinition
public interface IOutlierFactory {

    /**
     * Creates an outlier at the given position
     *
     * @param position The position of the outlier.
     * @return A new variable is returned.
     */
    IOutlierVariable create(Day position);

    /**
     * Creates an outlier at the given period. By default, it creates an outlier
     * at the beginning of the period
     *
     * @param p The given period
     * @return
     */
    @Deprecated
    default IOutlierVariable create(TsPeriod p) {
        return create(p.firstday());
    }

    /**
     * Gets the definition domain of the outlier, for a given time span.
     * @param tsdomain The time domain that will be used for the estimation
     * @return The time domain that can contain an outlier. It is always smaller or equal to
     * the given time domain. 
     */
    TsDomain definitionDomain(TsDomain tsdomain);

    /**
     *
     * @return
     */
    @Deprecated
    OutlierType getOutlierType();
    
    /**
     * The code that represents the outlier
     * @return 
     */
    String getOutlierCode();
}
