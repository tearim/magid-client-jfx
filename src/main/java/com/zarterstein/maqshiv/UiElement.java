package com.zarterstein.maqshiv;

import com.zarterstein.maqshiv.advanced.FilterApplier;
import com.zarterstein.maqshiv.ui.Style;
import com.zarterstein.maqshiv.ui.UiElementRenderer;
import com.zarterstein.maqshiv.utils.JsonUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public abstract class UiElement {
    private Style style;
    private List<UiElement> children;
    private UiElement parent;
    private String name;
    private String displayText;
    private Client client;
    private Region region;
    private Control control;
    private long defer;
    private UiElementRenderer renderer;
    // The batch's detached elements that belong to this narration or menu (see Client.routeDetachedElements).
    private final List<DetachedElement> detachedElements = new ArrayList<>();


    public UiElement() {
        children = new ArrayList<>();
        parent = null;
        displayText = "";
        style = new Style(this);
    }
    public UiElement(UiElement parent) {
        this();
        this.parent = parent;
        if ( parent != null ) {
            renderer = parent.renderer;
        }
    }

    public UiElement(UiElement parent, List<UiElement> children) {
        this(parent);
        this.children = children;
    }


    public List<DetachedElement> getDetachedElements() {
        return detachedElements;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public List<UiElement> getChildren() {
        return children;
    }

    public int getChildrenCount() {
        return children.size();
    }

    public UiElement getFirstChild() {
        if ( children == null) {
            return null;
        }
        return children.getFirst();
    }

    public UiElement getLastChild() {
        if ( children == null) {
            return null;
        }
        return children.getLast();
    }

    public void setChildren(List<UiElement> children) {
        this.children = children;
    }

    public UiElement getParent() {
        return parent;
    }

    public void setParent(UiElement parent) {
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    /**
     * Returns an empty string if no corresponding field is found in the supplied JSON. To be used in the cases,
     * where a string is needed and an empty string is preferred over a null value.
     * @param jObj
     * @param key
     * @return
     */
    public static String getStringIfSet(JsonObject jObj, String key) {
        return JsonUtils.getStringIfSet(jObj, key);
    }

    public abstract UiElement createFromJson(JsonObject jObj);

    public UiElement setClient(Client client) {
        this.client = client;
        this.renderer = client.getRenderer();
        return this;
    }

    public Client getClient() {
        return client;
    }

    public UiElement setRegion(Region region) {
        this.region = region;
        return this;
    }
    public Region getRegion() {
        return region;
    }

    public UiElement setControl(Control control) {
        this.control = control;
        return this;
    }
    public Control getControl() {
        return control;
    }
    public long getDefer() {
        return defer;
    }
    public void setDefer(long defer) {
        this.defer = defer;
    }
    public boolean isDeferred()  {
        return defer > 0;
    }
    public void applyRegionStyleClasses() {
        _applyStyleClassesToObject(region, "");
    }
    public void applyControlStyleClasses() {
        _applyStyleClassesToObject(control, style.getDefaultHtmlClass());
    }
    private void _applyStyleClassesToObject(Region _region, String defaultClassName) {
        _region.getStyleClass().clear();
        String classes = (style.getDefaultHtmlClass() + " " + style.getHtmlClass()).trim();
        if ( classes.contains(" ")) {
            for ( String className : classes.split(" ")) if ( !className.isEmpty()) {
                _region.getStyleClass().add(className);
            }
        } else {
            _region.getStyleClass().add(classes);
        }

    }

    public void processStyles() {
        placeOnRegion();
        processFilters();
    }


    public void placeOnRegion() {
        if ( region == null || style == null || client == null || control == null) return;

        Scene scene = client.getScene();
        double finalX = getStyle().getCssPropertyDouble("right");
        String selectorXUsed = "right";

        if ( getStyle().isCssPropertyPresent("left")) {
            finalX = getStyle().getCssPropertyDouble("left");
            selectorXUsed = "left";
        }
        if ( getStyle().isCssPropertyPresent("-mg-abs-x")) {
            finalX = getStyle().getCssPropertyDouble("-mg-abs-x");
            selectorXUsed = "-mg-abs-x";
        }

        if ( selectorXUsed.equals("right")) {
            if ( getStyle().isCssPropertyInPercentage(selectorXUsed)) {
                finalX = 100 - finalX;
            } else {
                final double realX = finalX;
                ChangeListener<Number> listener = (c, o, n) -> control.relocate(region.widthProperty().get() - realX, control.getLayoutY() );
                scene.widthProperty().addListener(listener);
                renderer.registerListener(scene.widthProperty(), listener);
                finalX = region.widthProperty().get() - finalX;
            }
        }

        double finalY =  getStyle().getCssPropertyDouble("bottom");
        String selectorYUsed = "bottom";

        if ( getStyle().isCssPropertyPresent("top")) {
            finalY = getStyle().getCssPropertyDouble("top");
            selectorYUsed = "top";
        }
        if ( getStyle().isCssPropertyPresent("-mg-abs-y")) {
            finalY = getStyle().getCssPropertyDouble("-mg-abs-y");
            selectorYUsed = "-mg-abs-y";
        }

        if ( selectorYUsed.equals("bottom")) {
            if ( getStyle().isCssPropertyInPercentage(selectorYUsed)) {
                finalY = 100 -  finalY;
            } else {
                final double realY = finalY;
                ChangeListener<Number> listener = (c, o, n) ->
                        control.relocate(control.getLayoutX(), region.heightProperty().get() - realY);

                region.heightProperty().addListener( listener );
                renderer.registerListener(region.heightProperty(), listener);
                finalY = region.heightProperty().get() - finalY;
            }
        }

        if ( getStyle().isCssPropertyInPercentage(selectorXUsed)) {
            double percentageX = finalX;
            // for resizing
            ChangeListener<Number> listener = (c, o, n) -> control.relocate((Double)n * (percentageX / 100), control.getLayoutY());
            scene.widthProperty().addListener(listener );
            renderer.registerListener(scene.widthProperty(), listener);
            // for initial placement
            finalX = scene.getWidth() * percentageX / 100;
        }
        if ( getStyle().isCssPropertyInPercentage(selectorYUsed)) {
            double percentageY = finalY;
            ChangeListener<Number> listener = (c, o, n) -> control.relocate(control.getLayoutX(), (Double)n * (percentageY / 100) );
            scene.heightProperty().addListener(listener);
            renderer.registerListener(scene.heightProperty(), listener);
            finalY = scene.getHeight() * percentageY / 100;
        }
        control.relocate(finalX, finalY);
    }

    public void recalculateCssPropertiesForSelfAndChildren() {
        getStyle().applyComputedStylesFromLoadedCSSFiles();
        for ( UiElement child : getChildren()) {
            child.recalculateCssPropertiesForSelfAndChildren();
        }
    }

    public void processFilters() {
        if (getStyle().isCssPropertyPresent("filter")) {
            FilterApplier.apply(getControl(), getStyle().getCssProperty("filter"));
        }
        if (getStyle().hasHoveredItems()) {
            getControl().hoverProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    String prefix = newValue?Style.STATE_HOVER:"";
                    FilterApplier.apply(getControl(), getStyle().getCssProperty(prefix + "filter"));
                }
            });
        }
    }

}
