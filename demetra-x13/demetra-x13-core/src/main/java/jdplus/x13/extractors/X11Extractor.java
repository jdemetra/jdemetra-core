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

import demetra.information.InformationMapping;
import demetra.sa.SaDictionary;
import demetra.timeseries.TsData;
import demetra.x11.X11Results;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class X11Extractor {
    private final InformationMapping<X11Results> MAPPING = new InformationMapping<>(X11Results.class);

    static {
        MAPPING.set("b1", TsData.class, source-> source.getB1());
        MAPPING.set("b2", TsData.class, source-> source.getB2());
        MAPPING.set("b3", TsData.class, source-> source.getB3());
        MAPPING.set("b4", TsData.class, source-> source.getB4());
        MAPPING.set("b5", TsData.class, source-> source.getB5());
        MAPPING.set("b6", TsData.class, source-> source.getB6());
        MAPPING.set("b7", TsData.class, source-> source.getB7());
        MAPPING.set("b8", TsData.class, source-> source.getB8());
        MAPPING.set("b9", TsData.class, source-> source.getB9());
        MAPPING.set("b10", TsData.class, source-> source.getB10());
        MAPPING.set("b11", TsData.class, source-> source.getB11());
        MAPPING.set("b13", TsData.class, source-> source.getB13());
        MAPPING.set("b17", TsData.class, source-> source.getB17());
        MAPPING.set("b20", TsData.class, source-> source.getB20());
        MAPPING.set("c1", TsData.class, source-> source.getC1());
        MAPPING.set("c2", TsData.class, source-> source.getC2());
        MAPPING.set("c4", TsData.class, source-> source.getC4());
        MAPPING.set("c5", TsData.class, source-> source.getC5());
        MAPPING.set("c6", TsData.class, source-> source.getC6());
        MAPPING.set("c7", TsData.class, source-> source.getC7());
        MAPPING.set("c9", TsData.class, source-> source.getC9());
        MAPPING.set("c10", TsData.class, source-> source.getC10());
        MAPPING.set("c11", TsData.class, source-> source.getC11());
        MAPPING.set("c13", TsData.class, source-> source.getC13());
        MAPPING.set("c17", TsData.class, source-> source.getC17());
        MAPPING.set("c20", TsData.class, source-> source.getC20());
        MAPPING.set("d1", TsData.class, source-> source.getD1());
        MAPPING.set("d2", TsData.class, source-> source.getD2());
        MAPPING.set("d4", TsData.class, source-> source.getD4());
        MAPPING.set("d5", TsData.class, source-> source.getD5());
        MAPPING.set("d6", TsData.class, source-> source.getD6());
        MAPPING.set("d7", TsData.class, source-> source.getD7());
        MAPPING.set("d8", TsData.class, source-> source.getD8());
        MAPPING.set("d9", TsData.class, source-> source.getD9());
        MAPPING.set("d10", TsData.class, source-> source.getD10());
        MAPPING.set("d11", TsData.class, source-> source.getD11());
        MAPPING.set("d12", TsData.class, source-> source.getD12());
        MAPPING.set("d13", TsData.class, source-> source.getD13());
    }

    public InformationMapping<X11Results> getMapping() {
        return MAPPING;
    }
    
}
