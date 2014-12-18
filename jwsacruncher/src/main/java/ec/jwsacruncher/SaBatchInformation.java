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

import ec.tss.TsInformationType;
import ec.tss.sa.SaItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class SaBatchInformation implements ISaBatchInformation {
    private String name_;
    private SaItem[] items_;
    private int bundlesize_;
    private ISaBundle[] bundles_;

    public SaBatchInformation(int bundlesize) {
        bundlesize_ = bundlesize;
    }

    public String getName() {
        return name_;
    }
    public void setName(String value) {
        name_ = value;
    }

    public Iterable<SaItem> getItems() {
        return Arrays.asList(items_);
    }
    
    public void setItems(Iterable<SaItem> value) {
        Iterator<SaItem> iter = value.iterator();
        List<SaItem> list = new ArrayList<>();
        while(iter.hasNext())
            list.add(iter.next());
        items_ = list.toArray(new SaItem[list.size()]);
    }

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public Iterator<ISaBundle> start() {
        for (int i = 0; i< items_.length; ++i)
            items_[i].getTs().query(TsInformationType.Data);

        if (bundlesize_ == 0)
            bundles_ = new ISaBundle[] { new SaBundle(name_, Arrays.asList(items_)) };
        else {
            int n = items_.length;
            int nb = 1 + (n - 1) / bundlesize_;
            bundles_ = new ISaBundle[nb];
            for (int i = 0, j = 0; i < nb; ++i, j += bundlesize_) {
                String id = name_;
                if (id == null)
                    id = "";
                StringBuilder builder = new StringBuilder();
                builder.append(id).append('_').append(i + 1);

                String m = builder.toString();
                SaItem[] items = new SaItem[Math.min(bundlesize_, n - j)];
                for (int k = 0; k < items.length; ++k)
                    items[k] = items_[j + k];
                bundles_[i] = new SaBundle(m, Arrays.asList(items));
            }
        }
        Iterable<ISaBundle> bundles = Arrays.asList(bundles_);
        return bundles.iterator();
    }

    @Override
    public void close() 
    {
    }
}
