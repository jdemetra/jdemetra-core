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
package ec.demetra.xml.sa.x13;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x11.X11Specification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class XmlX11SpecTest {

    public XmlX11SpecTest() {
    }

    @Test
    public void testMarshalling() {
        X11Specification spec = new X11Specification();
        test(spec);
        spec.setMode(DecompositionMode.Additive);
        test(spec);
        spec.setLowerSigma(2);
        test(spec);
        spec.setLowerSigma(3);
        test(spec);
        spec.setHendersonFilterLength(25);
        test(spec);
        spec.setSeasonalFilter(SeasonalFilterOption.Stable);
        test(spec);
        spec.setSeasonalFilter(null);
        SeasonalFilterOption[] sf = new SeasonalFilterOption[]{SeasonalFilterOption.S3X15, SeasonalFilterOption.Stable};
        spec.setSeasonalFilters(sf);
        test(spec);
        spec.setForecastHorizon(2);
        test(spec);
        spec.setCalendarSigma(CalendarSigma.Select);
        spec.setSigmavec(new SigmavecOption[]{SigmavecOption.Group1, SigmavecOption.Group2});
        test(spec);
        spec.setExcludefcst(true);
        spec.setSeasonal(false);
        test(spec);
    }

    private void test(X11Specification spec) {
        XmlX11Spec xspec = new XmlX11Spec();
        XmlX11Spec.MARSHALLER.marshal(spec, xspec);
        X11Specification nspec = new X11Specification();
        XmlX11Spec.UNMARSHALLER.unmarshal(xspec, nspec);
        assertTrue(spec.equals(nspec));
    }

}
