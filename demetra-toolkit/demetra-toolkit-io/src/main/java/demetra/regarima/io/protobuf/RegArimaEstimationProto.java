/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.regarima.io.protobuf;

import demetra.arima.SarimaOrders;
import demetra.data.Parameter;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaEstimationProto {

    RegArimaResultsProtos.Sarima arima(ModelEstimation model) {
        SarimaModel arima = model.getModel().arima();
        SarimaOrders orders = arima.orders();
        RegArimaResultsProtos.Sarima.Builder builder = RegArimaResultsProtos.Sarima.newBuilder()
                .setPeriod(orders.getPeriod())
                .setD(orders.getD())
                .setBd(orders.getBd())
                .setCovariance(ToolkitProtosUtility.convert(model.getArimaCovariance()));
        boolean[] fp = model.getFixedArimaParameters();
        double[] p = model.getArimaParameters();
        int idx = 0;
        for (int i = 0; i < orders.getP(); ++i, ++idx) {
            builder.addPhi(ToolkitProtosUtility.convert(fp[idx] ? Parameter.fixed(p[idx]) : Parameter.estimated(idx)));
        }
        for (int i = 0; i < orders.getBp(); ++i, ++idx) {
            builder.addBphi(ToolkitProtosUtility.convert(fp[idx] ? Parameter.fixed(p[idx]) : Parameter.estimated(idx)));
        }
        for (int i = 0; i < orders.getQ(); ++i, ++idx) {
            builder.addTheta(ToolkitProtosUtility.convert(fp[idx] ? Parameter.fixed(p[idx]) : Parameter.estimated(idx)));
        }
        for (int i = 0; i < orders.getBq(); ++i, ++idx) {
            builder.addBtheta(ToolkitProtosUtility.convert(fp[idx] ? Parameter.fixed(p[idx]) : Parameter.estimated(idx)));
        }
        
        return builder.build();
    }

    public RegArimaResultsProtos.RegArimaEstimation convert(ModelEstimation model) {
        RegArimaResultsProtos.RegArimaEstimation.Builder builder = RegArimaResultsProtos.RegArimaEstimation.newBuilder();
        builder.setTransformation(model.isLogTransformation() ? RegArimaProtos.Transformation.FN_LOG : RegArimaProtos.Transformation.FN_LEVEL)
                .setPreadjustment(RegArimaProtosUtility.convert(model.getLpTransformation()))
                .setCovariance(ToolkitProtosUtility.convert(model.getConcentratedLikelihood().covariance(model.getFreeArimaParametersCount(), true)))
                .setSarima(arima(model))
                .setLikelihood(ToolkitProtosUtility.convert(model.getStatistics()));

        return builder.build();
    }

}
