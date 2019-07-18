package com.example.review.DataStructureFile;

import java.util.ArrayList;
import java.util.Objects;

public class TreeNode<TYPE_VALUE, TYPE_INDEX> {
    public TreeNode() {

    }

    public TreeNode(TYPE_VALUE value) {
        this.value = value;
    }

    public TYPE_VALUE value;

    public TYPE_INDEX index;

    public ArrayList<TreeNode<TYPE_VALUE, TYPE_INDEX>> node;

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode<?, ?> treeNode = (TreeNode<?, ?>) o;
        return Objects.equals(value, treeNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
