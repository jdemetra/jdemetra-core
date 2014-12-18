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

package ec.tss.xml;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author pcuser
 */
@XmlType(name = XmlParameter.NAME)
public class XmlParameter implements IXmlConverter<Parameter> {

    static final String NAME = "paramType";
    /**
     *
     */
    @XmlAttribute
    public String name;
    /**
     *
     */
    @XmlAttribute
    public ParameterType type;
    /**
     *
     */
    @XmlElement
    public Double value;
    /**
     *
     */
    @XmlElement
    public Double stde;
    /**
     *
     */
    @XmlElement
    public Double tstat;

    /**
     *
     * @param t
     */
    @Override
    public void copy(Parameter t) {
	if (t.getType() == ParameterType.Undefined)
	    return;
	type = t.getType();
	value = t.getValue();
	double dstde = 0;
	if (type == ParameterType.Estimated || type == ParameterType.Derived)
	    dstde = t.getStde();
	if (dstde != 0) {
	    stde = dstde;
	    tstat = value / dstde;
	}
    }

    /**
     *
     * @return
     */
    @Override
    public Parameter create() {
	Parameter p = new Parameter();
	if (value != null)
	    p.setValue(value);
	if (stde != null)
	    p.setStde(stde);
	if (type != null)
	    p.setType(type);
	return p;
    }
}
