/*
 * Copyright 2017 National Bank of Belgium
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
package rssf;

import demetra.msts.MstsMapping;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class CompositeModel {

    private MstsMapping mapping;
    private final List<ModelItem> items = new ArrayList<>();
    private final List<ModelEquation> equations = new ArrayList<>();

    public void add(ModelItem item) {
        this.items.add(item);
    }

    public void add(ModelEquation eq) {
        this.equations.add(eq);
    }

    MstsMapping getMapping() {
        return mapping;
    }

    public void build() {
        mapping = new MstsMapping();
        for (ModelItem item : items) {
            item.addTo(mapping);
        }
        for (ModelEquation eq : equations) {
            eq.addTo(mapping);
        }
    }

}
