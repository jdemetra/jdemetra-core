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

package demetra.xml.sa.x13;

import ec.satoolkit.x13.X13Specification;
import demetra.xml.sa.uscb.AbstractXmlArimaSpec;
import ec.tstoolkit.modelling.arima.x13.AutoModelSpec;
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
    public Boolean acceptDef;
    @XmlElement
    public Boolean checkMu;
    @XmlElement
    public Boolean mixed;
    @XmlElement
    public Boolean balanced;
    @XmlElement
    public Boolean hr;
    @XmlElement
    public Double cancel;
    @XmlElement
    public Double fct;
    @XmlElement
    public Double ljungbox;
    @XmlElement
    public Double predcv;
    @XmlElement
    public Double armaLimit;
    @XmlElement
    public Double ub1;
    @XmlElement
    public Double ub2;
    @XmlElement
    public Double ubFinal;

    public static XmlAutoModelSpec create(AutoModelSpec s) {
        XmlAutoModelSpec t = new XmlAutoModelSpec();
        if (s.isAcceptDefault()) {
            t.acceptDef = Boolean.TRUE;
        }
        if (s.isBalanced()) {
            t.balanced = Boolean.TRUE;
        }
        if (s.getCancelationLimit() != AutoModelSpec.DEF_CANCEL) {
            t.cancel = s.getCancelationLimit();
        }
        if (s.getArmaSignificance() != AutoModelSpec.DEF_TSIG) {
            t.armaLimit = s.getArmaSignificance();
        }
        if (!s.isCheckMu()) {
            t.checkMu = Boolean.FALSE;
        }
        if (s.getPercentRSE() != AutoModelSpec.DEF_FCT) {
            t.fct = s.getPercentRSE();
        }
        if (s.isHannanRissannen()) {
            t.hr = Boolean.TRUE;
        }
        if (s.getLjungBoxLimit() != AutoModelSpec.DEF_LJUNGBOX) {
            t.ljungbox = s.getLjungBoxLimit();
        }
        if (!s.isMixed()) {
            t.mixed = Boolean.FALSE;
        }
        if (s.getPercentReductionCV() != AutoModelSpec.DEF_PREDCV) {
            t.predcv = s.getPercentReductionCV();
        }
        if (s.getInitialUnitRootLimit() != AutoModelSpec.DEF_UB1) {
            t.ub1 = s.getInitialUnitRootLimit();
        }
        if (s.getFinalUnitRootLimit() != AutoModelSpec.DEF_UB2) {
            t.ub2 = s.getFinalUnitRootLimit();
        }
        if (s.getUnitRootLimit() != AutoModelSpec.DEF_UBFINAL) {
            t.ubFinal = s.getUnitRootLimit();
        }
        return t;
    }

    @Override
    public void copyTo(X13Specification spec) {
        AutoModelSpec s = new AutoModelSpec();
        if (acceptDef != null) {
            s.setAcceptDefault(acceptDef);
        }
        if (armaLimit != null) {
            s.setArmaSignificance(armaLimit);
        }
        if (balanced != null) {
            s.setBalanced(balanced);
        }
        if (cancel != null) {
            s.setCancelationLimit(cancel);
        }
        if (checkMu != null) {
            s.setCheckMu(checkMu);
        }
        if (ub2 != null) {
            s.setFinalUnitRootLimit(ub2);
        }
        if (hr != null) {
            s.setHannanRissanen(hr);
        }
        if (ub1 != null) {
            s.setInitialUnitRootLimit(ub1);
        }
        if (ljungbox != null) {
            s.setLjungBoxLimit(ljungbox);
        }
        if (mixed != null) {
            s.setMixed(mixed);
        }
        if (predcv != null) {
            s.setPercentReductionCV(predcv);
        }
        if (fct != null) {
            s.setPercentRSE(fct);
        }
        if (ubFinal != null) {
            s.setUnitRootLimit(ubFinal);
        }
        spec.getRegArimaSpecification().setAutoModel(s);
    }
}
