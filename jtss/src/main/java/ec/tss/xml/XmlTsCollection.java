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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsCollectionInformation;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;

/**
 * 
 * @author pcuser
 */
@XmlRootElement(name = XmlTsCollection.RNAME)
@XmlType(name = XmlTsCollection.NAME)
public class XmlTsCollection implements IXmlConverter<TsCollectionInformation> {

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
    public void copy(TsCollectionInformation t)
    {
	source = t.moniker.getSource();
	identifier = t.moniker.getId();
	name = t.name;
	if (t.metaData == null || t.metaData.isEmpty())
	    metadata = null;
	else {
	    metadata = new XmlMetaData();
	    metadata.copy(t.metaData);
	}

	int n = t.items.size();
	if (n > 0) {
	    tslist = new XmlTs[n];
	    for (int i = 0; i < n; ++i) {
		XmlTs s = new XmlTs();
		s.copy(t.items.get(i));
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
    public TsCollectionInformation create()
    {
	TsMoniker moniker = TsMoniker.create(source, identifier);
	TsCollectionInformation cinfo = new TsCollectionInformation(moniker,
		TsInformationType.UserDefined);
        cinfo.name=name;
	if (metadata != null)
	    cinfo.metaData = metadata.create();
	if (tslist != null)
	    for (int i = 0; i < tslist.length; ++i)
		cinfo.items.add(tslist[i].create());
	return cinfo;
    }

    /**
     * 
     * @return
     */
    public TsCollection createTSCollection()
    {
	TsCollectionInformation info = create();
	if (info == null)
	    return null;
	ArrayList<Ts> ts = new ArrayList<>();
	if (info.items != null)
	    for (TsInformation tsinfo : info.items)
		ts.add(TsFactory.instance.createTs(tsinfo.name, tsinfo.moniker,
			tsinfo.metaData, tsinfo.data));
	return TsFactory.instance.createTsCollection(info.name, info.moniker,
		info.metaData, ts);
    }
}
