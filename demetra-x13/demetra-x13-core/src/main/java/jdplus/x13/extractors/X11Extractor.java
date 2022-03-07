/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.x13.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.modelling.SeriesInfo;
import demetra.sa.SaDictionaries;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import jdplus.x11.X11Results;
import jdplus.math.matrices.FastMatrix;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;
import demetra.math.matrices.Matrix;
import demetra.timeseries.TsDomain;
import demetra.x11.MsrTable;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.X11Dictionaries;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X11Extractor extends InformationMapping<X11Results> {

    public X11Extractor() {

//        set(SaDictionaries.T_CMP, TsData.class, source -> source.getD12());
//        set(SaDictionaries.SA_CMP, TsData.class, source -> source.getD11());
//        set(SaDictionaries.S_CMP, TsData.class, source -> source.getD10());
////        set(SaDictionaries.S_CMP+ SeriesInfo.F_SUFFIX, TsData.class, source->source.getD10a());
//        set(SaDictionaries.I_CMP, TsData.class, source -> source.getD13());

        set(X11Dictionaries.B1, TsData.class, source -> source.getB1());
        set(X11Dictionaries.B2, TsData.class, source -> source.getB2());
        set(X11Dictionaries.B3, TsData.class, source -> source.getB3());
        set(X11Dictionaries.B4, TsData.class, source -> source.getB4());
        set(X11Dictionaries.B5, TsData.class, source -> source.getB5());
        set(X11Dictionaries.B6, TsData.class, source -> source.getB6());
        set(X11Dictionaries.B7, TsData.class, source -> source.getB7());
        set(X11Dictionaries.B8, TsData.class, source -> source.getB8());
        set(X11Dictionaries.B9, TsData.class, source -> source.getB9());
        set(X11Dictionaries.B10, TsData.class, source -> source.getB10());
        set(X11Dictionaries.B11, TsData.class, source -> source.getB11());
        set(X11Dictionaries.B13, TsData.class, source -> source.getB13());
        set(X11Dictionaries.B17, TsData.class, source -> source.getB17());
        set(X11Dictionaries.B20, TsData.class, source -> source.getB20());
        set(X11Dictionaries.C1, TsData.class, source -> source.getC1());
        set(X11Dictionaries.C2, TsData.class, source -> source.getC2());
        set(X11Dictionaries.C4, TsData.class, source -> source.getC4());
        set(X11Dictionaries.C5, TsData.class, source -> source.getC5());
        set(X11Dictionaries.C6, TsData.class, source -> source.getC6());
        set(X11Dictionaries.C7, TsData.class, source -> source.getC7());
        set(X11Dictionaries.C9, TsData.class, source -> source.getC9());
        set(X11Dictionaries.C10, TsData.class, source -> source.getC10());
        set(X11Dictionaries.C11, TsData.class, source -> source.getC11());
        set(X11Dictionaries.C13, TsData.class, source -> source.getC13());
        set(X11Dictionaries.C17, TsData.class, source -> source.getC17());
        set(X11Dictionaries.C20, TsData.class, source -> source.getC20());
        set(X11Dictionaries.D1, TsData.class, source -> source.getD1());
        set(X11Dictionaries.D2, TsData.class, source -> source.getD2());
        set(X11Dictionaries.D4, TsData.class, source -> source.getD4());
        set(X11Dictionaries.D5, TsData.class, source -> source.getD5());
        set(X11Dictionaries.D6, TsData.class, source -> source.getD6());
        set(X11Dictionaries.D7, TsData.class, source -> source.getD7());
        set(X11Dictionaries.D8, TsData.class, source -> source.getD8());
        set(X11Dictionaries.D9, TsData.class, source -> source.getD9());
        set(X11Dictionaries.D10, TsData.class, source -> TsData.fitToDomain(source.getD10(), source.getActualDomain()));
        set(X11Dictionaries.D10A, TsData.class, source -> TsData.fitToDomain(source.getD10(), source.getForecastDomain()));
        set(X11Dictionaries.D11, TsData.class, source -> TsData.fitToDomain(source.getD11(), source.getActualDomain()));
        set(X11Dictionaries.D11A, TsData.class, source -> TsData.fitToDomain(source.getD11(), source.getForecastDomain()));
        set(X11Dictionaries.D12, TsData.class, source -> TsData.fitToDomain(source.getD12(), source.getActualDomain()));
        set(X11Dictionaries.D12A, TsData.class, source -> TsData.fitToDomain(source.getD12(), source.getForecastDomain()));
        set(X11Dictionaries.D13, TsData.class, source -> source.getD13());
        set(X11Dictionaries.TRENDFILTER, Integer.class, source -> source.getFinalHendersonFilterLength());
        set(X11Dictionaries.SEASONALFILTERS, String[].class, (X11Results source) ->{
            SeasonalFilterOption[] filters=source.getFinalSeasonalFilter();
            if (filters == null)
                return null;
            String[] f=new String[filters.length];
            for (int i=0; i<f.length; ++i){
                f[i]=filters[i].name();
            }
            return f;
        });
        set(X11Dictionaries.D9_GLOBALMSR, Double.class,(X11Results source) -> {
            MsrTable msr = source.getD9Msr();
            return msr == null ? null : msr.getGlobalMsr();
        });

        set(X11Dictionaries.D9_MSR, double[].class,(X11Results source) -> {
            MsrTable msr = source.getD9Msr();
            if (msr == null)
                return null;
            
            double[] m=new double[msr.getCount()];
            for (int i=0; i<m.length; ++i)
                m[i]=msr.getMsr(i);
            return m;
        });
         
        set(X11Dictionaries.D9_MSRTABLE, Matrix.class,(X11Results source) -> {
            MsrTable msr = source.getD9Msr();
            if (msr == null)
                return null;
            
            int n=msr.getCount();
            FastMatrix M=FastMatrix.make(3, n);
            double[] irr = msr.getMeanIrregularEvolutions();
            double[] seas = msr.getMeanSeasonalEvolutions();
            for (int i=0; i<n; ++i){
                M.set(0, i, irr[i]);
                M.set(1, i, seas[i]);
                M.set(2, i, msr.getMsr(i));
            }
            return M;
        });

        set(X11Dictionaries.X11_ALL, Matrix.class, source -> {
            TsData b1 = source.getB1();
            TsPeriod start = b1.getStart();
            int nr = b1.length(), nc = 38;

            FastMatrix m = FastMatrix.make(nr, nc);
            m.set(Double.NaN);
            int c = 0;
            m.column(c++).copy(b1.getValues());
            TsData s = source.getB2();
            int n0 = start.until(s.getStart()), n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB3();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB4();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB5();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB6();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB7();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB8();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB9();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB10();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB11();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB13();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB17();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getB20();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC1();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC2();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC4();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC5();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC6();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC7();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC9();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC10();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC11();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC13();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC17();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getC20();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD1();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD2();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD4();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD5();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD6();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD7();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD8();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD9();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD10();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD11();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD12();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            s = source.getD13();
            n0 = start.until(s.getStart());
            n1 = s.length();
            m.column(c++).range(n0, n0 + n1).copy(s.getValues());
            return m;
        });

    }

    @Override
    public Class<X11Results> getSourceClass() {
        return X11Results.class;
    }

}
