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

import ec.tss.sa.ISaOutputFactory;

/**
 *
 * @author Jean Palate
 */
public class CsvOutputFactory implements ISaOutputFactory {
    //public static final CsvOutputFactory Default = new CsvOutputFactory();

    public static final String NAME = "Csv";
    private CsvOutputConfiguration config_;
    private boolean enabled_ = true;

    public CsvOutputFactory() {
        config_ = new CsvOutputConfiguration();
    }

    public CsvOutputFactory(CsvOutputConfiguration config) {
        config_ = config;
    }

    public CsvOutputConfiguration getConfiguration() {
        return config_;
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Csv output";
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
        CsvOutputConfiguration config = (CsvOutputConfiguration) obj;
        if (config != null) {
            try {
                config_ = (CsvOutputConfiguration) config.clone();
            }
            catch (Exception ex) {
                config_ = null;
            }
        }
    }

    @Override
    public CsvOutput create() {
        return new CsvOutput(config_);
    }
}
