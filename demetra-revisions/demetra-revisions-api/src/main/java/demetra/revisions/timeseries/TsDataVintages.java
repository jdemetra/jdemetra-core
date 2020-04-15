/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.revisions.timeseries;

import demetra.data.Seq;
import demetra.revisions.timeseries.TsObsVintages.Entry;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PALATEJ
 * @param <K>
 */
@lombok.Value
public class TsDataVintages<K extends Comparable> implements Seq<TsObsVintages> {

    public static class Builder<K extends Comparable> {

        private final Map<TsPeriod, List< Entry<K>>> map = new HashMap<>();

        public void add(TsPeriod period, K key, double value) {
            List<Entry<K>> list = map.get(period);
            if (list != null) {
                list.add(new Entry<>(key, value));
            } else {
                list = new ArrayList<>();
                list.add(new Entry<>(key, value));
                map.put(period, list);
            }
        }

        public void add(TsPeriod period, Entry<K>[] items) {
            if (items == null) {
                return;
            }
            List<Entry<K>> list = map.get(period);
            if (list != null) {
                for (Entry<K> entry : items) {
                    list.add(entry);
                }
            } else {
                list = new ArrayList<>();
                for (Entry<K> entry : items) {
                    list.add(entry);
                }
                map.put(period, list);
            }
        }

        public void add(TsPeriod period, Collection<Entry<K>> items) {
            if (items == null) {
                return;
            }
            List<Entry<K>> list = map.get(period);
            if (list != null) {
                list.addAll(items);
            } else {
                list = new ArrayList<>();
                list.addAll(items);
                map.put(period, list);
            }
        }

        public TsDataVintages<K> build() {
            TsPeriod[] p = map.keySet().toArray(new TsPeriod[map.size()]);
            Arrays.sort(p);
            TsPeriod start = p[0], last = p[p.length - 1];
            TsObsVintages.Entry<K>[][] data = new TsObsVintages.Entry[start.until(last) + 1][];
            for (int i = 0; i < p.length; ++i) {
                int pos = start.until(p[i]);
                List<Entry<K>> list = map.get(p[i]);
                Entry<K>[] items = list.toArray(new Entry[list.size()]);
                Arrays.sort(items);
                data[pos] = items;
            }
            return new TsDataVintages(start, data);
        }
    }

    @lombok.NonNull
    TsPeriod start;

    @lombok.Getter(lombok.AccessLevel.PACKAGE)
    TsObsVintages.Entry<K>[][] data;

    @Override
    public TsObsVintages get(int index) throws IndexOutOfBoundsException {
        return new TsObsVintages(start.plus(index), data[index]);
    }

    @Override
    public int length() {
        return data.length;
    }

    public TsDomain getDomain() {
        return TsDomain.of(start, data.length);
    }

    public TsPeriod getFirstPeriod() {
        return start;
    }

    public TsPeriod getEndPeriod() {
        return start.plus(data.length);
    }

    public TsData initial() {
        double[] z = new double[data.length];
        for (int i = 0; i < data.length; ++i) {
            if (data[i] != null) {
                z[i] = data[i][0].getValue();
            } else {
                z[i] = Double.NaN;
            }
        }
        return TsData.ofInternal(start, z);
    }

    public TsData current() {
        double[] z = new double[data.length];
        for (int i = 0; i < data.length; ++i) {
            TsObsVintages.Entry<K>[] cur = data[i];
            if (cur != null) {
                z[i] = cur[cur.length - 1].getValue();
            } else {
                z[i] = Double.NaN;
            }
        }
        return TsData.ofInternal(start, z);
    }

    public TsData vintage(K vintage, boolean exact) {
        double[] z = new double[data.length];
        for (int i = 0; i < data.length; ++i) {
            TsObsVintages.Entry<K>[] cur = data[i];
            if (cur != null) {
                int pos = search(cur, vintage, exact);
                if (pos >= 0) {
                    z[i] = cur[pos].getValue();
                } else {
                    z[i] = Double.NaN;
                }
            } else {
                z[i] = Double.NaN;
            }
        }
        return TsData.ofInternal(start, z);
    }

    private int search(TsObsVintages.Entry<K>[] cur, K vintage, boolean exact) {
        int pos = -1;
        for (int j = 0; j < cur.length; ++j) {
            int cmp = cur[j].getKey().compareTo(vintage);
            switch (cmp) {
                case -1:
                    if (!exact) {
                        pos = j;
                    }
                    break;
                case 1:
                    return pos;
                default:
                    return j;
            }
        }
        return -1;
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        for (int i = 0; i < data.length; ++i) {
            if (data[i] != null) {
                builder.add(start.plus(i), data[i]);
            }
        }
        return builder;
    }
    
    public TsDataVintages<K> selectPeriods(TimeSelector ts){
        TsDomain selection = getDomain().select(ts);
        TsPeriod sstart=selection.getStartPeriod();
        int istart=start.until(sstart);
        Entry<K>[][] copy = Arrays.copyOfRange(data, istart, istart+selection.getLength());
        return new TsDataVintages<K>(sstart, copy);
    }

}
