package com.zarterstein.tests;

import com.zarterstein.maqshiv.advanced.FilterParser;

import java.util.List;

public class FilterLogicTest {
    public static void main(String[] args) {
        FilterParser fp = new FilterParser("blur(5) darken(0.5) flip(0, 43, 66) go() return(2)");

        List<String> list = fp.enumerateFilters();

        for ( String name : list ) {
            System.out.println(fp.getFilterName(name));
            if ( fp.hasParams(name)) {
                System.out.println("It has parameters: ");
                System.out.println(fp.getRawParams(name));
            }
        }
    }
}
