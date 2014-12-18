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

package ec.tss.xml.uscb;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.tss.xml.IXmlConverter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Kristof Bayens
 */
@XmlType(name = XmlX11Spec.NAME)
public class XmlX11Spec implements IXmlConverter<X11Specification> {

    static final String NAME = "x11SpecType";
    @XmlElement
    public DecompositionMode mode = DecompositionMode.Undefined;
    @XmlElement
    public boolean useForecasts = true;
    @XmlElement(name = "seasonalma")
    @XmlList
    public SeasonalFilterOption[] seasonalma;
    @XmlElement
    public Double lsigma = X11Specification.DEF_LSIGMA;

    public boolean isLsigmaSpecified() {
        return lsigma != null;
    }
    @XmlElement
    public Double usigma = X11Specification.DEF_USIGMA;

    public boolean isUsigmaSpecified() {
        return usigma != null;
    }
    @XmlElement
    public Integer trendma;

    public boolean isTrendmaSpecified() {
        return trendma != null;
    }

    public XmlX11Spec() {
    }

    public static XmlX11Spec create(X11Specification spec) {
        if (spec == null) {
            return null;
        }
        XmlX11Spec x = new XmlX11Spec();
        x.copy(spec);
        return x;
    }

    public void copyTo(X11Specification spec) {
        spec.setMode(mode);
        if (isLsigmaSpecified()) {
            spec.setLowerSigma(lsigma);
        }
        if (isUsigmaSpecified()) {
            spec.setUpperSigma(usigma);
        }
        if (isTrendmaSpecified()) {
            spec.setHendersonFilterLength(trendma);
        }
        if (seasonalma != null) {
            spec.setSeasonalFilters(seasonalma);
        }
        spec.setForecastHorizon(useForecasts ? -1 : 0);
//        if (sigmaVec != null) {
//            List<Integer> sig = new ArrayList<Integer>();
//            if (XmlFormatter.read(sig, sigmaVec)) {
//                boolean[] v = new boolean[sig.size()];
//                for (int i = 0; i < v.length; ++i)
//                    v[i] = sig.get(i) != 0;
//                spec.SigmaVec = v;
//            }
//        }
    }

    @Override
    public X11Specification create() {
        X11Specification spec = new X11Specification();
        copyTo(spec);
        return spec;
    }

    @Override
    public void copy(X11Specification t) {
        mode = t.getMode();
        lsigma = t.getLowerSigma();
        usigma = t.getUpperSigma();
        trendma = t.getHendersonFilterLength();
        seasonalma = t.getSeasonalFilters();
        useForecasts = t.getForecastHorizon() != 0;
//        boolean[] v = t.SigmaVec;
//        if (v != null) {
//            StringBuilder builder = new StringBuilder();
//            for (int i = 0; i < v.length; ++i) {
//                if (v[i])
//                    builder.append(1);
//                else
//                    builder.append(0);
//                if (i < v.length - 1)
//                    builder.append(' ');
//            }
//            sigmaVec = builder.toString();
//        }
    }
}
