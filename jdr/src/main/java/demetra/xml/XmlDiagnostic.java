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

import ec.tstoolkit.algorithm.ProcDiagnostic;
import ec.tstoolkit.algorithm.ProcQuality;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = XmlDiagnostic.NAME)
public class XmlDiagnostic implements IXmlConverter<ProcDiagnostic> {

    static final String NAME = "diagnosticType";
    @XmlAttribute
    public ProcQuality quality;
    @XmlAttribute
    public double value;

    public ProcDiagnostic create() {
        return new ProcDiagnostic(value, quality);
    }

    public void copy(ProcDiagnostic t) {
        value = t.value;
        quality = t.quality;
    }
}
