/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.highfreq;

import demetra.timeseries.TimeSelector;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true)
public class OutliersSpec {
    

    public static final int DEF_MAXOUTLIERS = 100, DEF_MAXROUND=100;
    
    // automatic outliers detection
    @lombok.NonNull
    private TimeSelector span;
    private String[] outliers;
    private double criticalValue;
    private int maxOutliers;
    private int maxRound;
    
    public static Builder builder(){
        return new Builder()
                .span(TimeSelector.all())
                .outliers(ALL_OUTLIERS)
                .maxOutliers(DEF_MAXOUTLIERS)
                .maxRound(DEF_MAXROUND)
                .criticalValue(0);
    }
    
    public static final String[] NO_OUTLIER=new String[0];
    public static final String[] ALL_OUTLIERS=new String[]{"AO", "LS", "WO"};
    public static final String[] DEF_OUTLIERS=new String[]{"AO", "LS"};
               
    public boolean isUsed(){
        return outliers.length>0;
    }
    
    public static final OutliersSpec DEFAULT_ENABLED=new Builder()
            .span(TimeSelector.all())
                .outliers(DEF_OUTLIERS)
                .maxOutliers(DEF_MAXOUTLIERS)
                .maxRound(DEF_MAXROUND)
                .criticalValue(0)
                .build();
    
    
    public static final OutliersSpec DEFAULT_DISABLED=new Builder()
            .span(TimeSelector.all())
                .outliers(NO_OUTLIER)
                .maxOutliers(DEF_MAXOUTLIERS)
                .maxRound(DEF_MAXROUND)
                .criticalValue(0)
                .build();
}
