import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Record {
    private final SimpleIntegerProperty id;
    private SimpleStringProperty firstName;
    private SimpleStringProperty lastName;
    private SimpleStringProperty sport;
    private SimpleStringProperty birthDate;
    private SimpleFloatProperty height;
    private SimpleBooleanProperty activeNow;

    public Record(int id, String firstName, String lastName, String sport, String birthDate, float height, boolean activeNow) {
        this.id = new SimpleIntegerProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.sport = new SimpleStringProperty(sport);
        this.birthDate = new SimpleStringProperty(birthDate);
        this.height = new SimpleFloatProperty(height);
        this.activeNow = new SimpleBooleanProperty(activeNow);
    }

    public int getId() { return id.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getSport() { return sport.get(); }
    public String getBirthDate() { return birthDate.get(); }
    public float getHeight() { return height.get(); }
    public boolean isActiveNow() { return activeNow.get(); }
}
