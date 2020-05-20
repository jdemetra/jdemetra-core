/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.revisions.timeseries;

import demetra.data.Seq;
import demetra.revisions.timeseries.TsObsVintages.Entry;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author PALATEJ
 * @param <K>
 */
@lombok.Value
public class TsDataVintages<K extends Comparable> implements Seq<TsObsVintages> {

    @FunctionalInterface
    public static interface DateComparator<K> {

        int compare(K key, LocalDate ld);
    }

    @FunctionalInterface
    public static interface TimeComparator<K> {

        int compare(K key, LocalDateTime ldt);
    }

    public static <K extends Comparable> Builder<K> builder() {
        return new Builder<>();
    }

    public static class Builder<K extends Comparable> {

        private Builder() {
        }

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
                list.addAll(Arrays.asList(items));
            } else {
                list = new ArrayList<>(Arrays.asList(items));
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
            Set<K> set = new HashSet<>();
            for (int i = 0; i < p.length; ++i) {
                int pos = start.until(p[i]);
                List<Entry<K>> list = map.get(p[i]);
                list.forEach(entry -> set.add(entry.getKey()));
                Entry<K>[] items = list.toArray(new Entry[list.size()]);
                Arrays.sort(items);
                data[pos] = items;
            }
            List<K> v = new ArrayList<>(set);
            v.sort(null);
            return new TsDataVintages(start, data, Collections.unmodifiableList(v));
        }
    }

    @lombok.NonNull
    TsPeriod start;

    @lombok.Getter(lombok.AccessLevel.PACKAGE)
    TsObsVintages.Entry<K>[][] data;

    @lombok.NonNull
    List<K> vintages;

    @Override
    public TsObsVintages get(int index) throws IndexOutOfBoundsException {
        return new TsObsVintages(start.plus(index), data[index]);
    }

    @Override
    public int length() {
        return data.length;
    }

    public int maxRevisionsCount() {

        int n = 0;
        for (int i = 0; i < data.length; ++i) {
            if (data[i] != null) {
                int ncur = data[i].length;
                if (ncur > n) {
                    n = ncur;
                }
            }
        }
        return n;
    }

    public TsDomain getDomain() {
        return TsDomain.of(start, data.length);
    }

    public TsData preliminary() {
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

    /**
     * Return the series corresponding to the given vintage.
     * When no obs has been registered for a given period for the given vintage,
     * the last registered vintage before the given one is used
     *
     * @param vintage The vintage
     * @return
     */
    public TsData vintage(K vintage) {
        double[] z = new double[data.length];
        for (int i = 0; i < data.length; ++i) {
            TsObsVintages.Entry<K>[] cur = data[i];
            if (cur != null) {
                int pos = search(cur, vintage);
                if (pos >= 0) {
                    z[i] = cur[pos].getValue();
                } else if (pos == -1) {
                    z[i] = Double.NaN;
                } else {
                    z[i] = cur[-pos - 2].getValue(); // before the insertion point
                }
            } else {
                z[i] = Double.NaN;
            }
        }
        return TsData.ofInternal(start, z).cleanExtremities();
    }

    /**
     * "Diagonal" vintage. The series is defined by the pos-th revision for each
     * obs.
     *
     * @param pos The position of the vintage
     * @return
     */
    public TsData vintage(int pos) {
        double[] z = new double[data.length];
        for (int i = 0; i < data.length; ++i) {
            TsObsVintages.Entry<K>[] cur = data[i];
            if (cur != null) {
                if (pos < cur.length) {
                    z[i] = cur[pos].getValue();
                } else {
                    z[i] = Double.NaN;
                }
            } else {
                z[i] = Double.NaN;
            }
        }
        return TsData.ofInternal(start, z).cleanExtremities();
    }

    /**
     *
     * @param cur
     * @param vintage
     * @return Position of the vintage in the array if positive, -1-insertion
     * position if not found.
     * The insertion position is the place that K should occupy in the array
     * after insertion
     */
    private int search(TsObsVintages.Entry<K>[] cur, K vintage) {
        for (int j = 0; j < cur.length; ++j) {
            int cmp = cur[j].getKey().compareTo(vintage);
            if (cmp > 0) {
                return -1 - j;
            } else if (cmp == 0) {
                return j;
            }
        }
        return -1 - cur.length;
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

    public TsDataVintages<K> select(TimeSelector ts) {
        TsDomain selection = getDomain().select(ts);
        TsPeriod sstart = selection.getStartPeriod();
        int istart = start.until(sstart);
        Entry<K>[][] copy = Arrays.copyOfRange(data, istart, istart + selection.getLength());
        Set<K> set = new HashSet<>();
        for (int i = 0; i < copy.length; ++i) {
            Entry<K>[] cur = copy[i];
            for (int j = 0; j < cur.length; ++j) {
                set.add(cur[j].getKey());
            }
        }
        List<K> v = new ArrayList<>(set);
        v.sort(null);
        return new TsDataVintages<>(sstart, copy, v);
    }

//    public TsDataVintages<K> select(VintageSelector<K> vs) {
//        if (vs.getType() == VintageSelectorType.All) {
//            return this;
//        }
//        Builder builder = new Builder();
//        for (int i = 0; i < data.length; ++i) {
//            Entry<K>[] cur = data[i];
//            if (cur != null) {
//                TsPeriod period = start.plus(i);
//                switch (vs.getType()) {
//                    case First:
//                        if (cur.length <= vs.getN0()) {
//                            builder.add(period, data[i]);
//                        } else {
//                            builder.add(period, Arrays.copyOfRange(cur, 0, vs.getN0()));
//                        }
//                        break;
//                    case Last:
//                        if (cur.length <= vs.getN1()) {
//                            builder.add(period, data[i]);
//                        } else {
//                            builder.add(period, Arrays.copyOfRange(cur, cur.length - vs.getN1(), cur.length));
//                        }
//                        break;
//                    case Custom:
//                        K k0 = vs.getK0(),
//                        k1 = vs.getK1();
//                        int pos0 = search(cur, k0),
//                        pos1 = search(cur, k1);
//                        if ((pos0 >= 0 || -1 - pos0 < cur.length) && (pos1 >= 0 || pos1 != -1)) { // not completely before and not completely after
//                            if (pos0 < 0) {
//                                pos0 = -pos0;
//                            }
//                            if (pos1 < 0) {
//                                pos1 = -pos1 - 1;
//                            } else {
//                                pos1 = pos1 + 1;
//                            }
//                            if (pos1 > pos0) {
//                                builder.add(period, Arrays.copyOfRange(cur, pos0, pos1));
//                            }
//                        }
//                        break;
//                    case Excluding:
//                        int n0 = vs.getN0(),
//                         n1 = cur.length - vs.getN1();
//                        if (n0 < n1) {
//                            builder.add(period, Arrays.copyOfRange(cur, n0, n1));
//                        }
//                        break;
//                }
//            }
//        }
//        return builder.build();
//    }
//
    public static <K extends Comparable> TsData seriesAt(TsDataVintages vintages, LocalDateTime dt, TimeComparator<K> cmp) {
        TsPeriod start = vintages.start;
        double[] data = new double[vintages.data.length];
        for (int i = 0; i < vintages.data.length; ++i) {
            Entry<K>[] cur = vintages.data[i];
            data[i] = find(cur, dt, cmp);
        }
        return TsData.ofInternal(start, data).cleanExtremities();
    }

    public static <K extends Comparable> TsData seriesAt(TsDataVintages vintages, LocalDate dt, DateComparator<K> cmp) {
        TsPeriod start = vintages.start;
        double[] data = new double[vintages.data.length];
        for (int i = 0; i < vintages.data.length; ++i) {
            Entry<K>[] cur = vintages.data[i];
            data[i] = find(cur, dt, cmp);
        }
        return TsData.ofInternal(start, data).cleanExtremities();
    }

    private static <K extends Comparable> double find(Entry<K>[] v, LocalDateTime dt, TimeComparator<K> cmp) {
        if (v == null) {
            return Double.NaN;
        }
        double cur = Double.NaN;
        for (int i = 0; i < v.length; ++i) {
            int pos = cmp.compare(v[i].getKey(), dt);
            if (pos == 0) {
                return v[i].getValue();
            } else if (pos > 0) {
                return cur;
            } else {
                cur = v[i].getValue();
            }
        }
        return cur;
    }

    private static <K extends Comparable> double find(Entry<K>[] v, LocalDate dt, DateComparator<K> cmp) {
        if (v == null) {
            return Double.NaN;
        }
        double cur = Double.NaN;
        for (int i = 0; i < v.length; ++i) {
            int pos = cmp.compare(v[i].getKey(), dt);
            if (pos == 0) {
                return v[i].getValue();
            } else if (pos > 0) {
                return cur;
            } else {
                cur = v[i].getValue();
            }
        }
        return cur;
    }
}
