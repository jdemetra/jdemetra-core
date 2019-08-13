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

package ec.tss.sa.output;

import ec.satoolkit.ISaSpecification;
import ec.tss.sa.ISaOutputFactory;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.IOutput;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Kristof Bayens
 */
@ServiceProvider(ISaOutputFactory.class)
public class TxtOutputFactory implements ISaOutputFactory {
    //public static final TxtOutputFactory Default = new TxtOutputFactory();

    public static final String NAME = "Txt";
    private TxtOutputConfiguration config_;
    private boolean enabled_ = true;

    public TxtOutputFactory() {
        config_ = new TxtOutputConfiguration();
    }

    public TxtOutputFactory(TxtOutputConfiguration config) {
        config_ = config;
    }

    public TxtOutputConfiguration getConfiguration() {
        return config_;
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Txt output";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled_;
    }

    @Override
    public void setEnabled(boolean enabled) {
        enabled_ = enabled;
    }

    @Override
    public Object getProperties() {
        try {
            return config_.clone();
        }
        catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void setProperties(Object obj) {
        TxtOutputConfiguration config = (TxtOutputConfiguration) obj;
        if (config != null) {
            try {
                config_ = (TxtOutputConfiguration) config.clone();
            }
            catch (Exception ex) {
                config_ = null;
            }
        }
    }

    @Override
    public IOutput<SaDocument<ISaSpecification>> create() {
        return new TxtOutput(config_);
    }
}
