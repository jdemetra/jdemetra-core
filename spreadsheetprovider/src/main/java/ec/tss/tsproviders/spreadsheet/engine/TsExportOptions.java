/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package ec.tss.tsproviders.spreadsheet.engine;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public final class TsExportOptions {

    @NonNull
    public static TsExportOptions create(boolean vertical, boolean showDates, boolean showTitle, boolean beginPeriod) {
        return new TsExportOptions(vertical, showDates, showTitle, beginPeriod);
    }

    @NonNull
    public static TsExportOptions getDefault() {
        return new TsExportOptions(true, true, true, true);
    }

    /**
     * true : one series per column, false : one series per line
     */
    private final boolean vertical;
    /**
     * show or not the dates
     */
    private final boolean showDates;
    /**
     * show or not the titles of the series
     */
    private final boolean showTitle;
    /**
     * true to set the dates at the beginning of the period, false for the end
     * of the period
     */
    private final boolean beginPeriod;

    //<editor-fold defaultstate="collapsed" desc="Generated code">
    private TsExportOptions(boolean vertical, boolean showDates, boolean showTitle, boolean beginPeriod) {
        this.vertical = vertical;
        this.showDates = showDates;
        this.showTitle = showTitle;
        this.beginPeriod = beginPeriod;
    }

    public boolean isVertical() {
        return vertical;
    }

    public boolean isShowDates() {
        return showDates;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public boolean isBeginPeriod() {
        return beginPeriod;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.vertical ? 1 : 0);
        hash = 59 * hash + (this.showDates ? 1 : 0);
        hash = 59 * hash + (this.showTitle ? 1 : 0);
        hash = 59 * hash + (this.beginPeriod ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TsExportOptions other = (TsExportOptions) obj;
        if (this.vertical != other.vertical) {
            return false;
        }
        if (this.showDates != other.showDates) {
            return false;
        }
        if (this.showTitle != other.showTitle) {
            return false;
        }
        if (this.beginPeriod != other.beginPeriod) {
            return false;
        }
        return true;
    }
    //</editor-fold>
}
