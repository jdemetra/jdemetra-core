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
package demetra.modelling.io.information;

import demetra.information.InformationSet;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import java.util.Collections;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TsContextVariableMapping {

    public static final String NAME_LEGACY = "name", ID = "id",
            EFFECT_LEGACY = "effect",
            FIRSTLAG = "firstlag",
            LASTLAG = "lastlag";

    public InformationSet writeLegacy(Variable<TsContextVariable> v, boolean verbose) {
        TsContextVariable var = v.getCore();
        InformationSet info = new InformationSet();
        info.set(NAME_LEGACY, var.getId());
        if (verbose || var.getFirstLag() != 0) {
            info.add(FIRSTLAG, var.getFirstLag());
        }
        if (verbose || var.getLastLag() != 0) {
            info.add(LASTLAG, var.getLastLag());
        }
        String effect = v.attribute("regeffect");
        if (effect == null) {
            effect = "Undefined";
        }
        if (verbose || !effect.equals("Undefined")) {
            info.add(EFFECT_LEGACY, effect);
        }
        return info;
    }

    public InformationSet write(Variable<TsContextVariable> v, boolean verbose) {
        TsContextVariable var = v.getCore();
        InformationSet info = new InformationSet();
        info.set(ID, var.getId());
        if (verbose || var.getFirstLag() != 0) {
            info.add(FIRSTLAG, var.getFirstLag());
        }
        if (verbose || var.getLastLag() != 0) {
            info.add(LASTLAG, var.getLastLag());
        }
        return info;
    }

    public Variable<TsContextVariable> readLegacy(InformationSet info) {

        String id = info.get(NAME_LEGACY, String.class);
        Integer FL = info.get(FIRSTLAG, Integer.class);
        int fl = FL == null ? 0 : FL;
        Integer LL = info.get(LASTLAG, Integer.class);
        int ll = LL == null ? 0 : LL;
        String effect = info.get(EFFECT_LEGACY, String.class);
        if (effect == null) {
            effect = "Undefined";
        }
        TsContextVariable var = new TsContextVariable(id, fl, ll);
        return Variable.variable(id, var, Collections.singletonMap("regeffect", effect));
    }

    public TsContextVariable read(InformationSet info) {

        String id = info.get(ID, String.class);
        Integer FL = info.get(FIRSTLAG, Integer.class);
        int fl = FL == null ? 0 : FL;
        Integer LL = info.get(LASTLAG, Integer.class);
        int ll = LL == null ? 0 : LL;
        return new TsContextVariable(id, fl, ll);
    }
}
