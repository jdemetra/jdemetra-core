/*
 * Copyright 2013 National Bank of Belgium
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
package internal.io;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class JaxbUtil {

    public <X> void forSingle(@Nullable X item, @Nonnull Consumer<? super X> action) {
        Objects.requireNonNull(action, "action");
        if (item != null) {
            action.accept(item);
        }
    }

    public <X> void forEach(@Nullable X[] array, @Nonnull Consumer<? super X> action) {
        Objects.requireNonNull(action, "action");
        if (array != null) {
            for (X o : array) {
                action.accept(o);
            }
        }
    }
}
