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


package ec.tss.sa.diagnostics;

import ec.tss.sa.ISaDiagnosticsFactory;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Kristof Bayens
 */
@ServiceProvider(service = ISaDiagnosticsFactory.class)
public class ResidualSeasonalityDiagnosticsFactory implements ISaDiagnosticsFactory {
    private ResidualSeasonalityDiagnosticsConfiguration config_;
    //public static final ResidualSeasonalityDiagnosticsFactory Default = new ResidualSeasonalityDiagnosticsFactory();

    public ResidualSeasonalityDiagnosticsFactory() {
        config_ = new ResidualSeasonalityDiagnosticsConfiguration();
    }

    public ResidualSeasonalityDiagnosticsFactory(ResidualSeasonalityDiagnosticsConfiguration config) {
        config_ = config;
    }

    public ResidualSeasonalityDiagnosticsConfiguration getConfiguration() {
        return config_;
    }

    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "Residual seasonality";
    }

    @Override
    public String getDescription() {
        return "Checks the presence of residual seasonality";
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
        ResidualSeasonalityDiagnosticsConfiguration config = (ResidualSeasonalityDiagnosticsConfiguration)obj;
        if (config != null) {
            config.check();
            config_ = config.clone();
        }
    }

    @Override
    public IDiagnostics create(CompositeResults rslts) {
        return ResidualSeasonalityDiagnostics.create(config_, rslts);
    }

    @Override
    public Scope getScope() {
        return Scope.Final;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
