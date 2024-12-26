package com.pboass3;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;


public class PhonebookController {
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private TableView<Contact> contactTable;
    @FXML private TableColumn<Contact, String> nameColumn;
    @FXML private TableColumn<Contact, String> phoneColumn;
    @FXML private TableColumn<Contact, String> emailColumn;
    @FXML private TableColumn<Contact, String> addressColumn;
    
    private ObservableList<Contact> contactList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        loadContacts();
        
        contactTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                phoneField.setText(newSelection.getPhone());
                emailField.setText(newSelection.getEmail());
                addressField.setText(newSelection.getAddress());
            }
        });
    }
    
    private void loadContacts() {
        contactList.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM contacts")) {
            
            while (rs.next()) {
                contactList.add(new Contact(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address")
                ));
            }
            contactTable.setItems(contactList);
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAdd() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO contacts (name, phone, email, address) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, phoneField.getText());
            pstmt.setString(3, emailField.getText());
            pstmt.setString(4, addressField.getText());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    Contact newContact = new Contact(
                        rs.getInt(1),
                        nameField.getText(),
                        phoneField.getText(),
                        emailField.getText(),
                        addressField.getText()
                    );
                    contactList.add(newContact);
                    clearFields();
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Could not add contact: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdate() {
        Contact selectedContact = contactTable.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            showAlert("Error", "Please select a contact to update");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE contacts SET name=?, phone=?, email=?, address=? WHERE id=?")) {
            
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, phoneField.getText());
            pstmt.setString(3, emailField.getText());
            pstmt.setString(4, addressField.getText());
            pstmt.setInt(5, selectedContact.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                selectedContact.setName(nameField.getText());
                selectedContact.setPhone(phoneField.getText());
                selectedContact.setEmail(emailField.getText());
                selectedContact.setAddress(addressField.getText());
                contactTable.refresh();
                clearFields();
            }
        } catch (SQLException e) {
            showAlert("Error", "Could not update contact: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        Contact selectedContact = contactTable.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            showAlert("Error", "Please select a contact to delete");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM contacts WHERE id=?")) {
            
            pstmt.setInt(1, selectedContact.getId());
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                contactList.remove(selectedContact);
                clearFields();
            }
        } catch (SQLException e) {
            showAlert("Error", "Could not delete contact: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
