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
import static ec.tss.sa.diagnostics.ResidualsDiagnosticsFactory.ALL;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Kristof Bayens
 */
@ServiceProvider(service = ISaDiagnosticsFactory.class)
public class SeatsDiagnosticsFactory implements ISaDiagnosticsFactory {

    public static final String SEAS_VAR = "seas variance", IRR_VAR = "irregular variance";
    public static final String SEAS_I_CORR = "seas/irr cross-correlation";
    public static final String NOTSAME = "Non decomposable model. Changed by Seats";
    public static final String CUTOFF = "Parameters cut off by Seats";
    public static final String NAME = "Seats";
    
    public static final List<String> ALL=Collections.unmodifiableList(Arrays.asList(SEAS_VAR, IRR_VAR, SEAS_I_CORR));

    //public static final SeatsDiagnosticsFactory Default = new SeatsDiagnosticsFactory();
    private SeatsDiagnosticsConfiguration config_;

    public SeatsDiagnosticsFactory() {
        SeatsDiagnosticsConfiguration config = null;
        if (config == null)
            config = new SeatsDiagnosticsConfiguration();
        config_ = config;
    }

    public SeatsDiagnosticsFactory(SeatsDiagnosticsConfiguration config) {
        config_ = config;
    }

    public SeatsDiagnosticsConfiguration getConfiguration() {
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
        return "Comparison of the variance/covariance of estimators/estimates";
    }

    @Override
    public List<String> getTestDictionary(){
        return ALL.stream().map(s->s+":2").collect(Collectors.toList());
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
        SeatsDiagnosticsConfiguration config = (SeatsDiagnosticsConfiguration) obj;
        if (config != null) {
            config.check();
            config_ = config.clone();
        }
    }

    @Override
    public IDiagnostics create(CompositeResults rslts) {
        return SeatsDiagnostics.create(config_, rslts);
    }

    @Override
    public Scope getScope() {
        return Scope.Decomposition;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
