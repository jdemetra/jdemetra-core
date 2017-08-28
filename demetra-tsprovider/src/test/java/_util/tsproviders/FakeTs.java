/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package _util.tsproviders;

import demetra.timeseries.TsFrequency;
import demetra.timeseries.simplets.TsData;
import demetra.tsprovider.OptionalTsData;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public final class FakeTs {

    @lombok.NonNull
    String label;
    @lombok.NonNull
    OptionalTsData data;
    @lombok.NonNull
    Map<String, String> meta = Collections.emptyMap();

    public static final FakeTs S1 = FakeTs.builder().label("Series 1").data(OptionalTsData.present(TsData.random(TsFrequency.MONTHLY, 1))).build();
    public static final FakeTs S2 = FakeTs.builder().label("Series 2").data(OptionalTsData.present(TsData.random(TsFrequency.MONTHLY, 2))).build();
    public static final FakeTs S3 = FakeTs.builder().label("Series 3").data(OptionalTsData.absent("Missing")).build();
}
