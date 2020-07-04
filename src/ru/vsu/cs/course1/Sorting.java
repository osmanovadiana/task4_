package ru.vsu.cs.course1;

import java.util.ArrayList;

public class Sorting {
    public static ArrayList<SortState> BubbleSort(int[] arr){
        ArrayList<SortState> list = new ArrayList<>();
        boolean isSorted = false;
        int tmp, j = 0;
        while(!isSorted){
            isSorted = true;
            list.add(new SortState(SortState.Type.State, arr, -1, arr.length - j, -1, -1));
            for (int i = 0; i < arr.length - 1 - j; i++) {
                list.add(new SortState(SortState.Type.Compare, arr, -1, arr.length - j, i, i + 1));
                if(arr[i] > arr[i + 1]){
                    list.add(new SortState(SortState.Type.Change, arr, -1, arr.length - j, i, i + 1));
                    isSorted = false;
                    tmp = arr[i];
                    arr[i] = arr[i + 1];
                    arr[i + 1] = tmp;
                }
                list.add(new SortState(SortState.Type.State, arr, -1, arr.length - j, -1, -1));
            }
            j++;
        }
        list.add(new SortState(SortState.Type.State, arr, arr.length, -1, -1, -1));
        return list;
    }
}
