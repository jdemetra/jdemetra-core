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

import nbbrd.design.Development;
import demetra.information.InformationMapping;
import java.util.function.Function;
import jdplus.x13.Mstatistics;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class MStatisticsExtractor {

    public final String M1 = "m1", M2 = "m2", M3 = "m3", M4 = "m4";
    public final String M5 = "m5", M6 = "m6", M7 = "m7", M8 = "m8";
    public final String M9 = "m9", M10 = "m10", M11 = "m11";
    public final String Q = "q", Q2 = "q-m2";

 
    // MAPPING
    public  InformationMapping<Mstatistics> getMapping() {
        return MAPPING;
    }

    public  <T> void setMapping(String name, Class<T> tclass, Function<Mstatistics, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    private final InformationMapping<Mstatistics> MAPPING = new InformationMapping<>(Mstatistics.class
    );

    static {
        MAPPING.set(M1, Double.class,
                 source -> source.getM(1));
        MAPPING.set(M2, Double.class,
                 source -> source.getM(2));
        MAPPING.set(M3, Double.class,
                 source -> source.getM(3));
        MAPPING.set(M4, Double.class,
                 source -> source.getM(4));
        MAPPING.set(M5, Double.class,
                 source -> source.getM(5));
        MAPPING.set(M6, Double.class,
                 source -> source.getM(6));
        MAPPING.set(M7, Double.class,
                 source -> source.getM(7));
        MAPPING.set(M8, Double.class,
                 source -> source.getM(8));
        MAPPING.set(M9, Double.class,
                 source -> source.getM(9));
        MAPPING.set(M10, Double.class,
                 source -> source.getM(10));
        MAPPING.set(M11, Double.class,
                 source -> source.getM(11));
        MAPPING.set(Q, Double.class,
                 source -> source.getQ());
        MAPPING.set(Q2, Double.class,
                 source -> source.getQm2());
    }
    
}
