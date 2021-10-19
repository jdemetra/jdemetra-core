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
package demetra.x13.io.information;

import demetra.information.InformationSet;
import demetra.regarima.OrderSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class OrderSpecMapping {

    final String TYPE = "type", REGULAR = "regular", SEASONAL = "seasonal";

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, TYPE), String.class);
        dic.put(InformationSet.item(prefix, REGULAR), Integer.class);
        dic.put(InformationSet.item(prefix, SEASONAL), Integer.class);
    }

    InformationSet write(OrderSpec spec, boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(TYPE, spec.getType().name());
        if (verbose || spec.getRegular() != 0) {
            info.add(REGULAR, spec.getRegular());
        }
        if (verbose || spec.getSeasonal() != 0) {
            info.add(SEASONAL, spec.getSeasonal());
        }
        return info;
    }

    OrderSpec read(InformationSet info) {
        if (info == null) {
            return null;
        }
        String type_ = info.get(TYPE, String.class);
        Integer regular_ = info.get(REGULAR, Integer.class);
        Integer seasonal_ = info.get(SEASONAL, Integer.class);

        OrderSpec.Type type = type_ == null ? OrderSpec.Type.Max : OrderSpec.Type.valueOf(type_);
        int r = regular_ == null ? 2 : regular_;
        int s = seasonal_ == null ? 1 : seasonal_;

        return OrderSpec.builder()
                .type(type)
                .regular(r)
                .seasonal(s)
                .build();
    }

}
