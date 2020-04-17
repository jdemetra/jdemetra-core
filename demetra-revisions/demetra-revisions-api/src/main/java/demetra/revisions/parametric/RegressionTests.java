/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.revisions.parametric;

import demetra.stats.TestResult;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class RegressionTests {
    TestResult breuschPagan;
    TestResult white;
    TestResult jarqueBera;
    TestResult arch;
}
