/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.data;

import java.util.Arrays;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Median {

//    int getPivot(int[] nums, int start, int end) {
//        int[] medians = new int[(end - start) / 5];
//        int i = start;
//        int k = 0;
//        for (int j = start + 5; j < end; i = j, j += 5) {
//            Arrays.sort(nums, i, j);
//            medians[k++] = nums[i + 2];
//        }
//
//        // The magic!!! we are calling our own primary method recursively to find the median of medians.
//        return kthSmallest(medians, 0, medians.length, medians.length / 2 + 1);
//    }
//
//    int arrangeByPivot(int[] nums, int start, int end, int pivot) {
//        int b = start;
//        int e = end - 1;
//        for (int i = start; i <= e;) {
//            if (nums[i] < pivot) {
//                int temp = nums[i];
//                nums[i] = nums[b];
//                nums[b] = temp;
//                b++;
//                i++;
//            } else if (nums[i] == pivot) {
//                i++;
//            } else {
//                int temp = nums[i];
//                nums[i] = nums[e];
//                nums[e] = temp;
//                e--;
//            }
//        }
//
//        return b;
//    }
//
//    int kthSmallest(int[] nums, int start, int end, int elm) {
//        if (end - start <= 5) {
//            Arrays.sort(nums, start, end);
//            return nums[start + elm - 1];
//        }
//
//        // find the pivot element that will split the 
//        // set as evenly as possible - this is where all the magic happens
//        int pivot = getPivot(nums, start, end);
//
//        // perform a partition based on the pivot found.
//        int loc = arrangeByPivot(nums, start, end, pivot);
//
//        // check where is the element we are seeking compared to the partition index
//        // call recursively depending on the partitioned location or return, if we have found the element
//        if (loc - start == elm - 1) {
//            return nums[loc];
//        } else if (loc - start > elm - 1) {
//            return kthSmallest(nums, start, loc, elm);
//        }
//
//        return kthSmallest(nums, loc + 1, end, (elm - (loc + 1 - start)));
//    }
//
//    public int findKthLargest(int[] nums, int k) {
//        return kthSmallest(nums, 0, nums.length, nums.length - k + 1);
//    }
    int partition(int arr[], int low, int high) {
        int pivot = arr[high];          //taken pivot element as last element
        int z = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {                                  //arranging all elements that are less than pivot
                z++;
                int temp = arr[z];
                arr[z] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[z + 1];
        arr[z + 1] = arr[high];     //finalizing th position of pivot element in array which is sorted position
        arr[high] = temp;

        return z + 1;
    }

    public int kselection(int[] arr, int low,
            int high, int k) {
        int partition_sorting_value = partition(arr, low, high);
        if (partition_sorting_value == k) //comparing the position returned with the desired position k
        {
            return arr[partition_sorting_value];
        } else if (partition_sorting_value < k) //partition value is less than k search left half array
        {
            return kselection(arr, partition_sorting_value + 1, high, k);
        } else //partition value is greater than k search right half array         
        {
            return kselection(arr, low, partition_sorting_value - 1, k);
        }
    }

    public int kselection(int[] arr, int k) {
        return kselection(arr, 0, arr.length - 1, k);
    }

    int partition(double arr[], int low, int high) {
        double pivot = arr[high];          //taken pivot element as last element
        int z = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {                                  //arranging all elements that are less than pivot
                z++;
                double temp = arr[z];
                arr[z] = arr[j];
                arr[j] = temp;
            }
        }
        double temp = arr[z + 1];
        arr[z + 1] = arr[high];     //finalizing th position of piviot element in array which is sorted position
        arr[high] = temp;

        return z + 1;
    }

    public double kselection(double[] arr, int low,
            int high, int k) {
        int partition_sorting_value = partition(arr, low, high);
        if (partition_sorting_value == k) //comparing the position returned with the desired position k
        {
            return arr[partition_sorting_value];
        } else if (partition_sorting_value < k) //partition value is less than k search left half array
        {
            return kselection(arr, partition_sorting_value + 1, high, k);
        } else //partition value is greater than k search right half array         
        {
            return kselection(arr, low, partition_sorting_value - 1, k);
        }
    }

    public double kselection(double[] arr, int k) {
        return kselection(arr, 0, arr.length - 1, k);
    }
}
