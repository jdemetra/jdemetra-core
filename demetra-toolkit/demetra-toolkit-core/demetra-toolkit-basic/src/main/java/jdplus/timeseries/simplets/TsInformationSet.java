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
package jdplus.timeseries.simplets;

//import demetra.timeseries.TsPeriodSelector;
//import static demetra.timeseries.simplets.TsDataToolkit.fitToDomain;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.SortedSet;
//import java.util.TreeSet;

///**
// *
// * @author Jean Palate
// */
//public class TsInformationSet {
//
//    /**
//     *
//     * @param input
//     */
//    public TsInformationSet(TsData[] input) {
//        for (int i = 0; i < input.length; ++i) {
//            table_.insert(-1, input[i]);
//        }
//    }
//
//    public TsData[] toArray() {
//        TsData[] s = new TsData[table_.getSeriesCount()];
//        for (int i = 0; i < s.length; ++i) {
//            s[i] = table_.series(i);
//        }
//        return s;
//    }
//
//    // 
//    /**
//     * Creates a new information set with only the revised data in comparison
//     * with this data set (the domains of the series of this data set are
//     * identical to the domains of the series of the returned information set
//     *
//     * @param newdata
//     * @return
//     */
//    public TsInformationSet revisedData(TsInformationSet newdata) {
//        TsData[] ndata = new TsData[table_.getSeriesCount()];
//        for (int i = 0; i < ndata.length; ++i) {
//            TsData cur = table_.series(i);
//            TsData ncur = newdata.table_.series(i);
//            ncur = fitToDomain(ncur, cur.domain());
//            for (int j = 0; j < cur.length(); ++j) {
//                if (cur.isMissing(j)) {
//                    ncur.setMissing(j);
//                }
//            }
//            ndata[i] = ncur;
//        }
//        return new TsInformationSet(ndata);
//    }
//
//    public TsInformationSet actualData() {
//        TsData[] ndata = new TsData[table_.getSeriesCount()];
//        for (int i = 0; i < ndata.length; ++i) {
//            ndata[i] = table_.series(i).cleanExtremities();
//        }
//        return new TsInformationSet(ndata);
//    }
//
//    public TsInformationSet extendTo(final Day end) {
//        TsData[] ndata = new TsData[table_.getSeriesCount()];
//        for (int i = 0; i < ndata.length; ++i) {
//            ndata[i] = table_.series(i).extendTo(end);
//        }
//        return new TsInformationSet(ndata);
//    }
//
//    /**
//     *
//     * @return
//     */
//    public TsDomain getCurrentDomain() {
//        return table_.getDomain();
//    }
//
//    public TsDomain getCommonDomain() {
//        if (table_.isEmpty()) {
//            return null;
//        }
//        TsFrequency f = table_.getDomain().getFrequency();
//        TsDomain common = null;
//        for (int i = 0; i < table_.getSeriesCount(); ++i) {
//            TsDomain cur = table_.series(i).getDomain();
//            TsPeriod p0 = new TsPeriod(f, cur.getStart().firstday());
//            TsPeriod p1 = new TsPeriod(f, cur.getEnd().firstday());
//            TsDomain fcur = new TsDomain(p0, p1.minus(p0));
//            common = common != null ? common.intersection(fcur) : fcur;
//        }
//        return common;
//    }
//
//    public int getSeriesCount() {
//        return table_.getSeriesCount();
//    }
//
//    public int getDataCount() {
//        int n = 0;
//        for (int i = 0; i < table_.getSeriesCount(); ++i) {
//            TsData series = table_.series(i);
//            n += series.getObsCount();
//        }
//        return n;
//    }
//
//    /**
//     *
//     * @param idx
//     * @return
//     */
//    public TsData series(int idx) {
//        return table_.series(idx);
//    }
//
//    /**
//     *
//     * @param domain
//     * @return
//     */
//    public Matrix generateMatrix(final TsDomain domain) {
//        TsDomain tdomain = table_.getDomain();
//        if (tdomain == null) {
//            return null;
//        }
//        if (domain == null) {
//            return generateMatrix(tdomain);
//        }
//        if (domain.getFrequency() != tdomain.getFrequency()) {
//            return null;
//        }
//        Matrix m = new Matrix(domain.getLength(), table_.getSeriesCount());
//        m.set(Double.NaN);
//        TsDomain common = tdomain.intersection(domain);
//        for (int i = 0, j = common.getStart().minus(domain.getStart()),
//                k = common.getStart().minus(tdomain.getStart()); i < common.getLength(); ++i, ++j, ++k) {
//            for (int s = 0; s < m.getColumnsCount(); ++s) {
//                TsDataTableInfo dataInfo = table_.getDataInfo(k, s);
//                if (dataInfo == TsDataTableInfo.Valid) {
//                    m.set(j, s, table_.getData(k, s));
//                }
//            }
//        }
//        return m;
//    }
//
//    /**
//     * Fill in periods for each series where new data are present (does not take
//     * into account values that have been revised). Takes new data before first
//     * element in old dataset, new data after last element in old dataset and
//     * new data where there was a missing value. Data revisions are not being
//     * added yet.
//     *
//     * @param ndata New data
//     * @return List of newly added data
//     */
//    public TsInformationUpdates updates(TsInformationSet ndata) {
//        int n = table_.getSeriesCount();
//        if (n != ndata.table_.getSeriesCount()) {
//            return null;
//        }
//        TsInformationUpdates updates = new TsInformationUpdates();
//        for (int i = 0; i < n; ++i) {
//            TsData olds = table_.series(i), news = ndata.table_.series(i);
//            int del = news.getStart().minus(olds.getStart());
//            TsPeriod start = news.getStart();
//            for (int j = 0; j < news.getLength(); ++j) {
//                if (!news.getValues().isMissing(j)) {
//                    int k = j + del;
//                    if (k < 0 || k >= olds.getLength() || olds.getValues().isMissing(k)) {
//                        updates.add(start.plus(j), i);
//                    }
//                }
//            }
//            
//            // Calculates revisions
//            start = olds.getStart();
//            TsData newFit = news.fittoDomain(olds.getDomain());
//            for (int j = 0; j < olds.getLength(); ++j) {
//                if (!newFit.getValues().isMissing(j) 
//                        && !olds.getValues().isMissing(j)
//                        && newFit.get(j) != olds.get(j)) {
//                    updates.addRevision(start.plus(j), i);
//                }
//            }
//        }
//        return updates;
//    }
//
//    public Day[] generatePublicationCalendar(int[] delays) {
//        SortedSet<Day> sdays = new TreeSet<>();
//        for (int i = 0; i < table_.getSeriesCount(); ++i) {
//            TsData s = table_.series(i);
//            TsDomain dom = s.getDomain();
//            int ndel = delays == null ? 0 : delays[i];
//            for (int j = 0; j < s.getLength(); ++j) {
//                if (!s.isMissing(j)) {
//                    TsPeriod p = dom.get(j);
//                    Day pub = p.lastday().plus(ndel);
//                    sdays.add(pub);
//                }
//            }
//        }
//        Day[] days = new Day[sdays.size()];
//        return sdays.toArray(days);
//    }
//
//    public Day[] generatePublicationCalendar(List<Integer> delays, Day start) {
//        SortedSet<Day> sdays = new TreeSet<>();
//        for (int i = 0; i < table_.getSeriesCount(); ++i) {
//            TsData s = table_.series(i);
//            TsDomain dom = s.getDomain();
//            int ndel = (delays == null || delays.isEmpty()) ? 0 : delays.get(i);
//            int pos = dom.search(start);
//            if (pos < 0 && start.isBefore(dom.getStart().firstday())) {
//                pos = 0;
//            }
//            if (pos >= 0) {
//                for (int j = pos; j < s.getLength(); ++j) {
//                    if (!s.isMissing(j)) {
//                        TsPeriod p = dom.get(j);
//                        Day pub = p.lastday().plus(ndel);
//                        sdays.add(pub);
//                    }
//                }
//            }
//        }
//        Day[] days = new Day[sdays.size()];
//        return sdays.toArray(days);
//    }
//
//    public TsInformationSet generateInformation(final List<Integer> delays, final LocalDateTime date) {
//        TsData[] inputc = new TsData[table_.getSeriesCount()];
//        for (int i = 0; i < inputc.length; ++i) {
//            TsPeriodSelector sel = TsPeriodSelector.all();
//            LocalDateTime last = date;
//            if (delays != null && !delays.isEmpty() && i < delays.size()) {
//                last = last.minusDays(delays.get(i));
//            }
//            sel.to(last);
//            inputc[i] = table_.series(i).select(sel);
//        }
//        return new TsInformationSet(inputc);
//    }
//
//    private final TsDataTable table_ = new TsDataTable();
//}
