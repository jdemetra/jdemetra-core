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
class CalendarSpecMapping {
    
    final String TD = "td", EASTER = "easter";
    
    void fillDictionary(String prefix, Map<String, Class> dic) {
        EasterSpecMapping.fillDictionary(InformationSet.item(prefix, EASTER), dic);
        TradingDaysSpecMapping.fillDictionary(InformationSet.item(prefix, TD), dic);
    }
    
    void writeLegacy(InformationSet regInfo, CalendarSpec spec, boolean verbose) {
        TradingDaysSpecMapping.writeLegacy(regInfo, spec.getTradingDays(), verbose);
        EasterSpecMapping.writeLegacy(regInfo, spec.getEaster(), verbose);
    }
    
    CalendarSpec readLegacy(InformationSet regInfo) {
        if (regInfo == null) {
            return CalendarSpec.DEFAULT;
        }
        CalendarSpec.Builder builder = CalendarSpec.builder();
        return builder
                .tradingDays(TradingDaysSpecMapping.readLegacy(regInfo))
                .easter(EasterSpecMapping.readLegacy(regInfo))
                .build();
    }
    
    InformationSet write(CalendarSpec spec, boolean verbose) {
        
        InformationSet tinfo = TradingDaysSpecMapping.write(spec.getTradingDays(), verbose);
        InformationSet einfo = EasterSpecMapping.write(spec.getEaster(), verbose);
        if (einfo == null && tinfo == null) {
            return null;
        }
        InformationSet cinfo = new InformationSet();
        if (tinfo != null) {
            cinfo.set(TD, tinfo);
        }
        if (einfo != null) {
            cinfo.set(EASTER, einfo);
        }
        return cinfo;        
    }
    
    CalendarSpec read(InformationSet cInfo) {
        if (cInfo == null) {
            return CalendarSpec.DEFAULT;
        }
        CalendarSpec.Builder builder = CalendarSpec.builder();
        return builder
                .tradingDays(TradingDaysSpecMapping.read(cInfo.getSubSet(TD)))
                .easter(EasterSpecMapping.read(cInfo.getSubSet(EASTER)))
                .build();
    }
}
