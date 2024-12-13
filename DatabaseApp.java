import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
//import Database;

public class DatabaseApp extends Application {

    private Database db = new Database("database.db"); // Экземпляр базы данных

    @Override
    public void start(Stage primaryStage) {
        db.createTable(); // Создаем таблицу, если её нет

        // Элементы интерфейса
        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField valueField = new TextField();
        valueField.setPromptText("Value");

        Button addButton = new Button("Add Record");
        addButton.setOnAction(e -> {
            String name = nameField.getText();
            String value = valueField.getText();
            db.addRecord(name, value); // Используем метод из Database
            nameField.clear();
            valueField.clear();
        });

        Button displayButton = new Button("Display Records");
        displayButton.setOnAction(e -> db.displayAllRecords()); // Отображаем записи (пока в консоли)

        // Раскладка интерфейса
        VBox layout = new VBox(10, nameField, valueField, addButton, displayButton);
        Scene scene = new Scene(layout, 300, 200);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Database Manager");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Запуск GUI-приложения
    }
}
