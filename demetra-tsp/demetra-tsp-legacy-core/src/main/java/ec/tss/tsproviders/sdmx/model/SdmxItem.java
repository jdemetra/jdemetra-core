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

package ec.tss.tsproviders.sdmx.model;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.Map.Entry;

/**
 *
 * @author Philippe Charles
 */
public abstract class SdmxItem {

    private static final Joiner.MapJoiner ID_JOINER = Joiner.on(", ").withKeyValueSeparator("=");
    public final ImmutableList<? extends Entry<String, String>> key;
    public final ImmutableList<? extends Entry<String, String>> attributes;
    public final String id;

    public SdmxItem(ImmutableList<? extends Entry<String, String>> key, ImmutableList<? extends Entry<String, String>> attributes) {
        this.key = key;
        this.attributes = attributes;
        this.id = ID_JOINER.join(key);
    }
}
