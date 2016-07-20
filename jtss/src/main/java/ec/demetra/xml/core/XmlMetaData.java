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
import ec.tstoolkit.MetaData;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Jean Palate
 */
@XmlRootElement(name = XmlMetaData.RNAME)
@XmlType(name = XmlMetaData.NAME)
public class XmlMetaData implements IXmlConverter<MetaData> {

     static final String RNAME = "MetaData", NAME=RNAME+"Type";
    /**
     *
     */
    @XmlElement(name = "Property")
    public XmlProperty[] properties;

    /**
     * 
     * @param t
     */
     @Override
    public void copy(MetaData t)
    {
	if (t.isEmpty()) {
	    properties = null;
	    return;
	}

	Set<String> keys = t.keySet();

	properties = new XmlProperty[keys.size()];
	int pos = 0;
	for (String key : keys) {
            properties[pos] = new XmlProperty();
	    properties[pos].name = key;
	    properties[pos].value = t.get(key);
	    ++pos;
	}
    }

    /**
     * 
     * @return
     */
    public MetaData create()
    {
	MetaData rslt = new MetaData();
	initialize(rslt);
	return rslt;
    }

    /**
     * 
     * @param rslt
     */
    public void initialize(MetaData rslt)
    {
	rslt.clear();
	if (properties == null)
	    return;
	for (int i = 0; i < properties.length; ++i)
	    rslt.put(properties[i].name, properties[i].value);
    }
}
