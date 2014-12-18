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

import ec.tss.tsproviders.utils.OptionalTsData;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Mats Maggi
 */
public final class TswSeries {

    //final int index;
    final String fileName;
    final String name;
    final OptionalTsData data;

    public TswSeries(String fileName, String name, OptionalTsData data) {
        this.fileName = fileName;
        this.name = name;
        this.data = data;
    }

    @Deprecated
    public static TswSeries load(File repository, String fileName) throws IOException {
        List<TswSeries> all = TswFactory.getDefault().loadFile(repository.toPath().resolve(fileName));
        if (all.isEmpty()) {
            throw new IOException("Cannot read file");
        }
        return all.get(0);
    }
}
