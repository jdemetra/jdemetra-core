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
import demetra.sa.SaDictionaries;
import demetra.sa.StationaryVarianceDecomposition;
import demetra.x13.X13Dictionaries;
import jdplus.sa.diagnostics.GenericSaTests;
import jdplus.x13.Mstatistics;
import jdplus.x13.X13Diagnostics;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class X13DiagnosticsExtractor extends InformationMapping<X13Diagnostics> {


    public X13DiagnosticsExtractor() {
        delegate(SaDictionaries.DIAGNOSTICS, GenericSaTests.class, source -> source.getGenericDiagnostics());
        
        delegate(SaDictionaries.VARIANCE, StationaryVarianceDecomposition.class, source -> source.getVarianceDecomposition());
        
        delegate(X13Dictionaries.MSTATISTICS, Mstatistics.class, source -> source.getMstatistics());
    }

    @Override
    public Class<X13Diagnostics> getSourceClass() {
        return X13Diagnostics.class;
    }

}
