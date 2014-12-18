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


package ec.tss.xml.arima;

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.ucarima.UcarimaModel;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlRootElement(name = XmlUcArima.RNAME)
@XmlType(name = XmlUcArima.NAME)
public class XmlUcArima implements IXmlConverter<UcarimaModel> {
    static final String NAME = "ucarimaModelType";
    static final String RNAME = "ucarimaModelType";

    @XmlElement
    public XmlArimaModel sum;

    @XmlElement(name = "cmp")
    public XmlArimaModel[] cmps;

    @Override
    public UcarimaModel create() {
        if (cmps == null)
            return null;
        List<ArimaModel> models = new ArrayList<>();
        for (int i = 0; i < cmps.length; ++i) {
            ArimaModel cur = cmps[i].create();
            if (cur != null)
                models.add(cur);
        }
        ArimaModel asum = null;
        if (sum != null)
            asum = sum.create();
        return new UcarimaModel(asum, models);
    }

    @Override
    public void copy(UcarimaModel t) {
        XmlArimaModel[] ltmp = new XmlArimaModel[t.getComponentsCount()];
        for (int i = 0; i < ltmp.length; ++i) {
            XmlArimaModel xcur = new XmlArimaModel();
            xcur.copy(t.getComponent(i));
            ltmp[i] = xcur;
        }
        cmps = ltmp;
        IArimaModel isum = t.sum();
        if (isum != null) {
            sum = new XmlArimaModel();
            sum.copy(ArimaModel.create(isum));
        }
    }
}
