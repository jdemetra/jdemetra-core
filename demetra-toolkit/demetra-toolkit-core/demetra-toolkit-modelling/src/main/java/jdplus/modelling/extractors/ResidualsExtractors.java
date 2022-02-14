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
package jdplus.modelling.extractors;

import demetra.information.DynamicMapping;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.toolkit.dictionaries.ResidualsDictionaries;
import jdplus.modelling.Residuals;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.experimental.UtilityClass
public class ResidualsExtractors {

    @ServiceProvider(InformationExtractor.class)
    public static class Specific extends InformationMapping<Residuals> {

        public Specific() {
            set(ResidualsDictionaries.TYPE, String.class, source -> source.getType().name());
            set(ResidualsDictionaries.RES, double[].class, source -> source.getRes().toArray());
            set(ResidualsDictionaries.TSRES, TsData.class, source
                    -> {
                TsPeriod start = source.getStart();
                if (start == null) {
                    return null;
                } else {
                    return TsData.of(start, source.getRes());
                }
            });
            set(ResidualsDictionaries.SER, Double.class, source->source.getSer());
            addTest(ResidualsDictionaries.MEAN);
            addTest(ResidualsDictionaries.DH);
            addTest(ResidualsDictionaries.SKEW);
            addTest(ResidualsDictionaries.KURT);
            addTest(ResidualsDictionaries.LB);
            addTest(ResidualsDictionaries.BP);
            addTest(ResidualsDictionaries.SEASLB);
            addTest(ResidualsDictionaries.SEASBP);
            addTest(ResidualsDictionaries.LB2);
            addTest(ResidualsDictionaries.BP2);
            addTest(ResidualsDictionaries.NRUNS);
            addTest(ResidualsDictionaries.LRUNS);
            addTest(ResidualsDictionaries.NUDRUNS);
            addTest(ResidualsDictionaries.LUDRUNS);
        }

        private void addTest(String k) {
            set(k, StatisticalTest.class, source -> source.getTests().get(k));
        }

        @Override
        public Class<Residuals> getSourceClass() {
            return Residuals.class;
        }

        @Override
        public int getPriority() {
            return 1;
        }
    }

    @ServiceProvider(InformationExtractor.class)
    public static class Dynamic extends DynamicMapping<Residuals, StatisticalTest> {

        public Dynamic() {
            super(null, v -> v.getTests());
        }

        @Override
        public Class<Residuals> getSourceClass() {
            return Residuals.class;
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

}
