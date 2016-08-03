/*
 * Copyright 2016 National Bank of Belgium
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
package ec.demetra.xml.sa.tramoseats;

import ec.tss.xml.IXmlConverter;
import ec.tss.xml.XmlEmptyElement;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlTransformationSpec.RNAME)
@XmlType(name = XmlTransformationSpec.NAME)
public class XmlTransformationSpec implements IXmlConverter<TransformSpec> {

    public static class XmlAuto {

        @XmlElement
        public Double Fct;
    }

    @XmlElement
    public XmlEmptyElement Log;

    @XmlElement(name="Auto")
    public XmlAuto Auto;

    static final String RNAME = "TransformationSpec", NAME = RNAME + "Type";

    @Override
    public TransformSpec create() {
        TransformSpec spec = new TransformSpec();
        if (Log != null) {
            spec.setFunction(DefaultTransformationType.Log);
        } else if (Auto != null) {
            spec.setFunction(DefaultTransformationType.Auto);
            spec.setFct(Auto.Fct);
        } else {
            spec.setFunction(DefaultTransformationType.None);
        }
        return spec;
    }

    @Override
    public void copy(TransformSpec v) {
        switch (v.getFunction()) {
            case Log:
                this.Log = new XmlEmptyElement();
                break;
            case Auto:
                this.Auto = new XmlAuto();
                this.Auto.Fct = v.getFct();
        }
    }

}
