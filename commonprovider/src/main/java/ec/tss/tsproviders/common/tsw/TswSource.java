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
package ec.tss.tsproviders.common.tsw;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Mats Maggi
 */
public final class TswSource {

    final File repository;
    final List<TswSeries> items;

    public TswSource(File repository, List<TswSeries> items) {
        this.repository = repository;
        this.items = items;
    }

    @Deprecated
    public static TswSource load(File repository) throws IOException {
        return TswFactory.getDefault().load(repository.toPath());
    }
}
