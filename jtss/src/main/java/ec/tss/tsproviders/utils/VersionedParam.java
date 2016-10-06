/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tss.tsproviders.utils;

import javax.annotation.Nonnull;

/**
 * Specialized param that defines a version.
 *
 * @author Philippe Charles
 * @since 2.2.0
 * @param <S>
 * @param <P>
 */
public interface VersionedParam<S extends IConfig, P> extends IParam<S, P> {

    @Nonnull
    String getVersion();

    @Nonnull
    static <S extends IConfig, P> VersionedParam<S, P> of(@Nonnull String version, @Nonnull IParam<S, P> param) {
        return new VersionedParam<S, P>() {
            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public P defaultValue() {
                return param.defaultValue();
            }

            @Override
            public P get(S config) {
                return param.get(config);
            }

            @Override
            public void set(IConfig.Builder<?, S> builder, P value) {
                param.set(builder, value);
            }
        };
    }
}
