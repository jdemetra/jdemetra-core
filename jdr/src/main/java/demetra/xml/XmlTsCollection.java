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
package demetra.xml;

import demetra.datatypes.TsCollection;
import demetra.datatypes.TsMoniker;
import java.util.ArrayList;

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
    public void copy(TsCollection t) {
        source = t.getMoniker().getSource();
        identifier = t.getMoniker().getId();
        name = t.getName();
        if (t.getMetaData().isEmpty()) {
            metadata = null;
        } else {
            metadata = new XmlMetaData();
            metadata.copy(t.getMetaData());
        }

        int n = t.getItems().size();
        if (n > 0) {
            tslist = new XmlTs[n];
            for (int i = 0; i < n; ++i) {
                XmlTs s = new XmlTs();
                s.copy(t.getItems().get(i));
                tslist[i] = s;
            }
        } else {
            tslist = null;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public TsCollection create() {
        TsMoniker moniker = TsMoniker.create(source, identifier);
        TsCollection.Builder builder = TsCollection.builder()
                .moniker(moniker)
                .name(name)
                .metaData(metadata.create());
        if (tslist != null) {
            for (int i = 0; i < tslist.length; ++i) {
                builder.item(tslist[i].create());
            }
        }
        return builder.build();
    }

}
