import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Database {
    private Connection connection;
    private ObservableList<Record> data = FXCollections.observableArrayList();

    public Database(String dbName) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
        } catch (SQLException e) {
            System.err.println("Open DB error: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Close DB error: " + e.getMessage());
        }
    }

    public void createTable() {
        //String dropSql = "DROP TABLE IF EXISTS records;";
        //executeSQL(dropSql);

        String sql = "CREATE TABLE IF NOT EXISTS records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_nae TEXT, " +
                "last_name TEXT, " +
                "sport TEXT, " +
                "birth_date DATE, " +
                "height REAL, " +
                "is_active_now BOOLEAN)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Table creation error: " + e.getMessage());
        }
    }

    public void addRecord(String firstName, String lastName, String sport, String birthDate, float height, boolean isActiveNow) {
        try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO records (first_name, last_name, sport, birth_date, height, is_active_now) VALUES (?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, sport);
            pstmt.setString(4, birthDate);
            pstmt.setFloat(5, height);
            pstmt.setBoolean(6, isActiveNow);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Insert error: " + e.getMessage());
        }
    }

    public void deleteRecord(String field, String value) {
        try (Statement stmt = connection.createStatement()) {
            String sql = String.format("DELETE FROM records WHERE %s = '%s'", field.toLowerCase().replace(" ", "_"), value);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Delete error: " + e.getMessage());
        }
    }

    public void displayAllRecords(TableView<Record> tableView) {
        data.clear();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM records")) {
            while (rs.next()) {
                data.add(new Record(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("sport"),
                        rs.getString("birth_date"),
                        rs.getFloat("height"),
                        rs.getBoolean("is_active_now")
                ));
            }
            tableView.setItems(data);
        } catch (SQLException e) {
            System.err.println("Display error: " + e.getMessage());
        }
    }


    public void searchRecords(String field, String value, TableView<Record>  tableView) {
        String sql = String.format("SELECT * FROM records WHERE %s LIKE ?", field.toLowerCase().replace(" ", "_"));
        data.clear();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + value + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                data.add(new Record(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("sport"),
                        rs.getString("birth_date"),
                        rs.getFloat("height"),
                        rs.getBoolean("is_active_now")
                ));
            }
            tableView.setItems(data); // view only suitable obj
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
    }

    public void updateRecord(int id, String firstName, String lastName, String sport, String birthDate, float height, boolean isActiveNow) {
        String sql = "UPDATE records SET first_name = ?, last_name = ?, sport = ?, birth_date = ?, height = ?, is_active_now = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, sport);
            pstmt.setString(4, birthDate);
            pstmt.setFloat(5, height);
            pstmt.setBoolean(6, isActiveNow);
            pstmt.setInt(7, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Update error: " + e.getMessage());
        }
    }

    // Метод для создания резервной копии
    public void createBackup(String backupFileName) {
        String dbName = "database.db";
        File dbFile = new File(dbName);
        File backupFile = new File(backupFileName);

        try (FileInputStream fis = new FileInputStream(dbFile);
             FileOutputStream fos = new FileOutputStream(backupFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            System.out.println("Backup created successfully: " + backupFileName);
        } catch (IOException e) {
            System.err.println("Backup error: " + e.getMessage());
        }
    }

    public void restoreFromBackup(String backupFileName) {
        String dbName = "database_frombackup.db";
        File backupFile = new File(backupFileName);
        File dbFile = new File(dbName);

        if (!backupFile.exists()) {
            System.err.println("Backup file does not exist: " + backupFileName);
            return;
        }
        try {
            // Закрываем текущее соединение с базой данных
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed successfully.");
            }
            // Заменяем текущий файл базы данных файлом резервной копии
            Files.copy(backupFile.toPath(), dbFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Database restored from backup: " + backupFileName);
            // Повторно открываем соединение
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            System.out.println("Database connection reopened successfully.");
        } catch (IOException e) {
            System.err.println("File copy error: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public void exportToExcel(String excelFileName) {
        String sql = "SELECT * FROM records";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(excelFileName)) {

            // Создаем новый лист
            Sheet sheet = workbook.createSheet("Records");

            // Заголовки таблицы
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "First Name", "Last Name", "Sport", "Birth Date", "Height", "Is Active Now"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Получаем данные из БД
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(rs.getInt("id"));
                    row.createCell(1).setCellValue(rs.getString("first_name"));
                    row.createCell(2).setCellValue(rs.getString("last_name"));
                    row.createCell(3).setCellValue(rs.getString("sport"));
                    row.createCell(4).setCellValue(rs.getString("birth_date"));
                    row.createCell(5).setCellValue(rs.getFloat("height"));
                    row.createCell(6).setCellValue(rs.getBoolean("is_active_now"));
                }
            }

            // Автоматическая подгонка ширины колонок
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Сохраняем файл Excel
            workbook.write(fileOut);
            System.out.println("Data successfully exported to: " + excelFileName);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("File writing error: " + e.getMessage());
        }
    }



    private void executeSQL(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

}
