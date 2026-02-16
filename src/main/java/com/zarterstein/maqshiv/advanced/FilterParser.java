package com.zarterstein.maqshiv.advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a simple filter parser. Do not expect much of it as it is now.
 * It can get a list of css-like filters  i.e:
 * filter: blur(0.5) darken(50);
 * or
 * filter: myFilter("param", 56)
 * Filter can have many params, but as for now they are simply split by commas.
 */
public class FilterParser {
    String filterString = "";
    List<String> filterStrings  = new ArrayList<>();
    Pattern filtersPattern = Pattern.compile("\\w[\\w\\d]*?\\(.*?\\)");
    Pattern paramsPattern = Pattern.compile("\\((.+?)\\)");
    public FilterParser(String fS) {
        filterString = fS;
    }

    public List<String> enumerateFilters() {
        List<String> result = new ArrayList<>();
        Matcher matcher = filtersPattern.matcher(filterString);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return filterStrings = result;
    }

    public String getFilterName(String filterString ) {
        return filterString.substring(0, filterString.indexOf("("));
    }

    public boolean hasParams(String filterString) {
        Matcher matcher = paramsPattern.matcher(filterString.replace(" ", ""));
        return matcher.find();
    }

    public List<String> getRawParams(String filterString) {
        List<String> result = new ArrayList<>();
        String onlyParams = "";
        Matcher matcher = paramsPattern.matcher(filterString);
        if ( matcher.find()) {
            onlyParams = matcher.group(1);
        }
        if ( !onlyParams.isEmpty()) {
            if ( onlyParams.contains(",")) {
                return List.of( onlyParams.split(","));
            }
            result.add(onlyParams);
        }
        return result;
    }


}
