/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.calendars;

import demetra.design.Development;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class MovingHoliday {

    public static enum Shape {
        Constant,
        LinearUp,
        LinearDown
    }
    
    @lombok.Value
    public static class Part {

        private int start, end;
        @lombok.NonNull
        private Shape shape;

        /**
         * Part of a moving holiday. Days are expressed in differences in
         * comparison middle the reference event
         *
         * @param start Starting day (included)
         * @param end Ending day (excluded)
         * @param shape
         */
        public Part(int start, int end, Shape shape) {
            if (end <= start) {
                throw new IllegalArgumentException();
            }
            this.start = start;
            this.end = end;
            this.shape = shape;
        }
    }
    @lombok.NonNull
    private String event;

    private Part begin, middle, end;

    /**
     *
     * @param event
     * @param begin
     * @param middle
     * @param end
     */
    public MovingHoliday(String event, Part begin, Part middle, Part end) {
        check(begin, middle, end);
        this.event = event;
        this.begin = begin;
        this.middle = middle;
        this.end = end;
    }

    private static void check(Part begin, Part middle, Part end) {
        if (begin != null && middle != null && begin.end > middle.start) {
            throw new IllegalArgumentException();
        }
        if (begin != null && end != null && begin.end > end.start) {
            throw new IllegalArgumentException();
        }
        if (middle != null && end != null && middle.end > end.start) {
            throw new IllegalArgumentException();
        }
    }

}
