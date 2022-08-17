/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.benchmarking.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.stats.StatisticalTest;
import jdplus.tempdisagg.univariate.ResidualsDiagnostics;
import demetra.timeseries.TsData;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(InformationExtractor.class)
public class ResidualsDiagnosticsExtractor extends InformationMapping<ResidualsDiagnostics> {

    public final String FRES = "fullresiduals", MEAN = "mean", SKEWNESS = "skewness",
            KURTOSIS = "kurtosis", DH = "doornikhansen", LJUNGBOX = "ljungbox",
            DW = "durbinwatson", UDRUNS_NUMBER = "nudruns", UDRUNS_LENGTH = "ludruns",
            RUNS_NUMBER = "nruns", RUNS_LENGTH = "lruns";

    public ResidualsDiagnosticsExtractor() {
        set(FRES, TsData.class, source -> source.getFullResiduals());
        set(MEAN, StatisticalTest.class, source -> source.getNiid().meanTest());
        set(SKEWNESS, StatisticalTest.class, source -> source.getNiid().skewness());
        set(KURTOSIS, StatisticalTest.class, source -> source.getNiid().kurtosis());
        set(DH, StatisticalTest.class, source -> source.getNiid().normalityTest());
        set(LJUNGBOX, StatisticalTest.class, source -> source.getNiid().ljungBox());
        set(RUNS_NUMBER, StatisticalTest.class, source -> source.getNiid().runsNumber());
        set(RUNS_LENGTH, StatisticalTest.class, source -> source.getNiid().runsLength());
        set(UDRUNS_NUMBER, StatisticalTest.class, source -> source.getNiid().upAndDownRunsNumbber());
        set(UDRUNS_LENGTH, StatisticalTest.class, source -> source.getNiid().upAndDownRunsLength());
    }

    @Override
    public Class getSourceClass() {
        return ResidualsDiagnostics.class;
    }

}
