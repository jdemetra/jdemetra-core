/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.simplets;

import demetra.data.Doubles;
import demetra.data.transformation.DataTransformation;
import demetra.data.transformation.LogJacobian;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;

/**
 *
 * @author palatej
 */
public class GenericTransformation implements TsDataTransformation {

    private final DataTransformation dataTransformation;

    public GenericTransformation(final DataTransformation dataTransformation) {
        this.dataTransformation = dataTransformation;
    }

    @Override
    public TsDataTransformation converse() {
        return new GenericTransformation(dataTransformation.converse());
    }

    @Override
    public TsData transform(TsData data, LogJacobian logjacobian) {
        return TsData.of(data.getStart(), Doubles.of(dataTransformation.transform(data.getValues(), logjacobian)));
    }

    @Override
    public double transform(TsPeriod period, double value) {
        return dataTransformation.transform(value);
    }
}