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
package demetra.sa;

import demetra.timeseries.TsInformationType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class SaItems {

    @lombok.NonNull
    @lombok.Builder.Default
    String name = "";

    @lombok.Singular("meta")
    @lombok.NonNull
    private Map<String, String> meta;

    @lombok.Singular("item")
    @lombok.NonNull
    private List<SaItem> items;

    public static SaItems empty() {
        return EMPTY;
    }

    public SaItems refresh(EstimationPolicy policy, TsInformationType info) {
        Builder builder = this.toBuilder()
                .clearItems();
        for (SaItem cur : items) {
            builder.item(cur.refresh(policy, info));
        }
        return builder.build();
    }

    public SaItems withMetadata(Map<String, String> meta) {
        return new SaItems(name, Collections.unmodifiableMap(meta), items);
    }

    public SaItems withItem(int pos, SaItem nitem) {
        List<SaItem> nitems = new ArrayList<>(items);
        nitems.set(pos, nitem);
        return new SaItems(name, meta, Collections.unmodifiableList(nitems));
    }

    public SaItems replaceItem(SaItem oitem, SaItem nitem) {
        List<SaItem> nitems = new ArrayList<>();
        for (SaItem cur : items) {
            if (cur == oitem) {
                nitems.add(nitem);
            } else {
                nitems.add(cur);
            }
        }
        return new SaItems(name, meta, Collections.unmodifiableList(nitems));
    }
    
    public SaItems replaceItems(Predicate<SaItem> test, UnaryOperator<SaItem> op) {
        List<SaItem> nitems = new ArrayList<>();
        for (SaItem cur : items) {
            if (test.test(cur)) {
                nitems.add(op.apply(cur));
            } else {
                nitems.add(cur);
            }
        }
        return new SaItems(name, meta, Collections.unmodifiableList(nitems));
    }
    

    public SaItems removeItems(SaItem... nitems) {
        List<SaItem> ritems = new ArrayList<>();
        Set<SaItem> sitems = new HashSet<>();
        for (SaItem item : nitems) {
            sitems.add(item);
        }
        for (SaItem item : items) {
            if (!sitems.contains(item)) {
                ritems.add(item);
            }
        }
        return new SaItems(name, meta, Collections.unmodifiableList(ritems));
    }

    public SaItems addItems(SaItem... nitems) {
        List<SaItem> ritems = new ArrayList<>(items);
        for (SaItem item : nitems) {
            ritems.add(item);
        }
        return new SaItems(name, meta, Collections.unmodifiableList(ritems));
    }

    private static final SaItems EMPTY = new SaItems("", Collections.emptyMap(),
            Collections.emptyList());

    public boolean isEmpty() {
        return items.isEmpty();
    }

}
