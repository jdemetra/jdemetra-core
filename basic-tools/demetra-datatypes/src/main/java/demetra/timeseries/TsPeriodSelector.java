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
package demetra.timeseries;

import demetra.design.Development;
import demetra.design.Immutable;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Defines selection in a time domain
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@Immutable
public class TsPeriodSelector implements Cloneable {

    public static enum SelectionType {
        /**
         *
         */
        None,
        /**
         *
         */
        All,
        /**
         *
         */
        From,
        /**
         *
         */
        To,
        /**
         *
         */
        Between,
        /**
         *
         */
        Last,
        /**
         *
         */
        First,
        /**
         *
         */
        Excluding;
    }

    public boolean equals(TsPeriodSelector ps) {
        if (ps == this) {
            return true;
        }
        if (ps == null && type == SelectionType.All) {
            return true;
        }
        if (type != ps.type) {
            return false;
        }
        switch (type) {
            case Excluding:
                return n0 == ps.n0 && n1 == ps.n1;
            case Last:
                return n1 == ps.n1;
            case First:
                return n0 == ps.n0;
            case Between:
                return d0.equals(ps.d0) && d1.equals(ps.d1);
            case From:
                return d0.equals(ps.d0);
            case To:
                return d1.equals(ps.d1);
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
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + this.n0;
        hash = 97 * hash + this.n1;
        return hash;
    }

    private final SelectionType type;
    private final LocalDateTime d0, d1;
    private final int n0, n1;

     private TsPeriodSelector(SelectionType type, LocalDateTime d0, LocalDateTime d1, int n0, int n1) {
        this.type=type;
        this.d0=d0;
        this.d1=d1;
        this.n0=n0;
        this.n1=n1;
    }

    /**
     * Select all the periods
     */
    public static TsPeriodSelector all() {
        return new TsPeriodSelector(SelectionType.All, null, null, 0, 0);
    }

    /**
     * Select all the periods between two days. The way incomplete periods are considered
     * is left to the classes that use the selector.
     *
     * @param d0 The starting day
     * @param d1 The ending day
     */
    public static TsPeriodSelector between(@Nonnull final LocalDateTime d0, @Nonnull final LocalDateTime d1) {
        return new TsPeriodSelector(SelectionType.Between, d0, d1, 0, 0);
     }

    /**
     * Excludes some periods at the beginning and/or at the end of a time domain
     *
     * @param n0 Number of periods excluded at the beginning of the time domain. Greater or equal to 0.
     * @param n1 Number of periods excluded at the end of the time domain. Greater or equal to 0.
     */
    public static TsPeriodSelector excluding(final int n0, final int n1) {
         return new TsPeriodSelector(SelectionType.Excluding, null, null, n0, n1);
    }

    /**
     * Select a given number of periods at the beginning of a time domain
     *
     * @param n The number of selected periods
     */
    public static TsPeriodSelector first(final int n) {
        return new TsPeriodSelector(SelectionType.First, null, null, n, 0);
    }

    /**
     * Select the periods after a given date. The way incomplete periods are considered
     * is left to the classes that use the selector.
     *
     * @param d0 The date for the selection
     */
    public static TsPeriodSelector from(@Nonnull final LocalDateTime d0) {
         return new TsPeriodSelector(SelectionType.From, d0, null, 0, 0);
    }

    /**
     * The starting day of the selector. Its interpretation depends on on the type of the selector
     *
     * @return
     */
    public LocalDateTime getD0() {
        return d0;
    }

    /**
     * The ending day of the selector. Its interpretation depends on on the type of the selector. May be unused
     *
     * @return
     */
    public LocalDateTime getD1() {
        return d1;
    }

    /**
     * The number of starting periods defined by the selector. Its interpretation depends on on the type of the selector. May be unused
     *
     * @return
     */
    public int getN0() {
        return n0;
    }

    /**
     * The number of ending periods defined by the selector. Its interpretation depends on on the type of the selector. May be unused
     *
     * @return
     */
    public int getN1() {
        return n1;
    }

    /**
     * The type of the selector
     *
     * @return
     */
    public SelectionType getType() {
        return type;
    }

    /**
     * Select a given number of periods at the end of a time domain
     *
     * @param n The number of selected periods
     */
    public static TsPeriodSelector last(final int n) {
        return new TsPeriodSelector(SelectionType.Last, null, null, 0, n);
    }

    /**
     * Select nothing
     */
    public static TsPeriodSelector none() {
        return new TsPeriodSelector(SelectionType.None, null, null, 0, 0);
    }

    /**
     * Select the periods up to a given date. The way incomplete periods are considered
     * is left to the classes that use the selector.
     *
     * @param d1 The date for the selection
     */
    public static TsPeriodSelector to(final LocalDateTime d1) {
        return new TsPeriodSelector(SelectionType.To, null, d1, 0, 0);
    }

    @Override
    public String toString() {
        switch (type) {
            case Between:
                return d0.toString() + " - " + d1.toString();
            case Excluding: {
                if (n0 == 0 && n1 == 0) {
                    return "";
                }
                StringBuilder builder = new StringBuilder();
                builder.append("All but ");
                if (n0 != 0) {
                    builder.append("first ");
                    if (n0 > 1) {
                        builder.append(n0).append(" periods");
                    } else if (n0 > 0) {
                        builder.append("period");
                    } else if (n0 < -1) {
                        builder.append(-n0).append(" years");
                    } else if (n0 < 0) {
                        builder.append("year");
                    }
                    if (n1 != 0) {
                        builder.append(" and ");
                    }
                }
                if (n1 != 0) {
                    builder.append("last ");
                    if (n1 > 1) {
                        builder.append(n1).append(" periods");
                    } else if (n1 > 0) {
                        builder.append("period");
                    } else if (n1 < -1) {
                        builder.append(-n1).append(" years");
                    } else if (n1 < 0) {
                        builder.append("year");
                    }
                }
                return builder.toString();
            }
            case First: {
                StringBuilder builder = new StringBuilder();
                if (n0 > 0) {
                    builder.append("first ");
                    if (n0 > 1) {
                        builder.append(n0).append(" periods");
                    } else {
                        builder.append("period");
                    }
                    if (n1 > 0) {
                        builder.append(" and ");
                    }
                }
                return builder.toString();
            }
            case Last: {
                StringBuilder builder = new StringBuilder();
                if (n1 > 0) {
                    builder.append("last ");
                    if (n1 > 1) {
                        builder.append(n1).append(" periods");
                    } else {
                        builder.append("period");
                    }
                }
                return builder.toString();
            }
            case From:
                return "From " + d0.toString();
            case To:
                return "Until " + d1.toString();
            case All:
                return "All";
            case None:
                return "None";
            default:
                return "";
        }
    }

    public boolean isAll() {
        return type == SelectionType.All;
    }

    public boolean isNone() {
        return type == SelectionType.None;
    }
}
