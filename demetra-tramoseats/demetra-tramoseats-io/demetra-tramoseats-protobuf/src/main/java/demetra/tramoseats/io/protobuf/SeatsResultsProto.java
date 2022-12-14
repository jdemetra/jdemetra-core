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

import demetra.modelling.io.protobuf.ModellingProtos;
import demetra.modelling.io.protobuf.ModellingProtosUtility;
import demetra.sa.io.protobuf.SaProtosUtility;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SeatsResultsProto {

    ModellingProtos.UcarimaModel convert(UcarimaModel ucm) {

        ModellingProtos.UcarimaModel.Builder builder = ModellingProtos.UcarimaModel.newBuilder()
                .setModel(ToolkitProtosUtility.convert(ucm.getModel(), "model"));

        if (ucm.getComponentsCount() == 4) {
            return builder
                    .addComplements(ToolkitProtosUtility.convert(ucm.getComplement(1), "seasonallyadjusted"))
                    .addComponents(ToolkitProtosUtility.convert(ucm.getComponent(0), "trend"))
                    .addComponents(ToolkitProtosUtility.convert(ucm.getComponent(1), "seasonal"))
                    .addComponents(ToolkitProtosUtility.convert(ucm.getComponent(2), "transitory"))
                    .addComponents(ToolkitProtosUtility.convert(ucm.getComponent(3), "irregular"))
                    .build();
        } else {
            return builder
                    .addComplements(ToolkitProtosUtility.convert(ucm.getComplement(1), "seasonallyadjusted"))
                    .addComponents(ToolkitProtosUtility.convert(ucm.getComponent(0), "trend"))
                    .addComponents(ToolkitProtosUtility.convert(ucm.getComponent(1), "seasonal"))
                    .addComponents(ToolkitProtosUtility.convert(ucm.getComponent(2), "irregular"))
                    .build();
        }
    }

    public SeatsResults convert(jdplus.seats.SeatsResults seats) {

        return SeatsResults.newBuilder()
                .setSeatsArima(ModellingProtosUtility.convert(seats.getFinalModel(), "seatsmodel"))
                .setMean(seats.isMeanCorrection())
                .setCanonicalDecomposition(convert(seats.getUcarimaModel()))
                .setStochastics(SaProtosUtility.convert(seats.getInitialComponents()))
                .build();
    }
}
