/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.data;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Median {

    private int getPivot(int[] nums, int start, int end) {
        int[] medians = new int[(end - start) / 5];
        int i = start;
        int k = 0;
        for (int j = start + 5; j < end; i = j, j += 5) {
            Arrays.sort(nums, i, j);
            medians[k++] = nums[i + 2];
        }
        return kthSmallest(medians, 0, medians.length, medians.length / 2);
    }

    int arrangeByPivot(int[] nums, int start, int end, int pivot) {
        int b = start;
        int e = end - 1;
        for (int i = start; i <= e;) {
            if (nums[i] < pivot) {
                int temp = nums[i];
                nums[i] = nums[b];
                nums[b] = temp;
                b++;
                i++;
            } else if (nums[i] == pivot) {
                i++;
            } else {
                int temp = nums[i];
                nums[i] = nums[e];
                nums[e] = temp;
                e--;
            }
        }

        return b;
    }

    private int kthSmallest(int[] nums, int start, int end, int elm) {
        if (end - start <= 5) {
            Arrays.sort(nums, start, end);
            return nums[elm];
        }

        // find the pivot element that will split the 
        // set as evenly as possible - this is where all the magic happens
        int pivot = getPivot(nums, start, end);

        // perform a partition based on the pivot found.
        int loc = arrangeByPivot(nums, start, end, pivot);

        // check where is the element we are seeking compared to the partition index
        // call recursively depending on the partitioned location or return, if we have found the element
        if (loc == elm) {
            return nums[loc];
        } else if (loc > elm) {
            return kthSmallest(nums, start, loc, elm);
        } else {
            return kthSmallest(nums, loc + 1, end, elm);
        }
    }

    public int findKthLargest(int[] nums, int k) {
        return kthSmallest(nums, 0, nums.length, nums.length - k - 1);
    }

    public int findKthSmallest(int[] nums, int k) {
        if (k < 0 || k >= nums.length) {
            throw new IllegalArgumentException();
        }
        return kthSmallest(nums, 0, nums.length, k);
    }

    private double getPivot(double[] nums, int start, int end) {
        double[] medians = new double[(end - start) / 5];
        int i = start;
        int k = 0;
        for (int j = start + 5; j < end; i = j, j += 5) {
            Arrays.sort(nums, i, j);
            medians[k++] = nums[i + 2];
        }
        return kthSmallest(medians, 0, medians.length, medians.length / 2);
    }

    private int arrangeByPivot(double[] nums, int start, int end, double pivot) {
        int b = start, i = start;
        int e = end;
        while (i < e) {
            if (nums[i] < pivot) {
                double temp = nums[i];
                nums[i] = nums[b];
                nums[b] = temp;
                b++;
                i++;
            } else if (nums[i] == pivot) {
                i++;
            } else {
                e--;
                double temp = nums[i];
                nums[i] = nums[e];
                nums[e] = temp;
            }
        }

        return b;
    }

    private double kthSmallest(double[] nums, int start, int end, int elm) {
        if (end - start <= 5) {
            Arrays.sort(nums, start, end);
            return nums[elm];
        }

        // find the pivot element that will split the 
        // set as evenly as possible - this is where all the magic happens
        double pivot = getPivot(nums, start, end);

        // perform a partition based on the pivot found.
        int loc = arrangeByPivot(nums, start, end, pivot);

        // check where is the element we are seeking compared to the partition index
        // call recursively depending on the partitioned location or return, if we have found the element
        if (loc == elm) {
            return nums[loc];
        } else if (loc > elm) {
            return kthSmallest(nums, start, loc, elm);
        } else {
            return kthSmallest(nums, loc + 1, end, elm);
        }
    }

    public double findKthLargest(double[] nums, int k) {
        return kthSmallest(nums, 0, nums.length, nums.length - k - 1);
    }

    public double findKthSmallest(double[] nums, int k) {
        if (k < 0 || k >= nums.length) {
            throw new IllegalArgumentException();
        }
        return kthSmallest(nums, 0, nums.length, k);
    }

    public int kselection(int[] arr, int low,
            int high, int k) {
        int pivot = arr[RND.nextInt(low, high)];
        int partition_sorting_value = arrangeByPivot(arr, low, high, pivot);
        if (partition_sorting_value == k) {
            return arr[partition_sorting_value];
        } else if (partition_sorting_value < k) {
            return kselection(arr, partition_sorting_value + 1, high, k);
        } else {
            return kselection(arr, low, partition_sorting_value, k);
        }
    }

    public int kselection(int[] arr, int k) {
        return kselection(arr, 0, arr.length, k);
    }

    public double kselection(double[] arr, int low,
            int high, int k) {

        double pivot = arr[RND.nextInt(low, high)];

//        int partition_sorting_value = partition(arr, low, high);
        int partition_sorting_value = arrangeByPivot(arr, low, high, pivot);

        if (partition_sorting_value == k) {
            return arr[partition_sorting_value];
        } else if (partition_sorting_value < k) {
            return kselection(arr, partition_sorting_value + 1, high, k);
        } else {
            return kselection(arr, low, partition_sorting_value, k);
        }
    }

    public double kselection(double[] arr, int k) {
        return kselection(arr, 0, arr.length, k);
    }

    private static final Random RND = new Random(0);
}
