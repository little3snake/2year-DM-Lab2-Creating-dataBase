import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class DatabaseApp extends Application {

    private TableView<Record> tableView;
    private Connection connection;
    private Database db = new Database("database.db"); // Экземпляр базы данных

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        connectToDB();
        db.createTable();

        primaryStage.setTitle("Database Manager");


        TableColumn<Record, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Record, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Record, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Record, String> sportColumn = new TableColumn<>("Sport");
        sportColumn.setCellValueFactory(new PropertyValueFactory<>("sport"));

        TableColumn<Record, String> birthDateColumn = new TableColumn<>("Birth Date");
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));

        TableColumn<Record, Float> heightColumn = new TableColumn<>("Height");
        heightColumn.setCellValueFactory(new PropertyValueFactory<>("height"));

        TableColumn<Record, Boolean> isActiveColumn = new TableColumn<>("Is Active");
        isActiveColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isActiveNow()));

        // Создание TableView
        tableView = new TableView<>();
        tableView.getColumns().addAll(idColumn, firstNameColumn, lastNameColumn, sportColumn, birthDateColumn, heightColumn, isActiveColumn);



        //                                                                   add
        // Поля для добавления записи
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField sportField = new TextField();
        sportField.setPromptText("Sport");

        TextField birthDateField = new TextField();
        birthDateField.setPromptText("Birth Date (YYYY-MM-DD)");

        TextField heightField = new TextField();
        heightField.setPromptText("Height");

        CheckBox isActiveCheckBox = new CheckBox("Is Active");

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            db.addRecord(firstNameField.getText(), lastNameField.getText(), sportField.getText(),
                    birthDateField.getText(), Float.parseFloat(heightField.getText()), isActiveCheckBox.isSelected());
            firstNameField.clear();
            lastNameField.clear();
            sportField.clear();
            birthDateField.clear();
            heightField.clear();
            isActiveCheckBox.setSelected(false);
            db.displayAllRecords(tableView);
        });

        //                                                                    edit
        // fields for edit
        TextField e_firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        TextField e_lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        TextField e_sportField = new TextField();
        sportField.setPromptText("Sport");

        TextField e_birthDateField = new TextField();
        birthDateField.setPromptText("Birth Date (YYYY-MM-DD)");

        TextField e_heightField = new TextField();
        heightField.setPromptText("Height");

        CheckBox e_isActiveCheckBox = new CheckBox("Is Active");
        // Кнопка для редактирования записи
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
            Record selectedRecord = tableView.getSelectionModel().getSelectedItem();
            if (selectedRecord != null) {
                db.updateRecord(
                        selectedRecord.getId(),
                        e_firstNameField.getText(),
                        e_lastNameField.getText(),
                        e_sportField.getText(),
                        e_birthDateField.getText(),
                        Float.parseFloat(e_heightField.getText()),
                        e_isActiveCheckBox.isSelected()
                );
                db.displayAllRecords(tableView);
            } else {
                System.out.println("No record selected for editing.");
            }
        });
        // Обработчик выбора записи из таблицы
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Record selectedRecord = tableView.getSelectionModel().getSelectedItem();
                e_firstNameField.setText(selectedRecord.getFirstName());
                e_lastNameField.setText(selectedRecord.getLastName());
                e_sportField.setText(selectedRecord.getSport());
                e_birthDateField.setText(selectedRecord.getBirthDate());
                e_heightField.setText(String.valueOf(selectedRecord.getHeight()));
                e_isActiveCheckBox.setSelected(selectedRecord.isActiveNow());
            }
        });


        //                                                                    search
        ComboBox<String> searchFieldCombo = new ComboBox<>();
        searchFieldCombo.setItems(FXCollections.observableArrayList("ID", "First Name", "Last Name", "Sport", "Birth Date", "Height", "Is Active"));
        searchFieldCombo.setPromptText("Field");

        TextField searchValueField = new TextField();
        searchValueField.setPromptText("Value");

        Button searchButton = new Button("Search");
        Button resetButton = new Button("Reset");

        searchButton.setOnAction(e -> {
            db.searchRecords(searchFieldCombo.getValue(), searchValueField.getText(), tableView);
            searchValueField.clear();
            searchFieldCombo.setPromptText("Field");
        });
        resetButton.setOnAction(e -> {
            db.displayAllRecords(tableView);
            searchFieldCombo.setPromptText("Field");
            searchValueField.clear();
        });

        //                                                              delete
        ComboBox<String> deleteFieldCombo = new ComboBox<>();
        deleteFieldCombo.setItems(FXCollections.observableArrayList("ID", "First Name", "Last Name", "Sport", "Birth Date", "Height", "Is Active"));
        deleteFieldCombo.setPromptText("Field");

        TextField deleteValueField = new TextField();
        deleteValueField.setPromptText("Value");

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            if (deleteFieldCombo.getValue() != null && !deleteValueField.getText().isEmpty()) {
                db.deleteRecord(deleteFieldCombo.getValue(), deleteValueField.getText());
                deleteFieldCombo.setPromptText("Field");
                deleteValueField.clear();
                db.displayAllRecords(tableView);
            }
        });

        //                                                            create backup
        TextField backupValueField = new TextField();
        searchValueField.setPromptText("Write backupFile name");

        Button backupButton = new Button("Create Backup");
        // eg "backup_database.db"
        backupButton.setOnAction(e -> {
            db.createBackup(backupValueField.getText());
            backupValueField.clear();
        });

        //                                                          restore from backup
        // Поле для имени файла резервной копии
        TextField restoreBackupField = new TextField();
        restoreBackupField.setPromptText("Backup File Name");

        Button restoreBackupButton = new Button("Restore Backup");
        restoreBackupButton.setOnAction(e -> {
            String backupFileName = restoreBackupField.getText();
            if (!backupFileName.isEmpty()) {
                db.restoreFromBackup(backupFileName);
                db.displayAllRecords(tableView); // Обновляем отображение данных после восстановления
                restoreBackupField.clear();
            } else {
                System.out.println("Please provide a backup file name.");
            }
        });


        // Компоновка интерфейса
        HBox addBox = new HBox(10, firstNameField, lastNameField, sportField, birthDateField, heightField, isActiveCheckBox, addButton);
        addBox.setPadding(new Insets(10));

        HBox editBox = new HBox(10, e_firstNameField, e_lastNameField, e_sportField, e_birthDateField, e_heightField, e_isActiveCheckBox, editButton);
        editBox.setPadding(new Insets(10));

        HBox searchBox = new HBox(10, searchFieldCombo, searchValueField, searchButton, resetButton);
        searchBox.setPadding(new Insets(10));

        HBox deleteBox = new HBox(10, deleteFieldCombo, deleteValueField, deleteButton);
        deleteBox.setPadding(new Insets(10));

        HBox backupBox = new HBox(10, backupValueField, backupButton, restoreBackupField, restoreBackupButton);
        backupBox.setPadding(new Insets(10));

        //HBox restoreBox = new HBox(10, );
        //restoreBox.setPadding(new Insets(10));

        VBox root = new VBox(10, tableView, addBox, editBox, searchBox, deleteBox, backupBox);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        db.displayAllRecords(tableView);
    }

    private void connectToDB() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}
