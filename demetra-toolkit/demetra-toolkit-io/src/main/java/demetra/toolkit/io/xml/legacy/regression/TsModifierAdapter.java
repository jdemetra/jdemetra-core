/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.toolkit.io.xml.legacy.regression;

import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;
import demetra.timeseries.regression.ModifiedTsVariable;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Jean Palate
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE)
public abstract class TsModifierAdapter extends XmlAdapter<XmlRegressionVariableModifier, ModifiedTsVariable.Modifier>{

     public abstract void xmlClasses(List<Class> lclass);
}
