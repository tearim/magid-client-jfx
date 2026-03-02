package com.zarterstein.maqshiv.ui;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import com.zarterstein.maqshiv.UiElement;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.*;

import javax.json.JsonObject;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class Style {
    public final static String STATE_HOVER  = "HOVER:";
    private String css;
    private String htmlClass;
    private String imageLink;
    private UiElement owner;
    private String defaultHtmlClass;

    private final Map<String, String> cssDeclarations = new HashMap<>();
    private boolean hasHoveredItems = false;

    public Style(UiElement owner) {
        css = htmlClass = imageLink = defaultHtmlClass = "";
        this.owner = owner;
    }
    public Style(UiElement owner, String css, String htmlClass, String imageLink) {
        this.owner = owner;
        this.css = css;
        this.htmlClass = htmlClass;
        this.imageLink = imageLink;
    }

    public Style(JsonObject jObj, String prefix) {
        applyFromJson(jObj, prefix);
    }

    public void applyFromJson(JsonObject jObj, String prefix) {
        setCssDeclarations();
        css = cssToFxCss( UiElement.getStringIfSet(jObj, prefix + "css"));
        setCssDeclarations();
        htmlClass =  UiElement.getStringIfSet(jObj, prefix + "class");
        imageLink =  UiElement.getStringIfSet(jObj, prefix + "image");
        applyComputedStylesFromLoadedCSSFiles();
    }

    private void setCssDeclarations() {
        InputSource source = new InputSource(new StringReader(css));
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        CSSStyleDeclaration declarations;
        try {
            declarations = parser.parseStyleDeclaration(source);
            for ( int i = 0; i < declarations.getLength(); i++) {
                final String propName = declarations.item(i);
                cssDeclarations.put(propName, declarations.getPropertyValue(propName));
            }

        } catch (Exception e ) {
            System.out.println("Something went wrong: "+ e.getMessage());
            // should not be an exception.
        }

    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getHtmlClass() {
        return htmlClass;
    }

    public void setHtmlClass(String htmlClass) {
        this.htmlClass = htmlClass;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public boolean isCssPropertyPresent(String property) {
        return cssDeclarations.containsKey(property);
    }

    public String getCssProperty(String property) {

        if ( cssDeclarations.containsKey(property)) {
            return cssDeclarations.get(property);
        }
        return "";
    }

    public boolean isCssPropertyInPercentage(String property) {
        if ( !cssDeclarations.containsKey(property)) return false;
        return cssDeclarations.get(property).matches("\\d+\\s*%");
    }

    public double getCssPropertyDouble(String property) {
        if ( cssDeclarations.containsKey(property)) {
            try {
                return Double.parseDouble(cssDeclarations.get(property).replace("%","").replace("px", ""));
            } catch ( Exception e) {
                return 0;
            }

        }
        return 0;
    }

    public int getCssPropertyInt(String property) {
        if ( cssDeclarations.containsKey(property)) {
            try {
                return Integer.parseInt(cssDeclarations.get(property));
            } catch ( Exception e) {
                return 0;
            }

        }
        return 0;
    }
    public String getDefaultHtmlClass() {
        return defaultHtmlClass;
    }
    public void setDefaultHtmlClass(String defaultHtmlClass) {
        this.defaultHtmlClass = defaultHtmlClass;
    }
    public static String cssToFxCss(String css) {
        if ( css == null || css.isEmpty()) return "";

        StringBuilder result = new StringBuilder();

        InputSource source = new InputSource(new StringReader(css));
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        CSSStyleDeclaration declarations;
        boolean addFxPrefixToAllRules = !css.contains("-fx-");
        try {
            declarations = parser.parseStyleDeclaration(source);
            for ( int i = 0; i < declarations.getLength(); i++) {
                final String propName = declarations.item(i);
                if ( addFxPrefixToAllRules ) {
                    result.append("-fx-");
                }
                result.append(propName)
                        .append(": ")
                        .append(declarations.getPropertyValue(propName)).append("; ");
            }

        } catch (Exception e ) {
            System.out.println("Something went wrong: "+ e.getMessage());
            // should not be an exception.
        }

        return result.toString();
    }

    public boolean hasExplicitCssPositioning() {
        return isCssPropertyPresent("-mg-abs-x") || isCssPropertyPresent("-mg-abs-y") ||
                isCssPropertyPresent("left") || isCssPropertyPresent("right") ||
                isCssPropertyPresent("top") || isCssPropertyPresent("bottom");
    }

    public void applyComputedStylesFromLoadedCSSFiles() {
        if ( owner == null || owner.getClient() == null ) {
            return;
        }
        for ( String key : owner.getClient().getCssFileSources().keySet() ) {
            String relevantHtmlClassNames = htmlClass + " " + defaultHtmlClass;
            for ( String classKey :  relevantHtmlClassNames.trim().split(" ") ) if (!classKey.isEmpty()) {
                //System.out.println("I will search for " + classKey + " in " + key );
                addCssDefinitions(classKey, owner.getClient().getCssFileSources().get(key));
            }
        }
    }
    public boolean hasHoveredItems(){
        return hasHoveredItems;
    }
    private void addCssDefinitions(String className, String cssText) {
        InputSource source = new InputSource(new StringReader(cssText));
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        CSSStyleSheet sheet;
        try {
             sheet = parser.parseStyleSheet(source, null, null);
        } catch (Exception e ) {
            System.out.println("Problems parsing CSS: " + e.getMessage());
            return;
        }
        CSSRuleList rules = sheet.getCssRules();
        for ( int i = 0; i < rules.getLength(); i++) {
            CSSRule rule = rules.item(i);
            if ( rule instanceof CSSStyleRule ) {
                String prefix = "";
                CSSStyleRule styleRule = (CSSStyleRule) rule;
                if ( styleRule.getSelectorText() != null &&
                        (styleRule.getSelectorText().endsWith("."+className) ||
                                styleRule.getSelectorText().endsWith("."+className + ":hover") )
                ) {
                    CSSStyleDeclaration declarations = styleRule.getStyle();
                    if ( styleRule.getSelectorText().contains(":hover")) {
                        hasHoveredItems = true;
                        prefix = STATE_HOVER;
                    }
                    for ( int j = 0; j < declarations.getLength();  j++ ) {
                        if ( !cssDeclarations.containsKey(prefix + declarations.item(j))) {
                            cssDeclarations.put(prefix + declarations.item(j),
                                    declarations.getPropertyCSSValue(declarations.item(j)).getCssText());
                        }
                    }
                }
            }
        }
    }




}
