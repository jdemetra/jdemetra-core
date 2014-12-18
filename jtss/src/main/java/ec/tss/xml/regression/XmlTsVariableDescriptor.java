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


package ec.tss.xml.regression;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlTsVariableDescriptor.NAME)
public class XmlTsVariableDescriptor implements IXmlConverter<TsVariableDescriptor> {
    static final String NAME = "tsVariableDescriptorType";

    @XmlElement
    public String name;
    @XmlAttribute
    public Integer firstLag;
    public boolean isFirstLagSpecified() {
        return firstLag != null;
    }
    @XmlAttribute
    public Integer lastLag;
    public boolean isLastLagSpecified() {
        return lastLag != null;
    }
    @XmlAttribute
    public int getLag() {
        return firstLag;
    }
    public void setLag(int value) {
        firstLag = value;
        lastLag = value;
    }
    public boolean isLagSpecified() {
        return firstLag.equals(lastLag) && !firstLag.equals(0);
    }

    @Override
    public TsVariableDescriptor create() {
        TsVariableDescriptor desc = new TsVariableDescriptor(name);
        desc.setLags(firstLag, lastLag);
        return desc;
    }

    @Override
    public void copy(TsVariableDescriptor t) {
        name = t.getName();
        if (isFirstLagSpecified())
            firstLag = t.getFirstLag();
        if (isLastLagSpecified())
            lastLag = t.getLastLag();
    }
}
