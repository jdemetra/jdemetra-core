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


package ec.tss.xml.x13;

import ec.satoolkit.x13.X13Specification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.sa.documents.X13Document;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.XmlTs;
import ec.tstoolkit.information.InformationSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlX13Document.RNAME)
@XmlType(name = XmlX13Document.NAME)
public class XmlX13Document implements IXmlConverter<X13Document> {
    static final String NAME = "x13DocType";
    static final String RNAME = "x13Doc";

    @XmlElement
    public XmlTs ts;
    @XmlElement
    public XmlX13Specification spec;
    @XmlAttribute
    public String name;

    @Override
    public X13Document create() {
        X13Document ndoc =
                new X13Document();

        InformationSet cnt = null;
        Ts xt=null;
        X13Specification xs=null;

        if (spec != null) {
            xs = new X13Specification();
            spec.copyTo(xs);
        }
        if (ts != null) {
            TsInformation info = ts.create();
            xt = TsFactory.instance.createTs(info.name, info.moniker, info.metaData, info.data);
        }
        ndoc.setSpecification(xs);
        ndoc.setTs(xt);

        ndoc.setLocked(true);
        return ndoc;
    }

    @Override
    public void copy(X13Document t) {
        ts = new XmlTs();
        Ts s = t.getTs();
        if (s != null) {
            ts = new XmlTs();
            ts.copy(new TsInformation(s.freeze(), TsInformationType.All));
        }
        spec = new XmlX13Specification();

        X13Specification tsSpec = t.getSpecification();

        spec.copy(tsSpec);
        //name = t.getName();
    }
}
