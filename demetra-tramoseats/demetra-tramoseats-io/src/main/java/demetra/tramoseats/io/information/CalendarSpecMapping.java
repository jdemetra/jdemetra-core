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
package demetra.tramoseats.io.information;

import demetra.information.InformationSet;
import demetra.tramo.CalendarSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class CalendarSpecMapping {

   public final String TD = "td", EASTER = "easter";

    public void fillDictionary(String prefix, Map<String, Class> dic) {
         EasterSpecMapping.fillDictionary(InformationSet.item(prefix, EASTER), dic);
         TradingDaysSpecMapping.fillDictionary(InformationSet.item(prefix, TD), dic);
   }
    
    public InformationSet write(CalendarSpec spec, boolean verbose) {
        if (!verbose && spec.isDefault()) {
            return null;
        }
        InformationSet specInfo = new InformationSet();
        if (verbose || !spec.getTradingDays().isDefault()) {
            InformationSet tdinfo = TradingDaysSpecMapping.write(spec.getTradingDays(), verbose);
            if (tdinfo != null) {
                specInfo.add(TD, tdinfo);
            }
        }
        if (verbose || !spec.getEaster().isDefault()) {
            InformationSet einfo = EasterSpecMapping.write(spec.getEaster(), verbose);
            if (einfo != null) {
                specInfo.add(EASTER, einfo);
            }
        }
        return specInfo;
    }

    public CalendarSpec read(InformationSet info) {
        if (info == null)
            return CalendarSpec.DEFAULT;
       CalendarSpec.Builder builder = CalendarSpec.builder();
              
       return builder
               .tradingDays(TradingDaysSpecMapping.read(info.getSubSet(TD)))
               .easter(EasterSpecMapping.read(info.getSubSet(TD)))
               .build();
    }
    
    
}
