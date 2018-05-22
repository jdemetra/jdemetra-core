/*
 * Copyright 2018 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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

import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TsFactoryBypass {

    public Ts series(String name) {
        return new Ts.Master(name);
    }

    public Ts series(String name, TsMoniker moniker) {
        return new Ts.Master(name, moniker);
    }

    public Ts series(String name, TsMoniker moniker, MetaData md, TsData d) {
        return new Ts.Master(name, moniker, md, d);
    }

    public TsCollection col(@Nullable String name) {
        return new TsCollection(name);
    }

    public TsCollection col(@Nullable String name, @Nonnull TsMoniker moniker) {
        return new TsCollection(name, moniker);
    }

    public TsCollection col(@Nullable String name, @Nonnull TsMoniker moniker, @Nullable MetaData md, @Nullable Iterable<Ts> ts) {
        return new TsCollection(name, moniker, md, ts);
    }
}
