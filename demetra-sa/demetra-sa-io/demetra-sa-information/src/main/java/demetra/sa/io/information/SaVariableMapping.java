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
package demetra.sa.io.information;

import demetra.information.InformationSet;
import demetra.sa.SaVariable;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SaVariableMapping {

    public final String NAME = "name",
            EFFECT = "effect",
            FIRSTLAG = "firstlag",
            LASTLAG = "lastlag";

    public void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, NAME), String.class);
        dic.put(InformationSet.item(prefix, EFFECT), String.class);
        dic.put(InformationSet.item(prefix, FIRSTLAG), Integer.class);
        dic.put(InformationSet.item(prefix, LASTLAG), Integer.class);
    }

    public InformationSet write(Variable<TsContextVariable> var, boolean verbose) {
        InformationSet info = new InformationSet();
        info.add(NAME, var.getName());
        if (verbose || var.getCore().getFirstLag() != 0) {
            info.add(FIRSTLAG, var.getCore().getFirstLag());
        }
        if (verbose || var.getCore().getLastLag() != 0) {
            info.add(LASTLAG, var.getCore().getLastLag());
        }
        String effect = var.attribute(SaVariable.REGEFFECT);
        if (effect != null) {
            info.add(EFFECT, effect);
        }
        return info;
    }

    public Variable<TsContextVariable> read(InformationSet info) {
        String name = info.get(NAME, String.class);
        int l0 = 0, l1 = 0;
        Integer flag = info.get(FIRSTLAG, Integer.class);
        if (flag != null) {
            l0 = flag;
        }
        Integer llag = info.get(LASTLAG, Integer.class);
        if (llag != null) {
            l1 = llag;
        }
        String effect = info.get(EFFECT, String.class);
        TsContextVariable v = new TsContextVariable(name, l0, l1);
        return Variable.<TsContextVariable>builder()
                .core(v)
                .name(name)
                .attribute(SaVariable.REGEFFECT, effect)
                .build();
    }

}
