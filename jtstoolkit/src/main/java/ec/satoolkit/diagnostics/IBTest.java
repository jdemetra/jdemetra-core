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

package ec.satoolkit.diagnostics;

import ec.businesscycle.impl.HodrickPrescott;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.HouseholderR;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.stats.Anova;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Collection;

/**
 *
 * @author pcuser
 */
public class IBTest {

    private TsDomain common_;
    private TsData[] series_;
    private TsData[] aseries_;
    private double lambda_ = 1600;
    private RegModel model_;
    private HouseholderR qr_;

    public boolean process(Collection<TsData> series) {
        if (!initSeries(series)) {
            return false;
        }
        if (!initModel()) {
            return false;
        }

        return computeModel();
    }

    private boolean initSeries(Collection<TsData> series) {
        if (Jdk6.Collections.isNullOrEmpty(series) || Jdk6.Collections.hasEmptyElements(series)) {
            return false;
        }
        common_ = null;
        for (TsData s : series) {
            common_ = TsDomain.and(common_, s.getDomain());
        }
        if (common_.isEmpty()) {
            return false;
        }
        if (common_.getFrequency() == TsFrequency.Yearly) {
            return false;
        }
        series_ = Jdk6.Collections.toArray(series, TsData.class);
        series_ = new TsData[series.size()];
        aseries_ = new TsData[series_.length];
        HodrickPrescott hp = new HodrickPrescott();
        hp.setLambda(getLambda());
        int idx = 0;
        for (TsData s : series) {
            TsData sc = s.fittoDomain(common_);
            sc.normalize();
            series_[idx] = sc;
            if (!hp.process(sc)) {
                return false;
            }
            TsData n = new TsData(common_.getStart(), hp.getNoise(), false);
            aseries_[idx] = n.abs();
            ++idx;
        }

        return true;

    }

    private boolean initModel() {

        // creates the stacked tot
        int n = common_.getLength(), m = aseries_.length;
        if (m == 1) {
            return false;
        }
        DataBlock tot = new DataBlock(n * m);
        DataBlock x = tot.extract(0, n);
        for (int i = 0; i < aseries_.length; ++i) {
            x.copy(aseries_[i]);
            x.slide(n);
        }
        // creates the regression variables
        // month contains the seasonal dummies
        int ifreq = common_.getFrequency().intValue();
        Matrix month = new Matrix(m * n, ifreq);
        for (int j = 0; j < m; ++j) {
            SubMatrix Q = month.subMatrix(j * n, (j + 1) * n, 0, ifreq);
            for (int i = 0; i < ifreq; ++i) {
                Q.column(i).extract(i, -1, ifreq).set(1);
            }
        }

        // creates the series dummies in series
        Matrix series = new Matrix(m * n, m);
        for (int i = 0; i < m; ++i) {
            series.column(i).extract(i * n, n).set(1);
        }

        // create the years dummies in year
        int ny = common_.getYearsCount();
        if (ny == 1) {
            return false;
        }

        Matrix year = new Matrix(m * n, ny);
        // complete the matrix
        for (int j = 0; j < m; ++j) {
            SubMatrix Z = year.subMatrix(j * n, (j + 1) * n, 0, ny);
            TsPeriod start = common_.getStart();
            start.move(start.getPosition());
            for (int i = 0; i < ny; ++i) {
                int i0 = common_.search(start);
                if (i0 < 0) {
                    i0 = 0;
                }
                int i1 = Math.min(i0 + ifreq, n);
                Z.column(i).extract(i0, i1 - i0).set(1);
                start.move(ifreq);
            }
        }

        // compute the regression variables and the linear model
        model_ = new RegModel();

        model_.setY(tot);
        model_.setMeanCorrection(true);
        for (int i = 1; i < ifreq; ++i) {
            model_.addX(month.column(i));
        }
        for (int i = 1; i < ny; ++i) {
            model_.addX(year.column(i));
        }
        for (int i = 1; i < m; ++i) {
            model_.addX(series.column(i));
        }
        return true;
    }

    private boolean computeModel() {

        qr_ = new HouseholderR(false);
        qr_.setEpsilon(1e-9);
        qr_.decompose(model_.variables());
        return true;
    }

    public int getPeriodVarsCount() {
        return common_.getFrequency().intValue() - 1;
    }

    public int getYearVarsCount() {
        return common_.getYearsCount() - 1;
    }

    public int getSeriesVarsCount() {
        return series_.length - 1;
    }
    
    public RegModel getModel(){
        return model_;
    }
    
    public TsDomain getDomain(){
        return common_;
    }
    
    public TsData getStationaryAbsoluteSeries(int i){
        return aseries_[i];
    }
    
    public TsData getSeries(int i){
        return series_[i];
    }
    
    public int getSeriesCount(){
        return series_.length;
    }

//    public ConcentratedLikelihood likelihood(int nvars) {
//        int rank = qr_.rank(nvars);
//        int n = qr_.getEquationsCount();
//        DataBlock res = new DataBlock(n - rank);
//        DataBlock b = new DataBlock(rank);
//        qr_.partialLeastSquares(model_.getY(), b, res);
//        ConcentratedLikelihood ll = new ConcentratedLikelihood();
//        double ssqerr = res.ssq();
//        Matrix u = UpperTriangularMatrix.inverse(qr_.getR());
//
//        // initializing the results...
//        double sig = ssqerr / n;
//        Matrix bvar = SymmetricMatrix.XXt(u);
//        bvar.mul(sig);
//        ll.set(ssqerr, 0, n);
//        ll.setRes(res.getData());
//        ll.setB(b.getData(), bvar, rank);
//        return ll;
//    }
//
//    public ConcentratedLikelihood[] nestedModelsEstimation() {
//        ConcentratedLikelihood[] ll = new ConcentratedLikelihood[4];
//        int n = 1;
//        ll[0] = likelihood(n);
//        n += getPeriodVarsCount();
//        ll[1] = likelihood(n);
//        n += getYearVarsCount();
//        ll[2] = likelihood(n);
//        ll[3] = likelihood(-1);
//        return ll;
//    }
//
//    public StatisticalTest[] anova() {
//        ConcentratedLikelihood[] ll = nestedModelsEstimation();
//        double ssq0 = ll[0].getSsqErr();
//        int df0 = ll[0].getDegreesOfFreedom(true, 0);
//        double ssq1 = ll[1].getSsqErr();
//        int df1 = ll[1].getDegreesOfFreedom(true, 0);
//        double ssq2 = ll[2].getSsqErr();
//        int df2 = ll[2].getDegreesOfFreedom(true, 0);
//        double ssq3 = ll[3].getSsqErr();
//        int df3 = ll[3].getDegreesOfFreedom(true, 0);
//
//        double dssq3 = (ssq2 - ssq3) * df3 / ((df2 - df3) * ssq3);
//        double dssq2 = (ssq1 - ssq2) * df3 / ((df1 - df2) * ssq3);
//        double dssq1 = (ssq0 - ssq1) * df3 / ((df0 - df1) * ssq3);
//
//
//        StatisticalTest[] test = new StatisticalTest[3];
//
//        F f0 = new F();
//        f0.setDFNum(df0 - df1);
//        f0.setDFDenom(df3);
//        test[0] = new StatisticalTest(f0, dssq1, TestType.Upper, false);
//        F f1 = new F();
//        f1.setDFNum(df1 - df2);
//        f1.setDFDenom(df3);
//        test[1] = new StatisticalTest(f1, dssq2, TestType.Upper, false);
//        F f2 = new F();
//        f2.setDFNum(df2 - df3);
//        f2.setDFDenom(df3);
//        test[2] = new StatisticalTest(f2, dssq3, TestType.Upper, false);
//        return test;
//    }

    /**
     * @return the lambda_
     */
    public double getLambda() {
        return lambda_;
    }

    /**
     * @param lambda the lambda coefficient of the Hodrick-Prescott filter
     */
    public void setLambda(double lambda) {
        this.lambda_ = lambda;
    }
    
    public Anova anova(){
        return new Anova(model_, new int[]{getPeriodVarsCount(), getYearVarsCount(), getSeriesVarsCount()});
    }
}
