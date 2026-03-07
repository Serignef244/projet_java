package client.controller;

import client.model.UserView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * UserListController - composant reutilisable pour l'affichage des membres.
 */
public class UserListController {
    @FXML
    private TextField champRechercheUtilisateur;
    @FXML
    private ListView<UserView> listeUtilisateurs;

    private final ObservableList<UserView> source = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        champRechercheUtilisateur.textProperty().addListener((obs, oldValue, newValue) -> appliquerFiltre());
        listeUtilisateurs.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UserView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                Label avatar = new Label(initiales(item.username()));
                avatar.getStyleClass().add("avatar");

                Label username = new Label(item.username());
                Label role = new Label(item.role());
                role.getStyleClass().add("caption-text");
                VBox infos = new VBox(username, role);
                infos.setSpacing(2);

                Label status = new Label(item.status());
                status.getStyleClass().addAll("status-pill",
                        "ONLINE".equalsIgnoreCase(item.status()) ? "status-online" : "status-offline");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(8, avatar, infos, spacer, status);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("member-row");
                setGraphic(row);
            }
        });
    }

    public void setUsers(List<UserView> users) {
        source.setAll(users);
        appliquerFiltre();
    }

    public ListView<UserView> listView() {
        return listeUtilisateurs;
    }

    private void appliquerFiltre() {
        String filtre = champRechercheUtilisateur.getText() == null ? "" : champRechercheUtilisateur.getText().trim().toLowerCase();
        listeUtilisateurs.getItems().setAll(source.stream()
                .filter(u -> filtre.isBlank() || u.username().toLowerCase().contains(filtre))
                .toList());
    }

    private String initiales(String username) {
        if (username == null || username.isBlank()) {
            return "?";
        }
        String[] parts = username.split("[._\\-\\s]+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return parts[0].length() >= 2 ? parts[0].substring(0, 2).toUpperCase() : parts[0].toUpperCase();
    }
}
