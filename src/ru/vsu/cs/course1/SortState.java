package ru.vsu.cs.course1;

import java.util.Arrays;

public class SortState {
    enum Type{State, Compare, Change}

    private Type type;
    private int[] array;
    private int left;
    private int right;
    private int a;
    private int b;

    SortState(Type type, int[] array, int left, int right, int a, int b){
        this.type = type;
        this.array = Arrays.copyOf(array, array.length);
        this.left = left;
        this.right = right;
        this.a = a;
        this.b = b;
    }

    public Type getType() { return type; }
    public int[] getArray() { return array; }
    public int getLeft() { return left; }
    public int getRight() { return right; }
    public int getA() { return a; }
    public int getB() { return b; }
}