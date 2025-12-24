package ma.examen.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ma.examen.dao.CoursDAO;
import ma.examen.dao.FiliereDAO;
import ma.examen.model.Cours;
import ma.examen.model.Filiere;
import java.sql.SQLException;
import java.util.List;

public class CoursController {
    @FXML private TextField txtCode, txtIntitule;
    @FXML private TableView<Cours> table;
    @FXML private TableColumn<Cours, String> colCode, colIntitule;

    @FXML private ComboBox<Filiere> cmbFiliereAssign;
    @FXML private ListView<String> listFiliereAffectees;

    private CoursDAO coursDAO = new CoursDAO();
    private FiliereDAO filiereDAO = new FiliereDAO();

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCode()));
        colIntitule.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIntitule()));

        try {
            loadData();
            cmbFiliereAssign.setItems(FXCollections.observableArrayList(filiereDAO.findAll()));
        } catch (SQLException e) {
            showAlert("Erreur Init", e.getMessage());
        }

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal != null) {
                txtCode.setText(newVal.getCode());
                txtIntitule.setText(newVal.getIntitule());
                loadAffectedFilieres(newVal.getId());
            } else {
                listFiliereAffectees.getItems().clear();
            }
        });
    }

    private void loadData() throws SQLException {
        table.setItems(FXCollections.observableArrayList(coursDAO.findAll()));
    }

    private void loadAffectedFilieres(int coursId) {
        try {
            List<String> names = coursDAO.getFilieresByCours(coursId);
            listFiliereAffectees.setItems(FXCollections.observableArrayList(names));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        if(txtCode.getText().trim().isEmpty() || txtIntitule.getText().trim().isEmpty()) {
            showAlert("Validation", "Le Code et l'Intitulé sont obligatoires.");
            return;
        }

        try {
            coursDAO.save(new Cours(0, txtCode.getText(), txtIntitule.getText()));
            loadData();
            clearFields();
        } catch (SQLException e) { showAlert("Erreur Ajout", e.getMessage()); }
    }

    @FXML
    private void handleModifier() {
        Cours selected = table.getSelectionModel().getSelectedItem();
        if(selected == null) {
            showAlert("Attention", "Veuillez sélectionner un cours à modifier.");
            return;
        }

        if(txtCode.getText().trim().isEmpty() || txtIntitule.getText().trim().isEmpty()) {
            showAlert("Validation", "Les champs ne peuvent pas être vides.");
            return;
        }

        try {
            Cours c = new Cours(selected.getId(), txtCode.getText(), txtIntitule.getText());
            coursDAO.update(c);
            loadData();
            clearFields();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cours modifié avec succès !");
            alert.show();
        } catch (SQLException e) {
            showAlert("Erreur Modification", e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Cours selected = table.getSelectionModel().getSelectedItem();
        if(selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le cours " + selected.getIntitule() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if(resp == ButtonType.YES) {
                try {
                    coursDAO.delete(selected.getId());
                    loadData();
                    clearFields();
                } catch (SQLException e) { showAlert("Erreur Suppression", e.getMessage()); }
            }
        });
    }

    @FXML
    private void handleAffecterFiliere() {
        Cours c = table.getSelectionModel().getSelectedItem();
        Filiere f = cmbFiliereAssign.getValue();

        if(c != null && f != null) {
            if (listFiliereAffectees.getItems().contains(f.getNom())) {
                showAlert("Info", "Ce cours est déjà affecté à cette filière.");
                return;
            }

            try {
                coursDAO.assignToFiliere(f.getId(), c.getId());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Cours affecté à " + f.getNom());
                alert.show();
                loadAffectedFilieres(c.getId()); // Refresh the list
            } catch(SQLException e) {
                showAlert("Erreur", e.getMessage());
            }
        } else {
            showAlert("Attention", "Sélectionnez un cours et une filière.");
        }
    }

    private void clearFields() {
        txtCode.clear();
        txtIntitule.clear();
        listFiliereAffectees.getItems().clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}