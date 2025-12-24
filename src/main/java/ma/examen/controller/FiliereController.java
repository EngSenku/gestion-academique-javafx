package ma.examen.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ma.examen.dao.FiliereDAO;
import ma.examen.model.Filiere;
import java.sql.SQLException;

public class FiliereController {
    @FXML private TextField txtCode, txtNom, txtDesc;
    @FXML private TableView<Filiere> table;
    @FXML private TableColumn<Filiere, String> colCode, colNom, colDesc;
    @FXML private TableColumn<Filiere, Integer> colNb;

    private FiliereDAO filiereDAO = new FiliereDAO();

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCode()));
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        colNb.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getNombreEleves()).asObject());

        loadData();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtCode.setText(newVal.getCode());
                txtNom.setText(newVal.getNom());
                txtDesc.setText(newVal.getDescription());
            }
        });
    }

    private void loadData() {
        try {
            table.setItems(FXCollections.observableArrayList(filiereDAO.findAll()));
        } catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
    }

    @FXML
    private void handleAjouter() {
        if (txtCode.getText().trim().isEmpty() || txtNom.getText().trim().isEmpty()) {
            showAlert("Validation", "Le Code et le Nom de la filière sont obligatoires.");
            return;
        }

        try {
            Filiere f = new Filiere(0, txtCode.getText(), txtNom.getText(), txtDesc.getText());
            filiereDAO.save(f);
            loadData();
            clearFields();
        } catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
    }

    @FXML
    private void handleModifier() {
        Filiere selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (txtCode.getText().trim().isEmpty() || txtNom.getText().trim().isEmpty()) {
            showAlert("Validation", "Le Code et le Nom ne peuvent pas être vides.");
            return;
        }

        try {
            Filiere f = new Filiere(selected.getId(), txtCode.getText(), txtNom.getText(), txtDesc.getText());
            filiereDAO.update(f);
            loadData();
            clearFields();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Filière mise à jour avec succès.");
            alert.show();
        } catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
    }

    @FXML
    private void handleSupprimer() {
        Filiere selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                filiereDAO.delete(selected.getId());
                loadData();
                clearFields();
            } catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
        }
    }

    private void clearFields() { txtCode.clear(); txtNom.clear(); txtDesc.clear(); }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}