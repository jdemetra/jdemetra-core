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
package demetra.tsprovider;

import demetra.timeseries.TsData;
import internal.tsprovider.util.TsDataBuilderUtil;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public final class Ts {

    @lombok.NonNull
    private TsMoniker moniker;

    @lombok.NonNull
    private TsInformationType type;

    private String name;

    @lombok.NonNull
    @lombok.Singular("meta")
    private Map<String, String> metaData;

    @lombok.NonNull
    @lombok.Builder.Default
    private TsData data = TsDataBuilderUtil.NO_DATA;

    public static class Builder {

        public TsMoniker getMoniker() {
            return moniker;
        }

        public TsInformationType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getMetaData() {
            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < metaData$key.size(); i++) {
                result.put(metaData$key.get(i), metaData$value.get(i));
            }
            return result;
        }

        public TsData getData() {
            return data;
        }
    }
}
