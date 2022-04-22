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
        double[] nums = new double[1000];
        Random rnd = new Random(0);
        for (int i = 0; i < nums.length; ++i) {
            nums[i] = rnd.nextDouble();
        }
        double[] nums2 = nums.clone();
//        long t0=System.currentTimeMillis();
        double k1 = Median.kselection(nums, nums.length / 2);
//        long t1=System.currentTimeMillis();
//        System.out.println(t1-t0);
//        System.out.println(k);
//        t0=System.currentTimeMillis();
        Arrays.sort(nums2);
//        t1=System.currentTimeMillis();
//        System.out.println(t1-t0);
        double k2 = nums2[nums.length / 2];
//        System.out.println();
        assertTrue(k1 == k2);
    }

    @Test
    public void testLargest() {
        int[] nums = new int[20];
        Random rnd = new Random(0);
        for (int i = 0; i < nums.length; ++i) {
            nums[i] = rnd.nextInt(100);
            //           System.out.println(nums[i]);
        }
//        System.out.println();

        for (int i = 0; i < 20; ++i) {
            int k = Median.findKthLargest(nums, i);
            //           System.out.println(k);
        }

        for (int i = 0; i < 20; ++i) {
            int k = Median.findKthSmallest(nums, i);
//            System.out.println(k);
        }
    }

    public static void main(String[] arg) {
        int N = 10000, K = 1000, Q = N / 2;
        double[] nums = new double[N];
        Random rnd = new Random();
        for (int i = 0; i < nums.length; ++i) {
            nums[i] = rnd.nextDouble();
        }
        double k1 = 0, k2 = 0, k3 = 0;
        long t0 = System.currentTimeMillis();
        for (int j = 0; j < K; ++j) {
            double[] c = nums.clone();
            k1 = Median.findKthSmallest(c, Q);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(k1);
        t0 = System.currentTimeMillis();
        for (int j = 0; j < K; ++j) {
            double[] c = nums.clone();
            Arrays.sort(c);
            k2 = c[Q];
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(k2);

        t0 = System.currentTimeMillis();
        for (int j = 0; j < K; ++j) {
            double[] c = nums.clone();
            k3 = Median.kselection(c, Q);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        System.out.println(k3);

    }

}
