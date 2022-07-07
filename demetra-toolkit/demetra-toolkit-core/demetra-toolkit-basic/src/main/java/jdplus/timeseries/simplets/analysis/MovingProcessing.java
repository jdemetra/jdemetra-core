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
package jdplus.timeseries.simplets.analysis;

import demetra.data.DoubleList;
import demetra.information.Explorable;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public class MovingProcessing<T> {

    //private ITsProcessing<T> m_processing;
    private final MovingProcessingFacade m_processing;

    /**
     *
     * @param processing
     * @param domain
     */
    public MovingProcessing(TsDomain domain, Function<TsDomain, T> processing) {
        m_processing = new MovingProcessingFacade(domain, processing);

    }

    /**
     *
     * @return
     */
    public Function<TsDomain, T> getProcessing() {
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
     * @param extractor
     * @return
     */
    /*public double[] movingInfo(String key) {
        return movingInfo(key, m_processing.getStart(), m_processing.getLength(), m_processing.getIncrement());
    }*/
    public Map<TsDomain, Double> movingInfo(ToDoubleFunction<T> extractor) {
        return movingInfo(m_processing.getStart(), m_processing.getLength(), m_processing.getIncrement(), extractor);
    }

    /**
     *
     * @param key
     * @param start
     * @param length
     * @param extractor
     * @param increment
     * @return
     */
    public Map<TsDomain, Double> movingInfo(TsPeriod start, int length,
            int increment, ToDoubleFunction<T> extractor) {
        Map<TsDomain, Double> map = new LinkedHashMap<>();
        DoubleList rslt = new DoubleList();
        TsDomain domain = TsDomain.of(start, length);

        while (!domain.end().isAfter(m_processing.getDomain().end())) {
            double data = m_processing.getData(domain, extractor);
            if (Double.isFinite(data)) {
                map.put(domain, data);
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
        m_processing.setStart(value);
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
