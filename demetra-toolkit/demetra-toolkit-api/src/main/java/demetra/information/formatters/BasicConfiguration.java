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
package demetra.information.formatters;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jean Palate
 */

@lombok.experimental.UtilityClass
public class BasicConfiguration {

    private final AtomicInteger ndec = new AtomicInteger(9);

    public int getFractionDigits() {
        return ndec.get();
    }

    public void setDecimalNumber(int n) {
        ndec.set(n);
    }


}
