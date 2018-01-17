/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.dfm;

import demetra.timeseries.TimeSeriesSelector;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author palatej
 */
public class PcSpec implements Cloneable {


//    private boolean enabled_;
//    private double ns_ = PcInitializer.DEF_NS;
//    private TsPeriodSelector span_ = TsPeriodSelector.all();
//
//    public void setSpan(TsPeriodSelector sel) {
//        if (sel == null) {
//            throw new IllegalArgumentException("Span");
//        }
//        span_ = sel;
//    }
//
//    public TsPeriodSelector getSpan() {
//        return span_;
//    }
//
//    public void setEnabled(boolean use) {
//        enabled_ = use;
//    }
//
//    public boolean isEnabled() {
//        return enabled_;
//    }
//
//    public double getMinPartNonMissingSeries() {
//        return ns_;
//    }
//
//    public void setMinPartNonMissingSeries(double ns) {
//        if (ns <= 0 || ns > 1) {
//            throw new IllegalArgumentException("MinPartNonMissingSeries");
//        }
//        ns_ = ns;
//    }
//
//    @Override
//    public PcSpec clone() {
//        try {
//            PcSpec spec = (PcSpec) super.clone();
//            spec.span_ = span_.clone();
//            return spec;
//        } catch (CloneNotSupportedException ex) {
//            throw new AssertionError();
//        }
//    }
//
//    @Override
//    public InformationSet write(boolean verbose) {
//        InformationSet info = new InformationSet();
//        info.set(ENABLED, enabled_);
//        if (span_.getType() != PeriodSelectorType.All || verbose) {
//            info.set(SPAN, span_);
//        }
//        if (ns_ != PcInitializer.DEF_NS || verbose) {
//            info.set(NS, ns_);
//        }
//        return info;
//    }
//
//    @Override
//    public boolean read(InformationSet info) {
//        if (info == null) {
//            return true;
//        }
//        Boolean enabled = info.get(ENABLED, Boolean.class);
//        if (enabled != null) {
//            enabled_ = enabled;
//        }
//        TsPeriodSelector span = info.get(SPAN, TsPeriodSelector.class);
//        if (span != null) {
//            span_ = span;
//        }
//        Double ns = info.get(NS, Double.class);
//        if (ns != null) {
//            ns_ = ns;
//        }
//        return true;
//    }
//    
//   @Override
//    public boolean equals(Object obj) {
//        return this == obj || (obj instanceof PcSpec && equals((PcSpec) obj));
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 23 * hash + (this.enabled_ ? 1 : 0);
//        hash = 23 * hash + (int) (Double.doubleToLongBits(this.ns_) ^ (Double.doubleToLongBits(this.ns_) >>> 32));
//        hash = 23 * hash + Objects.hashCode(this.span_);
//        return hash;
//    }
//
//    public boolean equals(PcSpec obj){
//        return enabled_==obj.enabled_ && span_.equals(obj.span_)
//                && ns_==obj.ns_;
//    }
//
//    public static void fillDictionary(String prefix, Map<String, Class> dic) {
//        dic.put(InformationSet.item(prefix, ENABLED), Boolean.class);
//        dic.put(InformationSet.item(prefix, SPAN), TsPeriodSelector.class);
//        dic.put(InformationSet.item(prefix, NS), Double.class);
//    }
//
}
