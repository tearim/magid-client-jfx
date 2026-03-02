package com.zarterstein.maqshiv.ui;

import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

public class MultiStoreUiRelocator {

    class Store {
        private double floor;
        private double lastHeight;
        private boolean wantsToShrink;
        private boolean wantsToGrow;
        private boolean requestSiblingRelocation;
        private final List<Region> tenants;
        public List<Region> getObjects() {
            return tenants;
        }
        public double getFloor() {
            return floor;
        }
        public void setFloor(double floor) {
            this.floor = floor;
        }
        public Store(List<Region> newTenants) {
            tenants = newTenants;
        }
        public Store(Region tenant) {
            tenants = new ArrayList<>();
            tenants.add(tenant);
        }
        public double storeHeight() {
            if ( tenants == null || tenants.isEmpty())
                return 0;
            return lastHeight = tenants.stream().mapToDouble(
                    parent -> parent.heightProperty().get()
                    ).max().orElse(0);
        }
        public void setWantsToShrink(boolean wantsToShrink) {
            this.wantsToShrink = wantsToShrink;
        }
        public void setWantsToGrow(boolean wantsToGrow) {
            this.wantsToGrow = wantsToGrow;
        }
        public boolean willShrink() {
            return wantsToShrink;
        }
        public boolean willGrow() {
            return wantsToGrow;
        }
        
        public double redrawStoreAndGetItsHeight(double newStoreFloor) {
            if ( tenants == null || tenants.isEmpty())
                return 0;
            Pane root = (Pane) tenants.getFirst().getParent();
            double effectiveHeight = 0;
            double currentElemHeight = 0;
            for ( Region tenant : tenants ) {
                tenant.relocate(tenant.layoutXProperty().get(), newStoreFloor);
                tenant.applyCss();
                currentElemHeight = tenant.heightProperty().get();
                if ( effectiveHeight < currentElemHeight ) {
                    effectiveHeight = currentElemHeight;
                }

            }
            if ( lastHeight <  currentElemHeight) {
                lastHeight = currentElemHeight;
                if ( willShrink() ) {
                    requestSiblingRelocation = true;
                }
            }
            if ( lastHeight > currentElemHeight) {
                lastHeight = currentElemHeight;
                if ( willGrow() ) {
                    requestSiblingRelocation = true;
                }
            }

            if ( root != null ) root.layout();
            return lastHeight;
        }
        public boolean needSubsequentialRedraw() {
            return requestSiblingRelocation;
        }
    }
    private final List<Store> stores = new ArrayList<>();
    private double gap = 15;
    private double currentLowestY = 0;// It would be better to delegate it to each store for better control over layout?
    public MultiStoreUiRelocator() {
    }
    public MultiStoreUiRelocator(Region firstTenant) {
        stores.add(new Store(firstTenant));
    }
    public MultiStoreUiRelocator(Region firstTenant, double gap) {
        this(firstTenant);
        this.gap = gap;
    }
    public void addFloor(Region... tenants) {
        stores.add(new Store(List.of(tenants)));
    }
    public void addFloor(List<Region> tenants) {
        stores.add(new Store(tenants));
    }
    public void addFloor(Region tenant) {
        stores.add(new Store(tenant));
    }
    public void addToFloor(int floorNo, Region tenant) {
        stores.get(floorNo).getObjects().add(tenant);
    }
    public void addToLastFloor(Region tenant) {
        stores.getLast().getObjects().add(tenant);
    }
    public void redrawAll() {
        double currentY = gap;
        double currentLowestY = gap;
        for ( Store store : stores) {
            currentY += store.redrawStoreAndGetItsHeight(currentY) + gap;
        }
        currentLowestY = currentY;
    }
    public double currentY() {
        return currentLowestY;
    }

    public double getXOffset() {
        return 10f;
    }


    public void vacate() {
        stores.clear();
        currentLowestY = gap;
    }
}
