/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.arima.ArimaModel;
import demetra.arima.AutoCovarianceFunction;
import demetra.arima.ssf.SsfArima;
import demetra.data.DoubleSequence;
import demetra.maths.MatrixType;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.polynomials.Polynomial;
import demetra.modelling.regression.GenericTradingDaysVariables;
import demetra.modelling.regression.RegressionUtility;
import demetra.sarima.SarimaMapping;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.ssf.SsfComponent;
import demetra.ssf.StateComponent;
import demetra.ssf.implementations.RegSsf;
import demetra.ssf.models.SsfAr;
import demetra.ssf.models.LocalLevel;
import demetra.ssf.models.LocalLinearTrend;
import demetra.ssf.models.SsfAr2;
import demetra.ssf.univariate.ISsf;
import demetra.sts.CyclicalComponent;
import demetra.sts.Noise;
import demetra.sts.SeasonalComponent;
import demetra.sts.SeasonalModel;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class AtomicModels {

    public ModelItem arma(final String name, double[] ar, double[] ma, double var, boolean fixed) {
        return mapping -> {
            final int nar = ar == null ? 0 : ar.length, nma = ma == null ? 0 : ma.length;
            if (nar > 0) {
                mapping.add(new StablePolynomial(name + "_ar", ar, fixed));
            }
            if (nma > 0) {
                mapping.add(new StablePolynomial(name + "_ma", ma, fixed));
            }
            VarianceParameter v = new VarianceParameter(name + "_var", var, true, true);
            mapping.add(v);
            mapping.add((p, builder) -> {
                BackFilter bar = BackFilter.ONE, bma = BackFilter.ONE;
                int pos = 0;
                if (nar > 0) {
                    Polynomial par = Polynomial.valueOf(1, p.extract(0, nar).toArray());
                    bar = new BackFilter(par);
                    pos += nar;
                }
                if (nma > 0) {
                    Polynomial pma = Polynomial.valueOf(1, p.extract(0, nma).toArray());
                    bma = new BackFilter(pma);
                    pos += ma.length;
                }
                double n = p.get(pos++);
                ArimaModel arima = new ArimaModel(bar, BackFilter.ONE, bma, n);
                StateComponent cmp = SsfArima.componentOf(arima);
                builder.add(name, cmp, null);
                return pos;
            });
        };
    }

    public ModelItem sarima(final String name, int period, int[] orders, int[] seasonal, double[] parameters, boolean fixed) {
        SarimaSpecification spec = new SarimaSpecification(period);
        spec.setP(orders[0]);
        spec.setD(orders[1]);
        spec.setQ(orders[2]);
        if (seasonal != null) {
            spec.setBp(seasonal[0]);
            spec.setBd(seasonal[1]);
            spec.setBq(seasonal[2]);
        }
        return mapping -> {
            mapping.add(new SarimaParameters(name, spec, parameters, fixed));
            mapping.add((p, builder) -> {
                int np = spec.getParametersCount();
                double[] c = p.extract(0, np).toArray();
                SarimaModel sarima = SarimaModel.builder(spec)
                        .parameters(DoubleSequence.ofInternal(c))
                        .build();
                StateComponent cmp = SsfArima.componentOf(sarima);
                builder.add(name, cmp, null);
                return np;
            });
        };
    }

    public ModelItem localLevel(String name, double lvar, boolean fixed) {
        return mapping -> {
            VarianceParameter v = new VarianceParameter(name + "_var", lvar, fixed, true);
            mapping.add(v);
            mapping.add((p, builder) -> {
                double var = p.get(0);
                SsfComponent cmp = LocalLevel.of(var);
                builder.add(name, cmp);
                return 1;
            });
        };
    }

    public ModelItem localLinearTrend(final String name, double lvar, double svar, boolean lfixed, boolean sfixed) {
        return mapping -> {
            VarianceParameter v1 = new VarianceParameter(name + "_lvar", lvar, lfixed, true);
            mapping.add(v1);
            VarianceParameter v2 = new VarianceParameter(name + "_svar", svar, sfixed, true);
            mapping.add(v2);
            mapping.add((p, builder) -> {
                double var1 = p.get(0);
                double var2 = p.get(1);
                SsfComponent cmp = LocalLinearTrend.of(var1, var2);
                builder.add(name, cmp);
                return 2;
            });
        };
    }

    public ModelItem seasonalComponent(String name, String smodel, int period, double seasvar, boolean fixed) {
        return mapping -> {
            VarianceParameter v = new VarianceParameter(name + "_var", seasvar, fixed, true);
            mapping.add(v);
            mapping.add((p, builder) -> {
                double var = p.get(0);
                SsfComponent cmp = SeasonalComponent.of(SeasonalModel.valueOf(smodel), period, var);
                builder.add(name, cmp);
                return 1;
            });
        };
    }

    public ModelItem noise(String name, double var, boolean fixed) {
        return mapping -> {
            VarianceParameter v = new VarianceParameter(name + "_var", var, fixed, true);
            mapping.add(v);
            mapping.add((p, builder) -> {
                double nv = p.get(0);
                SsfComponent cmp = Noise.of(nv);
                builder.add(name, cmp);
                return 1;
            });
        };
    }

    public ModelItem regression(String name, MatrixType x) {
        return mapping -> {
            mapping.add((p, builder) -> {
                SsfComponent cmp = RegSsf.of(Matrix.of(x));
                builder.add(name, cmp);
                return 0;
            });
        };
    }

    public ModelItem timeVaryingRegression(String name, MatrixType x, double var, boolean fixed) {
        return mapping -> {
            VarianceParameter v = new VarianceParameter(name + "_var", var, fixed, true);
            mapping.add(v);
            mapping.add((p, builder) -> {
                double nv = p.get(0);
                SsfComponent cmp = RegSsf.ofTimeVarying(Matrix.of(x), nv);
                builder.add(name, cmp);
                return 0;
            });
        };
    }

    public ModelItem timeVaryingRegression(String name, MatrixType x, final double[] vars, final boolean fixed) {
        return mapping -> {
            final int n = vars.length;
            for (int i = 0; i < n; ++i) {
                VarianceParameter v = new VarianceParameter(name + "_var" + (i + 1), vars[i], fixed, true);
            }
            mapping.add((p, builder) -> {
                DoubleSequence np = p.extract(0, n);
                SsfComponent cmp = RegSsf.ofTimeVarying(Matrix.of(x), np);
                builder.add(name, cmp);
                return 0;
            });
        };
    }

    public MatrixType tdContrasts(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        Matrix x = RegressionUtility.data(domain, new GenericTradingDaysVariables(gtd));
        return x.unmodifiable();
    }

    public MatrixType rawTd(TsDomain domain, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.of(dc);
        Matrix x = RegressionUtility.data(domain, new GenericTradingDaysVariables(gtd));
        return x.unmodifiable();
    }

    public ModelItem tdRegression(String name, TsDomain domain, int[] groups, final boolean contrast, final double var, final boolean fixed) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
        Matrix x = RegressionUtility.data(domain, new GenericTradingDaysVariables(gtd));
        Matrix mvar = generateVar(dc, contrast);
        return mapping -> {
            VarianceParameter v = new VarianceParameter(name + "_var", var, fixed, true);
            mapping.add(v);
            mapping.add((p, builder) -> {
                double pvar = p.get(0);
                Matrix xvar = mvar.deepClone();
                xvar.mul(pvar * pvar);
                SsfComponent cmp = RegSsf.ofTimeVarying(x, xvar);
                builder.add(name, cmp);
                return 1;
            });
        };
    }

    public ModelItem rawTdRegression(String name, TsDomain domain, int[] groups, final double[] vars, final boolean fixed) {
        DayClustering dc = DayClustering.of(groups);
        GenericTradingDays gtd = GenericTradingDays.of(dc);
        Matrix x = RegressionUtility.data(domain, new GenericTradingDaysVariables(gtd));
        return mapping -> {
            final int n = vars.length;
            for (int i = 0; i < n; ++i) {
                VarianceParameter v = new VarianceParameter(name + "_var" + (i + 1), vars[i], fixed, true);
                mapping.add(v);
            }
            mapping.add((p, builder) -> {
                DoubleSequence np = p.extract(0, n);
                double[] nv = np.toArray();
                for (int i = 0; i < nv.length; ++i) {
                    nv[i] = nv[i] * nv[i];
                }
                SsfComponent cmp = RegSsf.ofTimeVarying(x, DoubleSequence.ofInternal(nv));
                builder.add(name, cmp);
                return n;
            });
        };
    }

    public ModelItem ar(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, boolean zeroinit) {
        return mapping -> {
            mapping.add(new ArParameters(name + "_ar", ar, fixedar));
            VarianceParameter v = new VarianceParameter(name + "_var", var, fixedvar, true);
            mapping.add(v);
            mapping.add((p, builder) -> {
                double[] par = p.extract(0, ar.length).toArray();
                double w = p.get(ar.length);
                SsfComponent cmp = SsfAr.of(par, w, nlags, zeroinit);
                builder.add(name, cmp);
                return ar.length + 1;
            });
        };
    }

    public ModelItem sae(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int lag, boolean zeroinit) {
        return mapping -> {
            mapping.add(new ArParameters(name + "_sae", ar, fixedar));
            mapping.add(new VarianceParameter(name + "_saevar", var, fixedvar, true));
            mapping.add((p, builder) -> {
                double[] par = p.extract(0, ar.length).toArray();
                double w = p.get(ar.length);
                // compute the "normalized" covariance
                double[] car = new double[par.length + 1];
                double[] lpar = new double[par.length * lag];
                car[0] = 1;
                for (int i = 0, j = lag - 1; i < par.length; ++i, j += lag) {
                    lpar[j] = par[i];
                    car[i + 1] = -par[i];
                }
                AutoCovarianceFunction acf = new AutoCovarianceFunction(Polynomial.ONE, Polynomial.ofInternal(car), 1);
                SsfComponent cmp = SsfAr.of(lpar, w / acf.get(0), lpar.length, zeroinit);
                builder.add(name, cmp);
                return ar.length + 1;
            });
        };
    }

    public ModelItem waveSpecificSurveyError(String name, int nwaves, MatrixType wsae, double ar1, double[] ar2, boolean fixedar, int lag, boolean zeroinit) {
        return mapping -> {
            final boolean bar1 = Double.isFinite(ar1), bar2 = ar2 != null;
            if (bar1) {
                mapping.add(new ArParameters(name + "_sae1", new double[]{ar1}, fixedar));
            }
            if (bar2) {
                mapping.add(new ArParameters(name + "_sae2", ar2, fixedar));
            }
            mapping.add((p, builder) -> {
                int np = 0;
                double par1 = Double.NaN;
                if (bar1) {
                    par1 = p.get(0);
                    ++np;
                }
                double[] par2 = null;
                if (bar2) {
                    par2 = p.extract(np, 2).toArray();
                    np += 2;
                }
//                SsfComponent cmp = SsfAr.of(lpar, acf.get(0), lpar.length, zeroinit);
//                builder.add(name, cmp);
                return np;
            });
        };
    }

    public ModelItem ar(String name, double[] ar, boolean fixedar, double var, boolean fixedvar, int nlags, int nfcasts) {
        return mapping -> {
            mapping.add(new ArParameters(name + "_ar", ar, fixedar));
            VarianceParameter v = new VarianceParameter(name + "_var", var, fixedvar, true);
            mapping.add(v);
            mapping.add((p, builder) -> {
                double[] par = p.extract(0, ar.length).toArray();
                double w = p.get(ar.length);
                SsfComponent cmp = SsfAr2.of(par, w, nlags, nfcasts);
                builder.add(name, cmp);
                return ar.length + 1;
            });
        };
    }
    // TODO

    public ISsf arima(double[] ar, double[] diff, double[] ma, double var) {
        ArimaModel arima = new ArimaModel(BackFilter.ofInternal(ar), BackFilter.ofInternal(diff), BackFilter.ofInternal(ma), var);
        return SsfArima.of(arima);
    }

    public SsfComponent cycle(double dumpingFactor, double cyclicalPeriod, double cvar) {
        return CyclicalComponent.of(dumpingFactor, cyclicalPeriod, cvar);
    }

    private Matrix generateVar(DayClustering dc, boolean contrasts) {
        int groupsCount = dc.getGroupsCount();
        Matrix full = Matrix.square(7);
        if (!contrasts) {
            full.set(-1.0 / 7.0);
        }
        full.diagonal().add(1);
        Matrix Q = Matrix.make(groupsCount - 1, 7);
        int[] gdef = dc.getGroupsDefinition();
        for (int i = 1; i < groupsCount; ++i) {
            for (int j = 0; j < 7; ++j) {
                if (gdef[j] == i) {
                    Q.set(i - 1, j, 1);
                }
            }
        }
        return SymmetricMatrix.XSXt(full, Q);
    }
}
