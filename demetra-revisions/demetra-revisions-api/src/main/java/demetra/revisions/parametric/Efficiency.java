/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.revisions.parametric;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class Efficiency {
    enum Type{
        Preliminary,
        Previous
    }
    
    Type type;
    double b0, b1;
    
    RegressionTests tests;
}
