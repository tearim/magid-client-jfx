package com.zarterstein.maqshiv.advanced;

import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.MotionBlur;
import javafx.scene.layout.Region;

import java.util.List;

public class FilterApplier {
    FilterParser parser;
    private Effect constructEffect(String filter) {
        Effect result = null;
        switch ( parser.getFilterName(filter) ) {
            case "blur":

                List<String> params = parser.getRawParams(filter);
                double angle = 0;
                if (params.size() == 1) {
                    result = new GaussianBlur();
                } else {
                    result = new MotionBlur();
                }
                double radius = 1;
                try {
                     radius = Double.parseDouble(params.getFirst());
                     angle = Double.parseDouble(params.get(1));
                } catch (Exception e) {
                    // bad input, let's ignore.
                }
                if ( params.size() == 1 ) {
                    ((GaussianBlur) result).setRadius(radius);
                } else {
                    ((MotionBlur) result).setRadius(radius);
                    ((MotionBlur) result).setAngle(angle);
                }
                break;
        }
        return result;
    }
    public  void applyFilterToElement(Region element, String filters) {
        parser = new FilterParser(filters);
        for ( String filter : parser.enumerateFilters() ) {
            Effect effect = constructEffect(filter);

            if ( effect != null) {
                element.setEffect(effect);
            }
            element.applyCss();
            element.layout();
        }
    }

    private FilterApplier(Region element, String filters) {
        applyFilterToElement(element, filters);
    }

    public static void apply(Region element, String filters) {
        new FilterApplier(element, filters);
    }
}
