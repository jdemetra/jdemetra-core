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
package ec.satoolkit.special;

import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.Method;
import ec.tstoolkit.modelling.arima.NoPreprocessing;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.OutlierType;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class PreprocessingSpecification implements IProcSpecification, Cloneable {

    private static final String METHOD = "method", TRANSFORM = "transform", TD = "td", LP = "lp",
            EASTER = "easter", PRETEST = "pretest", AO = "ao", LS = "ls", TC = "tc", SO = "so";
    // pre-processing

    public Method method = Method.Tramo;
    public DefaultTransformationType transform = DefaultTransformationType.Auto;
    public TradingDaysType dtype = TradingDaysType.WorkingDays;
    public LengthOfPeriodType ltype = LengthOfPeriodType.LeapYear;
    public boolean easter = true;
    public boolean pretest = true;
    public boolean ao = true;
    public boolean ls = true;
    public boolean tc = true;
    public boolean so = false;

    private IPreprocessor buildTramo(ProcessingContext context) {
        TramoSpecification spec = new TramoSpecification();
        ec.tstoolkit.modelling.arima.tramo.TransformSpec tspec = new ec.tstoolkit.modelling.arima.tramo.TransformSpec();
        tspec.setFunction(transform);
        spec.setTransform(tspec);
        ec.tstoolkit.modelling.arima.tramo.RegressionSpec rspec = new ec.tstoolkit.modelling.arima.tramo.RegressionSpec();
        ec.tstoolkit.modelling.arima.tramo.CalendarSpec cspec = new ec.tstoolkit.modelling.arima.tramo.CalendarSpec();
        ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec tdspec = new ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec();
        tdspec.setLeapYear(ltype != LengthOfPeriodType.None);
        tdspec.setTradingDaysType(dtype);
        tdspec.setTest(pretest);
        cspec.setTradingDays(tdspec);
        if (easter) {
            ec.tstoolkit.modelling.arima.tramo.EasterSpec espec = new ec.tstoolkit.modelling.arima.tramo.EasterSpec();
            espec.setTest(pretest);
            espec.setOption(EasterSpec.Type.Standard);
            cspec.setEaster(espec);
        }
        rspec.setCalendar(cspec);
        spec.setRegression(rspec);
        if (ao || ls || tc || so) {
            ec.tstoolkit.modelling.arima.tramo.OutlierSpec ospec = new ec.tstoolkit.modelling.arima.tramo.OutlierSpec();
            if (ao) {
                ospec.add(OutlierType.AO);
            }
            if (ls) {
                ospec.add(OutlierType.LS);
            }
            if (tc) {
                ospec.add(OutlierType.TC);
            }
            if (so) {
                ospec.add(OutlierType.SO);
            }
            spec.setOutliers(ospec);
        }
        spec.setUsingAutoModel(false);
        spec.getArima().airline();
        return spec.build(context);
    }

    private IPreprocessor buildX13(ProcessingContext context) {
        RegArimaSpecification spec = new RegArimaSpecification();
        ec.tstoolkit.modelling.arima.x13.TransformSpec tspec = new ec.tstoolkit.modelling.arima.x13.TransformSpec();
        tspec.setFunction(transform);
        spec.setTransform(tspec);
        ec.tstoolkit.modelling.arima.x13.RegressionSpec rspec = new ec.tstoolkit.modelling.arima.x13.RegressionSpec();
        ec.tstoolkit.modelling.arima.x13.TradingDaysSpec tdspec = new ec.tstoolkit.modelling.arima.x13.TradingDaysSpec();
        tdspec.setTradingDaysType(dtype);
        tdspec.setLengthOfPeriod(ltype);
        tdspec.setAutoAdjust(true);
        if (pretest) {
            tdspec.setTest(RegressionTestSpec.Remove);
        }
        rspec.setTradingDays(tdspec);
        if (easter) {
            rspec.add(ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec.easterSpec(pretest));
        }
        spec.setRegression(rspec);
        if (ao || ls || tc || so) {
            ec.tstoolkit.modelling.arima.x13.OutlierSpec ospec = new ec.tstoolkit.modelling.arima.x13.OutlierSpec();
            if (ao) {
                ospec.add(OutlierType.AO);
            }
            if (ls) {
                ospec.add(OutlierType.LS);
            }
            if (tc) {
                ospec.add(OutlierType.TC);
            }
            if (so) {
                ospec.add(OutlierType.SO);
            }
            spec.setOutliers(ospec);
        }

        spec.setUsingAutoModel(false);
        spec.getArima().airline();
        return spec.build(context);

    }

    public IPreprocessor build(ProcessingContext context) {
        switch (method) {
            case Tramo:
                return buildTramo(context);
            case Regarima:
                return buildX13(context);
            default:
                return new NoPreprocessing();
        }
    }

    @Override
    public PreprocessingSpecification clone() {
        try {
            return (PreprocessingSpecification) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info=new InformationSet();
        info.set(METHOD, method.name());
        info.set(TRANSFORM, transform.name());
        info.set(TD, dtype.name());
        info.set(LP, ltype.name());
        info.set(EASTER, easter);
        info.set(PRETEST, pretest);
        info.set(AO, ao);
        info.set(LS, ls);
        info.set(TC, tc);
        info.set(SO, so);
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        String s=info.get(METHOD, String.class);
        if (s != null)
            method=Method.valueOf(s);
        s=info.get(TRANSFORM, String.class);
        if (s != null)
            transform=DefaultTransformationType.valueOf(s);
        s=info.get(TD, String.class);
        if (s != null)
            dtype=TradingDaysType.valueOf(s);
        s=info.get(LP, String.class);
        if (s != null)
            ltype=LengthOfPeriodType.valueOf(s);
        Boolean b=info.get(EASTER, Boolean.class);
        if (b != null)
            easter=b;
        b=info.get(PRETEST, Boolean.class);
        if (b != null)
            pretest=b;
        b=info.get(AO, Boolean.class);
        if (b != null)
            ao=b;
        b=info.get(LS, Boolean.class);
        if (b != null)
            ls=b;
        b=info.get(TC, Boolean.class);
        if (b != null)
            tc=b;
        b=info.get(SO, Boolean.class);
        if (b != null)
            so=b;
        return true;
    }

    public boolean equals(PreprocessingSpecification other) {
        return other.method == method && other.transform == transform && other.dtype == dtype
                && other.ltype == ltype && other.easter == easter && other.pretest == pretest
                && other.ao == ao && other.ls == ls && other.tc == tc && other.so == so;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof PreprocessingSpecification && equals((PreprocessingSpecification) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.method);
        hash = 97 * hash + Objects.hashCode(this.transform);
        hash = 97 * hash + Objects.hashCode(this.dtype);
        hash = 97 * hash + Objects.hashCode(this.ltype);
        hash = 97 * hash + (this.easter ? 1 : 0);
        hash = 97 * hash + (this.pretest ? 1 : 0);
        hash = 97 * hash + (this.ao ? 1 : 0);
        hash = 97 * hash + (this.ls ? 1 : 0);
        return hash;
    }

        public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, METHOD), String.class);
        dic.put(InformationSet.item(prefix, TRANSFORM), String.class);
        dic.put(InformationSet.item(prefix, TD), String.class);
        dic.put(InformationSet.item(prefix, LP), String.class);
        dic.put(InformationSet.item(prefix, EASTER), Boolean.class);
        dic.put(InformationSet.item(prefix, PRETEST), Boolean.class);
        dic.put(InformationSet.item(prefix, AO), Boolean.class);
        dic.put(InformationSet.item(prefix, LS), Boolean.class);
        dic.put(InformationSet.item(prefix, TC), Boolean.class);
        dic.put(InformationSet.item(prefix, SO), Boolean.class);
    }

}
