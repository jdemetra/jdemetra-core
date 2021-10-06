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
package jdplus.sa.diagnostics;

import java.util.concurrent.atomic.AtomicReference;
import jdplus.sa.tests.FTest;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
@Development(status = Development.Status.Release)
public class ResidualSeasonalityTestsOptions {

    public static final int DEF_FTEST_LAST = 8, DEF_QSTEST_LAST = 0;
    public static final int DEF_QS_LAGS = 2;
    public static final FTest.Model DEF_FTEST_MODEL = FTest.Model.AR;
    
    private static final AtomicReference<ResidualSeasonalityTestsOptions> DEFAULT
            = new AtomicReference<ResidualSeasonalityTestsOptions>(builder().build());

    public static void setDefault(ResidualSeasonalityTestsOptions config) {
        DEFAULT.set(config);
    }

    public static ResidualSeasonalityTestsOptions getDefault() {
        return DEFAULT.get();
    }


    private int flast;
    @lombok.NonNull
    private FTest.Model modelForFTest;
    private int qslast, qsLags;

    public static Builder builder() {
        return new Builder()
                .flast(DEF_FTEST_LAST)
                .modelForFTest(DEF_FTEST_MODEL)
                .qsLags(DEF_QS_LAGS)
                .qslast(DEF_QSTEST_LAST);
    }

}
