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

import java.util.ArrayList;
import ec.tstoolkit.utilities.WeightedItem;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;

/**
 * The TsAggregator class provides simple algorithms for computing the sum of
 * time series. The number of time series is not limited. All the series should
 * have the same frequency. The missing values can be considered as 0. In that
 * case, the time domain of the sum will be the union of the time domain of all
 * the series. The series can also be weighted and their weights can be
 * automatically normalised to 1. The normalisation is done separately for each
 * period. When the missing values are not considered as 0, the normalised
 * weights for a given period are computed on the series that are defined for
 * that period. So, the used weights for a series might implicitly vary. When
 * normalisation is used, the time domain of the sum is also the union of the
 * time domain of all the series. Otherwise (no normalisation, missing values
 * not considered as 0), the time domain of the sum will be their intersection
 * (perhaps empty). It should still be noted that the options (normalisation =
 * true, missing = 0) and (normalisation = true, missing != 0) may give
 * different results: in the first case, the weights are identical for each
 * periods; in the second case, they are computed only on the available series.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsAggregator {

    private boolean zero_;
    private boolean normalize_;
    private final ArrayList<WeightedItem<TsData>> items_ = new ArrayList<>();

    /**
     * Creates a new aggregation tool
     */
    public TsAggregator() {
    }

    /**
     * Adds a series to the aggregation tool.
     *
     * @param s The added series. Can't be null.
     */
    public void add(TsData s) {
        items_.add(new WeightedItem<>(s));
    }

    /**
     * Adds a weighted series to the aggregation tool.
     *
     * @param s The added series. Can't be null
     * @param weight The weight of the series. Can be any double.
     */
    public void add(TsData s, double weight) {
        items_.add(new WeightedItem<>(s, weight));
    }

    /**
     * Adds a collection of series. All the series are unweighted
     *
     * @param s The iterable collection of time series
     */
    public void addRange(Iterable<TsData> s) {
        for (TsData t : s) {
            items_.add(new WeightedItem<>(t));
        }
    }

    /**
     * Removes all series currently in the aggregation tool
     */
    public void clear() {
        items_.clear();
    }

    /**
     * Gives the number of series in the aggregation tool
     *
     * @return The number of series
     */
    public int getCount() {
        return items_.size();
    }

    /**
     * Computes the common time span of all the series.
     *
     * @return The common time domain. <I> null <\i> iff some series have
     * different frequencies. The common domain may be empty.
     */
    public TsDomain getIntersectionDomain() {
        int nts = items_.size();
        if (nts == 0) {
            return null;
        }
        TsDomain dom = items_.get(0).item.getDomain();
        // union
        for (int i = 1; i < nts; ++i) {
            dom = dom.intersection(items_.get(i).item.getDomain());
            if (dom == null) {
                return null;
            }
        }
        return dom;
    }

    /**
     * Computes the time span that contains all the series
     *
     * @return The overlapping time domain. <I> null <\i> iff some series have
     * different frequencies.
     */
    public TsDomain getUnionDomain() {
        int nts = items_.size();
        if (nts == 0) {
            return null;
        }
        TsDomain dom = items_.get(0).item.getDomain();
        // union
        for (int i = 1; i < nts; ++i) {
            dom = dom.union(items_.get(i).item.getDomain());
            if (dom == null) {
                return null;
            }
        }
        return dom;
    }

    /**
     * Indicates if the missing values are considered as 0. False by default.
     *
     * @return True if missing values are considered as 0, false otherwise
     */
    public boolean isMissingsEqualsToZero() {
        return zero_;
    }

    /**
     * Indicates if the weights are normalised to 1. False by default.
     *
     * @return True if the weights are normalised to 1, false otherwise.
     */
    public boolean isRescalingWeights() {
        return normalize_;
    }

    private void nwsum(TsData total) {
        TsDomain dom = total.getDomain();
        int nts = items_.size();
        if (zero_) {
            double w = 0;
            for (int i = 0; i < nts; ++i) {
                w += items_.get(i).weight;
            }
            if (w != 0) {
                for (int i = 0; i < dom.getLength(); ++i) {
                    double s = 0;
                    TsPeriod p = dom.get(i);
                    for (int j = 0; j < nts; ++j) {
                        WeightedItem<TsData> cur = items_.get(j);
                        double curw = cur.weight;
                        int idx = cur.item.getDomain().search(p);
                        if (idx >= 0) {
                            s += curw * cur.item.get(idx);
                        }
                    }
                    total.set(i, s / w);
                }
            }
        } else {
            for (int i = 0; i < dom.getLength(); ++i) {
                double s = 0, w = 0;
                TsPeriod p = dom.get(i);
                for (int j = 0; j < nts; ++j) {
                    WeightedItem<TsData> cur = items_.get(j);
                    double curw = cur.weight;
                    int idx = cur.item.getDomain().search(p);
                    if (idx >= 0) {
                        s += curw * cur.item.get(idx);
                        w += curw;
                    }
                }
                if (w != 0) {
                    total.set(i, s / w);
                }
            }
        }
    }

    /**
     * Indicates if the missing values are considered as 0. By default, missing
     * values are not considered as 0.
     *
     * @param value True if the missing values are considered as 0, false
     * otherwise.
     *
     */
    public void setMissingsEqualsToZero(boolean value) {
        zero_ = value;
    }

    /**
     * Indicates if the weights are normalised to 1.By default, weights are not
     * normalised.
     *
     * @param value True if the weights have to be normalised, false otherwise.
     */
    public void setRescalingWeights(boolean value) {
        normalize_ = value;
    }

    /**
     * Computes the sum of the series.
     *
     * @return A new TsData with the sum of the series.
     *
     */
    public TsData sum() {
        int nts = items_.size();
        if (nts == 0) {
            return null;
        } else if (nts == 1) {
            return witem(0);
        }
        TsDomain dom;
        if (zero_ || normalize_) {
            dom = getUnionDomain();
        } else {
            dom = getIntersectionDomain();
        }
        TsData total = new TsData(dom);
        total.set(()->0);

        if (normalize_) {
            nwsum(total);
        } else {
            sum(total);
        }

        return total;
    }

    private void sum(TsData total) {
        TsDomain dom = total.getDomain();
        double[] x = total.internalStorage();
        items_.stream().filter((w) -> (w.weight != 0)).forEach((w) -> {
            TsDomain wdom = w.item.getDomain();
            TsDomain idom = wdom.intersection(dom);
            int wstart = idom.getStart().minus(wdom.getStart());
            int tstart = idom.getStart().minus(dom.getStart());
            int n = idom.getLength();
            new DataBlock(x, tstart, tstart + n, 1).addAY(w.weight,
                    new DataBlock(w.item.internalStorage(),
                            wstart, wstart + n, 1));
        });
    }

    private TsData witem(int idx) {
        WeightedItem<TsData> item = items_.get(idx);
        if (item.weight == 1) {
            return item.item;
        } else {
            return item.item.times(item.weight);
        }
    }
}
