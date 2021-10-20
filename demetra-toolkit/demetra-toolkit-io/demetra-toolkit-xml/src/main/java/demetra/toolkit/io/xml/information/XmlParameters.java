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

package demetra.toolkit.io.xml.information;

import demetra.data.Parameter;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlType(name = XmlParameters.NAME)
public class XmlParameters implements IXmlConverter<Parameter[]> {

    static final String NAME = "paramsType";

    @XmlElement(name = "coef")
    public XmlParameter[] parameters;

    @Override
    public Parameter[] create() {
            if (parameters == null)
                return null;
            int n = parameters.length;
            Parameter[] p = new Parameter[n];
            for (int i = 0; i < n; ++i)
                if (parameters[i] != null)
                    p[i] = parameters[i].create();
            return p;
    }

    @Override
    public void copy(Parameter[] t) {
            parameters = null;
            if (t == null)
                return;
            int n = t.length;
            if (n == 0)
                return;
            parameters = new XmlParameter[n];
            for (int i = 0; i < n; ++i)
            {
                parameters[i] = new XmlParameter();
                parameters[i].copy(t[i]);
            }
    }
}
