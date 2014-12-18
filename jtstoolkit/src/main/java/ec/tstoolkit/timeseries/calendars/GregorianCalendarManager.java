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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.utilities.DefinitionMap;
import ec.tstoolkit.utilities.NameManager;
import ec.tstoolkit.utilities.WeightedItem;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Kristof Bayens
 */
public class GregorianCalendarManager extends NameManager<IGregorianCalendarProvider> {

    public static final String DEF = "Default";
    public static final String CAL = "Calendar_";
    private static final DefaultGregorianCalendarProvider def_= new DefaultGregorianCalendarProvider();
    
    public GregorianCalendarManager() {
        super(IGregorianCalendarProvider.class, CAL, null);
        set(DEF, def_);
        resetDirty();
        lock(DEF);
    }
    
    public static IGregorianCalendarProvider getDefault(String name){
        if (name.equals(DEF))
            return def_;
        else
            return null;
    }

    public boolean isEmpty() {
        return getCount() <= 1;
    }

    public static DefinitionMap defaultDefinitionMap(String code, HashMap<IGregorianCalendarProvider, String> dic) {
        return ProcessingContext.getActiveContext().getGregorianCalendars().buildDefinitionMap(code, dic);
    }

    public DefinitionMap buildDefinitionMap(String code, HashMap<IGregorianCalendarProvider, String> dic) {
        DefinitionMap map = new DefinitionMap(code);

        String[] calendars = getNames();
        for (String s : calendars) {
            dic.put(get(s), s);
        }

        for (Entry<IGregorianCalendarProvider, String> item : dic.entrySet()) {
            if (item.getKey() instanceof NationalCalendarProvider) {
                map.add(item.getValue(), null);
            }
            else if (item.getKey() instanceof ChainedGregorianCalendarProvider) {

                ChainedGregorianCalendarProvider cc = (ChainedGregorianCalendarProvider) item.getKey();
                map.add(item.getValue(), new String[]{cc.first, cc.second});
            }
            else if (item.getKey() instanceof CompositeGregorianCalendarProvider) {
                CompositeGregorianCalendarProvider wc = (CompositeGregorianCalendarProvider) item.getKey();
                String[] w = new String[wc.getCount()];
                int i = 0;
                for (WeightedItem<String> wi : wc.items()) {
                    w[i++] = wi.item;
                }
                map.add(item.getValue(), w);
                continue;
            }
        }

        return map;
    }
}
