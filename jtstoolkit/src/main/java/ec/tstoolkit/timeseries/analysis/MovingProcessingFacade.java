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

import ec.tstoolkit.Parameter;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 *
 * @author Mats Maggi
 */
public class MovingProcessingFacade<T extends IProcResults> {
    
    private ITsProcessing<T> processing;
    private HashMap<TsDomain, T> m_cache = new HashMap<>();
    private TsDomain m_domainT;
    private static final int g_nyears = 8;
    private int m_increment;
    private int m_length;
    private TsPeriod m_start;
    
    private static HashMap<Type, IDoubleFormatter> dictionary = new HashMap<>();
    
    public MovingProcessingFacade(ITsProcessing<T> processing, TsDomain domain) {
        this.processing = processing;
        m_domainT = domain;
	m_cache.put(m_domainT, processing.process(m_domainT));
	m_start = m_domainT.getStart();
	m_increment = m_domainT.getFrequency().intValue();
	m_length = m_increment * g_nyears;
        
        dictionary.put(Double.class, new DoubleFormatter());
        dictionary.put(Integer.class, new IntegerFormatter());
        dictionary.put(RegressionItem.class, new RegressionItemFormatter());
        dictionary.put(Parameter.class, new ParameterFormatter());
    }
    
    //<editor-fold defaultstate="collapsed" desc="Double Formatters">
    private static class RegressionItemFormatter implements IDoubleFormatter {
        @Override
        public Double getDoubleValue(Object item) {
            RegressionItem i = (RegressionItem)item;
            return i.coefficient;
        }
    }
    
    private static class IntegerFormatter implements IDoubleFormatter {
        @Override
        public Double getDoubleValue(Object item) {
            Integer i = (Integer)item;
            return Double.valueOf(i);
        }
    }
    
    private static class DoubleFormatter implements IDoubleFormatter {
        @Override
        public Double getDoubleValue(Object item) {
            Double i = (Double)item;
            return i;
        }
    }
    
    private static class ParameterFormatter implements IDoubleFormatter {
        @Override
        public Double getDoubleValue(Object item) {
            Parameter p = (Parameter)item;
            return p == null ? null : p.getValue();
        }
    }
    //</editor-fold>
    
    public Double getData(String key, TsDomain domain) {
        T info = tsInfo(domain);
        if (info == null) {
            return null;
        }
        Class c = info.getDictionary().get(key);
        Object o = info.getData(key, Object.class);
        if (o == null) {
            return null;
        } else {
            return dictionary.get(c).getDoubleValue(o);
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
    
    public T tsInfo(TsDomain domain)
    {
	T it=m_cache.get(domain);
	if (it == null) {
	    it = processing.process(domain);
	    m_cache.put(domain, it);
	}
	return it;
    }
    
    public TsData referenceSeries(String series)
    {
	T it = tsInfo(m_domainT);
        if (it == null)
            return null;
 	return it.getData(series, TsData.class);
    }
}
