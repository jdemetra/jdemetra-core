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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationConverter;
import ec.tstoolkit.information.InformationLinker;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.DefaultNameValidator;
import ec.tstoolkit.utilities.INameValidator;
import ec.tstoolkit.utilities.NameManager;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class TsVariables extends NameManager<ITsVariable> {

    public static final InformationLinker<ITsVariable> LINKER = new InformationLinker<>();
    public static final String X = "x_";

    public TsVariables() {
        super(ITsVariable.class, X, new DefaultNameValidator(".+-*/"));
    }

    public TsVariables(String prefix, INameValidator validator) {
        super(ITsVariable.class, prefix, validator);
    }

    public boolean isEmpty() {
        return getCount() < 1;
    }

    public TsDomain common(TsFrequency freq) {
        TsDomain all = null;
        for (ITsVariable var : this.variables()) {
            TsFrequency fcur = var.getDefinitionFrequency();
            if (fcur == TsFrequency.Undefined || fcur == freq) {
                TsDomain cur = var.getDefinitionDomain();
                all = TsDomain.and(all, cur);
            }
        }
        return all;
    }

    public TsFrequency[] frequencies() {
        TsFrequency[] all = new TsFrequency[TsFrequency.values().length];
        int idx = 0;
        for (ITsVariable var : this.variables()) {
            TsFrequency fcur = var.getDefinitionFrequency();
            if (fcur != TsFrequency.Undefined) {
                int i = 0;
                while (i < idx) {
                    if (all[i] == fcur) {
                        break;
                    }
                    ++i;
                }
                if (i == idx) {
                    all[idx++] = fcur;
                }
            }
        }
        return Arrays.copyOf(all, idx);
    }

    @Override
    public InformationSet write(boolean verbose) {
        synchronized (LINKER) {
            InformationSet info = new InformationSet();
            String[] names = getNames();
            for (int i = 0; i < names.length; ++i) {
                ITsVariable var = get(names[i]);
                InformationSet subset = LINKER.encode(var, verbose);
                if (subset != null) {
                    info.set(names[i], subset);
                }
            }
            return info;
        }
    }

    @Override
    public boolean read(InformationSet info) {
        synchronized (LINKER) {
            List<Information<InformationSet>> sel = info.select(InformationSet.class);
            for (Information<InformationSet> item : sel) {
                ITsVariable var = LINKER.decode(item.value);
                if (var != null) {
                    set(item.name, var);
                }
            }
            return true;
        }
    }

}
