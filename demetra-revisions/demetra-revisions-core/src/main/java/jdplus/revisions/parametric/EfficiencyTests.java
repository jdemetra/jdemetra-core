/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.revisions.parametric;

import jdplus.stats.tests.StatisticalTest;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class EfficiencyTests {
    StatisticalTest breuschPagan;
    StatisticalTest white;
    StatisticalTest jarqueBera;
    StatisticalTest arch;
}
