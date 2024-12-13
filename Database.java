import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class Database {
    private Connection connection;

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
        String sql = "CREATE TABLE IF NOT EXISTS records (id INTEGER PRIMARY KEY, name TEXT UNIQUE, value TEXT);";
        executeSQL(sql);
    }

    public void addRecord(String name, String value) {
        String sql = "INSERT INTO records (name, value) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    public void deleteRecord(String fieldName, String value) {
        String sql = "DELETE FROM records WHERE " + fieldName + " = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    public void deleteRecord(int id) {
        String sql = "DELETE FROM records WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    public void displayAllRecords() {
        String sql = "SELECT * FROM records;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String value = rs.getString("value");
                System.out.println("ID: " + id + ", Name: " + name + ", Value: " + value);
            }
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
    }

    public void searchRecord(String fieldName, String value) {
        String sql = "SELECT * FROM records WHERE " + fieldName + " = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String recordValue = rs.getString("value");
                    System.out.println("ID: " + id + ", Name: " + name + ", Value: " + recordValue);
                }
            }
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
    }

    public void editRecord(int id, String newValue) {
        String sql = "UPDATE records SET value = ? WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newValue);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    public void backupDatabase(String backupFileName) {
        try (InputStream source = new FileInputStream("database.db");
             OutputStream dest = new FileOutputStream(backupFileName)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                dest.write(buffer, 0, length);
            }
            System.out.println("Backup created");
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }

    public void importFromCsv(String csvFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            // Skip header
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    addRecord(parts[0], parts[1]);
                }
            }
            System.out.println("CSV file imported successfully");
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }

    public void restoreDatabase(String backupFileName) {
        try (InputStream source = new FileInputStream(backupFileName);
             OutputStream dest = new FileOutputStream("database.db")) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                dest.write(buffer, 0, length);
            }
            System.out.println("DB restored from backup successfully.");
        } catch (IOException e) {
            System.err.println("File error: " + e.getMessage());
        }
    }

    private void executeSQL(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Database db = new Database("database.db");
        db.createTable();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Specify a key(name): ");
                    String name = scanner.nextLine();
                    System.out.print("Specify a value: ");
                    String value = scanner.nextLine();
                    db.addRecord(name, value);
                    break;
                case 2:
                    System.out.print("Search by id[1] or key(name) and value[2]: ");
                    int mode = scanner.nextInt();
                    scanner.nextLine();
                    if (mode == 1) {
                        System.out.print("Specify ID: ");
                        int id = scanner.nextInt();
                        db.deleteRecord(id);
                    } else if (mode == 2) {
                        System.out.print("Search by key(name) or value? ");
                        String fieldName = scanner.nextLine();
                        System.out.print("Specify value: ");
                        String fieldValue = scanner.nextLine();
                        db.deleteRecord(fieldName, fieldValue);
                    } else {
                        System.out.println("Wrong input, try again");
                    }
                    break;
                case 3:
                    System.out.print("Search by key(name) or value? ");
                    String searchField = scanner.nextLine();
                    System.out.print("Specify value: ");
                    String searchValue = scanner.nextLine();
                    db.searchRecord(searchField, searchValue);
                    break;
                case 4:
                    System.out.print("Specify ID: ");
                    int editId = scanner.nextInt();
                    scanner.nextLine();
                    System.out.print("Specify new value: ");
                    String newValue = scanner.nextLine();
                    db.editRecord(editId, newValue);
                    break;
                case 5:
                    db.backupDatabase("backup.db");
                    break;
                case 6:
                    db.restoreDatabase("backup.db");
                    break;
                case 7:
                    db.displayAllRecords();
                    break;
                case 8:
                    System.out.print("Path/to/.csv file: ");
                    String csvFile = scanner.nextLine();
                    db.importFromCsv(csvFile);
                    break;
                case 0:
                    db.close();
                    return;
                default:
                    System.out.println("Wrong input, try again");
            }
        }
    }

    public static void displayMenu() {
        System.out.println("______________________________________________");
        System.out.println("Select an action:");
        System.out.println("1. Add a record");
        System.out.println("2. Delete record");
        System.out.println("3. Search");
        System.out.println("4. Edit an existing record");
        System.out.println("5. Create a backup");
        System.out.println("6. Restore DB from backup");
        System.out.println("7. Display DB");
        System.out.println("8. CSV import");
        System.out.println("0. Exit");
    }
}
