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
package ec.demetra.xml.regression;

import ec.demetra.xml.core.XmlParameter;
import ec.demetra.xml.core.XmlParameters;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlRegressionItem.RNAME)
@XmlType(name = XmlRegressionItem.NAME)
public class XmlRegressionItem {

    static final String RNAME = "RegressionItem", NAME = RNAME + "Type";
    
    @XmlElement(name="Variable")
    public XmlRegressionVariable variable;
    
    @XmlElement(name="Coefficient")
    public XmlParameter scoeff;
    
    @XmlElement(name="Coefficients")
    public XmlParameters mcoeff;
}
