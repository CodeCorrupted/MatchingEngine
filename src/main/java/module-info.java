module group20tup.matchingengine {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens group20tup.matchingengine.controller to javafx.fxml;
    opens group20tup.matchingengine.css to javafx.graphics;
    exports group20tup.matchingengine;

    opens group20tup.matchingengine to javafx.fxml;
    opens group20tup.matchingengine.images to javafx.graphics;
}