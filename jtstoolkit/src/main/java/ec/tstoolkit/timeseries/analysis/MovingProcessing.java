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
package ec.tstoolkit.timeseries.analysis;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.DoubleList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class MovingProcessing<T extends IProcResults> {

    //private ITsProcessing<T> m_processing;
    private MovingProcessingFacade m_processing;

    /**
     *
     * @param processing
     * @param domain
     */
    public MovingProcessing(ITsProcessing<T> processing,
            TsDomain domain) {
        m_processing = new MovingProcessingFacade(processing, domain);

    }

    /**
     *
     * @return
     */
    public ITsProcessing<T> getProcessing() {
        return m_processing.getProcessing();
    }

    /**
     *
     * @return
     */
    public TsDomain getReferenceDomain() {
        return m_processing.getDomain();
    }

    /**
     *
     * @return
     */
    public T getReferenceInfo() {
        return (T) m_processing.tsInfo(m_processing.getDomain());
    }

    /**
     *
     * @return
     */
    public TsPeriod getStart() {
        return m_processing.getStart();
    }

    /**
     *
     * @return
     */
    public int getWindowIncrement() {
        return m_processing.getIncrement();
    }

    /**
     *
     * @return
     */
    public int getWindowLength() {
        return m_processing.getLength();
    }

    /**
     *
     * @param key
     * @return
     */
    /*public double[] movingInfo(String key) {
        return movingInfo(key, m_processing.getStart(), m_processing.getLength(), m_processing.getIncrement());
    }*/
    
    public Map<TsDomain, Double> movingInfo(String key) {
        return movingInfo(key, m_processing.getStart(), m_processing.getLength(), m_processing.getIncrement());
    }

    /**
     *
     * @param key
     * @param start
     * @param length
     * @param increment
     * @return
     */
    /*public double[] movingInfo(String key, TsPeriod start, int length,
            int increment) {
        DoubleList rslt = new DoubleList();
        TsDomain domain = new TsDomain(start, length);

        while (domain.getEnd().isNotAfter(m_processing.getDomain().getEnd())) {
            Double data = m_processing.getData(key, domain);

            if (data != null) {
                rslt.add(data);
            } else {
                rslt.add(Double.NaN);
            }
            domain = domain.move(increment);
        }

        return rslt.toArray();
    }*/
    
    public Map<TsDomain, Double> movingInfo(String key, TsPeriod start, int length,
            int increment) {
        Map<TsDomain, Double> map = new LinkedHashMap<>();
        DoubleList rslt = new DoubleList();
        TsDomain domain = new TsDomain(start, length);

        while (domain.getEnd().isNotAfter(m_processing.getDomain().getEnd())) {
            Double data = m_processing.getData(key, domain);

            if (data != null) {
                map.put(domain, data);
            } else {
                map.put(domain, Double.NaN);
            }
            domain = domain.move(increment);
        }

        return map;
    }

    /**
     *
     * @param value
     */
    public void setStart(TsPeriod value) {
        value.clone();
    }

    /**
     *
     * @param value
     */
    public void setWindowIncrement(int value) {
        m_processing.setIncrement(value);
    }

    /**
     *
     * @param value
     */
    public void setWindowLength(int value) {
        m_processing.setLength(value);
    }
}
