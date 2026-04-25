//This file is gonna represent a single hotel room. It is kinda like the data structure room
// Implements serializable so it can be saved to disk. Variables are roomNum, type, priceperDay
// available. The getStatusDisplay() method returns Available or Occupied this is what the
//  TableView column shows directly via PropertyValueFactory

package hotel;
import java.io.Serializable;
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private int roomNumber;
    private String type;
    private double pricePerDay;
    private boolean available;
    public Room(int roomNumber, String type, double pricePerDay, boolean available) {
        this.roomNumber = roomNumber; this.type = type;
        this.pricePerDay = pricePerDay; this.available = available;
    }
    public int getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public double getPricePerDay() { return pricePerDay; }
    public boolean isAvailable() { return available; }
    public String getStatusDisplay() { return available ? "Available" : "Occupied"; }
    public void setAvailable(boolean available) { this.available = available; }
    public String toString() { return roomNumber + " - " + type + " (Rs." + pricePerDay + "/night)"; }
}
