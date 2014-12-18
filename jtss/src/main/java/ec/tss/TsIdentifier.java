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

import com.google.common.base.Strings;

/**
 *
 * @author pcuser
 */
public class TsIdentifier implements ITsIdentified {

    private final String name;
    private final TsMoniker moniker;

    public TsIdentifier(ITsIdentified i) {
        name = i.getName();
        moniker = i.getMoniker();
    }

    public TsIdentifier(String name, TsMoniker moniker) {
        this.name = Strings.nullToEmpty(name);
        this.moniker = moniker;
    }

    @Override
    public TsMoniker getMoniker() {
        return moniker;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " - " + moniker;
    }
}
