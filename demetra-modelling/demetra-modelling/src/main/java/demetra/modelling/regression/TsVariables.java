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

import demetra.utilities.DefaultNameValidator;
import demetra.utilities.INameValidator;
import demetra.utilities.NameManager;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class TsVariables extends NameManager<ITsVariable> {

    public static final String X = "x_";

    public TsVariables() {
        super(ITsVariable.class, X, new DefaultNameValidator(".+-*/"));
    }

    public TsVariables(String prefix, INameValidator validator) {
        super(ITsVariable.class, prefix, validator);
    }

    public boolean isEmpty() {
        return getCount() < 1;
    }


}
