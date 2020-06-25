/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sa;

import demetra.timeseries.regression.modelling.ModellingContext;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
public final class SaItem {

    String name;

    private Map<String, String> meta = new HashMap<>();

    /**
     * Operational. Importance of this estimation
     */
    private final int priority;

    private final SaDefinition definition;

    /**
     * All information available after processing.
     * SA processors must be able to generate full estimations starting from
     * definitions
     */
    private volatile SaEstimation estimation;

    public SaItem(String name, SaDefinition definition) {
        this.name = name;
        this.definition = definition;
        this.priority = 0;
    }

    public SaItem(String name, SaDefinition definition, int priority) {
        this.name = name;
        this.definition = definition;
        this.priority = priority;
    }

    public SaItem withPriority(int priority) {
        SaItem nitem = new SaItem(name, definition, priority);
        nitem.estimation = this.estimation;
        return nitem;
    }

    public SaDefinition getDefinition() {
        return this.definition;
    }

    public boolean isProcessed() {
        return estimation != null;
    }

    /**
     * Process this item. The Processing is always executed, even if the item has already been
     * estimated. To avoid re-estimation, use getEstimation (which is not verbose by default)
     * @param verbose
     * @return 
     */
    public boolean process(boolean verbose) {
        synchronized (this) {
            estimation = SaManager.process(definition, ModellingContext.getActiveContext(), verbose);
        }
        return estimation != null;
    }

    /**
     * Gets the current estimation (executes it silently if not processed yet).
     * @return The current estimation
     */
    public SaEstimation getEstimation() {
        SaEstimation e = estimation;
        if (e == null) {
            synchronized (this) {
                e = estimation;
                if (e == null) {
                    e = SaManager.process(definition, ModellingContext.getActiveContext(), false);
                    estimation = e;
                }
            }
        }
        return e;
    }

    public int getPriority() {
        return this.priority;
    }
}
