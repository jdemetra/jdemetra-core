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
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.x11.X11Results;
import jdplus.math.matrices.Matrix;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X11Extractor extends InformationMapping<X11Results> {

    public X11Extractor() {
        set("b1", TsData.class, source -> source.getB1());
        set("b2", TsData.class, source -> source.getB2());
        set("b3", TsData.class, source -> source.getB3());
        set("b4", TsData.class, source -> source.getB4());
        set("b5", TsData.class, source -> source.getB5());
        set("b6", TsData.class, source -> source.getB6());
        set("b7", TsData.class, source -> source.getB7());
        set("b8", TsData.class, source -> source.getB8());
        set("b9", TsData.class, source -> source.getB9());
        set("b10", TsData.class, source -> source.getB10());
        set("b11", TsData.class, source -> source.getB11());
        set("b13", TsData.class, source -> source.getB13());
        set("b17", TsData.class, source -> source.getB17());
        set("b20", TsData.class, source -> source.getB20());
        set("c1", TsData.class, source -> source.getC1());
        set("c2", TsData.class, source -> source.getC2());
        set("c4", TsData.class, source -> source.getC4());
        set("c5", TsData.class, source -> source.getC5());
        set("c6", TsData.class, source -> source.getC6());
        set("c7", TsData.class, source -> source.getC7());
        set("c9", TsData.class, source -> source.getC9());
        set("c10", TsData.class, source -> source.getC10());
        set("c11", TsData.class, source -> source.getC11());
        set("c13", TsData.class, source -> source.getC13());
        set("c17", TsData.class, source -> source.getC17());
        set("c20", TsData.class, source -> source.getC20());
        set("d1", TsData.class, source -> source.getD1());
        set("d2", TsData.class, source -> source.getD2());
        set("d4", TsData.class, source -> source.getD4());
        set("d5", TsData.class, source -> source.getD5());
        set("d6", TsData.class, source -> source.getD6());
        set("d7", TsData.class, source -> source.getD7());
        set("d8", TsData.class, source -> source.getD8());
        set("d9", TsData.class, source -> source.getD9());
        set("d10", TsData.class, source -> source.getD10());
        set("d11", TsData.class, source -> source.getD11());
        set("d12", TsData.class, source -> source.getD12());
        set("d13", TsData.class, source -> source.getD13());

        set("all", MatrixType.class, source -> {
            TsData b1 = source.getB1();
            TsPeriod start = b1.getStart();
            int nr = b1.length(), nc = 38;

            Matrix m = Matrix.make(nr, nc);
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
