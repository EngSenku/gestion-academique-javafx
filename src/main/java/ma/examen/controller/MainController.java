package ma.examen.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML private BorderPane mainPane;

    @FXML
    public void initialize() {
        showFiliereView();
    }

    @FXML
    private void showFiliereView() { loadView("FiliereView.fxml"); }

    @FXML
    private void showEleveView() { loadView("EleveView.fxml"); }

    @FXML
    private void showCoursView() { loadView("CoursView.fxml"); }

    private void loadView(String fxmlFile) {
        try {
            URL fxmlUrl = getClass().getResource("/ma/examen/view/" + fxmlFile);
            if (fxmlUrl == null) {
                throw new IOException("Fichier FXML introuvable : " + fxmlFile);
            }
            Parent view = FXMLLoader.load(fxmlUrl);
            mainPane.setCenter(view);
        } catch (IOException e) {
            showAlert("Erreur de Navigation", "Impossible de charger la vue " + fxmlFile + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}