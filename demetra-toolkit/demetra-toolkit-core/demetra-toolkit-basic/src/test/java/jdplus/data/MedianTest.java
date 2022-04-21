/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.data;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class MedianTest {
    
    public MedianTest() {
    }

    @Test
    public void testMedian() {
        double[] nums=new double[1000];
        Random rnd=new Random(0);
        for (int i=0; i<nums.length; ++i)
            nums[i]=rnd.nextDouble();
        double[] nums2=nums.clone();
//        long t0=System.currentTimeMillis();
        double k1 = Median.kselection(nums, nums.length/2);
//        long t1=System.currentTimeMillis();
//        System.out.println(t1-t0);
//        System.out.println(k);
//        t0=System.currentTimeMillis();
        Arrays.sort(nums2);
//        t1=System.currentTimeMillis();
//        System.out.println(t1-t0);
        double k2=nums2[nums.length/2];
//        System.out.println();
        assertTrue(k1==k2);
    }
    
}
