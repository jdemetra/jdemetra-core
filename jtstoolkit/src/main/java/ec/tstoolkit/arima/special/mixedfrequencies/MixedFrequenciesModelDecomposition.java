/*
 * Copyright 2014 National Bank of Belgium
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
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.benchmarking.Cumulator;
import ec.benchmarking.ssf.SsfDisaggregation;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ssf.DiffuseFilteringResults;
import ec.tstoolkit.ssf.DiffuseSquareRootInitializer;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.timeseries.DataType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequenciesModelDecomposition implements IProcResults {

    private UcarimaModel ucm_;
    private DefaultSeriesDecomposition decomposition_;
    private static double EPS = 1e-3;
    private double ur_ = 1;
    private TsDomain domain_;
    private SsfData y_;
    private int c_;
    private boolean mul, mean;

    public void setUr(double ur) {
        ur_ = ur;
    }

    public double getUr() {
        return ur_;
    }

    public boolean decompose(TsData hdata, TsData ldata, SarimaModel arima, boolean mean, DataType type, boolean mul) {
        clear();
        this.mul = mul;
        this.mean=mean;
        if (!calcDomain(hdata, ldata)) {
            return false;
        }
        SarimaModel clone = arima.clone();
        ucm_ = doCanonicalDecomposition(clone, mean, ur_);
        if (ucm_ == null) {
            return false;
        }
        buildSsfY(hdata, ldata, type);
        return calcEstimates();
    }

    public DefaultSeriesDecomposition getDecomposition() {
        return decomposition_;
    }

    public UcarimaModel getUcarimaModel() {
        return ucm_;
    }

    public boolean isMultiplicative() {
        return mul;
    }

    private void clear() {
        domain_ = null;
        ucm_ = null;
        decomposition_ = null;
        y_ = null;
        c_ = 0;
    }

    private boolean calcDomain(TsData hdata, TsData ldata) {
        TsFrequency freq = hdata.getFrequency();
        TsDomain hdom = hdata.getDomain(), ldom = ldata.getDomain();
        domain_ = hdom.union(ldom.changeFrequency(freq, false));
        c_ = freq.ratio(ldom.getFrequency());
        return !domain_.isEmpty();
    }

    private void buildSsfY(TsData hdata, TsData ldata, DataType type) {
        TsFrequency freq = hdata.getFrequency();
        TsData s = hdata.fittoDomain(domain_.extend(0, freq.intValue()));
        DataBlock y = new DataBlock(mul ? s.log() : s);
        if (type == DataType.Flow) {
            Cumulator cumul = new Cumulator(c_);
            cumul.transform(y);
        }
        int j = domain_.search(ldata.getStart().lastPeriod(hdata.getFrequency()));
        if (mul) {
            double cl = Math.log(c_);
            for (int i = 0; i < ldata.getLength(); ++i, j += c_) {
                double cur = Math.log(ldata.get(i));
                if (type == DataType.Flow) {
                    y.set(j, c_ * (cur - cl));
                } else {
                    y.set(j, cur);
                }
            }
        } else {
            for (int i = 0; i < ldata.getLength(); ++i, j += c_) {
                y.set(j, ldata.get(i));
            }
        }
        y_ = new SsfData(y, null);
    }

    private UcarimaModel doCanonicalDecomposition(SarimaModel arima, boolean mean, double ur) {
        if (ur == 1) {
            fixMaUnitRoots(arima, EPS);
        } else {
            checkModel(arima, ur);
        }
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(new TrendCycleSelector());
        decomposer.add(new SeasonalSelector(arima.getFrequency()));
        UcarimaModel ucm = decomposer.decompose(arima);
        ucm.setVarianceMax(-1);
         if (mean) {
            UcarimaModel tmp = new UcarimaModel();
            ArimaModel tm = ucm.getComponent(0);
            BackFilter urb = BackFilter.D1;
            if (tm.isNull()) {
                tm = new ArimaModel(null, urb, urb, 0);
            } else {
                tm = new ArimaModel(tm.getStationaryAR(), tm.getNonStationaryAR().times(urb), tm.getMA().times(urb),
                        tm.getInnovationVariance());
            }
            tmp.addComponent(tm);
            for (int i = 1; i < ucm.getComponentsCount(); ++i) {
                tmp.addComponent(ucm.getComponent(i));
            }
            ucm = tmp;
            //IArimaModel sum=ucm.getModel();
        } 
         if (ucm.isValid()) {
            ucm.compact(2, 2);
            return ucm;
        } else {
            return null;
        }
    }

    private boolean fixMaUnitRoots(SarimaModel arima, double eps) {
        SarimaSpecification spec = arima.getSpecification();
        boolean changed = false;
        double ur = 1 - eps;
        if (spec.getBQ() > 0) {

            double sur = Math.pow(ur, arima.getFrequency());
            double bth = arima.btheta(1);
            if (bth < -sur) {
                changed = true;
                arima.setBTheta(1, -1);
            } else if (bth > sur) {
                changed = true;
                arima.setBTheta(1, 1);
            }
        }
        if (spec.getQ() == 1) {
            double th = arima.theta(1);
            if (th < -ur) {
                changed = true;
                arima.setTheta(1, -1);
            } else if (th > ur) {
                changed = true;
                arima.setBTheta(1, 1);
            }
        } else if (spec.getQ() > 1) {
            Polynomial q = arima.getRegularMA();
            Complex[] roots = q.roots();
            boolean qchanged = false;
            for (int i = 0; i < roots.length; ++i) {
                double l = roots[i].abs();
                if (l < ur) {
                    qchanged = true;
                    roots[i] = roots[i].div(l);
                }
            }
            if (qchanged) {
                q = Polynomial.fromComplexRoots(roots);
                for (int i = 1; i <= spec.getQ(); ++i) {
                    arima.setTheta(i, q.get(i) / q.get(0));
                }
            }
        }
        return changed;
    }

    private boolean checkModel(SarimaModel arima, double ur) {
        SarimaSpecification spec = arima.getSpecification();
        boolean changed = false;
        if (spec.getBQ() > 0) {
            double sur = Math.pow(ur, arima.getFrequency());
            double bth = arima.btheta(1);
            if (bth < -sur) {
                changed = true;
                arima.setBTheta(1, -sur);
            } else if (bth > sur) {
                changed = true;
                arima.setBTheta(1, sur);
            }
        }
        if (spec.getQ() == 1) {
            double th = arima.theta(1);
            if (th < -ur) {
                changed = true;
                arima.setTheta(1, -ur);
            } else if (th > ur) {
                changed = true;
                arima.setBTheta(1, ur);
            }
        } else if (spec.getQ() > 1) {
            Polynomial q = arima.getRegularMA();
            Complex[] roots = q.roots();
            boolean qchanged = false;
            for (int i = 0; i < roots.length; ++i) {
                double l = roots[i].abs() * ur;
                if (l < 1) {
                    qchanged = true;
                    roots[i] = roots[i].times(1 / l);
                }
            }
            if (qchanged) {
                q = Polynomial.fromComplexRoots(roots);
                for (int i = 1; i <= spec.getQ(); ++i) {
                    arima.setTheta(i, q.get(i) / q.get(0));
                }
            }
        }
        return changed;
    }

    private boolean calcEstimates() {

        SsfUcarima ussf = new SsfUcarima(ucm_);
        SsfDisaggregation disagg = new SsfDisaggregation(c_, ussf);
        // System.out.println(s);
        Smoother smoother = new Smoother();
        smoother.setCalcVar(true);
        smoother.setSsf(disagg);
        DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
        frslts.getVarianceFilter().setSavingP(true);
        frslts.getFilteredData().setSavingA(true);
        Filter<ISsf> filter = new Filter<>();
        filter.setInitializer(new DiffuseSquareRootInitializer());
        filter.setSsf(disagg);
        if (!filter.process(y_, frslts)) {
            return false;
        }

        SmoothingResults srslts = new SmoothingResults(true, true);
        if (!smoother.process(y_, frslts, srslts)) {
            return false;
        }
        double[] t = null, s = null, i = null;
        double[] et = null, es = null, ei = null;
        int tpos = ussf.cmpPos(0);
        DataBlock z = new DataBlock(disagg.getStateDim());
        DataBlock zsa = new DataBlock(disagg.getStateDim());
        if (tpos >= 0) {
            z.set(tpos + 1, 1);
            zsa.set(tpos + 1, 1);
            t = srslts.component(tpos + 1);
            et = srslts.componentStdev(tpos + 1);
        }
        int spos = ussf.cmpPos(1);
        if (spos >= 0) {
            z.set(spos + 1, 1);
            s = srslts.component(spos + 1);
            es = srslts.componentStdev(spos + 1);
        }
        int ipos = ussf.cmpPos(2);
        if (ipos >= 0) {
            z.set(ipos + 1, 1);
            zsa.set(ipos + 1, 1);
            i = srslts.component(ipos + 1);
            ei = srslts.componentStdev(ipos + 1);
        }

        double[] sa = srslts.zcomponent(zsa);
        double[] esa = srslts.zvariance(zsa);
        double[] y = srslts.zcomponent(z);
        double[] ey = srslts.zvariance(z);

        for (int k = 0; k < y.length; ++k) {
            if (ey[k] < 1e-9) {
                ey[k] = 0;
            } else {
                ey[k] = Math.sqrt(ey[k]);
            }
        }
        for (int k = 0; k < y.length; ++k) {
            if (esa[k] < 1e-9) {
                esa[k] = 0;
            } else {
                esa[k] = Math.sqrt(esa[k]);
            }
        }

        decomposition_ = new DefaultSeriesDecomposition(DecompositionMode.Additive);
        int len = domain_.getLength();
        TsPeriod start = domain_.getStart(), fstart = start.plus(len);
        if (t != null) {
            DataBlock T = new DataBlock(t);
            DataBlock ET = new DataBlock(et);
            decomposition_.add(new TsData(start, T.range(0, len)), ComponentType.Trend);
            decomposition_.add(new TsData(start, ET.range(0, len)), ComponentType.Trend, ComponentInformation.Stdev);
            decomposition_.add(new TsData(fstart, T.extract(len, -1)), ComponentType.Trend, ComponentInformation.Forecast);
            decomposition_.add(new TsData(fstart, ET.extract(len, -1)), ComponentType.Trend, ComponentInformation.StdevForecast);
        }
        if (s != null) {
            DataBlock S = new DataBlock(s);
            DataBlock ES = new DataBlock(es);
            decomposition_.add(new TsData(start, S.range(0, len)), ComponentType.Seasonal);
            decomposition_.add(new TsData(start, ES.range(0, len)), ComponentType.Seasonal, ComponentInformation.Stdev);
            decomposition_.add(new TsData(fstart, S.extract(len, -1)), ComponentType.Seasonal, ComponentInformation.Forecast);
            decomposition_.add(new TsData(fstart, ES.extract(len, -1)), ComponentType.Seasonal, ComponentInformation.StdevForecast);
        }
        if (i != null) {
            DataBlock I = new DataBlock(i);
            DataBlock EI = new DataBlock(ei);
            decomposition_.add(new TsData(start, I.range(0, len)), ComponentType.Irregular);
            decomposition_.add(new TsData(start, EI.range(0, len)), ComponentType.Irregular, ComponentInformation.Stdev);
            decomposition_.add(new TsData(fstart, I.extract(len, -1)), ComponentType.Irregular, ComponentInformation.Forecast);
            decomposition_.add(new TsData(fstart, EI.extract(len, -1)), ComponentType.Irregular, ComponentInformation.StdevForecast);
        }
        DataBlock Y = new DataBlock(y);
        DataBlock EY = new DataBlock(ey);
        DataBlock SA = new DataBlock(sa);
        DataBlock ESA = new DataBlock(esa);
        decomposition_.add(new TsData(start, Y.range(0, len)), ComponentType.Series);
        decomposition_.add(new TsData(start, EY.range(0, len)), ComponentType.Series, ComponentInformation.Stdev);
        decomposition_.add(new TsData(fstart, Y.extract(len, -1)), ComponentType.Series, ComponentInformation.Forecast);
        decomposition_.add(new TsData(fstart, EY.extract(len, -1)), ComponentType.Series, ComponentInformation.StdevForecast);
        decomposition_.add(new TsData(start, SA.range(0, len)), ComponentType.SeasonallyAdjusted);
        decomposition_.add(new TsData(start, ESA.range(0, len)), ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
        decomposition_.add(new TsData(fstart, SA.extract(len, -1)), ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        decomposition_.add(new TsData(fstart, ESA.extract(len, -1)), ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
        return true;
    }

    public TsData getSeries(ComponentType componentType, ComponentInformation componentInformation) {
        TsData s = decomposition_.getSeries(componentType, componentInformation);
        return (s != null && mul) ? s.exp() : s;
    }

    @Override
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        mapper.fillDictionary(null, map);
        return map;
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            return mapper.contains(id);
        }
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        synchronized (mapper) {
            return mapper.getData(this, id, tclass);
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static void fillDictionary(String prefix, Map<String, Class> map) {
        mapper.fillDictionary(prefix, map);
    }

    // MAPPERS
    public static <T> void addMapping(String name, InformationMapper.Mapper<MixedFrequenciesModelDecomposition, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<MixedFrequenciesModelDecomposition> mapper = new InformationMapper<>();

    static {
        mapper.add(ModellingDictionary.Y_CMP, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.Series, ComponentInformation.Value);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.T_CMP, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.Trend, ComponentInformation.Value);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.S_CMP, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.I_CMP, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.Irregular, ComponentInformation.Value);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.Y_CMP+ SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.Series, ComponentInformation.Forecast);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.T_CMP+ SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.SA_CMP+ SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.S_CMP+ SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.I_CMP+ SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData s = source.decomposition_.getSeries(ComponentType.Irregular, ComponentInformation.Forecast);
                return (s != null && source.mul) ? s.exp() : s;
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Series, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Series, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.T_LIN, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Trend, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.T_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Trend, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.SA_LIN, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.SA_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.S_LIN, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.S_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Seasonal, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.I_LIN, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Irregular, ComponentInformation.Value);
            }
        });
        mapper.add(ModellingDictionary.I_LIN + SeriesInfo.E_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Irregular, ComponentInformation.Stdev);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Series, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Series, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.T_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.T_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Trend, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.SA_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.SA_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.S_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.S_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.I_LIN + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Irregular, ComponentInformation.Forecast);
            }
        });
        mapper.add(ModellingDictionary.I_LIN + SeriesInfo.EF_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                return source.decomposition_.getSeries(ComponentType.Irregular, ComponentInformation.StdevForecast);
            }
        });
        mapper.add(ModellingDictionary.SI_LIN, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelDecomposition source) {
                TsData i = source.decomposition_.getSeries(ComponentType.Irregular, ComponentInformation.Value);
                TsData s = source.decomposition_.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                TsData si = TsData.add(s, i);
                if (source.mul) {
                    return si.exp();
                } else {
                    return si;
                }
            }
        });
        mapper.add(ModellingDictionary.MODE, new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, DecompositionMode>(DecompositionMode.class) {

            @Override
            public DecompositionMode retrieve(MixedFrequenciesModelDecomposition source) {
                return source.mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive;
            }
        });
        mapper.add("seasonality", new InformationMapper.Mapper<MixedFrequenciesModelDecomposition, Boolean>(Boolean.class) {

            @Override
            public Boolean retrieve(MixedFrequenciesModelDecomposition source) {
                return !source.ucm_.getComponent(1).isNull();
            }
        });
    }

}
