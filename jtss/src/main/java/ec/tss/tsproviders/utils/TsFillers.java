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

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import java.util.Objects;

/**
 * Package-private supporting class for TsFiller.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
final class TsFillers {

    private TsFillers() {
        // static class
    }

    static final class NoOpFiller implements TsFiller {

        static final NoOpFiller INSTANCE = new NoOpFiller();

        @Override
        public boolean fillCollection(TsCollectionInformation info) {
            Objects.requireNonNull(info);
            return true;
        }

        @Override
        public boolean fillSeries(TsInformation info) {
            Objects.requireNonNull(info);
            return true;
        }
    }
}
