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
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Jean Palate
 */
@XmlType(name = XmlParameter.NAME)
public class XmlParameter implements IXmlConverter<Parameter> {

    static final String NAME = "ParameterType";
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
    @XmlAttribute
    public Integer index;
    
    @XmlElement
    public Double Value;
    /**
     *
     */
    @XmlElement
    public Double Stde;
    /**
     *
     */
    @XmlElement
    public Double Tstat;

    /**
     *
     * @param t
     */
    @Override
    public void copy(Parameter t) {
	if (t.getType() == ParameterType.Undefined)
	    return;
	type = t.getType();
	Value = t.getValue();
	double dstde = 0;
	if (type == ParameterType.Estimated || type == ParameterType.Derived)
	    dstde = t.getStde();
	if (dstde != 0) {
	    Stde = dstde;
	    Tstat = Value / dstde;
	}
    }

    /**
     *
     * @return
     */
    @Override
    public Parameter create() {
	Parameter p = new Parameter();
	if (Value != null)
	    p.setValue(Value);
	if (Stde != null)
	    p.setStde(Stde);
	if (type != null)
	    p.setType(type);
	return p;
    }
}
