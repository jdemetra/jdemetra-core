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

import demetra.timeseries.Ts;
import demetra.timeseries.TsCollection;
import demetra.timeseries.TsInformationType;
import demetra.timeseries.TsMoniker;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * @author Jean Palate
 */
@XmlRootElement(name = XmlTsCollection.RNAME)
@XmlType(name = XmlTsCollection.NAME)
public class XmlTsCollection implements IXmlConverter<TsCollection> {

    static final String NAME = "tsCollectionType";
    static final String RNAME = "tsCollection";
    /**
     *
     */
    @XmlElement
    public XmlMetaData metadata;
    /**
     *
     */
    @XmlAttribute
    public String name;
    /**
     *
     */
    @XmlAttribute
    public String source;
    /**
     *
     */
    @XmlAttribute
    public String identifier;
    /**
     *
     */
    @XmlElement(name = "ts")
    @XmlElementWrapper(name = "data")
    public XmlTs[] tslist;

    /**
     * 
     * @param t
     */
    @Override
    public void copy(TsCollection t)
    {
	source = t.getMoniker().getSource();
	identifier = t.getMoniker().getId();
	name = t.getName();
        Map<String, String> meta = t.getMeta();
	if (meta == null || meta.isEmpty())
	    metadata = null;
	else {
	    metadata = new XmlMetaData();
	    metadata.copy(meta);
	}

        List<Ts> items = t.getItems();
	int n = items.size();
	if (n > 0) {
	    tslist = new XmlTs[n];
	    for (int i = 0; i < n; ++i) {
		XmlTs s = new XmlTs();
		s.copy(items.get(i));
		tslist[i] = s;
	    }
	} else
	    tslist = null;
    }

    /**
     * 
     * @return
     */
    @Override
    public TsCollection create()
    {
       TsMoniker moniker = (source == null && identifier == null)? TsMoniker.of() : TsMoniker.of(source, identifier);
	TsCollection.Builder cinfo = TsCollection.builder()
                .moniker(moniker)
                .type(TsInformationType.UserDefined)
                .name(name);
	if (metadata != null)
	    cinfo.meta(metadata.create());
	if (tslist != null)
	    for (int i = 0; i < tslist.length; ++i)
		cinfo.item(tslist[i].create());
	return cinfo.build();
    }
}
