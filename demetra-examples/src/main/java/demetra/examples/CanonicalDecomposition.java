/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.examples;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.sarima.SarimaModel;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class CanonicalDecomposition {

    public void main(String[] args) {
        SarimaOrders spec = new SarimaOrders(12);
        spec.setRegular(1, 1, 0);
        spec.setSeasonal(1, 1, 1);
        SarimaModel arima = SarimaModel.builder(spec)
                .phi(-.75)
                .bphi(-.49)
                .btheta(-.79)
                .build();
        decompose(arima);

        spec = SarimaOrders.airline(12);
        arima = SarimaModel.builder(spec)
                .theta(-.7)
                .btheta(-.8)
                .build();
        decompose(arima);
    }

    private void decompose(SarimaModel arima) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        DoubleUnaryOperator s = arima.getSpectrum().asFunction();
        DoubleUnaryOperator s0 = ucm.getComponent(0).getSpectrum().asFunction();
        DoubleUnaryOperator s1 = ucm.getComponent(1).getSpectrum().asFunction();

        DoubleSeq p = DoubleSeq.onMapping(360, i -> cut(s.applyAsDouble(Math.PI * i / 360.0)));
        DoubleSeq p0 = DoubleSeq.onMapping(360, i -> cut(s0.applyAsDouble(Math.PI * i / 360.0)));
        DoubleSeq p1 = DoubleSeq.onMapping(360, i -> cut(s1.applyAsDouble(Math.PI * i / 360.0)));

        System.out.println(p);
        System.out.println(p0);
        System.out.println(p1);
        UcarimaModel ucm2 = ucm.setVarianceMax(-1, true);

        DoubleUnaryOperator t = ucm2.getModel().getSpectrum().asFunction();
        DoubleUnaryOperator t0 = ucm2.getComponent(0).getSpectrum().asFunction();
        DoubleUnaryOperator t1 = ucm2.getComponent(1).getSpectrum().asFunction();

        p = DoubleSeq.onMapping(360, i -> cut(t.applyAsDouble(Math.PI * i / 360.0)));
        p0 = DoubleSeq.onMapping(360, i -> cut(t0.applyAsDouble(Math.PI * i / 360.0)));
        p1 = DoubleSeq.onMapping(360, i -> cut(t1.applyAsDouble(Math.PI * i / 360.0)));

        System.out.println(p);
        System.out.println(p0);
        System.out.println(p1);
    }

    private double cut(double x) {
        if (!Double.isFinite(x) || x > 999) {
            return 999;
        } else {
            return x;
        }
    }
}
