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

import demetra.data.Parameter;
import demetra.information.Explorable;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.RegressionItem;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.function.ToDoubleFunction;

/**
 *
 * @author Mats Maggi
 * @param <T>
 */
public class MovingProcessingFacade<T extends Explorable> {

    private ITsProcessing<T> processing;
    private HashMap<TsDomain, T> m_cache = new HashMap<>();
    private TsDomain m_domainT;
    private static final int NYEARS = 8;
    private int m_increment;
    private int m_length;
    private TsPeriod m_start;

    private static final HashMap<Type, ToDoubleFunction<Object> > dictionary = new HashMap<>();
    
    static{
        dictionary.put(Double.class, o->(Double)o);
        dictionary.put(Integer.class, o ->((Integer) o).doubleValue());
        dictionary.put(RegressionItem.class, o->((RegressionItem)o).getCoefficient());
        dictionary.put(Parameter.class, o-> ((Parameter) o).getValue());
    }

    public MovingProcessingFacade(ITsProcessing<T> processing, TsDomain domain) {
        this.processing = processing;
        m_domainT = domain;
        m_cache.put(m_domainT, processing.process(m_domainT));
        m_start = m_domainT.get(0);
        m_increment = m_domainT.getAnnualFrequency();
        m_length = m_increment * NYEARS;

    }

     //</editor-fold>

    public Double getData(String key, TsDomain domain) {
        T info = tsInfo(domain);
        if (info == null) {
            return null;
        }
        Object o = info.getData(key, Object.class);
        if (o == null) {
            return null;
        } else {
            return dictionary.get(o.getClass()).applyAsDouble(o);
        }

    }

    public HashMap<TsDomain, T> getCache() {
        return m_cache;
    }

    public void setCache(HashMap<TsDomain, T> m_cache) {
        this.m_cache = m_cache;
    }

    public TsDomain getDomain() {
        return m_domainT;
    }

    public void setDomain(TsDomain m_domainT) {
        this.m_domainT = m_domainT;
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

    public ITsProcessing<T> getProcessing() {
        return processing;
    }

    public void setProcessing(ITsProcessing<T> processing) {
        this.processing = processing;
    }

    public T tsInfo(TsDomain domain) {
        T it = m_cache.get(domain);
        if (it == null) {
            it = processing.process(domain);
            m_cache.put(domain, it);
        }
        return it;
    }

    public TsData referenceSeries(String series) {
        T it = tsInfo(m_domainT);
        if (it == null) {
            return null;
        }
        return it.getData(series, TsData.class);
    }
}
