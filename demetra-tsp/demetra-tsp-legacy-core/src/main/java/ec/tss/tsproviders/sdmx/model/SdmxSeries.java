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

import com.google.common.collect.ImmutableList;
import ec.tss.tsproviders.sdmx.engine.TimeFormat;
import ec.tss.tsproviders.utils.OptionalTsData;
import java.util.Map;

/**
 *
 * @author Philippe Charles
 */
public class SdmxSeries extends SdmxItem {

    public final TimeFormat timeFormat;
    public final OptionalTsData data;

    public SdmxSeries(ImmutableList<? extends Map.Entry<String, String>> key, ImmutableList<? extends Map.Entry<String, String>> attributes, TimeFormat timeFormat, OptionalTsData data) {
        super(key, attributes);
        this.timeFormat = timeFormat;
        this.data = data;
    }
}
