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
package demetra.timeseries;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TsFactory {

    public static final String 
            DESCRIPTION="@description", OWNER="@owner",
            SOURCE="@source", ID="@id", DATE="@timestamp",
            DOCUMENT="@document", SUMMARY="@summary", 
            NOTE="@note", TODO="@todo",
            ALGORITHM="@algorithm", 
            QUALITY="@quality";

    public Ts makeTs(TsMoniker moniker, TsInformationType info) {
        List<TsProvider> providers = TsProviderLoader.load();
        Optional<TsProvider> curprovider = providers.stream().filter(provider -> provider.getSource().equals(moniker.getSource())).findFirst();
        if (curprovider.isPresent()) {
            try {
                return curprovider.get().getTs(moniker, info);
            } catch (IOException ex) {
                Logger.getLogger(TsFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(TsFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public TsCollection makeTsCollection(TsMoniker moniker, TsInformationType info) {
        List<TsProvider> providers = TsProviderLoader.load();
        Optional<TsProvider> curprovider = providers.stream().filter(provider -> provider.getSource().equals(moniker.getSource())).findFirst();
        if (curprovider.isPresent()) {
            try {
                return curprovider.get().getTsCollection(moniker, info);
            } catch (IOException ex) {
                Logger.getLogger(TsFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(TsFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
}
