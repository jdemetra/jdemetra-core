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
package ec.jwsacruncher;

import ec.satoolkit.ISaSpecification;
import ec.tss.sa.ISaOutputFactory;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaManager;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.IOutput;
import ec.tstoolkit.utilities.LinearId;
import java.util.Collection;

/**
 *
 * @author Kristof Bayens
 */
public class SaBundle implements ISaBundle {

    private String name_;
    private Collection<SaItem> items_;

    public SaBundle(String name, Collection<SaItem> items) {
        name_ = name;
        items_ = items;
    }

    @Override
    public Collection<SaItem> getItems() {
        return items_;
    }

    @Override
    public void flush(ISaBatchFeedback fb) {
        for (ISaOutputFactory fac : SaManager.instance.getOutput()) {
            if (fac.isAvailable()) {
                IOutput<SaDocument<ISaSpecification>> output = fac.create();
                try {
                    LinearId id = new LinearId(name_);
                    output.start(id);
                    for (SaItem item : items_) {
                        output.process(item.toDocument());
                    }
                    output.end(id);
                    if (fb != null)
                        fb.showItem(output.getName(), "generated");
                } catch (Exception err) {
                    if (fb != null)
                        fb.showItem(output.getName(), "failed: "+err.getMessage());
                }
            }
        }
        for (SaItem item : items_) {
            item.compress();
        }
        System.gc();
    }
}
