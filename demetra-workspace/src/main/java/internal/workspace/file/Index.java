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
package internal.workspace.file;

import demetra.workspace.WorkspaceFamily;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
class Index {

    @lombok.NonNull
    @lombok.With
    String name;

    @lombok.NonNull
    @lombok.Singular
    Map<Key, Value> items;

    @NonNull
    public Index withItem(@NonNull Key key, @NonNull Value value) {
        return toBuilder().item(key, value).build();
    }

    @NonNull
    public Index withoutItem(@NonNull Key key) {
        Index.Builder result = Index.builder().name(getName());
        getItems().forEach((k, v) -> {
            if (!k.equals(key)) {
                result.item(k, v);
            }
        });
        return result.build();
    }

    @lombok.Value
    public static class Key {

        @lombok.NonNull
        WorkspaceFamily family;

        @lombok.NonNull
        String id;
    }

    @lombok.Value
    public static class Value {

        String label;
        
        boolean readOnly;

        String comments;
    }
}
