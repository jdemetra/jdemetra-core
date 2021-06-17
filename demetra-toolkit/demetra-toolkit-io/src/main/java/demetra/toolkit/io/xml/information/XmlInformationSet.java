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

import demetra.information.Information;
import demetra.information.InformationSet;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlInformationSet.RNAME)
@XmlType(name = XmlInformationSet.NAME)
public class XmlInformationSet implements IXmlConverter<InformationSet> {

    static final String NAME = "informationSetType";
    static final String RNAME = "informationSet";

    @XmlElement
    public XmlInformation[] item;

    @Override
    public InformationSet create() {
        InformationSet info = new InformationSet();
        if (item != null) {
            for (int i = 0; i < item.length; ++i) {
                try {
                    Information<Object> si = item[i].toInformation();
                    if (si.getName().indexOf(InformationSet.SEP) >= 0) {
                        String[] split = InformationSet.split(si.getName());
                        info.add(split, si.getValue());
                    } else {
                        info.add(si);
                    }
                } catch (Exception ex) {
                }
            }
        }
        return info;
    }

    @Override
    public void copy(InformationSet t) {
        if (t == null) {
            return;
        }
        List<Information<Object>> items = t.select(Object.class);
        item = new XmlInformation[items.size()];
        int cur = 0;
        for (Information<Object> info : items) {
            try {
                XmlInformation tmp = XmlInformation.create(info);
                if (tmp != null) {
                    item[cur++] = tmp;
                }
            } catch (Exception ex) {
            }
        }

        if (cur < item.length) {
            item = Arrays.copyOfRange(item, 0, cur);
        }
    }

    public void flatCopy(InformationSet t) {
        if (t == null) {
            return;
        }
        List<Information<Object>> items = t.deepSelect(Object.class);
        items.removeIf(item->InformationSet.class.isInstance(item.getValue()));
        item = new XmlInformation[items.size()];
        int cur = 0;
        for (Information<Object> info : items) {
            try {
                XmlInformation tmp = XmlInformation.create(info);
                if (tmp != null) {
                    item[cur++] = tmp;
                }
            } catch (Exception ex) {
            }
        }

        if (cur < item.length) {
            item = Arrays.copyOfRange(item, 0, cur);
        }
    }

}
