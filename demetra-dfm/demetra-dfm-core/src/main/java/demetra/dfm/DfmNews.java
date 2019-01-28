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
package demetra.dfm;

//import java.util.List;
//
///**
// * Computation of the news (See Banbura an Modagno, appendix D of the reference
// * paper for further details)
// *
// * @author Jean Palate
// */
//public class DfmNews {
//
//    private MSmoothingResults srslts0_, srslts1_, srslts01_;
//    private final DynamicFactorModel model_;
//    private DynamicFactorModel modelex_;
//    private DynamicFactorModel modelexRev_;
//    private final IMSsf ssf_;
//    private TsInformationSet oldset_, newset_, revisedset_;
//    private TsInformationUpdates updates_;
//    private Matrix mcov_, lcov_;
//    private Matrix mcovRev_, lcovRev_;
//    private TsPeriod first_, last_;
//
//    private int ext_ = 2;
//    /**
//     * "Complete" domain, which is the union of the domains of the old
//     * information set and of the new information set
//     */
//    private TsDomain fullDomain_;
//    /**
//     * domain containing all the news
//     */
//    private TsDomain nDomain_;
//    private TsDomain rDomain_;
//
//    private TsDomain iDomain0_, iDomain1_;
//
//    /**
//     *
//     * @param model
//     */
//    public DfmNews(DynamicFactorModel model) {
//        model_ = model;
//        ssf_ = model_.ssfRepresentation();
//    }
//
//    public TsInformationSet getOldInformationSet() {
//        return this.oldset_;
//    }
//
//    public TsInformationSet getNewInformationSet() {
//        return this.newset_;
//    }
//
//    /**
//     * Returns the old data set with revisions from the new data set. The news
//     * are not present.
//     *
//     * @return Old data + revisions
//     */
//    public TsInformationSet getRevisedInformationSet() {
//        return this.revisedset_;
//    }
//
//    public DynamicFactorModel getModel() {
//        return model_;
//    }
//
//    public TsDomain getNewsDomain() {
//        return nDomain_;
//    }
//
//    public TsDomain getRevisionsDomain() {
//        return rDomain_;
//    }
//
//    /**
//     * Computes the news between two consecutive information set
//     *
//     * @param oldSet The old information set
//     * @param newSet The new information set
//     * @return True if the news have been successfully computed
//     */
//    public boolean process(TsInformationSet oldSet, TsInformationSet newSet) {
//        oldset_ = oldSet;
//        newset_ = newSet;
//        revisedset_ = oldset_.revisedData(newset_);
//        updates_ = oldset_.updates(newset_);    // Calculates news and revisions
//        if (updates_.news().isEmpty()) {
//            return false;
//        }
//
//        // 5 lines are useless (same methods in computeDomains())
//        TsDomain d0 = revisedset_.getCurrentDomain();
//        TsDomain d1 = newset_.getCurrentDomain();
//        fullDomain_ = d0.union(d1);
//        last_ = fullDomain_.getLast();
//        first_ = fullDomain_.getStart();
//
//        computeDomains();
//        return calcNews();
//    }
//
//    private boolean calcNews() {
//        // Calculates News
//        computeNewsDomain();
//
//        Matrix M = revisedset_.generateMatrix(fullDomain_);
//        Matrix M_old = oldset_.generateMatrix(fullDomain_);
//
//        if (!smoothOldData(M_old)) {
//            return false;
//        }
//        
//        if(!smoothRevData(M)) {
//            return false;
//        }
//        
//        updateNews();
//
//        if (!smoothNewDataEx(M)) {
//            return false;
//        }
//        computeNewsCovariance();
//
//        // Calculates Revisions
//        if (!updates_.revisions().isEmpty()) {
//            computeRevisionsDomain();
//            updateRevisions();
//
//            if (!smoothRevisionDataEx(M_old)) {
//                return false;
//            }
//            computeRevisionsCovariance();
//        }
//
//        return true;
//    }
//
//    public double getOldForecast(int series, TsPeriod p) {
//        int pos = p.lastPeriod(fullDomain_.getFrequency()).minus(fullDomain_.getStart());
//        DataBlock A = getOldSmoothingResults().A(pos);
//        return A != null ? ssf_.ZX(pos, series, A) : Double.NaN;
//    }
//
//    public double getNewForecast(int series, TsPeriod p) {
//        int pos = p.lastPeriod(fullDomain_.getFrequency()).minus(fullDomain_.getStart());
//        DataBlock A = getNewSmoothingResults().A(pos);
//        return A != null ? ssf_.ZX(pos, series, A) : Double.NaN;
//    }
//
//    public int getMaxNewsExtensionPeriod() {
//        return ext_;
//    }
//
//    public void setMaxNewsExtensionPeriod(int n) {
//        if (n != ext_) {
//            ext_ = n;
//            calcNews();
//        }
//    }
//
//    private void computeDomains() {
//        TsDomain d0 = revisedset_.getCurrentDomain();
//        TsDomain d1 = newset_.getCurrentDomain();
//        fullDomain_ = d0.union(d1);
//        last_ = fullDomain_.getLast();
//        first_ = fullDomain_.getStart();
//        iDomain0_ = revisedset_.getCommonDomain();
//        iDomain1_ = newset_.getCommonDomain();
//    }
//
//    /**
//     * For every series, calculates the domain of the data that has been added
//     * (from the last news until either the first news or the last available
//     * data).
//     */
//    private void computeNewsDomain() {
//        nDomain_ = updates_.updatesDomain(fullDomain_.getFrequency(), updates_.news());
//        for (int i = 0; i < revisedset_.getSeriesCount(); ++i) {
//            TsData s = revisedset_.series(i);
//            int j = s.getLength();
//            while (j > 0 && s.isMissing(j - 1)) {
//                --j;
//            }
//            TsPeriod last = s.getStart().plus(j).lastPeriod(nDomain_.getFrequency());
//            // n = missing values between the last observation and the first news
//            int n = nDomain_.getStart().minus(last);
//            if (n > 0 && ext_ != 0) {
//                if (ext_ > 0) {
//                    // returns in the past to the minimum betwen n and 
//                    // ext_ * frequency value (monthly = 12, quart. = 4)
//                    n = Math.min(n, ext_ * nDomain_.getFrequency().intValue());
//                }
//                nDomain_ = nDomain_.extend(n, 0);
//            }
//        }
//    }
//
//    private void computeRevisionsDomain() {
//        rDomain_ = updates_.updatesDomain(fullDomain_.getFrequency(), updates_.revisions());
//        for (int i = 0; i < revisedset_.getSeriesCount(); ++i) {
//            TsData s = revisedset_.series(i);
//            int j = s.getLength();
//            while (j > 0 && s.isMissing(j - 1)) {
//                --j;
//            }
//            TsPeriod last = s.getStart().plus(j).lastPeriod(rDomain_.getFrequency());
//            int n = rDomain_.getStart().minus(last);
//            if (n > 0 && ext_ != 0) {
//                if (ext_ > 0) {
//                    n = Math.min(n, ext_ * rDomain_.getFrequency().intValue());
//                }
//                rDomain_ = rDomain_.extend(n, 0);
//            }
//        }
//    }
//
//    /**
//     * Computes the smoothed states corresponding to the old data set. The
//     * variance of the smoothed states are not computed. The states are saved
//     * till the period corresponding to the domain of the news.
//     *
//     * @param M
//     */
//    private boolean smoothOldData(Matrix M) {
//        MultivariateSsfData ssfData = new MultivariateSsfData(M.subMatrix().transpose(), null);
//        MSmoother smoother = new MSmoother();
//        srslts0_ = new MSmoothingResults();
//        int last = fullDomain_.search(iDomain0_.getStart());
//        srslts0_.setSavingStart(last);
//        smoother.setStopPosition(last);
//        smoother.setCalcVariance(false);
//        return smoother.process(ssf_, ssfData, srslts0_);
//    }
//
//    private boolean smoothNewData(Matrix M) {
//        MultivariateSsfData ssfData = new MultivariateSsfData(M.subMatrix().transpose(), null);
//        MSmoother smoother = new MSmoother();
//        srslts1_ = new MSmoothingResults();
//        int last = fullDomain_.search(iDomain1_.getStart());
//        srslts1_.setSavingStart(last);
//        smoother.setStopPosition(last);
//        smoother.setCalcVariance(false);
//        return smoother.process(ssf_, ssfData, srslts1_);
//    }
//    
//    private boolean smoothRevData(Matrix M) {
//        MultivariateSsfData ssfData = new MultivariateSsfData(M.subMatrix().transpose(), null);
//        MSmoother smoother = new MSmoother();
//        srslts01_ = new MSmoothingResults();
//        int last = fullDomain_.search(iDomain0_.getStart());
//        srslts01_.setSavingStart(last);
//        smoother.setStopPosition(last);
//        smoother.setCalcVariance(false);
//        return smoother.process(ssf_, ssfData, srslts01_);
//    }
//
//    /**
//     * Updates the news with the forecasts computed on the old data
//     */
//    private void updateNews() {
//        TsFrequency freq = first_.getFrequency();
//        for (Update update : updates_.news()) {
//            update.y = newset_.series(update.series).get(update.period);
//            int pos = update.period.lastPeriod(freq).minus(first_);
//            update.fy = ssf_.ZX(pos, update.series, srslts01_.A(pos));
//        }
//    }
//
//    private void updateRevisions() {
//        for (Update update : updates_.revisions()) {
//            TsFrequency freq = oldset_.series(update.series).getFrequency();
//            update.y = revisedset_.series(update.series).get(update.period.lastPeriod(freq));
//            update.fy = oldset_.series(update.series).get(update.period.lastPeriod(freq));
//        }
//    }
//
//    /**
//     * Computes the smoothed states of the extended model
//     *
//     * @return
//     */
//    private boolean smoothNewDataEx(Matrix M) {
//        // extends the model
//        modelex_ = model_.clone();
//        modelex_.setBlockLength(model_.getBlockLength() + last_.minus(nDomain_.getStart()) + 1);
//        int last = fullDomain_.getLength() - 1;
//        IMSsf ssf = modelex_.ssfRepresentation();
//        MFilteringResults frslts = new MFilteringResults();
//        frslts.saveAll(last);
//        MFilter filter = new MFilter();
//        MultivariateSsfData ssfData = new MultivariateSsfData(M.subMatrix().transpose(), null);
//        filter.process(ssf, ssfData, frslts);
//        MSmoother smoother = new MSmoother();
//        smoother.setStopPosition(last);
//        smoother.setCalcVariance(true);
//        MSmoothingResults srslts = new MSmoothingResults();
//        srslts.setSavingStart(last);
//        smoother.process(ssf, ssfData, frslts, srslts);
//        mcov_ = new Matrix(srslts.P(last));
//        return true;
//    }
//
//    private boolean smoothRevisionDataEx(Matrix M) {
//        // extends the model
//        modelexRev_ = model_.clone();
//        modelexRev_.setBlockLength(model_.getBlockLength() + last_.minus(rDomain_.getStart()) + 1);
//        int last = fullDomain_.getLength() - 1;
//        IMSsf ssf = modelexRev_.ssfRepresentation();
//        MFilteringResults frslts = new MFilteringResults();
//        frslts.saveAll(last);
//        MFilter filter = new MFilter();
//        MultivariateSsfData ssfData = new MultivariateSsfData(M.subMatrix().transpose(), null);
//        filter.process(ssf, ssfData, frslts);
//        MSmoother smoother = new MSmoother();
//        smoother.setStopPosition(last);
//        smoother.setCalcVariance(true);
//        MSmoothingResults srslts = new MSmoothingResults();
//        srslts.setSavingStart(last);
//        smoother.process(ssf, ssfData, frslts, srslts);
//        mcovRev_ = new Matrix(srslts.P(last));
//        return true;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public MSmoothingResults getOldSmoothingResults() {
//        return srslts0_;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public MSmoothingResults getNewSmoothingResults() {
//        if (srslts1_ == null) {
//            Matrix M = newset_.generateMatrix(fullDomain_);
//            smoothNewData(M);
//        }
//        return srslts1_;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public IMSsf getSsf() {
//        return ssf_;
//    }
//
//    private void computeNewsCovariance() {
//        TsFrequency freq = last_.getFrequency();
//        List<Update> updates = updates_.news();
//        int n = updates.size();
//        int c = model_.getBlockLength();
//        int xc = modelex_.getBlockLength();
//        int nb = model_.getFactorsCount();
//        lcov_ = new Matrix(n, n);
//        int d = ssf_.getStateDim();
//        Matrix V = new Matrix(d, d);
//        DataBlockIterator vcols = V.columns();
//        DataBlock vcol = vcols.getData();
//        DataBlock tmp = new DataBlock(d);
//        for (int i = 0; i < n; ++i) {
//            Update iupdate = updates.get(i);
//            int istart = last_.minus(iupdate.period.lastPeriod(freq));
//            for (int j = 0; j <= i; ++j) {
//                Update jupdate = updates.get(j);
//                // copy the right covariance
//                int jstart = last_.minus(jupdate.period.lastPeriod(freq));
//                V.set(0);
//                for (int r = 0; r < nb; ++r) {
//                    for (int s = 0; s < nb; ++s) {
//                        V.subMatrix(r * c, r * c + c, s * c, s * c + c).copy(
//                                mcov_.subMatrix(r * xc + istart, r * xc + istart + c,
//                                        s * xc + jstart, s * xc + jstart + c));
//                    }
//                }
//
//                tmp.set(0);
//                vcols.begin();
//                do {
//                    tmp.set(vcols.getPosition(), ssf_.ZX(0, iupdate.series, vcol));
//                } while (vcols.next());
//                double q = ssf_.ZX(0, jupdate.series, tmp);
//                if (i == j) {
//                    q += model_.getMeasurements().get(iupdate.series).var;
//                }
//                lcov_.set(i, j, q);
//            }
//        }
//        SymmetricMatrix.fromLower(lcov_);
//        SymmetricMatrix.lcholesky(lcov_, MFilter.Zero);
//    }
//
//    private void computeRevisionsCovariance() {
//        TsFrequency freq = last_.getFrequency();
//        List<Update> updates = updates_.revisions();
//        int n = updates.size();
//        int c = model_.getBlockLength();
//        int xc = modelexRev_.getBlockLength();
//        int nb = model_.getFactorsCount();
//        lcovRev_ = new Matrix(n, n);
//        int d = ssf_.getStateDim();
//        Matrix V = new Matrix(d, d);
//        DataBlockIterator vcols = V.columns();
//        DataBlock vcol = vcols.getData();
//        DataBlock tmp = new DataBlock(d);
//        for (int i = 0; i < n; ++i) {
//            Update iupdate = updates.get(i);
//            int istart = last_.minus(iupdate.period.lastPeriod(freq));
//            for (int j = 0; j <= i; ++j) {
//                Update jupdate = updates.get(j);
//                // copy the right covariance
//                int jstart = last_.minus(jupdate.period.lastPeriod(freq));
//                V.set(0);
//                for (int r = 0; r < nb; ++r) {
//                    for (int s = 0; s < nb; ++s) {
//                        V.subMatrix(r * c, r * c + c, s * c, s * c + c).copy(
//                                mcovRev_.subMatrix(r * xc + istart, r * xc + istart + c,
//                                        s * xc + jstart, s * xc + jstart + c));
//                    }
//                }
//
//                tmp.set(0);
//                vcols.begin();
//                do {
//                    tmp.set(vcols.getPosition(), ssf_.ZX(0, iupdate.series, vcol));
//                } while (vcols.next());
//                double q = ssf_.ZX(0, jupdate.series, tmp);
//                if (i == j) {
//                    q += model_.getMeasurements().get(iupdate.series).var;
//                }
//                lcovRev_.set(i, j, q);
//            }
//        }
//        SymmetricMatrix.fromLower(lcovRev_);
//        SymmetricMatrix.lcholesky(lcovRev_, MFilter.Zero);
//    }
//
//    /**
//     *
//     * @return
//     */
//    public Matrix getStateCovariance() {
//        return mcov_;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public TsInformationUpdates newsDetails() {
//        return updates_;
//    }
//
//    /**
//     *
//     * @return
//     */
//    public DataBlock news() {
//        List<Update> news = updates_.news();
//        int n = news.size();
//        DataBlock a = new DataBlock(n);
//        for (int i = 0; i < n; ++i) {
//            a.set(i, news.get(i).getNews());
//        }
//        return a;
//    }
//
//    public DataBlock revisions() {
//        List<Update> revisions = updates_.revisions();
//        int n = revisions.size();
//        DataBlock a = new DataBlock(n);
//        for (int i = 0; i < n; ++i) {
//            a.set(i, revisions.get(i).getNews());
//        }
//        return a;
//    }
//
//    /**
//     *
//     * @param series
//     * @param p
//     * @return
//     */
//    public DataBlock weights(int series, TsPeriod p) {
//        List<Update> updates = updates_.news();
//
//        int n = updates.size();
//        DataBlock a = new DataBlock(n);
//        int c = model_.getBlockLength();
//        int xc = modelex_.getBlockLength();
//        int nb = model_.getFactorsCount();
//        int d = ssf_.getStateDim();
//        Matrix V = new Matrix(d, d);
//        DataBlockIterator vcols = V.columns();
//        DataBlock vcol = vcols.getData();
//        DataBlock tmp = new DataBlock(d);
//        int istart = last_.minus(p);
//        TsFrequency freq = last_.getFrequency();
//        for (int j = 0; j < n; ++j) {
//            Update jupdate = updates.get(j);
//            int jstart = last_.minus(jupdate.period.lastPeriod(freq));
//            V.set(0);
//            for (int r = 0; r < nb; ++r) {
//                for (int s = 0; s < nb; ++s) {
//                    V.subMatrix(r * c, r * c + c, s * c, s * c + c).copy(
//                            mcov_.subMatrix(r * xc + istart, r * xc + istart + c,
//                                    s * xc + jstart, s * xc + jstart + c));
//                }
//            }
//            tmp.set(0);
//            vcols.begin();
//            do {
//                tmp.set(vcols.getPosition(), ssf_.ZX(0, series, vcol));
//            } while (vcols.next());
//            double q = ssf_.ZX(0, jupdate.series, tmp);
//            a.set(j, q);
//        }
//        // w = A * (LL')^-1 <-> w(LL')=A
//        // B = wL, BL' = A <-> LB'=A'
//        LowerTriangularMatrix.rsolve(lcov_, a, MFilter.Zero); // B
//        LowerTriangularMatrix.lsolve(lcov_, a, MFilter.Zero);
//        return a;
//    }
//
//    public DataBlock weightsRevisions(int series, TsPeriod p) {
//        List<Update> updates = updates_.revisions();
//        int n = updates.size();
//        DataBlock a = new DataBlock(n);
//        int c = model_.getBlockLength();
//        int xc = modelexRev_.getBlockLength();
//        int nb = model_.getFactorsCount();
//        int d = ssf_.getStateDim();
//        Matrix V = new Matrix(d, d);
//        DataBlockIterator vcols = V.columns();
//        DataBlock vcol = vcols.getData();
//        DataBlock tmp = new DataBlock(d);
//        int istart = last_.minus(p);
//        TsFrequency freq = last_.getFrequency();
//        for (int j = 0; j < n; ++j) {
//            Update jupdate = updates.get(j);
//            int jstart = last_.minus(jupdate.period.lastPeriod(freq));
//            V.set(0);
//            for (int r = 0; r < nb; ++r) {
//                for (int s = 0; s < nb; ++s) {
//                    V.subMatrix(r * c, r * c + c, s * c, s * c + c).copy(
//                            mcovRev_.subMatrix(r * xc + istart, r * xc + istart + c,
//                                    s * xc + jstart, s * xc + jstart + c));
//                }
//            }
//            tmp.set(0);
//            vcols.begin();
//            do {
//                tmp.set(vcols.getPosition(), ssf_.ZX(0, series, vcol));
//            } while (vcols.next());
//            double q = ssf_.ZX(0, jupdate.series, tmp);
//            a.set(j, q);
//        }
//        // w = A * (LL')^-1 <-> w(LL')=A
//        // B = wL, BL' = A <-> LB'=A'
//        LowerTriangularMatrix.rsolve(lcovRev_, a, MFilter.Zero); // B
//        LowerTriangularMatrix.lsolve(lcovRev_, a, MFilter.Zero);
//        return a;
//    }
//}
