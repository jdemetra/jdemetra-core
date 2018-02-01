/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tstoolkit.jdr.mapping;

import demetra.information.InformationMapping;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.stats.RunsTestKind;
import ec.tstoolkit.stats.TestofUpDownRuns;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class ResidualsInfo {

    public final String RESIDUALS = "residuals",
            RES_DATA = "res",
            RES_STDERR = "stderr",
            RES_MEAN = "mean",
            RES_SKEWNESS = "skewness",
            RES_KURTOSIS = "kurtosis",
            RES_DH = "dh",
            RES_LB = "lb",
            RES_LB2 = "lb2",
            RES_SEASLB = "seaslb",
            RES_BP = "bp",
            RES_BP2 = "bp2",
            RES_SEASBP = "seasbp",
            RES_UD_NUMBER = "nruns",
            RES_UD_LENGTH = "lruns";

    // MAPPING
    public static InformationMapping<NiidTests> getMapping() {
        return MAPPING;
    }

    private static final InformationMapping<NiidTests> MAPPING = new InformationMapping<>(NiidTests.class);

    static {

        MAPPING.set(RES_DATA, double[].class, source -> source.getStatistics().observations());
        MAPPING.set(RES_MEAN, StatisticalTest.class,
                source -> StatisticalTest.create(source.getMeanTest()));
        MAPPING.set(RES_SKEWNESS, StatisticalTest.class,
                source -> StatisticalTest.create(source.getSkewness()));
        MAPPING.set(RES_KURTOSIS, StatisticalTest.class,
                source -> StatisticalTest.create(source.getKurtosis()));
        MAPPING.set(RES_DH, StatisticalTest.class,
                source -> StatisticalTest.create(source.getNormalityTest()));
        MAPPING.set(RES_LB, StatisticalTest.class,
                source -> StatisticalTest.create(source.getLjungBox()));
        MAPPING.set(RES_LB2, StatisticalTest.class,
                source -> StatisticalTest.create(source.getLjungBoxOnSquare()));
        MAPPING.set(RES_SEASLB, StatisticalTest.class,
                source -> StatisticalTest.create(source.getSeasonalLjungBox()));
        MAPPING.set(RES_BP, StatisticalTest.class,
                source -> StatisticalTest.create(source.getBoxPierce()));
        MAPPING.set(RES_BP2, StatisticalTest.class,
                source -> StatisticalTest.create(source.getBoxPierceOnSquare()));
        MAPPING.set(RES_SEASBP, StatisticalTest.class,
                source -> StatisticalTest.create(source.getSeasonalBoxPierce()));
        MAPPING.set(RES_UD_NUMBER, StatisticalTest.class,
                source -> {
                    TestofUpDownRuns ud = source.getUpAndDownRuns();
                    if (ud == null) {
                        return null;
                    }
                    ud.setKind(RunsTestKind.Number);
                    return StatisticalTest.create(ud);
                });
        MAPPING.set(RES_UD_LENGTH, StatisticalTest.class,
                source -> {
                    TestofUpDownRuns ud = source.getUpAndDownRuns();
                    if (ud == null) {
                        return null;
                    }
                    ud.setKind(RunsTestKind.Length);
                    return StatisticalTest.create(ud);
                });
    }
}
