/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package appayuda;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import netscape.javascript.JSObject;




/**
 *
 * @author Alejandro
 */
public class AppAyuda extends Application {
    private Scene scene;
 
    @Override
    public void start(Stage stage) {
  
        stage.setTitle("Web View");
        scene = new Scene(new Browser(), 750, 500, Color.web("#666970"));
        //scene = new Scene(, 0, 0, paint)
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}

class Browser extends Region {

    private HBox toolBar;
    private static String[] imageFiles = new String[]{
        "/Imagenes/moodle.jpg",
        "/Imagenes/facebook.jpg",
        "/Imagenes/documentation.png",
        "/Imagenes/twitter.jpg",
        "/Imagenes/help.png"
    };
    private static String[] captions = new String[]{
        "Moodle",
        "Facebook",
        "Documentacion",
        "Twitter",
        "Help"
    };
    private static String[] urls = new String[]{
        "https://moodle.org/?lang=es",
        "https://www.facebook.com/ieslosmontecillos/",
        AppAyuda.class.getResource("/Help/Alumnos_Topic.html").toExternalForm(),
        "https://twitter.com/i/flow/login?input_flow_data=%7B%22requested_variant%22%3A%22eyJsYW5nIjoiZXMifQ%3D%3D%22%7D",
        AppAyuda.class.getResource("/Help/help.html").toExternalForm()
    };

    final ImageView selectedImage = new ImageView();
    final Hyperlink[] hpls = new Hyperlink[captions.length];
    final Image[] images = new Image[imageFiles.length];
    private boolean needDocumentationButton = false;
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    final Button toggleHelpTopics = new Button("Toggle Help Topics");
    final WebView smallView = new WebView();
 
    
    
    public Browser() {

        getStyleClass().add("browser");
        for (int i = 0; i < captions.length; i++) {
            Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
            Image image = images[i]= new Image(getClass().getResourceAsStream(imageFiles[i]));
            hpl.setGraphic(new ImageView(image));
            final String url = urls[i];
            final boolean addButton = (hpl.getText().equals("Help"));
            //proccess event
            hpl.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    needDocumentationButton = addButton;
                    webEngine.load(url);
                }
            });
        }
        
  
    webEngine.load("http://www.ieslosmontecillos.es/wp/");

        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().addAll(hpls);
        toolBar.getChildren().add(createSpacer());
        toggleHelpTopics.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                webEngine.executeScript("toggle_visibility('help_topics')");
            }
        });
        smallView.setPrefSize(120, 80);
        //handle popup windows
        webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
            @Override
            public WebEngine call(PopupFeatures config) {
                smallView.setFontScale(0.8);
                if (!toolBar.getChildren().contains(smallView)) {
                    toolBar.getChildren().add(smallView);
                }
                return smallView.getEngine();
            }
        }
        );

        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                toolBar.getChildren().remove(toggleHelpTopics);
                if (newState == State.SUCCEEDED) {
                   JSObject win = (JSObject) webEngine.executeScript("window");
                    win.setMember("app", new JavaApp());
                    if (needDocumentationButton) {
                        toolBar.getChildren().add(toggleHelpTopics);
                    }
                }
            }
        }
        );
        getChildren().add(toolBar);
        getChildren().add(browser);
        
        
        final ComboBox comboBox = new ComboBox();

        comboBox.setPrefWidth(60);
        toolBar.getChildren().add(comboBox);
    
        final WebHistory history = webEngine.getHistory();
        history.getEntries().addListener(new ListChangeListener<WebHistory.Entry>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Entry> c) {
                c.next();
                for (Entry e : c.getRemoved()) {
                    comboBox.getItems().remove(e.getUrl());
                }
                for (Entry e : c.getAddedSubList()) {
                    comboBox.getItems().add(e.getUrl());
                }
            }
        });
        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ev) {
                int offset= comboBox.getSelectionModel().getSelectedIndex() - history.getCurrentIndex();
                history.go(offset);
            }
        });
    }
    
    public class JavaApp {

        public void exit() {
            Platform.exit();
        }
    }
    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser, 0, 0, w, h - tbHeight, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar, 0, h- tbHeight, w, tbHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 500;
    }
    
    
}

