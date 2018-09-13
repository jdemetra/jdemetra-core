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


package ec.tss.xml.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.sa.documents.TramoSeatsDocument;
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
@XmlRootElement(name = XmlTramoSeatsDocument.RNAME)
@XmlType(name = XmlTramoSeatsDocument.NAME)
public class XmlTramoSeatsDocument implements IXmlConverter<TramoSeatsDocument> {
    static final String NAME = "tramoseatsDocType";
    static final String RNAME = "tramoseatsDoc";

    @XmlElement
    public XmlTs ts;
    @XmlElement
    public XmlTramoSeatsSpecification spec;
    @XmlAttribute
    public String name;
    
    @Override
    public TramoSeatsDocument create() {
        TramoSeatsDocument ndoc =
                new TramoSeatsDocument();
//        if (name != null)
//            ndoc.setName(name);

        InformationSet cnt = null;
        Ts xt=null;
        TramoSeatsSpecification xs=null;

        if (spec != null) {
            xs = new TramoSeatsSpecification();
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
    public void copy(TramoSeatsDocument t) {
        ts = new XmlTs();
        Ts s = t.getTs();
        if (s != null) {
            ts = new XmlTs();
            ts.copy(s.freeze().toInfo(TsInformationType.All));
        }
        spec = new XmlTramoSeatsSpecification();

        TramoSeatsSpecification tsSpec = t.getSpecification();

        spec.copy(tsSpec);
        //name = t.getName();
    }
}
