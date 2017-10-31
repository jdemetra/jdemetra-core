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
package demetra.x11;

import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate
 */
public class DummySeasonalNormalizer implements ISeasonalNormalizer {

    public static final DummySeasonalNormalizer instance = new DummySeasonalNormalizer();

    private DummySeasonalNormalizer() {
    }

    @Override
    public TsData normalize(TsData s, TsDomain xdomain) {
        if (xdomain != null) {
            return s.fittoDomain(xdomain);
        } else {
            return s.clone();
        }
    }

    @Override
    public void setContext(X11Context context) {
    }

}
