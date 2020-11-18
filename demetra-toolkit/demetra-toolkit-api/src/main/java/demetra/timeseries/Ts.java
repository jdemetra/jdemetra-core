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
package demetra.timeseries;

import java.util.Map;
import nbbrd.design.LombokWorkaround;
import internal.timeseries.util.TsDataBuilderUtil;
import internal.timeseries.LombokHelper;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class Ts implements TsResource<TsData> {

    @lombok.NonNull
    private TsMoniker moniker;

    @lombok.NonNull
    private TsInformationType type;

    @lombok.NonNull
    private String name;

    @lombok.NonNull
    @lombok.Singular("meta")
    private Map<String, String> meta;

    @lombok.NonNull
    private TsData data;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.UserDefined)
                .name("")
                .data(TsDataBuilderUtil.NO_DATA);
    }

    public static class Builder implements TsResource {

        @Override
        public TsMoniker getMoniker() {
            return moniker;
        }

        @Override
        public TsInformationType getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Map<String, String> getMeta() {
            return LombokHelper.getMap(meta$key, meta$value);
        }

        @Override
        public TsData getData() {
            return data;
        }
    }
}
