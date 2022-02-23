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
import demetra.x13.X13Dictionaries;
import jdplus.x13.Mstatistics;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class MstatsExtractor extends InformationMapping<Mstatistics> {


    // MAPPING
    public MstatsExtractor() {
        set(X13Dictionaries.M1, Double.class,
                source -> source.getM(1));
        set(X13Dictionaries.M2, Double.class,
                source -> source.getM(2));
        set(X13Dictionaries.M3, Double.class,
                source -> source.getM(3));
        set(X13Dictionaries.M4, Double.class,
                source -> source.getM(4));
        set(X13Dictionaries.M5, Double.class,
                source -> source.getM(5));
        set(X13Dictionaries.M6, Double.class,
                source -> source.getM(6));
        set(X13Dictionaries.M7, Double.class,
                source -> source.getM(7));
        set(X13Dictionaries.M8, Double.class,
                source -> source.getM(8));
        set(X13Dictionaries.M9, Double.class,
                source -> source.getM(9));
        set(X13Dictionaries.M10, Double.class,
                source -> source.getM(10));
        set(X13Dictionaries.M11, Double.class,
                source -> source.getM(11));
        set(X13Dictionaries.Q, Double.class,
                source -> source.getQ());
        set(X13Dictionaries.Q2, Double.class,
                source -> source.getQm2());
    }

    @Override
    public Class<Mstatistics> getSourceClass() {
        return Mstatistics.class;
    }

}
