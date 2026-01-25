package com.zarterstein.tests;

import com.zarterstein.maqshiv.ResponseNodeType;
import com.zarterstein.maqshiv.UIElement;

public class ElemTest {
    public static void main(String[] args) {
        UIElement elem = ResponseNodeType.byLabel("menu");

        System.out.println(elem.getClass().getName());

        elem = ResponseNodeType.byLabel("aaaa");
        if ( elem == null ) {
            System.out.println("LOL");
        }

    }
}
