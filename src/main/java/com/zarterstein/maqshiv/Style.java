package com.zarterstein.maqshiv;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;

import javax.json.JsonObject;
import java.io.StringReader;

public class Style {
    private String css;
    private String htmlClass;
    private String imageLink;

    public Style() {
        css = htmlClass = imageLink = "";
    }
    public Style(String css, String htmlClass, String imageLink) {
        this.css = css;
        this.htmlClass = htmlClass;
        this.imageLink = imageLink;
    }

    public Style(JsonObject jObj, String prefix) {
        applyFromJson(jObj, prefix);
    }

    public void applyFromJson(JsonObject jObj, String prefix) {
        css = cssToFxCss( UIElement.getStringIfSet(jObj, prefix + "css"));
        htmlClass =  UIElement.getStringIfSet(jObj, prefix + "class");
        imageLink =  UIElement.getStringIfSet(jObj, prefix + "image");
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

    public static String cssToFxCss(String css) {
        if ( css == null || css.isEmpty()) return "";

        StringBuilder result = new StringBuilder();

        InputSource source = new InputSource(new StringReader(css));
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        CSSStyleDeclaration declarations;
        try {
            declarations = parser.parseStyleDeclaration(source);
            for ( int i = 0; i < declarations.getLength(); i++) {
                final String propName = declarations.item(i);
                result.append("-fx-")
                        .append(propName)
                        .append(": ")
                        .append(declarations.getPropertyValue(propName)).append("; ");
            }

        } catch (Exception e ) {
            System.out.println("Something went wrong: "+ e.getMessage());
            // should not be an exception.
        }

        return result.toString();
    }




}
