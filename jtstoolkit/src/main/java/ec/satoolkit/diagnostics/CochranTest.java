/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.diagnostics;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Christiane Hofer
 */
@Development(status = Development.Status.Exploratory)
public class CochranTest {

    public CochranTest(TsData tsData, Boolean isMulti) {
        ts_ = tsData;
        isMulti_ = isMulti;

        //       mode_= mode;
    }
    ;

    private final TsData ts_;
    private boolean isMulti_ = false;
    private double[] s_;
    private double tw_;
    private double tt_;// critical vaule
    boolean I1_ = true; //is the boolen that is true if the test has not be rejected
    private int nminNumberOfYears = 0;

//  Critical values for monthly data
    private static final double[] t12 = {0.5410, 0.3934, 0.3264, 0.2880, 0.2624, 0.2439, 0.2299, 0.2187,
                                         0.2098, 0.2020, 0.1980, 0.194, 0.186, 0.182, 0.178, 0.174, 0.17,
                                         0.166, 0.162, 0.158, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15,
                                         0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0.1403, 0.14,
                                         0.14, 0.14, 0.14};

//critical values for quarterly data
    private static final double[] t4 = {
        0.9065, 0.7679, 0.6841, 0.6287, 0.5895, 0.5598, 0.5365, 0.5175,
        .5017, 0.4884, 0.480, 0.471, 0.463, 0.454, 0.445, 0.4366,
        .433, 0.430, 0.427, 0.424, 0.421, 0.417, 0.414, 0.411, 0.408,
        .404, 0.401, 0.398, 0.395, 0.391, 0.388, 0.385, 0.382, 0.379,
        .375, 0.3720, 0.369, 0.366, 0.362, 0.359};
//calculate the Corantest for a given Timseries, and gives the false if the Nullhypothesis
    // of equal variances of the periodes has to be rejected and different standarddeviations should be used
    //for outlier detection

    /**
     * The critical values C are calculated for up to 41 years from
     * C=[1+(N-1)/(F(0.05/N;n-1;(N-1)(n-1)))]^(-1)
     * N=number of observation per year
     * n= number of years
     * F distribution
     */
    private static final double[] t2 = {
        0.9985, 0.975, 0.9392, 0.9057, 0.8772, 0.8534, 0.8332, 0.8159, 0.801, 0.788, 0.7765,
        0.7662, 0.757, 0.7487, 0.7411, 0.7341, 0.7278, 0.7219, 0.7164, 0.7114, 0.7066, 0.7022,
        0.698, 0.6941, 0.6904, 0.6869, 0.6836, 0.6805, 0.6775, 0.6747, 0.672, 0.6694, 0.6669,
        0.6646, 0.6623, 0.6601, 0.658, 0.656, 0.6541, 0.6522};

    public void calcCochranTest() {
        int Ib = ts_.getStart().getPosition(); //Ib index of the first period //X conatins the values from the beginning to the end of the,
        int iPeriode; // is the concidered periode, with corresponds to the relevant periode
        int j; //counter for the periodes from 1,...4 or 12
        int n1; //number of values in a periode eg. in January
        int nmin; // minimal number of observations of a period
        boolean blngoto = true;
        //  double[] s;//array for the standarddeviation for each period dimension 1,...4 or 12

        double smax; //max standarddeviation of periods
        double st;// theoretical mean 0 for multi and 1 for add
        // double tw;// teststatistik

        int Ny = ts_.getFrequency().intValue(); //Observations per year 12 or 4
        s_ = new double[Ny]; //original PSP first remains empty 0,...,Ny-1
        //  Double X[],t[40],t4[40],s[PSP];

//     This routine performs Cochran's test to determine if the months or quaters
//     are heteroskedastic.
//
        tw_ = 0;
        smax = -10.0;
        nmin = 100;

        st = 0; //Additve
        if (isMulti_) {
            st = 1;
        }

        for (int i = 0; i <= Ny - 1; i++) { //each period is taken into accoutn

            n1 = 1;
            j = i; //
            iPeriode = i + Ib;
            if (iPeriode > Ny - 1) {
                iPeriode = iPeriode - Ny;
            }
            s_[iPeriode] = 0;//  s_[i] = 0;
            blngoto = true;
            do {
                if (!Double.isNaN(ts_.get(j))) {
                    // s_[i] = s_[i] + ((ts_.getValues().get(j) - st)*(ts_.getValues().get(j) - st));//
                    s_[iPeriode] = s_[iPeriode] + ((ts_.get(j) - st) * (ts_.get(j) - st));//
                    n1 = n1 + 1;//count values
                }
                j = j + Ny; // for each year
                if (j > ts_.getLength() - 1) {
                    if (nmin > n1 - 3) {
                        nmin = n1 - 3;//
                    }
                    //  s_[i] = s_[i] / (n1 - 1);
                    s_[iPeriode] = s_[iPeriode] / (n1 - 1);
                    if (smax < s_[iPeriode]) { //if (smax < s_[i])
                        smax = s_[iPeriode];          //   smax = s_[i];
                    }
                    tw_ = tw_ + s_[iPeriode];    //           tw_ = tw_ + s_[i];
                    blngoto = false;
                }

            } while (blngoto);

        }
        if (!(tw_ == 0)) {
            tw_ = smax / tw_;
        }
        nminNumberOfYears = nmin + 1;
        if (nmin > 39) {
            nmin = 39;
        }

        tt_ = t12[nmin];
        if (Ny == 4) {
            tt_ = t4[nmin];
        } else if (Ny == 2) {
            tt_ = t2[nmin];
        }

        if (tw_ >= tt_) {
            I1_ = false;
        }

    }

    public double getCriticalValue() {
        return tt_;
    }

    ;

          public double getTestValue() {
        return tw_;
    }

    /**
     *
     * @return standardeviation for each period
     */
    public double[] getS() {
        return s_;
    }

    /**
     *
     * @return true if CriticalValue > TestValue; Nullhypothesis for identical
     *         variances each period has to be rejected, and different variances should
     *         be used
     */
    public boolean getTestResult() {
        return I1_;
    }

    /**
     *
     * @return the minimumn numbers of yeas, this is the number of values per
     *         periode
     *         taken into account when the cochrantest ist calculated,
     */
    public int getMinNumberOfYearsPerPeriod() {
        return nminNumberOfYears;
    }
;
}
