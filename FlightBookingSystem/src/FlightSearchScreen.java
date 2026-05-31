import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class FlightSearchScreen extends JFrame {
    private Customer customer;
    private JTextField originField;
    private JTextField destinationField;
    private JTextField dateField;
    private JTextArea resultArea;

    public FlightSearchScreen(Customer customer) {
        this.customer = customer;
        setTitle("Search Flights");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBackground(Color.WHITE);

        inputPanel.add(new JLabel("Origin:"));
        originField = new JTextField();
        inputPanel.add(originField);

        inputPanel.add(new JLabel("Destination:"));
        destinationField = new JTextField();
        inputPanel.add(destinationField);

        inputPanel.add(new JLabel("Date (yyyy-MM-dd):"));
        dateField = new JTextField();
        inputPanel.add(dateField);

        JButton searchButton = new JButton("Search");
        searchButton.setForeground(Color.BLUE);
        searchButton.addActionListener(this::handleSearch);
        inputPanel.add(new JLabel());
        inputPanel.add(searchButton);

        add(inputPanel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setForeground(Color.BLUE);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
    }

    private void handleSearch(ActionEvent e) {
        String origin = originField.getText().trim();
        String destination = destinationField.getText().trim();
        String dateStr = dateField.getText().trim();

        if (origin.isEmpty() || destination.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            List<Flight> flights = FlightDAO.getAllFlights();
            List<Flight> results = customer.searchFlights(flights, origin, destination, date);

            resultArea.setText("");
            if (results.isEmpty()) {
                resultArea.setText("No flights found.");
            } else {
                for (Flight f : results) {
                    resultArea.append(f.toString() + "\n\n");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd");
        }
    }
}


