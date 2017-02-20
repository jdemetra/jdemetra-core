/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tss.sa.diagnostics;

import ec.tss.sa.ISaDiagnosticsFactory;
import ec.tss.sa.SaManager;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = ISaDiagnosticsFactory.class)
public class AdvancedResidualSeasonalityDiagnosticsFactory implements ISaDiagnosticsFactory {
    
    static final String NAME="Residual seasonality tests", DESC="Residual seasonality tests";

    private AdvancedResidualSeasonalityDiagnosticsConfiguration config_;

    public AdvancedResidualSeasonalityDiagnosticsFactory() {
        config_ = new AdvancedResidualSeasonalityDiagnosticsConfiguration();
    }

    public AdvancedResidualSeasonalityDiagnosticsFactory(AdvancedResidualSeasonalityDiagnosticsConfiguration config) {
        config_ = config;
    }

    @Override
    public Scope getScope() {
        return Scope.Final; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getOrder() {
        return 0; //To change body of generated methods, choose Tools | Templates.
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
        return DESC;
    }

    @Override
    public boolean isEnabled() {
        return config_.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        config_.setEnabled(enabled);
    }

    @Override
    public Object getProperties() {
        return config_.clone();
    }

    @Override
    public void setProperties(Object obj) {
        if (obj instanceof AdvancedResidualSeasonalityDiagnosticsConfiguration) {
            AdvancedResidualSeasonalityDiagnosticsConfiguration nconfig = (AdvancedResidualSeasonalityDiagnosticsConfiguration) obj;
            config_ = nconfig.clone();
        }
    }

    @Override
    public IDiagnostics create(CompositeResults rslts) {
        return AdvancedResidualSeasonalityDiagnostics.create(rslts, config_);
    }

    /**
     * @return the config_
     */
    public AdvancedResidualSeasonalityDiagnosticsConfiguration getConfiguration() {
        return config_;
    }

}
