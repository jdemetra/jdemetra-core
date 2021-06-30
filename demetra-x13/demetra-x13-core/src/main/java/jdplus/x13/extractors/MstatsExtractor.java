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
import nbbrd.design.Development;
import demetra.information.InformationMapping;
import jdplus.x13.Mstatistics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class MstatsExtractor extends InformationMapping<Mstatistics> {

    public final String M1 = "m1", M2 = "m2", M3 = "m3", M4 = "m4";
    public final String M5 = "m5", M6 = "m6", M7 = "m7", M8 = "m8";
    public final String M9 = "m9", M10 = "m10", M11 = "m11";
    public final String Q = "q", Q2 = "q-m2";

    // MAPPING
    public MstatsExtractor() {
        set(M1, Double.class,
                source -> source.getM(1));
        set(M2, Double.class,
                source -> source.getM(2));
        set(M3, Double.class,
                source -> source.getM(3));
        set(M4, Double.class,
                source -> source.getM(4));
        set(M5, Double.class,
                source -> source.getM(5));
        set(M6, Double.class,
                source -> source.getM(6));
        set(M7, Double.class,
                source -> source.getM(7));
        set(M8, Double.class,
                source -> source.getM(8));
        set(M9, Double.class,
                source -> source.getM(9));
        set(M10, Double.class,
                source -> source.getM(10));
        set(M11, Double.class,
                source -> source.getM(11));
        set(Q, Double.class,
                source -> source.getQ());
        set(Q2, Double.class,
                source -> source.getQm2());
    }

    @Override
    public Class<Mstatistics> getSourceClass() {
        return Mstatistics.class;
    }

}
