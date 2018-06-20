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
package demetra.modelling.regression;

import demetra.timeseries.TsData;
import demetra.timeseries.TsDataSupplier;
import demetra.util.DefaultNameValidator;
import demetra.util.INameValidator;
import demetra.util.NameManager;
import java.util.function.Supplier;

/**
 *
 * @author Jean Palate
 */
public class TsVariables extends NameManager<TsDataSupplier> {

    public static final String X = "x_";

    public TsVariables() {
        super(TsDataSupplier.class, X, new DefaultNameValidator(".+-*/"));
    }

    public TsVariables(String prefix, INameValidator validator) {
        super(TsDataSupplier.class, prefix, validator);
    }

    public boolean isEmpty() {
        return getCount() < 1;
    }
}
