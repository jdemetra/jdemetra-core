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
package demetra.timeseries.simplets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

///**
// *
// * @author Jean Palate
// */
//public class TsInformationUpdates {
//
//    /**
//     *
//     */
//    public static class Update {
//
////        Update(final TsPeriod period, final int series) {
////            this.period = period;
////            this.series = series;
////        }
//        Update(final TsPeriod period, final int series) {
//            this.period = period;
//            this.series = series;
//        }
//
//        /**
//         *
//         * @return
//         */
//        public double getObservation() {
//            return y;
//        }
//
//        /**
//         *
//         * @return
//         */
//        public double getForecast() {
//            return fy;
//        }
//
//        /**
//         *
//         * @return
//         */
//        public double getNews() {
//            return y - fy;
//        }
//
//        /**
//         *
//         */
//        public final TsPeriod period;
//        /**
//         *
//         */
//        public final int series;
//
//        public double y, fy;
//
//        @Override
//        public String toString() {
//            StringBuilder builder = new StringBuilder();
//            builder.append("var:").append(series).append('\t').append(period)
//                    .append('\t').append(y).append('\t').append(fy);
//            return builder.toString();
//        }
//    }
//
//    private final List<Update> news_ = new ArrayList<>();
//    private final List<Update> revisions_ = new ArrayList<>();
//
//    TsInformationUpdates() {
//    }
//
//    /**
//     *
//     * @param p
//     * @param series
//     */
//    public void add(TsPeriod p, int series) {
//        news_.add(new Update(p, series));
//    }
//
//    public void addRevision(TsPeriod p, int series) {
//        revisions_.add(new Update(p, series));
//    }
//    
//    /**
//     *
//     * @param freq
//     * @return
//     */
//    public TsPeriod firstUpdate(TsFrequency freq) {
//        TsPeriod first = null;
//        for (Update update : news_) {
//            if (first == null) {
//                first = update.period.lastPeriod(freq);
//            } else {
//                TsPeriod cur = update.period.lastPeriod(freq);
//                if (cur.isBefore(first)) {
//                    first = cur;
//                }
//            }
//        }
//        return first;
//    }
//
//    /**
//     *
//     * @param freq
//     * @return
//     */
//    public TsPeriod lastUpdate(TsFrequency freq) {
//        TsPeriod last = null;
//        for (Update update : news_) {
//            if (last == null) {
//                last = update.period.lastPeriod(freq);
//            } else {
//                TsPeriod cur = update.period.lastPeriod(freq);
//                if (cur.isAfter(last)) {
//                    last = cur;
//                }
//            }
//        }
//        return last;
//    }
//
//    /**
//     *
//     * @param freq
//     * @return
//     */
//    public TsDomain updatesDomain(TsFrequency freq, List<Update> updates) {
//        TsPeriod first = null;
//        TsPeriod last = null;
//        for (Update update : updates) {
//            if (first == null) {
//                first = update.period.lastPeriod(freq);
//                last = first;
//            } else {
//                TsPeriod cur = update.period.lastPeriod(freq);
//                if (cur.isBefore(first)) {
//                    first = cur;
//                }
//                if (cur.isAfter(last)) {
//                    last = cur;
//                }
//            }
//        }
//        if (last == null) {
//            return null;
//        }
//        return new TsDomain(first, last.minus(first) + 1);
//    }
//
//    /**
//     *
//     * @return
//     */
//    public List<Update> news() {
//        return Collections.unmodifiableList(news_);
//    }
//    
//    public List<Update> revisions() {
//        return Collections.unmodifiableList(revisions_);
//    }
//}
