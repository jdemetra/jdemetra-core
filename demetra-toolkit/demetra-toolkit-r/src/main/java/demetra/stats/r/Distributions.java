/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.r;

import demetra.stats.ProbabilityType;
import jdplus.dstats.Chi2;
import jdplus.dstats.Exponential;
import jdplus.dstats.F;
import jdplus.dstats.Gamma;
import jdplus.dstats.InverseGamma;
import jdplus.dstats.InverseGaussian;
import jdplus.dstats.LogNormal;
import jdplus.dstats.Normal;
import jdplus.dstats.T;
import jdplus.random.RandomNumberGenerator;
import jdplus.random.XorshiftRNG;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Distributions {

    public double[] randomsT(double df, int n) {

        T dist = new T(df);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfT(double df, double[] x) {

        T dist = new T(df);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityT(double df, double[] x) {

        T dist = new T(df);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }

    public double[] randomsChi2(double df, int n) {

        Chi2 dist = new Chi2(df);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfChi2(double df, double[] x) {

        Chi2 dist = new Chi2(df);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityChi2(double df, double[] x) {

        Chi2 dist = new Chi2(df);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }

    public double[] randomsF(double dfnum, double dfdenom, int n) {

        F dist = new F(dfnum, dfdenom);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfF(double dfnum, double dfdenom, double[] x) {

        F dist = new F(dfnum, dfdenom);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityF(double dfnum, double dfdenom, double[] x) {

        F dist = new F(dfnum, dfdenom);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }
    
    public double[] randomsNormal(double mean, double stdev, int n) {

        Normal dist = new Normal(mean, stdev);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfNormal(double mean, double stdev, double[] x) {

        Normal dist = new Normal(mean, stdev);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityNormal(double mean, double stdev, double[] x) {

        Normal dist = new Normal(mean, stdev);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }
    
    public double[] randomsLogNormal(double mean, double stdev, int n) {

        LogNormal dist = new LogNormal(mean, stdev);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfLogNormal(double mean, double stdev, double[] x) {

        LogNormal dist = new LogNormal(mean, stdev);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityLogNormal(double mean, double stdev, double[] x) {

        LogNormal dist = new LogNormal(mean, stdev);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }

    public double[] randomsGamma(double shape, double scale, int n) {

        Gamma dist = new Gamma(shape, scale);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfGamma(double shape, double scale, double[] x) {

        Gamma dist = new Gamma(shape, scale);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityGamma(double shape, double scale, double[] x) {

        Gamma dist = new Gamma(shape, scale);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }

    public double[] randomsInverseGamma(double shape, double scale, int n) {

        InverseGamma dist = new InverseGamma(shape, scale);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfInverseGamma(double shape, double scale, double[] x) {

        InverseGamma dist = new InverseGamma(shape, scale);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityInverseGamma(double shape, double scale, double[] x) {

        InverseGamma dist = new InverseGamma(shape, scale);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }

    public double[] randomsInverseGaussian(double shape, double scale, int n) {

        InverseGaussian dist = new InverseGaussian(shape, scale);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfInverseGaussian(double shape, double scale, double[] x) {

        InverseGaussian dist = new InverseGaussian(shape, scale);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityInverseGaussian(double shape, double scale, double[] x) {

        InverseGaussian dist = new InverseGaussian(shape, scale);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }

    public double[] randomsExponential(double scale, int n) {

        Exponential dist = new Exponential(scale);
        RandomNumberGenerator rnd = XorshiftRNG.fromSystemNanoTime();
        double[] r = new double[n];
        for (int i = 0; i < n; ++i) {
            r[i] = dist.random(rnd);
        }
        return r;
    }

    public double[] cdfExponential(double scale, double[] x) {

        Exponential dist = new Exponential(scale);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getProbability(x[i], ProbabilityType.Lower);
        }
        return r;
    }

    public double[] densityExponential(double scale, double[] x) {

        Exponential dist = new Exponential(scale);
        double[] r = new double[x.length];
        for (int i = 0; i < r.length; ++i) {
            r[i] = dist.getDensity(x[i]);
        }
        return r;
    }
}
