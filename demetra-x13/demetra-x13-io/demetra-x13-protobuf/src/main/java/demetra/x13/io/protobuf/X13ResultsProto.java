/*
 * Copyright 2021 National Bank of Belgium
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
package demetra.x13.io.protobuf;

import demetra.regarima.io.protobuf.RegArimaEstimationProto;
import demetra.sa.io.protobuf.SaProtos;
import demetra.sa.io.protobuf.SaProtosUtility;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import jdplus.x13.X13Finals;
import jdplus.x13.X13Preadjustment;
import jdplus.x13.Mstatistics;
import jdplus.x13.X13Diagnostics;
import jdplus.x13.X13Results;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X13ResultsProto {
    
    public X13Protos.X13Finals convert(X13Preadjustment preadjust, X13Finals finals){
        return X13Protos.X13Finals.newBuilder()
                .setA1(ToolkitProtosUtility.convert(preadjust.getA1()))
                .setA1A(ToolkitProtosUtility.convert(preadjust.getA1a()))
                .setA6(ToolkitProtosUtility.convert(preadjust.getA6()))
                .setA7(ToolkitProtosUtility.convert(preadjust.getA7()))
                .setA8(ToolkitProtosUtility.convert(preadjust.getA8()))
                .setA9(ToolkitProtosUtility.convert(preadjust.getA9()))
                .setD10Final(ToolkitProtosUtility.convert(finals.getD10final()))
                .setD10Final(ToolkitProtosUtility.convert(finals.getD10final()))
                .setD11Final(ToolkitProtosUtility.convert(finals.getD11final()))
                .setD12Final(ToolkitProtosUtility.convert(finals.getD12final()))
                .setD12Final(ToolkitProtosUtility.convert(finals.getD12final()))
                .setD13Final(ToolkitProtosUtility.convert(finals.getD13final()))
                .setD16(ToolkitProtosUtility.convert(finals.getD16()))
                .setD18(ToolkitProtosUtility.convert(finals.getD18()))
                .setD10A(ToolkitProtosUtility.convert(finals.getD10a()))
                .setD11A(ToolkitProtosUtility.convert(finals.getD11a()))
                .setD12A(ToolkitProtosUtility.convert(finals.getD12a()))
                .setD16A(ToolkitProtosUtility.convert(finals.getD16a()))
                .setD18A(ToolkitProtosUtility.convert(finals.getD18a()))
                .setE1(ToolkitProtosUtility.convert(finals.getE1()))
                .setE2(ToolkitProtosUtility.convert(finals.getE2()))
                .setE3(ToolkitProtosUtility.convert(finals.getE3()))
                .setE11(ToolkitProtosUtility.convert(finals.getE11()))
                .build();
    }
    
    public X13Protos.X13Results convert(X13Results rslts) {
        X13Protos.X13Results.Builder builder = X13Protos.X13Results.newBuilder();
        X13Diagnostics diags=rslts.getDiagnostics();
        builder.setPreprocessing(RegArimaEstimationProto.convert(rslts.getPreprocessing()))
                .setDecomposition(X11ResultsProto.convert(rslts.getDecomposition()))
                .setFinal(convert(rslts.getPreadjustment(), rslts.getFinals()))
                .setDiagnosticsX13(X13Protos.Diagnostics.newBuilder()
                        .setMstatistics(convert(diags.getMstatistics()))
                        .build())
                .setDiagnosticsSa(SaProtosUtility.of(diags.getGenericDiagnostics(), 
                        diags.getVarianceDecomposition()));
        return builder.build();
    }
    
    public X13Protos.MStatistics convert(Mstatistics mstats){
        return X13Protos.MStatistics.newBuilder()
                .setM1(mstats.getM(1))
                .setM2(mstats.getM(2))
                .setM3(mstats.getM(3))
                .setM4(mstats.getM(4))
                .setM5(mstats.getM(5))
                .setM6(mstats.getM(6))
                .setM7(mstats.getM(7))
                .setM8(mstats.getM(8))
                .setM9(mstats.getM(9))
                .setM10(mstats.getM(10))
                .setM11(mstats.getM(11))
                .setQ(mstats.getQ())
                .setQm2(mstats.getQm2())
                .build();
    }

    public SaProtos.VarianceDecomposition of(Mstatistics mstatistics) {
        return SaProtos.VarianceDecomposition.newBuilder()
                .setCycle(mstatistics.getVarC())
                .setSeasonal(mstatistics.getVarS())
                .setIrregular(mstatistics.getVarI())
                .setCalendar(mstatistics.getVarTD())
                .setOthers(mstatistics.getVarP())
                .setTotal(mstatistics.getVarTotal())
                .build();
    }
}
