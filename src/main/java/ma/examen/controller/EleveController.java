package ma.examen.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import ma.examen.dao.EleveDAO;
import ma.examen.dao.FiliereDAO;
import ma.examen.dao.CoursDAO;
import ma.examen.model.Eleve;
import ma.examen.model.Filiere;
import ma.examen.model.Cours;
import ma.examen.model.DossierAdministratif;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;

public class EleveController {
    @FXML private TextField txtMatricule, txtNom, txtPrenom, txtEmail;
    @FXML private ComboBox<Filiere> cmbFiliere;
    @FXML private TableView<Eleve> table;
    @FXML private TableColumn<Eleve, String> colMat, colNom, colPrenom, colFil;
    @FXML private ListView<Cours> listCoursInscrits;

    private EleveDAO eleveDAO = new EleveDAO();
    private FiliereDAO filiereDAO = new FiliereDAO();
    private CoursDAO coursDAO = new CoursDAO();

    @FXML
    public void initialize() {
        colMat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMatricule()));
        colNom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNom()));
        colPrenom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrenom()));
        colFil.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFiliere().getNom()));

        listCoursInscrits.setCellFactory(param -> new ListCell<Cours>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getIntitule());
            }
        });

        try {
            cmbFiliere.setItems(FXCollections.observableArrayList(filiereDAO.findAll()));
            cmbFiliere.setConverter(new javafx.util.StringConverter<Filiere>() {
                @Override public String toString(Filiere f) { return f == null ? null : f.getNom(); }
                @Override public Filiere fromString(String string) { return null; }
            });
            loadData();
        } catch (SQLException e) { showAlert("Erreur Init", e.getMessage()); }

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal != null) {
                txtMatricule.setText(newVal.getMatricule());
                txtNom.setText(newVal.getNom());
                txtPrenom.setText(newVal.getPrenom());
                txtEmail.setText(newVal.getEmail());

                for(Filiere f : cmbFiliere.getItems()) {
                    if(f.getId() == newVal.getFiliere().getId()) {
                        cmbFiliere.getSelectionModel().select(f);
                        break;
                    }
                }
                loadEnrolledCourses(newVal.getId());
            } else {
                clearFields();
            }
        });
    }

    private void loadData() throws SQLException {
        table.setItems(FXCollections.observableArrayList(eleveDAO.findAll()));
    }

    private void loadEnrolledCourses(int eleveId) {
        try {
            listCoursInscrits.setItems(FXCollections.observableArrayList(eleveDAO.findCoursByEleve(eleveId)));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleGestionInscriptions() {
        Eleve selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Attention", "Veuillez sélectionner un étudiant.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Gestion des Inscriptions");
        dialog.setHeaderText("Inscriptions pour : " + selected.getNom() + " " + selected.getPrenom());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Label lblInstructions = new Label("Mode d'emploi : Sélectionnez un cours disponible (gauche) et cliquez sur 'Ajouter >>' pour inscrire l'étudiant.\nPour désinscrire, sélectionnez un cours inscrit (droite) et cliquez sur '<< Retirer'.");
        lblInstructions.setStyle("-fx-font-style: italic; -fx-text-fill: #555; -fx-padding: 0 0 10 0;");

        ListView<Cours> lvAvailable = new ListView<>();
        ListView<Cours> lvEnrolled = new ListView<>();

        Callback<ListView<Cours>, ListCell<Cours>> cellFactory = p -> new ListCell<Cours>() {
            @Override protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getIntitule());
            }
        };
        lvAvailable.setCellFactory(cellFactory);
        lvEnrolled.setCellFactory(cellFactory);

        Button btnAdd = new Button("Ajouter >>");
        Button btnRemove = new Button("<< Retirer");
        VBox buttons = new VBox(10, btnAdd, btnRemove);
        buttons.setStyle("-fx-alignment: center; -fx-padding: 10;");

        HBox listsLayout = new HBox(10);
        VBox left = new VBox(5, new Label("Cours Disponibles"), lvAvailable);
        VBox right = new VBox(5, new Label("Cours Inscrits"), lvEnrolled);

        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        listsLayout.getChildren().addAll(left, buttons, right);

        VBox rootLayout = new VBox(5, lblInstructions, listsLayout);
        dialog.getDialogPane().setContent(rootLayout);

        Runnable refreshLists = () -> {
            try {
                List<Cours> allMajorCourses = coursDAO.findByFiliere(selected.getFiliere().getId());
                List<Cours> enrolled = eleveDAO.findCoursByEleve(selected.getId());

                List<Integer> enrolledIds = enrolled.stream().map(Cours::getId).collect(Collectors.toList());
                List<Cours> available = allMajorCourses.stream()
                        .filter(c -> !enrolledIds.contains(c.getId()))
                        .collect(Collectors.toList());

                lvAvailable.setItems(FXCollections.observableArrayList(available));
                lvEnrolled.setItems(FXCollections.observableArrayList(enrolled));
                listCoursInscrits.setItems(FXCollections.observableArrayList(enrolled));

            } catch (SQLException e) { e.printStackTrace(); }
        };

        refreshLists.run();

        btnAdd.setOnAction(e -> {
            Cours c = lvAvailable.getSelectionModel().getSelectedItem();
            if (c != null) {
                try {
                    eleveDAO.addInscription(selected.getId(), c.getId());
                    refreshLists.run();
                } catch (SQLException ex) { showAlert("Erreur", ex.getMessage()); }
            }
        });

        btnRemove.setOnAction(e -> {
            Cours c = lvEnrolled.getSelectionModel().getSelectedItem();
            if (c != null) {
                try {
                    eleveDAO.removeInscription(selected.getId(), c.getId());
                    refreshLists.run();
                } catch (SQLException ex) { showAlert("Erreur", ex.getMessage()); }
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleAjouter() {
        if(!isValid()) return;
        try {
            Eleve e = new Eleve(0, txtMatricule.getText(), txtNom.getText(), txtPrenom.getText(), txtEmail.getText(), cmbFiliere.getValue());
            eleveDAO.save(e);
            loadData();
            clearFields();
            showAlert("Succès", "Étudiant ajouté.");
        } catch (SQLException e) { showAlert("Erreur Ajout", e.getMessage()); }
    }

    @FXML
    private void handleModifier() {
        Eleve selected = table.getSelectionModel().getSelectedItem();
        if(selected == null) { showAlert("Attention", "Sélectionnez un élève."); return; }
        if(!isValid()) return;

        try {
            Eleve e = new Eleve(selected.getId(), txtMatricule.getText(), txtNom.getText(), txtPrenom.getText(), txtEmail.getText(), cmbFiliere.getValue());
            eleveDAO.update(e);
            loadData();
            clearFields();
            showAlert("Succès", "Étudiant modifié.");
        } catch (SQLException e) { showAlert("Erreur Modification", e.getMessage()); }
    }

    @FXML
    private void handleSupprimer() {
        Eleve selected = table.getSelectionModel().getSelectedItem();
        if(selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + selected.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if(resp == ButtonType.YES) {
                try {
                    eleveDAO.delete(selected.getId());
                    loadData();
                    clearFields();
                } catch (SQLException e) { showAlert("Erreur Suppression", e.getMessage()); }
            }
        });
    }

    @FXML
    private void handleDossier() {
        Eleve selected = table.getSelectionModel().getSelectedItem();
        if(selected == null) {
            showAlert("Attention", "Sélectionnez un élève.");
            return;
        }

        try {
            DossierAdministratif d = eleveDAO.getDossier(selected.getId());
            if(d == null) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Nouveau Dossier");
                dialog.setHeaderText("Création du dossier pour " + selected.getNom());
                dialog.setContentText("Numéro d'inscription :");
                dialog.showAndWait().ifPresent(num -> {
                    if(num.trim().isEmpty()) return;
                    try {
                        eleveDAO.saveDossier(new DossierAdministratif(0, num, LocalDate.now(), selected.getId()));
                        Alert info = new Alert(Alert.AlertType.INFORMATION, "Dossier créé!");
                        info.show();
                    } catch (SQLException e) { showAlert("Erreur", e.getMessage()); }
                });
            } else {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Dossier Administratif");
                dialog.setHeaderText("Modifier Dossier");

                ButtonType btnSave = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10);

                TextField txtNum = new TextField(d.getNumeroInscription());
                DatePicker datePicker = new DatePicker(d.getDateCreation());
                datePicker.setDisable(true);

                grid.add(new Label("Numéro Inscription:"), 0, 0);
                grid.add(txtNum, 1, 0);
                grid.add(new Label("Date Création:"), 0, 1);
                grid.add(datePicker, 1, 1);

                dialog.getDialogPane().setContent(grid);

                dialog.showAndWait().ifPresent(response -> {
                    if (response == btnSave) {
                        d.setNumeroInscription(txtNum.getText());
                        try {
                            eleveDAO.updateDossier(d);
                            Alert info = new Alert(Alert.AlertType.INFORMATION, "Dossier mis à jour!");
                            info.show();
                        } catch (SQLException e) { showAlert("Erreur Update", e.getMessage()); }
                    }
                });
            }
        } catch (SQLException e) { showAlert("Erreur SQL", e.getMessage()); }
    }

    private void clearFields() {
        txtMatricule.clear(); txtNom.clear(); txtPrenom.clear(); txtEmail.clear();
        cmbFiliere.getSelectionModel().clearSelection();
        table.getSelectionModel().clearSelection();
        listCoursInscrits.getItems().clear();
    }

    private boolean isValid() {
        if(txtMatricule.getText().isEmpty() || txtNom.getText().isEmpty() || cmbFiliere.getValue() == null) {
            showAlert("Validation", "Matricule, Nom et Filière sont obligatoires.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}