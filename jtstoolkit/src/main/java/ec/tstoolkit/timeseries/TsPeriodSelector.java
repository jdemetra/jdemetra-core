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
import java.util.Objects;

/**
 * Defines selection in a time domain
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class TsPeriodSelector implements Cloneable {

    public boolean equals(TsPeriodSelector ps) {
        if (ps == this) {
            return true;
        }
        if (ps == null && type_ == PeriodSelectorType.All) {
            return true;
        }
        if (type_ != ps.type_)
            return false;
        switch (type_){
            case Excluding:
                return n0_ == ps.n0_ && n1_ == ps.n1_;
            case Last:
                return n1_ == ps.n1_;
            case First:
                return n0_ == ps.n0_;
            case Between:
                return d0_.equals(ps.d0_) && d1_.equals(ps.d1_);
            case From:   
                return d0_.equals(ps.d0_);
            case To:
                return d1_.equals(ps.d1_);
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsPeriodSelector && equals((TsPeriodSelector) obj));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.type_);
        hash = 97 * hash + this.n0_;
        hash = 97 * hash + this.n1_;
        return hash;
    }
    
    public static final Day DEF_BEG=new Day(1900, Month.January, 0), DEF_END=new Day(2015, Month.December, 30);
    
    private PeriodSelectorType type_ = PeriodSelectorType.All;
    private Day d0_ = DEF_BEG, d1_ = DEF_END;
    private int n0_, n1_;

    /**
     * Default period selector. By default, it selects all the periods of a domain
     */
    public TsPeriodSelector() {
    }

    /**
     *
     * @param p
     */
    @Deprecated
    public TsPeriodSelector(final TsPeriodSelector p) {
        type_ = p.type_;
        n0_ = p.n0_;
        n1_ = p.n1_;
        d0_ = p.d0_;
        d1_ = p.d1_;
    }

    /**
     * Select all the periods
     */
    public void all() {
        doClear();
    }

    /**
     * Select all the periods between two days. The way incomplete periods are considered
     * is left to the classes that use the selector.
     * @param d0 The starting day
     * @param d1 The ending day
     */
    public void between(final Day d0, final Day d1) {
        doClear();
        if (d1.isBefore(d0)) {
            type_ = PeriodSelectorType.None;
        } else {
            type_ = PeriodSelectorType.Between;
            d0_ = d0;
            d1_ = d1;
        }
    }

    @Override
    public TsPeriodSelector clone() {
        try {
            TsPeriodSelector obj = (TsPeriodSelector) super.clone();
//        if (d0_ != null) {
//            obj.d0_ = d0_;
//        }
//        if (d1_ != null) {
//            obj.d1_ = d1_;
//        }
            return obj;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    private void doClear() {
        n0_ = 0;
        n1_ = 0;
        d0_ = null;
        d1_ = null;
        type_ = PeriodSelectorType.All;
    }


    /**
     * Excludes some periods at the beginning and/or at the end of a time domain
     * @param n0 Number of periods excluded at the beginning of the time domain. Greater or equal to 0.
     * @param n1 Number of periods excluded at the end of the time domain. Greater or equal to 0.
     */
    public void excluding(final int n0, final int n1) {
        doClear();
        type_ = PeriodSelectorType.Excluding;
        n0_ = n0;
        n1_ = n1;
    }

    /**
     * Select a given number of periods at the beginning of a time domain
     * @param n The number of selected periods
     */
    public void first(final int n) {
        doClear();
        type_ = PeriodSelectorType.First;
        n0_ = n;
    }

    /**
     * Select the periods after a given date. The way incomplete periods are considered
     * is left to the classes that use the selector.
     * @param d0 The date for the selection
     */
    public void from(final Day d0) {
        doClear();
        type_ = PeriodSelectorType.From;
        d0_ = d0;
    }

    /**
     * The starting day of the selector. Its interpretation depends on on the type of the selector
     * @return
     */
    public Day getD0() {
        return d0_;
    }

    @Deprecated
    public void setD0(Day d) {
        d0_ = d;
    }

    /**
     * The ending day of the selector. Its interpretation depends on on the type of the selector. May be unused
     * @return
     */
    public Day getD1() {
        return d1_;
    }

    @Deprecated
    public void setD1(Day d) {
        d1_ = d;
    }

    /**
     * The number of starting periods defined by the selector. Its interpretation depends on on the type of the selector. May be unused
     * @return
     */
    public int getN0() {
        return n0_;
    }

    @Deprecated
    public void setN0(int i) {
        n0_ = i;
    }

    /**
     * The number of ending periods defined by the selector. Its interpretation depends on on the type of the selector. May be unused
     * @return
     */
    public int getN1() {
        return n1_;
    }

    @Deprecated
    public void setN1(int i) {
        n1_ = i;
    }

    /**
     * The type of the selector
     * @return
     */
    public PeriodSelectorType getType() {
        return type_;
    }

    @Deprecated
    public void setType(PeriodSelectorType type) {
        type_ = type;
    }

    /**
     * Select a given number of periods at the end of a time domain
     * @param n The number of selected periods
     */
    public void last(final int n) {
        doClear();
        type_ = PeriodSelectorType.Last;
        n1_ = n;
    }

    /**
     * Select nothing
     */
    public void none() {
        doClear();
        type_ = PeriodSelectorType.None;
    }

    /**
     * Select the periods up to a given date. The way incomplete periods are considered
     * is left to the classes that use the selector.
     * @param d1 The date for the selection
     */
    public void to(final Day d1) {
        doClear();
        type_ = PeriodSelectorType.To;
        d1_ = d1;
    }

    @Override
    public String toString() {
        switch (type_) {
            case Between:
                return d0_.toString() + " - " + d1_.toString();
            case Excluding: {
                if (n0_ == 0 && n1_ == 0) {
                    return "";
                }
                StringBuilder builder = new StringBuilder();
                builder.append("All but ");
                if (n0_ != 0) {
                    builder.append("first ");
                    if (n0_ > 1) {
                        builder.append(n0_).append(" periods");
                    } else if (n0_ > 0) {
                        builder.append("period");
                    } else if (n0_ < -1) {
                        builder.append(-n0_).append(" years");
                    } else if (n0_ < 0) {
                        builder.append("year");
                    }
                    if (n1_ != 0) {
                        builder.append(" and ");
                    }
                }
                if (n1_ != 0) {
                    builder.append("last ");
                    if (n1_ > 1) {
                        builder.append(n1_).append(" periods");
                    } else if (n1_ > 0){
                        builder.append("period");
                    } else if (n1_ <-1) {
                        builder.append(-n1_).append(" years");
                    } else if (n1_ < 0){
                        builder.append("year");
                    }
                }
                return builder.toString();
            }
            case First: {
                StringBuilder builder = new StringBuilder();
                if (n0_ > 0) {
                    builder.append("first ");
                    if (n0_ > 1) {
                        builder.append(n0_).append(" periods");
                    } else {
                        builder.append("period");
                    }
                    if (n1_ > 0) {
                        builder.append(" and ");
                    }
                }
                return builder.toString();
            }
            case Last: {
                StringBuilder builder = new StringBuilder();
                if (n1_ > 0) {
                    builder.append("last ");
                    if (n1_ > 1) {
                        builder.append(n1_).append(" periods");
                    } else {
                        builder.append("period");
                    }
                }
                return builder.toString();
            }
            case From:
                return "From " + d0_.toString();
            case To:
                return "Until " + d1_.toString();
            case All:
                return "All";
            case None:
                return "None";
            default:
                return "";
        }
    }
}
