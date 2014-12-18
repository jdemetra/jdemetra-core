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

package ec.tss.xml.x12;

import ec.satoolkit.x13.X13Specification;
import ec.tss.xml.uscb.AbstractXmlArimaSpec;
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

    private boolean isAcceptDefSpecified() {
        return acceptDef != null;
    }
    @XmlElement
    public Boolean checkMu;

    public boolean isCheckMuSpecified() {
        return checkMu != null;
    }
    @XmlElement
    public Boolean mixed;

    public boolean isMixedSpecified() {
        return mixed != null;
    }
    @XmlElement
    public Boolean balanced;

    public boolean isBalancedSpecified() {
        return balanced != null;
    }
    @XmlElement
    public Boolean hr;

    public boolean isHrSpecified() {
        return hr != null;
    }
    @XmlElement
    public Double cancel = AutoModelSpec.DEF_CANCEL;

    public boolean isCancelSpecified() {
        return cancel != null;
    }
    @XmlElement
    public Double fct = AutoModelSpec.DEF_FCT;

    public boolean isFctSpecified() {
        return fct != null;
    }
    @XmlElement
    public Double ljungbox = AutoModelSpec.DEF_LJUNGBOX;

    public boolean isLjungboxSpecified() {
        return ljungbox != null;
    }
    @XmlElement
    public Double predcv = AutoModelSpec.DEF_PREDCV;

    public boolean isPredcvSpecified() {
        return predcv != null;
    }
    @XmlElement
    public Double armaLimit = AutoModelSpec.DEF_TSIG;

    public boolean isArmaLimitSpecified() {
        return armaLimit != null;
    }
    @XmlElement
    public Double ub1 = AutoModelSpec.DEF_UB1;

    public boolean isUb1Specified() {
        return ub1 != null;
    }
    @XmlElement
    public Double ub2 = AutoModelSpec.DEF_UB2;

    public boolean isUb2Specified() {
        return ub2 != null;
    }
    @XmlElement
    public Double ubFinal = AutoModelSpec.DEF_UBFINAL;

    public boolean isUbFinalSpecified() {
        return ubFinal != null;
    }

    public static XmlAutoModelSpec create(AutoModelSpec s) {
        XmlAutoModelSpec t = new XmlAutoModelSpec();
        t.acceptDef = s.isAcceptDefault();
        t.balanced = s.isBalanced();
        if (s.getCancelationLimit() != AutoModelSpec.DEF_CANCEL) {
            t.cancel = s.getCancelationLimit();
        }
        t.armaLimit = s.getArmaSignificance();
        t.checkMu = s.isCheckMu();
        t.fct = s.getPercentRSE();
        t.hr = s.isHannanRissannen();
        t.ljungbox = s.getLjungBoxLimit();
        t.mixed = s.isMixed();
        t.predcv = s.getPercentReductionCV();
        t.ub1 = s.getInitialUnitRootLimit();
        t.ub2 = s.getFinalUnitRootLimit();
        t.ubFinal = s.getUnitRootLimit();
        return t;
    }

    @Override
    public void copyTo(X13Specification spec) {
        AutoModelSpec s = new AutoModelSpec();
        if (isAcceptDefSpecified()) {
            s.setAcceptDefault(acceptDef);
        }
        if (isArmaLimitSpecified()) {
            s.setArmaSignificance(armaLimit);
        }
        if (isBalancedSpecified()) {
            s.setBalanced(balanced);
        }
        if (isCancelSpecified()) {
            s.setCancelationLimit(cancel);
        }
        if (isCheckMuSpecified()) {
            s.setCheckMu(checkMu);
        }
        if (isUb2Specified()) {
            s.setFinalUnitRootLimit(ub2);
        }
        if (isHrSpecified()) {
            s.setHannanRissanen(hr);
        }
        if (isUb1Specified()) {
            s.setInitialUnitRootLimit(ub1);
        }
        if (isLjungboxSpecified()) {
            s.setLjungBoxLimit(ljungbox);
        }
        if (isMixedSpecified()) {
            s.setMixed(mixed);
        }
        if (isPredcvSpecified()) {
            s.setPercentReductionCV(predcv);
        }
        if (isFctSpecified()) {
            s.setPercentRSE(fct);
        }
        if (isUbFinalSpecified()) {
            s.setUnitRootLimit(ubFinal);
        }
        spec.getRegArimaSpecification().setAutoModel(s);
    }
}
