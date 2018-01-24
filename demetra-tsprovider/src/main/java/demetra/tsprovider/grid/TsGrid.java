/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package demetra.tsprovider.grid;

import demetra.timeseries.TsData;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsInformationType;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value(staticConstructor = "of")
public class TsGrid {

    @lombok.NonNull
    private String name;

    @lombok.NonNull
    private TsData data;

    @Nonnull
    public static TsGrid fromTs(@Nonnull Ts o) {
        return TsGrid.of(o.getName(), o.getData());
    }

    @Nonnull
    public static Ts toTs(@Nonnull TsGrid o) {
        Ts.Builder tsInfo = Ts.builder();
        tsInfo.name(o.getName());
        tsInfo.type(TsInformationType.All);
        tsInfo.data(o.getData());
        return tsInfo.build();
    }
}
