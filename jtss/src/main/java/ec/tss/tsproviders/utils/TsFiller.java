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
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Defines the ability to fill the content of a time series or a collection.
 * Note that implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface TsFiller {

    /**
     * Fills a collection info according to its request.
     *
     * @param info the collection info to fill
     * @return true if the process performed properly, false otherwise
     */
    boolean fillCollection(@Nonnull TsCollectionInformation info);

    /**
     * Fills a time series info according to its request.
     *
     * @param info the time series info to fill
     * @return true if the process performed properly, false otherwise
     */
    boolean fillSeries(@Nonnull TsInformation info);

    /**
     * Creates a new instance of TsFiller that does nothing.
     *
     * @return a non-null instance
     */
    @Nonnull
    static TsFiller noOp() {
        return TsFillers.NoOpFiller.INSTANCE;
    }
}
