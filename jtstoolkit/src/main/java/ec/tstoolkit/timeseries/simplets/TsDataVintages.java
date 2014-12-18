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

package ec.tstoolkit.timeseries.simplets;

import java.util.Map.Entry;
import java.util.*;

/**
 *
 * @author pcuser
 */
public class TsDataVintages<T extends Comparable> {

    private SortedMap< TsPeriod, SortedMap<T, Double>> data_ = new TreeMap< >();

    public void add(TsPeriod period, double value, T vintage) {
        SortedMap<T, Double> cur = data_.get(period);
        if (cur == null) {
            cur = new TreeMap<>();
            data_.put(period, cur);
        }
        cur.put(vintage, value);
    }

    public void add(TsData data, T vintage) {
        for (int i = 0; i < data.getLength(); ++i) {
            add(data.getDomain().get(i), data.get(i), vintage);
        }
    }

    public T lastVintage(TsPeriod p) {
        SortedMap<T, Double> cur = data_.get(p);
        if (cur == null) {
            return null;
        }
        else {
            return cur.lastKey();
        }
    }

    public TsData current() {
        TsPeriod start = data_.firstKey(), end = data_.lastKey();
        TsData rslt = new TsData(start, end.minus(start) + 1);
        Iterator<Entry<TsPeriod, SortedMap<T, Double>>> iterator = data_.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<TsPeriod, SortedMap<T, Double>> cur = iterator.next();
            int pos = cur.getKey().minus(start);
            SortedMap<T, Double> map = cur.getValue();
            double val = map.get(map.lastKey());
            rslt.set(pos, val);
        }
        return rslt;
    }

    public TsData initial() {
        TsPeriod start = data_.firstKey(), end = data_.lastKey();
        TsData rslt = new TsData(start, end.minus(start) + 1);
        Iterator<Entry<TsPeriod, SortedMap<T, Double>>> iterator = data_.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<TsPeriod, SortedMap<T, Double>> cur = iterator.next();
            int pos = cur.getKey().minus(start);
            SortedMap<T, Double> map = cur.getValue();
            double val = map.get(map.firstKey());
            rslt.set(pos, val);
        }
        return rslt;
    }

    public TsData data(T vintage, boolean exactVintage) {
        TsPeriod start = data_.firstKey(), end = data_.lastKey();
        TsData rslt = new TsData(start, end.minus(start) + 1);
        Iterator<Entry<TsPeriod, SortedMap<T, Double>>> iterator = data_.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<TsPeriod, SortedMap<T, Double>> cur = iterator.next();
            int pos = cur.getKey().minus(start);
            SortedMap<T, Double> map = cur.getValue();
            Double val = map.get(vintage);
            if (val == null && ! exactVintage){
                SortedMap<T, Double> head= map.headMap(vintage);
                if (! head.isEmpty())
                    val=map.get(head.lastKey());
            }
            if (val != null) {
                rslt.set(pos, val);
            }
        }
        return rslt;
    }
    
    public SortedMap<T, Double> vintages(TsPeriod p) {
        return data_.get(p);
    }

    public SortedSet<T> allVintages() {
        TreeSet<T> set = new TreeSet<>();
        Iterator<Entry<TsPeriod, SortedMap<T, Double>>> iterator = data_.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<TsPeriod, SortedMap<T, Double>> cur = iterator.next();
            for (T t : cur.getValue().keySet()) {
                if (!set.contains(t)) {
                    set.add(t);
                }
            }
        }
        return set;
    }

    public double[] dataVintages(TsPeriod p) {
        SortedMap<T, Double> v = vintages(p);
        if (v == null) {
            return null;
        }
        double[] vals = new double[v.size()];
        int i = 0;
        for (Double x : v.values()) {
            vals[i++] = x;
        }
        return vals;
    }
    
    public TsMatrix toMatrix(Collection<T> vintages, boolean exactVintages){
        TsData[] s=new TsData[vintages.size()];
        int i=0;
        for (T t: vintages)
            s[i++]=data(t, exactVintages);
        return new TsMatrix(s);
    }
}
