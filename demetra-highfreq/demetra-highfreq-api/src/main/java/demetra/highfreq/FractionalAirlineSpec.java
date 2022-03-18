/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.math.matrices.Matrix;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.timeseries.calendars.HolidaysOption;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(toBuilder=true)
public class FractionalAirlineSpec implements ProcSpecification{
    
    
    // series
    private boolean log;
    
    // regression variables
    private boolean meanCorrection;
    private Matrix X;
    private String[] Xnames;
    
    private String calendar;
    private HolidaysOption holidaysOption;
    private boolean single;
    
    // Periodic airline model
    private double[] periodicities;
    private int differencingOrder;
    private boolean ar;
    
    // automatic outliers detection
    private String[] outliers;
    private double criticalValue;
    
    // operational
    private boolean adjustToInt;
    private double precision;
    private boolean approximateHessian;
    
    public static final String[] NO_OUTLIER=new String[0];
    public static final double[] NO_PERIOD=new double[0];
    
    private static final String[] ALL_OUTLIERS=new String[]{"AO", "WO", "LS"};
   
    public static Builder builder(){
        return new Builder()
                .differencingOrder(-1)
                .criticalValue(8)
                .log(false)
                .meanCorrection(false)
                .periodicities(NO_PERIOD)
                .outliers(ALL_OUTLIERS)
                .holidaysOption(HolidaysOption.Default)
                .precision(1e-9);
    }
    
    public static final FractionalAirlineSpec DEFAULT_Y=builder()
            .periodicities(new double[]{7, 365.25})
            .adjustToInt(true)
            .build();
    
    public static final FractionalAirlineSpec DEFAULT_FY=builder()
            .periodicities(new double[]{7, 365.25})
            .adjustToInt(false)
            .build();

    public static final FractionalAirlineSpec DEFAULT_W=builder()
            .periodicities(new double[]{365.25/7})
            .adjustToInt(true)
            .build();
    
    public static final FractionalAirlineSpec DEFAULT_FW=builder()
            .periodicities(new double[]{365.25/7})
            .adjustToInt(false)
            .build();

    public static final String METHOD = "fractionalairline";
    public static final String FAMILY = "Modelling";
    public static final String VERSION = "0.1.0.0";


    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    }
    
    @Override
    public String display(){
        return "Fractional airline";
    }
    
}
