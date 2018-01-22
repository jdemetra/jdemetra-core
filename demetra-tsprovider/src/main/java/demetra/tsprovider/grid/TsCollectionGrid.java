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
package demetra.tsprovider.grid;

import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsInformationType;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class TsCollectionGrid {

    @lombok.NonNull
    private GridLayout layout;

    @lombok.NonNull
    @lombok.Singular
    private List<TsGrid> items;

    @Nonnull
    public static TsCollectionGrid fromTsCollection(@Nonnull TsCollection o) {
        TsCollectionGrid.Builder result = TsCollectionGrid.builder().layout(GridLayout.UNKNOWN);
        o.getItems().forEach(ts -> result.item(TsGrid.fromTs(ts)));
        return result.build();
    }

    @Nonnull
    public static TsCollection toTsCollection(@Nonnull TsCollectionGrid o) {
        TsCollection.Builder result = TsCollection.builder().type(TsInformationType.All);
        o.getItems().forEach(ts -> result.item(TsGrid.toTs(ts)));
        return result.build();
    }
}
