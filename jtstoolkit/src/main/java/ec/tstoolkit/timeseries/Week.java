/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/
package ec.tstoolkit.timeseries;

import ec.tstoolkit.design.Development;
import java.util.Date;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class Week implements IPeriod, Cloneable {

    /**
     * 
     * @param d0
     * @param d1
     * @return
     */
    public static int subtract(final Week d0, final Week d1) {
	return (d0.m_d0 - d1.m_d0) / 7;
    }

    private int m_d0;

    /**
     * 
     * @param d0
     */
    public Week(final Day d0) {
	m_d0 = d0.getId();
    }

    /**
     * 
     * @param day
     * @param start
     */
    public Week(final Day day, final DayOfWeek start) {
	m_d0 = day.getId() - start.intValue();

    }

    Week(final int d0) {
	m_d0 = d0;
    }

    /**
     * Pos is the position of the first complete week in the year (0<pos<52)
     * 
     * @param year 
     * @param pos
     */
    public Week(final int year, final int pos) {
	Day day = new Day(year, Month.January, 0); // 1/1/year
	int dweek = day.getDayOfWeek().intValue();
	int delta = 0;
	if (dweek != 0)
	    delta = 7 - dweek;
	delta += 7 * pos;
	day = day.plus(delta);
	m_d0 = day.getId();
    }

    /**
     * 
     * @param w
     */
    public Week(final Week w) {
	m_d0 = w.m_d0;
    }

    @Override
    public Object clone() {
	return new Week(this);
    }

    /**
     * 
     * @param dt
     * @return
     */
    public boolean contains(final Date dt) {
	int n = new Day(dt).getId() - m_d0;
	return (n >= 0) && (n <= 6);
    }

    /**
     * 
     * @param day
     * @return
     */
    public boolean contains(final Day day) {
	int n = day.getId() - m_d0;
	return (n >= 0) && (n <= 6);
    }

    /**
     * 
     * @param w1
     * @return
     */
    public int difference(final Week w1) {
	return (m_d0 - w1.m_d0) / 7;
    }

    // public static int operator -(Week d0, Week d1)
    // { return (d0.m_d0 - d1.m_d0) / 7; }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Week && equals((Week) obj));
    }
    
    private boolean equals(Week other) {
        return other.m_d0 == m_d0;
    }

    /**
     * 
     * @return
     */
    public Day firstday()
    {
	return new Day(m_d0);
    }

    int getId() {
	return m_d0;
    }

    @Override
    public int hashCode() {
	return m_d0;
    }

    /**
     * 
     * @return
     */
    public Day lastday() {
	return new Day(m_d0 + 6);
    }

    /**
     * 
     * @param nWeeks
     */
    public void move(final int nWeeks)
    {
	m_d0 += nWeeks * 7;
    }

    /**
     * 
     * @param start
     */
    public void set(final Date start) {
	m_d0 = new Day(start).getId();
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder(32);
	builder.append('[').append(firstday().toString()).append('-').append(
		lastday().toString()).append(']');
	return builder.toString();
    }

}
