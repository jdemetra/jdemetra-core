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

import demetra.data.DoubleList;
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
        return TsData.ofInternal(start, z).cleanExtremities();
    }

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

    public TsDataVintages<K> select(TimeSelector ts) {
        TsDomain selection = getDomain().select(ts);
        TsPeriod sstart = selection.getStartPeriod();
        int istart = start.until(sstart);
        Entry<K>[][] copy = Arrays.copyOfRange(data, istart, istart + selection.getLength());
        return new TsDataVintages<>(sstart, copy);
    }

    public TsDataVintages<K> select(VintageSelector vs) {
        if (vs.getType() == VintageSelectorType.All) {
            return this;
        }
        Builder builder = new Builder();
        for (int i = 0; i < data.length; ++i) {
            Entry<K>[] cur = data[i];
            if (cur != null) {
                TsPeriod period = start.plus(i);
                switch (vs.getType()) {
                    case First:
                        if (cur.length <= vs.getN0()) {
                            builder.add(period, data[i]);
                        } else {
                            builder.add(period, Arrays.copyOfRange(cur, 0, vs.getN0()));
                        }
                        break;
                    case Last:
                        if (cur.length <= vs.getN1()) {
                            builder.add(period, data[i]);
                        } else {
                            builder.add(period, Arrays.copyOfRange(cur, cur.length - vs.getN1(), cur.length));
                        }
                        break;
                    case Custom:
                        int pos0 = vs.getN0(),
                         pos1 = vs.getN1() + 1;
                        if (pos0 < cur.length) {
                            pos1 = Math.min(pos1, cur.length);
                            builder.add(period, Arrays.copyOfRange(cur, pos0, pos1));
                        }
                        break;
                    case Excluding:
                        int n0 = vs.getN0(),
                         n1 = cur.length - vs.getN1();
                        if (n0 < n1) {
                            builder.add(period, Arrays.copyOfRange(cur, n0, n1));
                        }
                        break;

                }
            }
        }
        return builder.build();
    }

    public static <K extends Comparable & TimeComparable> TsData seriesAt(TsDataVintages vintages, LocalDateTime dt) {
        TsPeriod start = vintages.start;
        double[] data = new double[vintages.data.length];
        for (int i = 0; i < vintages.data.length; ++i) {
            Entry<K>[] cur = vintages.data[i];
            data[i] = find(cur, dt);
        }
        return TsData.ofInternal(start, data).cleanExtremities();
    }

    public static <K extends Comparable & DateComparable> TsData seriesAt(TsDataVintages vintages, LocalDate dt) {
        TsPeriod start = vintages.start;
        double[] data = new double[vintages.data.length];
        for (int i = 0; i < vintages.data.length; ++i) {
            Entry<K>[] cur = vintages.data[i];
            data[i] = find(cur, dt);
        }
        return TsData.ofInternal(start, data).cleanExtremities();
    }

    private static <K extends Comparable & TimeComparable> double find(Entry<K>[] v, LocalDateTime dt) {
        if (v == null) {
            return Double.NaN;
        }
        double cur = Double.NaN;
        for (int i = 0; i < v.length; ++i) {
            int pos = v[i].getKey().compareToTime(dt);
            switch (pos) {
                case 0:
                    return v[i].getValue();
                case -1:
                    cur = v[i].getValue();
                    break;
                case 1:
                    return cur;
            }
        }
        return cur;
    }

    private static <K extends Comparable & DateComparable> double find(Entry<K>[] v, LocalDate dt) {
        if (v == null) {
            return Double.NaN;
        }
        double cur = Double.NaN;
        for (int i = 0; i < v.length; ++i) {
            int pos = v[i].getKey().compareToDate(dt);
            switch (pos) {
                case 0:
                    return v[i].getValue();
                case -1:
                    cur = v[i].getValue();
                    break;
                case 1:
                    return cur;
            }
        }
        return cur;
    }
}
