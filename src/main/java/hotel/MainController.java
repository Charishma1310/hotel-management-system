/*
this file, Implements Initializable 
the initialize() method runs automatically after FXML loads.

the main functions used in this file are:
initialize() — Loads data from files into ObservableLists. 
Sets up PropertyValueFactory for every table column (this links the column to a getter by property name, e.g. "customerName" → getCustomerName()).
Creates two FilteredList<Booking> views (both filter Booking::isActive) — one for bookingTable, one for activeTable. historyTable gets the full unfiltered list. Calls refreshRoomCombo().
handleAddRoom() — Parses inputs, validates (no duplicates, price > 0, type selected),
creates a Room with available=true, adds to list, persists, refreshes combo.
handleBooking() — Validates all fields. Contact must match \d{10} regex. Calculates base = price × days. 
Generates booking ID using "BK" + (currentTimeMillis % 100000). Sets room to unavailable. Shows confirmation alert with GST breakdown (12%).
handleCheckout() — Gets selected row from activeTable. Sets room.setAvailable(true) and booking.setActive(false). 
Calls refresh() on all tables (needed because FilteredList updates reactively but TableView UI may need a nudge). 
Generates the formatted receipt in billArea with String.format and %n for newlines.
refreshRoomCombo() — Rebuilds the available-rooms dropdown by streaming roomList, filtering isAvailable(), mapping to room numbers.
persist() — Saves both lists to disk after every mutation.
*/

package hotel;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
public class MainController implements Initializable {
    private ObservableList<Room> roomList;
    private ObservableList<Booking> bookingList;
    @FXML private TextField tfRoomNo, tfPrice, tfName, tfContact, tfDays;
    @FXML private ComboBox<String> cbRoomType;
    @FXML private ComboBox<Integer> cbAvailRooms;
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room,Integer> colRoomNo;
    @FXML private TableColumn<Room,String> colRoomType, colRoomStatus;
    @FXML private TableColumn<Room,Double> colRoomPrice;
    @FXML private TableView<Booking> bookingTable, activeTable, historyTable;
    @FXML private TableColumn<Booking,String> colBkId, colBkName, colBkContact, colBkDate;
    @FXML private TableColumn<Booking,Integer> colBkRoom, colBkDays;
    @FXML private TableColumn<Booking,Double> colBkAmt;
    @FXML private TableColumn<Booking,String> colActId, colActName, colActContact, colActDate;
    @FXML private TableColumn<Booking,Integer> colActRoom, colActDays;
    @FXML private TableColumn<Booking,Double> colActAmt;
    @FXML private TableColumn<Booking,String> colHisId, colHisName, colHisContact, colHisDate, colHisStatus;
    @FXML private TableColumn<Booking,Integer> colHisRoom, colHisDays;
    @FXML private TableColumn<Booking,Double> colHisAmt;
    @FXML private TextArea billArea;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roomList    = FXCollections.observableArrayList(DataManager.loadRooms());
        bookingList = FXCollections.observableArrayList(DataManager.loadBookings());
        cbRoomType.setItems(FXCollections.observableArrayList("Single","Double","Deluxe"));
        colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colRoomPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        roomTable.setItems(roomList);
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        FilteredList<Booking> active = new FilteredList<>(bookingList, Booking::isActive);
        colBkId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colBkName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colBkContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colBkRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colBkDate.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colBkDays.setCellValueFactory(new PropertyValueFactory<>("days"));
        colBkAmt.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        bookingTable.setItems(active);
        FilteredList<Booking> active2 = new FilteredList<>(bookingList, Booking::isActive);
        colActId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colActName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colActContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colActRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colActDate.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colActDays.setCellValueFactory(new PropertyValueFactory<>("days"));
        colActAmt.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        activeTable.setItems(active2);
        colHisId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colHisName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colHisContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colHisRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colHisDate.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        colHisDays.setCellValueFactory(new PropertyValueFactory<>("days"));
        colHisAmt.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colHisStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        historyTable.setItems(bookingList);
        refreshRoomCombo();
    }
    @FXML private void handleAddRoom() {
        try {
            int num = Integer.parseInt(tfRoomNo.getText().trim());
            String type = cbRoomType.getValue();
            double price = Double.parseDouble(tfPrice.getText().trim());
            if (type == null) { alert(Alert.AlertType.ERROR,"Please select a room type."); return; }
            if (price <= 0)   { alert(Alert.AlertType.ERROR,"Price must be greater than zero."); return; }
            if (roomList.stream().anyMatch(r -> r.getRoomNumber() == num)) {
                alert(Alert.AlertType.ERROR,"Room "+num+" already exists."); return; }
            roomList.add(new Room(num, type, price, true));
            persist(); refreshRoomCombo();
            tfRoomNo.clear(); cbRoomType.setValue(null); tfPrice.clear();
            alert(Alert.AlertType.INFORMATION,"Room "+num+" ("+type+") added successfully.");
        } catch (NumberFormatException e) {
            alert(Alert.AlertType.ERROR,"Invalid input. Room Number must be integer, Price must be a number.");
        }
    }
    @FXML private void showAllRooms() { roomTable.setItems(roomList);
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); }
    @FXML private void showAvailableRooms() {
        ObservableList<Room> avail = FXCollections.observableArrayList();
        roomList.stream().filter(Room::isAvailable).forEach(avail::add);
        roomTable.setItems(avail);
    }
    @FXML private void handleBooking() {
        String name = tfName.getText().trim(), contact = tfContact.getText().trim(), daysStr = tfDays.getText().trim();
        Integer roomNo = cbAvailRooms.getValue();
        if (name.isEmpty()||contact.isEmpty()||roomNo==null||daysStr.isEmpty()) {
            alert(Alert.AlertType.ERROR,"All fields are required."); return; }
        if (!contact.matches("\\d{10}")) { alert(Alert.AlertType.ERROR,"Contact must be 10 digits."); return; }
        int days;
        try { days = Integer.parseInt(daysStr); if (days<=0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { alert(Alert.AlertType.ERROR,"Nights must be a positive whole number."); return; }
        Room room = roomList.stream().filter(r->r.getRoomNumber()==roomNo&&r.isAvailable()).findFirst().orElse(null);
        if (room==null) { alert(Alert.AlertType.ERROR,"Room "+roomNo+" is no longer available."); refreshRoomCombo(); return; }
        double base = room.getPricePerDay()*days;
        String id = "BK"+(System.currentTimeMillis()%100000);
        bookingList.add(new Booking(id,name,contact,roomNo,LocalDate.now(),days,base,true));
        room.setAvailable(false); persist(); refreshRoomCombo(); roomTable.refresh();
        tfName.clear(); tfContact.clear(); cbAvailRooms.setValue(null); tfDays.clear();
        alert(Alert.AlertType.INFORMATION,"Booking Confirmed!\nID: "+id+"\nRoom: "+roomNo+" ("+room.getType()+")\nNights: "+days+"\nBase: Rs."+String.format("%.2f",base)+"\nGST 12%: Rs."+String.format("%.2f",base*0.12)+"\nTOTAL: Rs."+String.format("%.2f",base*1.12));
    }
    @FXML private void handleCheckout() {
        Booking sel = activeTable.getSelectionModel().getSelectedItem();
        if (sel==null) { alert(Alert.AlertType.ERROR,"Please select a booking to check out."); return; }
        Room room = roomList.stream().filter(r->r.getRoomNumber()==sel.getRoomNumber()).findFirst().orElse(null);
        if (room!=null) room.setAvailable(true);
        sel.setActive(false); persist(); refreshRoomCombo();
        roomTable.refresh(); bookingTable.refresh(); activeTable.refresh(); historyTable.refresh();
        double base=sel.getTotalAmount(), gst=base*0.12, total=base+gst;
        billArea.setText(String.format(
            "+================================================+%n"+
            "|       LUXSTAY HOTEL  --  TAX INVOICE          |%n"+
            "+================================================+%n"+
            "|  Booking ID  : %-30s|%n"+
            "|  Customer    : %-30s|%n"+
            "|  Contact     : %-30s|%n"+
            "+------------------------------------------------+%n"+
            "|  Room No.    : %-30s|%n"+
            "|  Room Type   : %-30s|%n"+
            "|  Check-In    : %-30s|%n"+
            "|  Check-Out   : %-30s|%n"+
            "|  Nights      : %-30s|%n"+
            "+------------------------------------------------+%n"+
            "|  Rate/Night  : Rs. %-28s|%n"+
            "|  Room Charges: Rs. %-28s|%n"+
            "|  GST  (12%%) : Rs. %-28s|%n"+
            "+------------------------------------------------+%n"+
            "|  TOTAL AMOUNT: Rs. %-28s|%n"+
            "+------------------------------------------------+%n"+
            "|    Thank you for staying at LuxStay!          |%n"+
            "+================================================+%n",
            sel.getBookingId(), sel.getCustomerName(), sel.getContact(),
            sel.getRoomNumber(), room!=null?room.getType():"N/A",
            sel.getCheckInDate(), LocalDate.now(), sel.getDays()+" night(s)",
            room!=null?String.format("%.2f",room.getPricePerDay()):"N/A",
            String.format("%.2f",base), String.format("%.2f",gst), String.format("%.2f",total)));
        alert(Alert.AlertType.INFORMATION,"Checkout complete! Room "+sel.getRoomNumber()+" is now available.");
    }
    private void refreshRoomCombo() {
        ObservableList<Integer> avail = FXCollections.observableArrayList();
        roomList.stream().filter(Room::isAvailable).map(Room::getRoomNumber).forEach(avail::add);
        cbAvailRooms.setItems(avail);
    }
    private void persist() {
        DataManager.saveRooms(new ArrayList<>(roomList));
        DataManager.saveBookings(new ArrayList<>(bookingList));
    }
    private void alert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type); a.setTitle("LuxStay"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
