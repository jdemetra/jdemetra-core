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

package ec.tss;

import ec.tstoolkit.information.InformationConverter;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.regression.TsVariable;
import static ec.tstoolkit.timeseries.regression.TsVariables.LINKER;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.IDynamicObject;

/**
 *
 * @author Jean Palate
 */
public class DynamicTsVariable extends TsVariable implements IDynamicObject {

    private final TsMoniker moniker_;

    private static TsData fromMoniker(TsMoniker moniker) {
        Ts s = TsFactory.instance.createTs(null, moniker, TsInformationType.Data);
        if (s.hasData() == TsStatus.Undefined) {
            s.load(TsInformationType.Data);
        }
        return s.getTsData();

    }

    public DynamicTsVariable(String desc, TsMoniker moniker, TsData data) {
        super(desc, data);
        moniker_ = moniker;
    }

    public DynamicTsVariable(String desc, TsMoniker moniker) {
        super(desc, fromMoniker(moniker));
        moniker_ = moniker;
    }

    public TsMoniker getMoniker() {
        return moniker_;
    }

    @Override
    public boolean refresh() {
        TsData data = fromMoniker(moniker_);
        if (data == null) {
            return false;
        }
        setData(data);
        return true;
    }
    
    // Dynamic ts variable
    
    private static class DynamicTsVariableConverter implements InformationConverter<DynamicTsVariable> {

        @Override
        public DynamicTsVariable decode(InformationSet info) {
            TsMoniker moniker = info.get(MONIKER, TsMoniker.class);
            TsData data = info.get(DATA, TsData.class);
            String desc = info.get(DESC, String.class);
            return new DynamicTsVariable(desc, moniker, data);
        }

        @Override
        public InformationSet encode(DynamicTsVariable t, boolean verbose) {
            InformationSet info = new InformationSet();
            info.set(MONIKER, t.getMoniker());
            info.set(DATA, t.getTsData());
            info.set(DESC, t.getDescription());
            return info;
        }

        @Override
        public Class<DynamicTsVariable> getInformationType() {
            return DynamicTsVariable.class;
        }

        @Override
        public String getTypeDescription() {
            return TYPE;
        }
        static final String TYPE = "dynamic time series", MONIKER = "moniker", DATA = "data", DESC = "description";
    };
    
    private static final InformationConverter<DynamicTsVariable> tsvar = new DynamicTsVariableConverter();

    public static void register(){
        LINKER.register(DynamicTsVariableConverter.TYPE, DynamicTsVariable.class, tsvar);
     }
}
