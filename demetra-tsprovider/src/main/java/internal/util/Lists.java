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
package internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Lists {

    @Nonnull
    public <T> List<T> immutableCopyOf(@Nonnull Iterable<T> input) {
        if (input instanceof Collection) {
            return immutableCopyOf((Collection<T>) input);
        }
        List<T> result = new ArrayList<>();
        input.forEach(result::add);
        return Collections.unmodifiableList(result);
    }

    @Nonnull
    public <T> List<T> immutableCopyOf(@Nonnull T... input) {
        switch (input.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return Collections.singletonList(input[0]);
            default:
                List<T> result = new ArrayList<>();
                Collections.addAll(result, input);
                return Collections.unmodifiableList(result);
        }
    }

    @Nonnull
    public <T> List<T> immutableCopyOf(@Nonnull Collection<T> input) {
        switch (input.size()) {
            case 0:
                return Collections.emptyList();
            case 1:
                return Collections.singletonList(input.iterator().next());
            default:
                return Collections.unmodifiableList(new ArrayList<>(input));
        }
    }
}
