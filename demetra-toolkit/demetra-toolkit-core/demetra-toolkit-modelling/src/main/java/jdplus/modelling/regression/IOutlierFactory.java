/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import jdplus.data.DataBlock;
import demetra.design.Development;
import demetra.modelling.regression.IOutlier;
import jdplus.maths.linearfilters.RationalBackFilter;
import java.time.LocalDateTime;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
public interface IOutlierFactory {

    public static class FilterRepresentation {

        public final RationalBackFilter filter;
        public final double correction;

        public FilterRepresentation(RationalBackFilter filter, double correction) {
            this.filter = filter;
            this.correction = correction;
        }
    }

    /**
     * Creates an outlier at the given position
     *
     * @param position The position of the outlier.
     * @return A new variable is returned.
     */
    IOutlier make(LocalDateTime position);

    /**
     * Fills the buffer with an outlier positioned at outlierPosition. The
     * position should be insied the buffer
     *
     * @param outlierPosition
     * @param buffer
     */
    void fill(int outlierPosition, DataBlock buffer);

    /**
     * Filter representation of this type of outlier.
     *
     * @return
     */
    FilterRepresentation getFilterRepresentation();

    /**
     * Some outliers cannot be identified at the beginning of a series. This
     * method returns the number of such periods
     *
     * @return A positive or zero integer
     */
    int excludingZoneAtStart();

    /**
     * Some outliers cannot be identified at the end of a series. This method
     * returns the number of such periods
     *
     * @return A positive or zero integer
     */
    int excludingZoneAtEnd();

    /**
     * The code that represents the outlier
     *
     * @return
     */
    String getCode();
}
