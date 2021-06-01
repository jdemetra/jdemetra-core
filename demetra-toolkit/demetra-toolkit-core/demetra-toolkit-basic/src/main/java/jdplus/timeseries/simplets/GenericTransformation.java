/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.timeseries.simplets;

import demetra.data.Doubles;
import jdplus.data.transformation.DataTransformation;
import jdplus.data.transformation.LogJacobian;
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
        return TsData.ofInternal(data.getStart(), dataTransformation.transform(data.getValues(), logjacobian).toArray());
    }

    @Override
    public double transform(TsPeriod period, double value) {
        return dataTransformation.transform(value);
    }
}