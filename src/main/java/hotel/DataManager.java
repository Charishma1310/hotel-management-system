
/*
So basically, this DataManager class is the responsible adult of the program 
it saves all the room and booking data using serialization so nothing gets lost like my motivation during exams. 
It uses ObjectOutputStream and ObjectInputStream to dump and retrieve entire objects directly, 
because who has time to write everything manually? All methods are static so we don’t even need to create an object
lazy but efficient, just how we like it. And if something goes wrong or files don’t exist, it doesn’t panic,
 just returns an empty list like ‘it is what it is’,
 keeping the program from crashing. 

*/

package hotel;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
public class DataManager {
    // gonna use these constants to define file paths for storing serialized data.
    private static final String ROOMS_FILE = "rooms.dat";
    private static final String BOOKINGS_FILE = "bookings.dat";
    public static void saveRooms(List<Room> rooms) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ROOMS_FILE))) {
            oos.writeObject(rooms);
        } catch (IOException e) { System.err.println("Could not save rooms: " + e.getMessage()); }
    }
    @SuppressWarnings("unchecked")
    public static List<Room> loadRooms() {
        File f = new File(ROOMS_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<Room>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) { return new ArrayList<>(); }
    }
    public static void saveBookings(List<Booking> bookings) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKINGS_FILE))) {
            oos.writeObject(bookings);
        } catch (IOException e) { System.err.println("Could not save bookings: " + e.getMessage()); }
    }
    @SuppressWarnings("unchecked")
    public static List<Booking> loadBookings() {
        File f = new File(BOOKINGS_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<Booking>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) { return new ArrayList<>(); }
    }
}


/*
I used supress warnings cause java warns on unsafe casting, or unchecked type casting
so for asving: 
Create file stream
Wrap with obj stream
Write obj
Close auto

and for loading
Check if file exists
Open input stream
Read obj
Cast to correct type
Return data
*/