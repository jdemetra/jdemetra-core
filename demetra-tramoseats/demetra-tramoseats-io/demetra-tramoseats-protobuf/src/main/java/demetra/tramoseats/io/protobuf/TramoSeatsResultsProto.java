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
package demetra.tramoseats.io.protobuf;

import demetra.regarima.io.protobuf.RegArimaEstimationProto;
import demetra.sa.io.protobuf.SaProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSeatsResultsProto {


    public TramoSeatsResults convert(jdplus.tramoseats.TramoSeatsResults rslts) {
        TramoSeatsResults.Builder builder = TramoSeatsResults.newBuilder();
        builder.setPreprocessing(RegArimaEstimationProto.convert(rslts.getPreprocessing()))
                .setDecomposition(SeatsResultsProto.convert(rslts.getDecomposition()))
                .setFinal(SaProtosUtility.convert(rslts.getFinals()))
                .setDiagnosticsSa(SaProtosUtility.of(rslts.getDiagnostics().getGenericDiagnostics(), rslts.getDiagnostics().getVarianceDecomposition()));
        return builder.build();
    }

 }
