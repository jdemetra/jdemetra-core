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


package demetra.xml.sa.tramoseats;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.modelling.arima.tramo.AutoModelSpec;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlAutoModelSpec.NAME)
public class XmlAutoModelSpec extends AbstractXmlArimaSpec {
    static final String NAME = "autoModelSpecType";

    @XmlElement
    public Double pcr;
    public boolean isPcrSpecified() {
        return pcr != null;
    }
    @XmlElement
    public Double ub1;
    public boolean isUb1Specified() {
        return ub1 != null;
    }
    @XmlElement
    public Double ub2;
    public boolean isUb2Specified() {
        return ub2 != null;
    }
    @XmlElement
    public Double cancel;
    public boolean isCancelSpecified() {
        return cancel != null;
    }
    @XmlElement
    public Double tsig;
    public boolean isTsigSpecified() {
        return tsig != null;
    }
    @XmlElement
    public Double pc;
    public boolean isPcSpecified() {
        return pc != null;
    }

    public static XmlAutoModelSpec create(AutoModelSpec s) {
        XmlAutoModelSpec t = new XmlAutoModelSpec();
        if (s.getPcr() != AutoModelSpec.DEF_PCR)
            t.pcr = s.getPcr();
        if (s.getUb1() != AutoModelSpec.DEF_UB1)
            t.ub1 = s.getUb1();
        if (s.getUb2() != AutoModelSpec.DEF_UB2)
            t.ub2 = s.getUb2();
        if (s.getCancel() != AutoModelSpec.DEF_CANCEL)
            t.cancel = s.getCancel();
        if (s.getTsig() != AutoModelSpec.DEF_TSIG)
            t.tsig = s.getTsig();
        if (s.getPc() != AutoModelSpec.DEF_PC)
            t.pc = s.getPc();
        return t;
    }

    @Override
    public void copyTo(TramoSeatsSpecification trspec) {
        AutoModelSpec spec = new AutoModelSpec();
        if (isPcrSpecified())
            spec.setPcr(pcr);
        if (isUb1Specified())
            spec.setUb1(ub1);
        if (isUb2Specified())
            spec.setUb2(ub2);
        if (isCancelSpecified())
            spec.setCancel(cancel);
        if (isTsigSpecified())
            spec.setTsig(tsig);
        if (isPcSpecified())
            spec.setPc(pc);
        trspec.getTramoSpecification().setAutoModel(spec);
    }
}
