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
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class QK21 {

    /* Gauss quadrature weights and kronrod quadrature abscissae and
   weights as evaluated with 80 decimal digit arithmetic by
   L. W. Fullerton, Bell Labs, Nov. 1981. */
    final double[] XGK
            = /* abscissae of the 21-point kronrod rule */ {
                0.995657163025808080735527280689003,
                0.973906528517171720077964012084452,
                0.930157491355708226001207180059508,
                0.865063366688984510732096688423493,
                0.780817726586416897063717578345042,
                0.679409568299024406234327365114874,
                0.562757134668604683339000099272694,
                0.433395394129247190799265943165784,
                0.294392862701460198131126603103866,
                0.148874338981631210884826001129720,
                0.000000000000000000000000000000000
            };

    /* xgk[1], xgk[3], ... abscissae of the 10-point gauss rule. 
   xgk[0], xgk[2], ... abscissae to optimally extend the 10-point gauss rule */
    final double WG[]
            = /* weights of the 10-point gauss rule */ {
                0.066671344308688137593568809893332,
                0.149451349150580593145776339657697,
                0.219086362515982043995534934228163,
                0.269266719309996355091226921569469,
                0.295524224714752870173892994651338
            };

    final double WGK[]
            = /* weights of the 21-point kronrod rule */ {
                0.011694638867371874278064396062192,
                0.032558162307964727478818972459390,
                0.054755896574351996031381300244580,
                0.075039674810919952767043140916190,
                0.093125454583697605535065465083366,
                0.109387158802297641899210590325805,
                0.123491976262065851077958109831074,
                0.134709217311473325928054001771707,
                0.142775938577060080797094273138717,
                0.147739104901338491374841515972068,
                0.149445554002916905664936468389821
            };

    QK of(DoubleUnaryOperator f, double a, double b) {
        return QK.of(f, a, b, XGK, WG, WGK);
    }

    public IntegrationRule rule() {
        return (f, a, b) -> of(f, a, b).result();
    }
}
