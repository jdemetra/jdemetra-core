/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.revisions.timeseries;

/**
 *
 * @author PALATEJ
 */
@Deprecated
public enum VintageSelectorType {
    /**
     * Takes all vintages
     */
    All,
    /**
     * Last vintages
     */
    Last,
    /**
     * First vintages
     */
    First,
    /**
     * All vintages but...
     */
    Excluding,
    /**
     * Custom selection
     */
    Custom;
}