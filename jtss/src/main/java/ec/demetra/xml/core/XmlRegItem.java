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
package ec.demetra.xml.core;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.information.RegressionItem;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlRegItem.NAME)
public class XmlRegItem implements IXmlConverter<RegressionItem> {

    static final String NAME = "RegressionItemType";
    
    @XmlElement(name="Description")
    public String description;
    @XmlElement(name="Coefficient")
    public double coefficient;
    @XmlElement(name="Stde")
    public double stderror;

    @Override
    public RegressionItem create() {
        return new RegressionItem(description, coefficient, stderror);
    }

    @Override
    public void copy(RegressionItem t) {
        description = t.description;
        coefficient = t.coefficient;
        stderror = t.stdError;
    }
}
