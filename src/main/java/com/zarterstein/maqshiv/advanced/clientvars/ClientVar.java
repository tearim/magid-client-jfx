package com.zarterstein.maqshiv.advanced.clientvars;

import com.zarterstein.maqshiv.Client;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * TODO: Rewrite as a factory that gets each var-processor in its own respective class.
 *       As for now the var-processors will live in different static functions due to time constraints.
 */
public class ClientVar {
    private final String varName;
    private final String varVal;
    private static Client client;
    static private final Map<String, BiConsumer<String, String>> handlers = new HashMap<>();
    static{
        handlers.put("view-port", ClientVar::processViewPort);
        handlers.put("menu-position", ClientVar::processMenuPosition);

    }
    public ClientVar(String varName, String varVal) {
        this.varName = varName;
        this.varVal = varVal;
    }

    public void process() {
        ClientVar.processChange(varName, varVal);
    }

    public static void setClient(Client client) {
        ClientVar.client = client;
    }

    private static void processChange(String varName, String varValue) {
        if ( handlers.containsKey(varName) ) {
            handlers.get(varName).accept(varName, varValue);
        }

    }

    private static void processViewPort(String varName, String varValue) {
        if ( varValue == null || varValue.isEmpty()) return;
        Scene scene = client.getScene();
        if ( !client.getRenderer().getIgnoreMaximization()) {
            ((Stage) scene.getWindow()).setMaximized(varValue.equals("maximized"));
        }
    }


    private static final int UNDECIDED = -1;
    private static final int FOR_X = 0;
    private static final int FOR_Y = 1;
    private static final int FOR_BOTH = 2;
    private static void processMenuPosition(String varName, String varValue) {
        if ( true ) return ;
        if ( varValue == null || varValue.isEmpty()) return;
        int for_what = UNDECIDED;
        String posX = "", posY = "";
        if ( varValue.contains(";") ) {
            posX = varValue.split(";")[0];
            posY = varValue.split(";")[1];
        } else {
            posX = posX = varValue;
            for_what = FOR_BOTH;
        }
        Pane innerPane = client.getResponsePane();
        Pane outerPane = (Pane)innerPane.getParent();
        System.out.println("Nice: " + outerPane.getWidth() + "  " + outerPane.getHeight());
        System.out.println("Nicer: " + innerPane.getWidth() + "  " + innerPane.getHeight());
        switch (posX) {
            case "center":
                if ( for_what == FOR_BOTH) {
                    outerPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                        centeredPosition(innerPane, FOR_BOTH);
                    });
                    centeredPosition(innerPane, FOR_BOTH);
                } else {
                    outerPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                        centeredPosition(innerPane, FOR_X);
                    });
                    centeredPosition(innerPane, FOR_X);
                }
                break;
            case "left":
                innerPane.relocate(0, innerPane.getLayoutY());
                break;
            case "right":
                outerPane.widthProperty().addListener((observable, oldValue, newValue) -> {
                    innerPane.relocate(innerPane.getLayoutX() - innerPane.getWidth(), innerPane.getLayoutY());
                });
                innerPane.relocate(innerPane.getLayoutX() - innerPane.getWidth(), innerPane.getLayoutY());
                break;
        }

        switch (posY) {
            case "center":
              //  System.out.println("I was here too.");
                outerPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                    centeredPosition(innerPane, FOR_Y);
                });
                centeredPosition(innerPane, FOR_Y);
                break;
            case "top":
                innerPane.relocate( innerPane.getLayoutX(), 0);
                break;
            case "bottom":
                outerPane.heightProperty().addListener((observable, oldValue, newValue) -> {
                    innerPane.relocate( innerPane.getLayoutX(), innerPane.getLayoutY() - innerPane.getHeight());
                });
                innerPane.relocate( innerPane.getLayoutX(), innerPane.getLayoutY() - innerPane.getHeight());
                break;
        }
    }
    private static void centeredPosition(Pane innerPane, int forWhat) {
        Pane outerPane = (Pane)innerPane.getParent();
        double width = outerPane.getWidth();
        double height = outerPane.getHeight();
        double innerWidth = innerPane.getWidth();
        double innerHeight = innerPane.getHeight();
        double candidateX = 0;
        double candidateY = 0;

        candidateX = ( width - innerWidth  ) / 2;
        candidateY = ( height - innerHeight  ) / 2;
        System.out.println("IWidth: " + innerWidth + "  Height: " + innerHeight);
        System.out.println("OWidth: " + width + "  Height: " + height);
        System.out.println("Candidates: " + candidateX + ", " + candidateY);
        switch (forWhat) {
            case FOR_X:
                innerPane.relocate(candidateX, innerPane.getLayoutY());
                break;
            case FOR_Y:
                innerPane.relocate(innerPane.getLayoutX(), candidateY);
                break;
            case FOR_BOTH:
                innerPane.relocate(candidateX, candidateY);
                break;
        }

    }
}
