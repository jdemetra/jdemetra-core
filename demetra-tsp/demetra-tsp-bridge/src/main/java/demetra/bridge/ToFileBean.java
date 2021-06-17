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
package demetra.bridge;

import demetra.tsprovider.FileBean;
import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ToFileBean implements FileBean {

    public static @NonNull FileBean toFileBean(ec.tss.tsproviders.@NonNull IFileBean delegate) {
        return delegate instanceof FromFileBean
                ? ((FromFileBean) delegate).getDelegate()
                : new ToFileBean(delegate);
    }

    @lombok.Getter
    @lombok.NonNull
    @lombok.experimental.Delegate
    private final ec.tss.tsproviders.IFileBean delegate;
}
