package com.example.review;

import com.example.review.New.ArrayStoreList;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SortLib extends ArrayStoreList<SortLib.SortStru> {
    public SortLib() {

    }

    public SortLib(String src) {
        decode(src);
    }

    public SortLib(File src) {
        try {
            read(src);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decode(String src) {
        String[] sorts = src.split("\n");
        for (String sortRaw : sorts) {
            if (!sortRaw.isEmpty()) add(new SortStru(sortRaw));
        }
    }

    public List<String> getElementInSort(String sort) {
        for (SortStru item : this) {
            if (item.matchSort(sort)) {
                //防止多次引用
                if (item.inUse) return null;
                item.inUse = true;

                List<String>      elements = item.getElements();
                ArrayList<String> temp     = new ArrayList<>();
                ArrayList<String> refer    = new ArrayList<>();

                //把引用的元素列举出来
                for (String element : elements) {
                    if (element.contains("@")) {
                        refer.add(element);
                        element = element.substring(1);
                        List<String> elementInSort = getElementInSort(element);
                        temp.addAll(elementInSort);
                    }
                }
                elements.addAll(temp);
                elements.removeAll(refer);
                item.inUse = false;
                return elements;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean listAll) {
        StringBuilder sb = new StringBuilder();
        for (SortStru sortStru : this) sb.append("\n" + sortStru.toString(listAll ? this : null));
        sb.replace(0, 1, "");
        return sb.toString();
    }

    class SortStru {
        private String       name;
        private List<String> elements = new LinkedList<>();
        private boolean      inUse;

        SortStru(String src) {
            String[] split = src.split("[：:]");
            name = split[0];
            if (split.length == 2) {
                String[] elements = split[1].split("[、,]");
                this.elements.addAll(Arrays.asList(elements));
            }
        }

        SortStru(String name, List<String> elements) {
            setName(name);
            setElements(elements);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setElements(List<String> elements) {
            this.elements = elements;
        }

        public List<String> getElements() {
            return new ArrayList<>(elements);
        }

        public boolean matchSort(String sortName) {
            return sortName.equals(name);
        }

        public List<String> getElements(SortLib sortStrus) {
            return sortStrus.getElementInSort(name);
        }

        @Override
        public String toString() {
            return toString(null);
        }

        public String toString(SortLib sortStrus) {
            StringBuffer sb       = new StringBuffer();
            List<String> elements = (sortStrus != null) ? getElements(sortStrus) : getElements();
            for (String element : elements) sb.append(element).append('、');
            int length = sb.length();
            sb.replace(length - 1, length, "");
            String format = String.format("%s：%s", name, sb);
            return format;
        }
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        StringBuffer sb = new StringBuffer();
        for (SortStru sortStru : this) {
            String str = sortStru.toString();
            sb.append("\n").append(str);
        }
        sb.replace(0, 1, "");
        String value = sb.toString();
        dos.write(value.getBytes());
    }

    @Override
    public void loadWith(DataInputStream dis) throws IOException {
        byte[]                buffer = new byte[1024];
        ByteArrayOutputStream baos   = new ByteArrayOutputStream();
        int                   length;
        while ((length = dis.read(buffer)) != -1) baos.write(buffer, 0, length);
        decode(new String(baos.toByteArray()));
    }
}
