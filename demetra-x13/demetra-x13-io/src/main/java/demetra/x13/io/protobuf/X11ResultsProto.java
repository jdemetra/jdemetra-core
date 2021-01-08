/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.io.protobuf;

import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import demetra.x11.X11Results;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X11ResultsProto {

    public X13ResultsProtos.X11Results convert(X11Results x11) {
        return X13ResultsProtos.X11Results.newBuilder()
                .setMode(X13ProtosUtility.convert(x11.getMode()))
                .setB1(ToolkitProtosUtility.convert(x11.getB1()))
                .setB2(ToolkitProtosUtility.convert(x11.getB2()))
                .setB3(ToolkitProtosUtility.convert(x11.getB3()))
                .setB4(ToolkitProtosUtility.convert(x11.getB4()))
                .setB5(ToolkitProtosUtility.convert(x11.getB5()))
                .setB6(ToolkitProtosUtility.convert(x11.getB6()))
                .setB7(ToolkitProtosUtility.convert(x11.getB7()))
                .setB8(ToolkitProtosUtility.convert(x11.getB8()))
                .setB9(ToolkitProtosUtility.convert(x11.getB9()))
                .setB10(ToolkitProtosUtility.convert(x11.getB10()))
                .setB11(ToolkitProtosUtility.convert(x11.getB11()))
                .setB13(ToolkitProtosUtility.convert(x11.getB13()))
                .setB17(ToolkitProtosUtility.convert(x11.getB17()))
                .setB20(ToolkitProtosUtility.convert(x11.getB20()))
                .setC1(ToolkitProtosUtility.convert(x11.getC1()))
                .setC2(ToolkitProtosUtility.convert(x11.getC2()))
                .setC4(ToolkitProtosUtility.convert(x11.getC4()))
                .setC5(ToolkitProtosUtility.convert(x11.getC5()))
                .setC6(ToolkitProtosUtility.convert(x11.getC6()))
                .setC7(ToolkitProtosUtility.convert(x11.getC7()))
                .setC9(ToolkitProtosUtility.convert(x11.getC9()))
                .setC10(ToolkitProtosUtility.convert(x11.getC10()))
                .setC11(ToolkitProtosUtility.convert(x11.getC11()))
                .setC13(ToolkitProtosUtility.convert(x11.getC13()))
                .setC17(ToolkitProtosUtility.convert(x11.getC17()))
                .setC20(ToolkitProtosUtility.convert(x11.getC20()))
                .setD1(ToolkitProtosUtility.convert(x11.getD1()))
                .setD2(ToolkitProtosUtility.convert(x11.getD2()))
                .setD4(ToolkitProtosUtility.convert(x11.getD4()))
                .setD5(ToolkitProtosUtility.convert(x11.getD5()))
                .setD6(ToolkitProtosUtility.convert(x11.getD6()))
                .setD7(ToolkitProtosUtility.convert(x11.getD7()))
                .setD8(ToolkitProtosUtility.convert(x11.getD8()))
                .setD9(ToolkitProtosUtility.convert(x11.getD9()))
                .setD10(ToolkitProtosUtility.convert(x11.getD10()))
                .setD11(ToolkitProtosUtility.convert(x11.getD11()))
                .setD12(ToolkitProtosUtility.convert(x11.getD12()))
                .setD13(ToolkitProtosUtility.convert(x11.getD13()))
                .setFinalHendersonFilter(x11.getFinalHendersonFilterLength())
//                .addFinalSeasonalFilter(X13ProtosUtility.convert(x11.getFinalSeasonalFilter()[0]))
                .build();
    }
}
