/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.demetra.xml.sa.x13;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x11.X11Specification;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for X11SpecType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="X11SpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Mode" type="{ec/eurostat/jdemetra/sa}DecompositionEnum"/&gt;
 *         &lt;element name="Seasonal" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="Forecasts" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="Backcasts" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="LowerSigma" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.5"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="UpperSigma" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double"&gt;
 *               &lt;minExclusive value="0.5"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="SeasonalMA" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="Filter" type="{ec/eurostat/jdemetra/sa/x13}SeasonalFilterEnum"/&gt;
 *                   &lt;element name="Filters" type="{ec/eurostat/jdemetra/sa/x13}SeasonalFilters"/&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TrendMA" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}unsignedInt"&gt;
 *               &lt;maxInclusive value="99"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="CalendarSigma" type="{ec/eurostat/jdemetra/sa/x13}CalendarSigmaEnum"/&gt;
 *           &lt;element name="SigmaVec" type="{ec/eurostat/jdemetra/core}UnsignedInts"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="ExcludeForecasts" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "X11SpecType", propOrder = {
    "mode",
    "seasonal",
    "forecasts",
    "backcasts",
    "lowerSigma",
    "upperSigma",
    "seasonalMA",
    "trendMA",
    "calendarSigma",
    "sigmaVec",
    "excludeForecasts"
})
public class XmlX11Spec {

    @XmlElement(name = "Mode", required = true)
    @XmlSchemaType(name = "NMTOKEN")
    protected DecompositionMode mode;
    @XmlElement(name = "Seasonal", defaultValue = "true")
    protected Boolean seasonal;
    @XmlElement(name = "Forecasts", defaultValue = "-1")
    protected Integer forecasts;
    @XmlElement(name = "Backcasts", defaultValue = "0")
    protected Integer backcasts;
    @XmlElement(name = "LowerSigma", defaultValue = "1.5")
    protected Double lowerSigma;
    @XmlElement(name = "UpperSigma", defaultValue = "2.5")
    protected Double upperSigma;
    @XmlElement(name = "SeasonalMA")
    protected SeasonalMA seasonalMA;
    @XmlElement(name = "TrendMA")
    protected Integer trendMA;
    @XmlElement(name = "CalendarSigma")
    @XmlSchemaType(name = "NMTOKEN")
    protected CalendarSigma calendarSigma;
    @XmlList
    @XmlElement(name = "SigmaVec", type = Integer.class)
    protected int[] sigmaVec;
    @XmlElement(name = "ExcludeForecasts", defaultValue = "false")
    protected Boolean excludeForecasts;

    /**
     * Gets the value of the mode property.
     *
     * @return possible object is {@link DecompositionEnum }
     *
     */
    public DecompositionMode getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     *
     * @param value allowed object is {@link DecompositionEnum }
     *
     */
    public void setMode(DecompositionMode value) {
        this.mode = value;
    }

    /**
     * Gets the value of the seasonal property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isSeasonal() {
        return seasonal;
    }

    /**
     * Sets the value of the seasonal property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setSeasonal(Boolean value) {
        if (value != null && value) {
            this.seasonal = null;
        } else {
            this.seasonal = value;
        }
    }

    /**
     * Gets the value of the forecasts property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getForecasts() {
        return forecasts;
    }

    /**
     * Sets the value of the forecasts property.
     *
     * @param value allowed object is {@link Integer }
     *
     */
    public void setForecasts(Integer value) {
        if (value != null && value == X11Specification.DEF_FCASTS) {
            this.forecasts = null;
        } else {
            this.forecasts = value;
        }
    }

    /**
     * Gets the value of the backcasts property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getBackcasts() {
        return backcasts;
    }

    /**
     * Sets the value of the backcasts property.
     *
     * @param value allowed object is {@link Integer }
     *
     */
    public void setBackcasts(Integer value) {
        if (value != null && value == 0) {
            this.forecasts = null;
        } else {
            this.backcasts = value;
        }
    }

    /**
     * Gets the value of the lowerSigma property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getLowerSigma() {
        return lowerSigma;
    }

    /**
     * Sets the value of the lowerSigma property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setLowerSigma(Double value) {
        if (value != null && value == X11Specification.DEF_LSIGMA) {
            this.lowerSigma = null;
        } else {
            this.lowerSigma = value;
        }
    }

    /**
     * Gets the value of the upperSigma property.
     *
     * @return possible object is {@link Double }
     *
     */
    public Double getUpperSigma() {
        return upperSigma;
    }

    /**
     * Sets the value of the upperSigma property.
     *
     * @param value allowed object is {@link Double }
     *
     */
    public void setUpperSigma(Double value) {
        if (value != null && value == X11Specification.DEF_USIGMA) {
            this.upperSigma = null;
        } else {
            this.upperSigma = value;
        }
    }

    /**
     * Gets the value of the seasonalMA property.
     *
     * @return possible object is {@link X11SpecType.SeasonalMA }
     *
     */
    public SeasonalMA getSeasonalMA() {
        return seasonalMA;
    }

    /**
     * Sets the value of the seasonalMA property.
     *
     * @param value allowed object is {@link X11SpecType.SeasonalMA }
     *
     */
    public void setSeasonalMA(SeasonalMA value) {
        this.seasonalMA = value;
    }

    /**
     * Gets the value of the trendMA property.
     *
     * @return possible object is {@link Long }
     *
     */
    public Integer getTrendMA() {
        return trendMA;
    }

    /**
     * Sets the value of the trendMA property.
     *
     * @param value allowed object is {@link Long }
     *
     */
    public void setTrendMA(Integer value) {
        if (value != null && value == 0) {
            this.trendMA = null;
        } else {
            this.trendMA = value;
        }
    }

    /**
     * Gets the value of the calendarSigma property.
     *
     * @return possible object is {@link CalendarSigmaEnum }
     *
     */
    public CalendarSigma getCalendarSigma() {
        return calendarSigma;
    }

    /**
     * Sets the value of the calendarSigma property.
     *
     * @param value allowed object is {@link CalendarSigmaEnum }
     *
     */
    public void setCalendarSigma(CalendarSigma value) {
        this.calendarSigma = value;
    }

    /**
     * Gets the value of the sigmaVec property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the sigmaVec property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSigmaVec().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Long }
     *
     *
     */
    public int[] getSigmaVec() {
        return this.sigmaVec;
    }

    public void setSigmaVec(int[] value) {
        this.sigmaVec = value;
    }

    /**
     * Gets the value of the excludeForecasts property.
     *
     * @return possible object is {@link Boolean }
     *
     */
    public Boolean isExcludeForecasts() {
        return excludeForecasts;
    }

    /**
     * Sets the value of the excludeForecasts property.
     *
     * @param value allowed object is {@link Boolean }
     *
     */
    public void setExcludeForecasts(Boolean value) {
        if (value != null && !value) {
            this.excludeForecasts = null;
        } else {
            this.excludeForecasts = value;
        }
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     *
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;choice&gt;
     *         &lt;element name="Filter" type="{ec/eurostat/jdemetra/sa/x13}SeasonalFilterEnum"/&gt;
     *         &lt;element name="Filters" type="{ec/eurostat/jdemetra/sa/x13}SeasonalFilters"/&gt;
     *       &lt;/choice&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "filter",
        "filters"
    })
    public static class SeasonalMA {

        @XmlElement(name = "Filter")
        @XmlSchemaType(name = "NMTOKEN")
        protected SeasonalFilterOption filter;
        @XmlList
        @XmlElement(name = "Filters")
        protected SeasonalFilterOption[] filters;

        /**
         * Gets the value of the filter property.
         *
         * @return possible object is {@link SeasonalFilterEnum }
         *
         */
        public SeasonalFilterOption getFilter() {
            return filter;
        }

        /**
         * Sets the value of the filter property.
         *
         * @param value allowed object is {@link SeasonalFilterEnum }
         *
         */
        public void setFilter(SeasonalFilterOption value) {
            this.filter = value;
        }

        public SeasonalFilterOption[] getFilters() {
            return this.filters;
        }

        public void setFilters(SeasonalFilterOption[] value) {
            this.filters = value;
        }
    }

    public static final InPlaceXmlMarshaller<XmlX11Spec, X11Specification> MARSHALLER = (X11Specification v, XmlX11Spec xml) -> {
        if (v.isDefault()) {
            return true;
        }
        xml.setMode(v.getMode());
        //xml.setBackcasts(v.getBackcastHorizon);
        xml.setForecasts(v.getForecastHorizon());
        xml.setLowerSigma(v.getLowerSigma());
        xml.setUpperSigma(v.getUpperSigma());
        xml.setTrendMA(v.getHendersonFilterLength());
        xml.setExcludeForecasts(v.isExcludefcst());
        xml.setSeasonal(v.isSeasonal());
        SeasonalFilterOption[] seasonalFilters = v.getSeasonalFilters();
        if (seasonalFilters != null) {
            SeasonalMA sma = new SeasonalMA();
            if (seasonalFilters.length == 1) {
                sma.filter = seasonalFilters[0];
            } else {
                sma.filters = seasonalFilters;
            }
            xml.setSeasonalMA(sma);
        }
        CalendarSigma cs = v.getCalendarSigma();
        if (cs != CalendarSigma.None){
            xml.setCalendarSigma(cs);
            if (cs == CalendarSigma.Select)
                xml.setSigmaVec(convert(v.getSigmavec()));
        }
        return true;
    };

    public static final InPlaceXmlUnmarshaller<XmlX11Spec, X11Specification> UNMARSHALLER = (XmlX11Spec xml, X11Specification v) -> {
        if (xml.mode != null)
            v.setMode(xml.mode);
        if (xml.forecasts != null)
            v.setForecastHorizon(xml.forecasts);
        if (xml.trendMA != null)
            v.setHendersonFilterLength(xml.trendMA);
        if (xml.seasonal != null)
            v.setSeasonal(xml.seasonal);
        if (xml.excludeForecasts != null)
            v.setExcludefcst(xml.excludeForecasts);
        if (xml.seasonalMA != null){
            if (xml.seasonalMA.filter != null)
                v.setSeasonalFilter(xml.seasonalMA.filter);
            else if (xml.seasonalMA.filters != null)
                v.setSeasonalFilters(xml.seasonalMA.filters);
        }
        if (xml.lowerSigma != null)
            v.setLowerSigma(xml.lowerSigma);
        if (xml.upperSigma != null)
            v.setUpperSigma(xml.upperSigma);
        if (xml.calendarSigma != null){
            v.setCalendarSigma(xml.calendarSigma);
            if (xml.sigmaVec != null)
                v.setSigmavec(convert(xml.sigmaVec));
        }
        return true;
    };
    
    private static int[] convert(SigmavecOption[] sv){
        if (sv == null)
            return null;
        int[] v=new int[sv.length];
        for (int i=0; i<sv.length; ++i){
            if (sv[i]==SigmavecOption.Group1)
                v[i]=1;
            else
                v[i]=2;
        }
        return v;
    }
    
    private static SigmavecOption[] convert(int[] v){
        if (v == null)
            return null;
        SigmavecOption[] sv=new SigmavecOption[v.length];
        for (int i=0; i<v.length; ++i){
            if (v[i]==1)
                sv[i]=SigmavecOption.Group1;
            else
                sv[i]=SigmavecOption.Group2;
        }
        return sv;
    }
}
