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
package demetra.tsprovider.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Tool that loads/stores values from/to a key-value structure. It provides a
 * best-effort retrieval behavior where a failure returns a default value
 * instead of an error. All implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 1.0.0
 * @param <S>
 * @param <P>
 */
@ThreadSafe
public interface IParam<S extends IConfig, P> {

    @Nonnull
    P defaultValue();

    @Nonnull
    P get(@Nonnull S config);

    void set(@Nonnull IConfig.Builder<?, S> builder, @Nullable P value);
}
