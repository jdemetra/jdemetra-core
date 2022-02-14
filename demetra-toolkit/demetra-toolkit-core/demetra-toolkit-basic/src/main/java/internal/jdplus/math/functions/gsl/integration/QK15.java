/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.jdplus.math.functions.gsl.integration;

import java.util.function.DoubleUnaryOperator;

/**
 *
 * @author Mats Maggi
 */
@lombok.experimental.UtilityClass
public class QK15 {

    /* Gauss quadrature weights and kronrod quadrature abscissae and
      weights as evaluated with 80 decimal digit arithmetic by
      L. W. Fullerton, Bell Labs, Nov. 1981. */
    final double XGK[]
            = /* abscissae of the 15-point kronrod rule */ {
                0.991455371120812639206854697526329,
                0.949107912342758524526189684047851,
                0.864864423359769072789712788640926,
                0.741531185599394439863864773280788,
                0.586087235467691130294144838258730,
                0.405845151377397166906606412076961,
                0.207784955007898467600689403773245,
                0.000000000000000000000000000000000};

    /* xgk[1], xgk[3], ... abscissae of the 7-point gauss rule.
     xgk[0], xgk[2], ... abscissae to optimally extend the 7-point gauss rule */
    final double WG[]
            = /* weights of the 7-point gauss rule */ {
                0.129484966168869693270611432679082,
                0.279705391489276667901467771423780,
                0.381830050505118944950369775488975,
                0.417959183673469387755102040816327};

    final double WGK[]
            = /* weights of the 15-point kronrod rule */ {
                0.022935322010529224963732008058970,
                0.063092092629978553290700663189204,
                0.104790010322250183839876322541518,
                0.140653259715525918745189590510238,
                0.169004726639267902826583426598550,
                0.190350578064785409913256402421014,
                0.204432940075298892414161999234649,
                0.209482141084727828012999174891714};

    QK of(DoubleUnaryOperator f, double a, double b) {
        return QK.of(f, a, b, XGK, WG, WGK);
    }

    public IntegrationRule rule() {
        return (f, a, b) -> of(f, a, b).result();
    }
}
