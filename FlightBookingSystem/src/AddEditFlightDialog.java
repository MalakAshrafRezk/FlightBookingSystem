
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddEditFlightDialog extends JDialog {
    private JTextField flightNumberField, airlineField, sourceField, destinationField,
            departureField, arrivalField, seatsField, priceField;
    private JComboBox<String> flightTypeBox;
    private JButton saveButton, cancelButton;
    private boolean saved = false;
    private Flight originalFlight;

    public AddEditFlightDialog(Frame windowAncestor, Frame owner, Flight flightToEdit) {
        super(owner, true);
        this.originalFlight = flightToEdit;

        setTitle(flightToEdit == null ? "Add Flight" : "Edit Flight");
        setSize(400, 500);
        setLocationRelativeTo(owner);

        initUI();
        if (flightToEdit != null) {
            populateFields(flightToEdit);
        }
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridLayout(9, 2, 10, 10));

        flightNumberField = new JTextField();
        airlineField = new JTextField();
        sourceField = new JTextField();
        destinationField = new JTextField();
        departureField = new JTextField();
        arrivalField = new JTextField();
        seatsField = new JTextField();
        priceField = new JTextField();
        flightTypeBox = new JComboBox<>(new String[]{"Domestic", "International"});

        formPanel.add(new JLabel("Flight Number:"));
        formPanel.add(flightNumberField);
        formPanel.add(new JLabel("Airline:"));
        formPanel.add(airlineField);
        formPanel.add(new JLabel("Source:"));
        formPanel.add(sourceField);
        formPanel.add(new JLabel("Destination:"));
        formPanel.add(destinationField);
        formPanel.add(new JLabel("Departure (yyyy-MM-dd HH:mm):"));
        formPanel.add(departureField);
        formPanel.add(new JLabel("Arrival (yyyy-MM-dd HH:mm):"));
        formPanel.add(arrivalField);
        formPanel.add(new JLabel("Seats Available:"));
        formPanel.add(seatsField);
        formPanel.add(new JLabel("Price (Economy):"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Flight Type:"));
        formPanel.add(flightTypeBox);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(this::saveFlight);
        cancelButton.addActionListener(e -> dispose());
    }

    private void populateFields(Flight f) {
        flightNumberField.setText(f.getFlightNumber());
        airlineField.setText(f.getAirline());
        sourceField.setText(f.getOrigin());
        destinationField.setText(f.getDestination());
        departureField.setText(formatDate(f.getDepartureTime()));
        arrivalField.setText(formatDate(f.getArrivalTime()));
        seatsField.setText(String.valueOf(f.getAvailableSeats().getOrDefault("economy", 0)));
        priceField.setText(String.valueOf(f.getPrices().getOrDefault("economy", 0.0)));
        flightNumberField.setEditable(false);
    }

    private void saveFlight(ActionEvent e) {
        try {
            String fn = flightNumberField.getText().trim();
            String al = airlineField.getText().trim();
            String o = sourceField.getText().trim();
            String d = destinationField.getText().trim();
            Date dep = parseDate(departureField.getText().trim());
            Date arr = parseDate(arrivalField.getText().trim());
            int seats = Integer.parseInt(seatsField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            Map<String, Integer> seatsMap = new HashMap<>();
            seatsMap.put("economy", seats);
            Map<String, Double> pricesMap = new HashMap<>();
            pricesMap.put("economy", price);

            Flight flight;
            String type = (String) flightTypeBox.getSelectedItem();
            if ("Domestic".equals(type)) {
                flight = new DomesticFlight(fn, al, o, d, dep, arr, seatsMap, pricesMap);
            } else {
                flight = new InternationalFlight(fn, al, o, d, dep, arr, seatsMap, pricesMap);
            }

            FlightDAO.addFlight(flight);
            saved = true;
            dispose();

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd HH:mm");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format for price or seats.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private Date parseDate(String str) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(str);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    public boolean isSaved() {
        return saved;
    }
}