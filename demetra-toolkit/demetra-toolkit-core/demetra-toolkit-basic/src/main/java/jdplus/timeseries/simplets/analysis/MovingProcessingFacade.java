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

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 *
 * @author Mats Maggi
 * @param <T>
 */
public class MovingProcessingFacade<T> {

    private final Function<TsDomain, T> processing;
    private final HashMap<TsDomain, T> m_cache = new HashMap<>();
    private final TsDomain m_domainT;
    private static final int NYEARS = 8;
    private int m_increment;
    private int m_length;
    private TsPeriod m_start;

    public MovingProcessingFacade(TsDomain domain, Function<TsDomain, T> processing) {
        this.processing = processing;
        m_domainT = domain;
        m_cache.put(m_domainT, processing.apply(m_domainT));
        m_start = m_domainT.get(0);
        m_increment = m_domainT.getAnnualFrequency();
        m_length = m_increment * NYEARS;

    }

    //</editor-fold>
    public double getData(TsDomain domain, ToDoubleFunction<T> extractor) {
        T info = tsInfo(domain);
        if (info == null) {
            return Double.NaN;
        }
        return extractor.applyAsDouble(info);
    }

    public TsDomain getDomain() {
        return m_domainT;
    }

    public int getIncrement() {
        return m_increment;
    }

    public void setIncrement(int m_increment) {
        this.m_increment = m_increment;
    }

    public int getLength() {
        return m_length;
    }

    public void setLength(int m_length) {
        this.m_length = m_length;
    }

    public TsPeriod getStart() {
        return m_start;
    }

    public void setStart(TsPeriod m_start) {
        this.m_start = m_start;
    }

    public Function<TsDomain, T> getProcessing() {
        return processing;
    }

    public T tsInfo(TsDomain domain) {
        synchronized (m_cache) {
            T it = m_cache.get(domain);
            if (it == null) {
                if (!m_cache.containsKey(domain)) {
                    try {
                        it = processing.apply(domain);
                    } catch (Exception ex) {
                    }
                    m_cache.put(domain, it);
                }
            }
            return it;
        }
    }
}
