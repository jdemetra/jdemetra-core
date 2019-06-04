/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.data.transformation;

import demetra.design.Development;

    /**
     * Contains the log of the Jacobian of a transformation of a time series.
     * @author Jean Palate
     */
    @Development(status = Development.Status.Alpha)
    public class LogJacobian {

        /**
         * The value of the Jacobian
         */
        public double value;
        /**
         * The starting position (included) of the transformation
         */
        /**
         * The ending position (excluded) of the transformation
         */
        public final int start, end;

        /**
         * Creates a log Jacobian with the limits of the transformation
         *
         * @param start Starting position (included)
         * @param end Ending position (excluded)
         */
        public LogJacobian(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
