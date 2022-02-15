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
public class QK31 {

    /* Gauss quadrature weights and kronrod quadrature abscissae and
      weights as evaluated with 80 decimal digit arithmetic by
      L. W. Fullerton, Bell Labs, Nov. 1981. */
    final double XGK[]
            = /* abscissae of the 31-point kronrod rule */ {
                0.998002298693397060285172840152271,
                0.987992518020485428489565718586613,
                0.967739075679139134257347978784337,
                0.937273392400705904307758947710209,
                0.897264532344081900882509656454496,
                0.848206583410427216200648320774217,
                0.790418501442465932967649294817947,
                0.724417731360170047416186054613938,
                0.650996741297416970533735895313275,
                0.570972172608538847537226737253911,
                0.485081863640239680693655740232351,
                0.394151347077563369897207370981045,
                0.299180007153168812166780024266389,
                0.201194093997434522300628303394596,
                0.101142066918717499027074231447392,
                0.000000000000000000000000000000000};

    /* xgk[1], xgk[3], ... abscissae of the 15-point gauss rule.
      xgk[0], xgk[2], ... abscissae to optimally extend the 15-point gauss rule */
    final double WG[]
            = /* weights of the 15-point gauss rule */ {
                0.030753241996117268354628393577204,
                0.070366047488108124709267416450667,
                0.107159220467171935011869546685869,
                0.139570677926154314447804794511028,
                0.166269205816993933553200860481209,
                0.186161000015562211026800561866423,
                0.198431485327111576456118326443839,
                0.202578241925561272880620199967519};

    final double WGK[]
            = /* weights of the 31-point kronrod rule */ {
                0.005377479872923348987792051430128,
                0.015007947329316122538374763075807,
                0.025460847326715320186874001019653,
                0.035346360791375846222037948478360,
                0.044589751324764876608227299373280,
                0.053481524690928087265343147239430,
                0.062009567800670640285139230960803,
                0.069854121318728258709520077099147,
                0.076849680757720378894432777482659,
                0.083080502823133021038289247286104,
                0.088564443056211770647275443693774,
                0.093126598170825321225486872747346,
                0.096642726983623678505179907627589,
                0.099173598721791959332393173484603,
                0.100769845523875595044946662617570,
                0.101330007014791549017374792767493};

    QK of(DoubleUnaryOperator f, double a, double b) {
        return QK.of(f, a, b, XGK, WG, WGK);
    }

    public IntegrationRule rule() {
        return (f, a, b) -> of(f, a, b).result();
    }
}
