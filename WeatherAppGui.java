import org.json.simple.JSONObject;
import java.util.Map;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.imageio.ImageIO;
import java.time.LocalDateTime;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.List; // Import List from java.util package
import java.util.Map; // Import Map from java.util package
import org.json.simple.parser.JSONParser; // Import JSONParser from json-simple library


public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;
    private JTextField searchTextField;
    private String temperatureUnit = "Celsius";
    private JLabel weatherConditionImage;
    private ArrayList<String> searchHistory;
    // Declare latitude and longitude variables
    private double latitude;
    private double longitude;

    public WeatherAppGui() {
        // setup our gui and add a title
        super("Weather App");
        // Initialize search history
        searchHistory = new ArrayList<>();

        setAppColorScheme();
        // configure gui to end the program's process once it has been closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set the size of our gui (in pixels)
        setSize(450, 650);

        // load our gui at the center of the screen
        setLocationRelativeTo(null);

        // make our layout manager null to manually position our components within the gui
        setLayout(null);

        addTemperatureUnitDropdown(); // Add temperature unit dropdown menu

        // prevent any resize of our gui
        setResizable(false);

        addGuiComponents();
    }

    private void updateUIComponents() {
        // Update current weather components
        updateWeatherImage();
    }

    private String fetchDataFromApi(String apiUrl) {
        StringBuilder responseData = new StringBuilder();
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    responseData.append(inputLine);
                }
                in.close();
            } else {
                System.out.println("Failed to fetch data from API. Response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return responseData.toString();
    }

    private void addTemperatureUnitDropdown() {
        String[] temperatureUnits = {"Celsius", "Fahrenheit"};
        JComboBox<String> temperatureUnitDropdown = new JComboBox<>(temperatureUnits);
        temperatureUnitDropdown.setBounds(375, 65, 80, 30);
        temperatureUnitDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedUnit = (String) temperatureUnitDropdown.getSelectedItem();
                if (selectedUnit != null) {
                    temperatureUnit = selectedUnit.equals("Celsius") ? "C" : "F";
                }
            }
        });
        add(temperatureUnitDropdown);
    }

    private void setAppColorScheme() {
        LocalDateTime currentTime = LocalDateTime.now();
        int hour = currentTime.getHour();
        // Check if it's day or night based on the hour
        if (hour >= 6 && hour < 18) { // Daytime (6 AM to 6 PM)
            setAppColor(Color.WHITE, Color.BLACK); // Set light color scheme
        } else { // Nighttime
            setAppColor(Color.LIGHT_GRAY, Color.WHITE); // Set dark color scheme
        }
    }

    private void setAppColor(Color backgroundColor, Color foregroundColor) {
        getContentPane().setBackground(backgroundColor); // Set background color
        // Set foreground (text) color for all components
        for (Component component : getComponents()) {
            component.setForeground(foregroundColor);
        }
    }

    private void addGuiComponents() {
        // search field
        searchTextField = new JTextField();

        // set the location and size of our component
        searchTextField.setBounds(15, 15, 351, 45);

        // change the font style and size
        searchTextField.setFont(new Font("Dialog", Font.PLAIN, 24));

        add(searchTextField);

        // weather image
        weatherConditionImage = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // temperature text
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));

        // center the text
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // weather condition description
        JLabel weatherConditionDesc = new JLabel("Cloudy");
        weatherConditionDesc.setBounds(0, 405, 450, 36);
        weatherConditionDesc.setFont(new Font("Dialog", Font.PLAIN, 32));
        weatherConditionDesc.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherConditionDesc);

        // humidity image
        JLabel humidityImage = new JLabel(loadImage("src/assets/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // humidity text
        JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        // windspeed image
        JLabel windspeedImage = new JLabel(loadImage("src/assets/windspeed.png"));
        windspeedImage.setBounds(220, 500, 74, 66);
        add(windspeedImage);

        // windspeed text
        JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15km/h</html>");
        windspeedText.setBounds(310, 500, 85, 55);
        windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windspeedText);

        // search button
        JButton searchButton = new JButton(loadImage("src/assets/search.png"));

        // change the cursor to a hand cursor when hovering over this button
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 47, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get location from user
                String userInput = searchTextField.getText();

                // validate input - remove whitespace to ensure non-empty text
                if (userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }

                // retrieve weather data
                // retrieve weather data with selected temperature unit
                weatherData = WeatherApp.getWeatherData(userInput, temperatureUnit);

                if (weatherData != null) {
                    // Update weather image
                    updateWeatherImage();

                    // update temperature text
                    updateTemperatureText(temperatureText);

                    // update weather condition text
                    updateWeatherConditionText(weatherConditionDesc);

                    // update humidity text
                    updateHumidityText(humidityText);

                    updateWindspeedText(windspeedText);

                    // Add to search history
                    addToSearchHistory(userInput);
                } else {
                    JOptionPane.showMessageDialog(null, "Error: Couldn't retrieve weather data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(searchButton);

        // History button
        JButton historyButton = new JButton("History");
        historyButton.setBounds(15, 430, 100, 30);
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSearchHistory();
            }
        });
        add(historyButton);

        // Forecast button
        JButton forecastButton = new JButton("Forecast");
        forecastButton.setBounds(330, 430, 100, 30);
        forecastButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the method with location and API key parameters
                showForecastWindow("United Kingdom", "6VKMTN7Z2F3Q6GUR6Z9VAUGQA");
            }
        });
        add(forecastButton);
    }

    // Method to show the forecast window
    private void showForecastWindow(String location, String apiKey) {
        try {
            String encodedLocation = URLEncoder.encode(location, "UTF-8");
            String apiUrl = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" + encodedLocation + "?unitGroup=metric&key=" + apiKey;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            // Parse JSON response
            JSONParser parser = new JSONParser();
            Map<String, Object> jsonResponse = (Map<String, Object>) parser.parse(response.toString());


            List<Map<String, Object>> days = (List<Map<String, Object>>) jsonResponse.get("days");


            // Process data for the current day (index 0)
            Map<String, Object> currentDay = days.get(0);
            String date = (String) currentDay.get("datetime");
            double maxTemp = (double) currentDay.get("tempmax");
            double minTemp = (double) currentDay.get("tempmin");
            String conditions = (String) currentDay.get("conditions");

            // Construct weather info string for current day
            StringBuilder currentWeatherInfo = new StringBuilder();
            currentWeatherInfo.append("Date: ").append(date).append("\n");
            currentWeatherInfo.append("Conditions: ").append(conditions).append("\n");
            currentWeatherInfo.append("Max Temp: ").append(maxTemp).append("°C\n");
            currentWeatherInfo.append("Min Temp: ").append(minTemp).append("°C\n\n");

            // Construct forecast info string for the next few days
            StringBuilder forecastInfo = new StringBuilder("Forecast:\n");
            for (int i = 1; i < days.size() && i <= 7; i++) {
                Map<String, Object> day = days.get(i);
                String forecastDate = (String) day.get("datetime");
                double forecastMaxTemp = (double) day.get("tempmax");
                double forecastMinTemp = (double) day.get("tempmin");
                String forecastConditions = (String) day.get("conditions");
                forecastInfo.append("Date: ").append(forecastDate).append("\n");
                forecastInfo.append("Conditions: ").append(forecastConditions).append("\n");
                forecastInfo.append("Max Temp: ").append(forecastMaxTemp).append("°C\n");
                forecastInfo.append("Min Temp: ").append(forecastMinTemp).append("°C\n\n");
            }

            // Display the forecast information in a separate window
            JTextArea forecastTextArea = new JTextArea(currentWeatherInfo.toString() + forecastInfo.toString());
            forecastTextArea.setEditable(false);
            forecastTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adding padding
            JScrollPane scrollPane = new JScrollPane(forecastTextArea);

            JFrame forecastFrame = new JFrame("Weather Forecast");
            forecastFrame.getContentPane().add(scrollPane);
            forecastFrame.pack();
            forecastFrame.setLocationRelativeTo(null);
            forecastFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching forecast data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Update temperature text
    private void updateTemperatureText(JLabel temperatureText) {
        double temperature = (double) weatherData.get("temperature");
        temperatureText.setText(temperature + " " + temperatureUnit);
    }

    // Update weather condition text
    private void updateWeatherConditionText(JLabel weatherConditionDesc) {
        String weatherCondition = (String) weatherData.get("weather_condition");
        weatherConditionDesc.setText(weatherCondition);
    }

    // Update humidity text
    private void updateHumidityText(JLabel humidityText) {
        long humidity = (long) weatherData.get("humidity");
        humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");
    }

    // Update windspeed text
    private void updateWindspeedText(JLabel windspeedText) {
        double windspeed = (double) weatherData.get("windspeed");
        windspeedText.setText("<html><b>Windspeed</b> " + windspeed + " km/h</html>");
    }

    // Add to search history
    private void addToSearchHistory(String userInput) {
// Add current timestamp along with the search query to the search history list
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = formatter.format(date);
        searchHistory.add("[" + timestamp + "] " + userInput);
    }

   // Show search history
    private void showSearchHistory() {
// Create a new frame for displaying search history
        JFrame historyFrame = new JFrame("Search History");
        historyFrame.setSize(400, 400);
        historyFrame.setLocationRelativeTo(null);
        historyFrame.setLayout(new BorderLayout());
// Create a text area to display search history
        JTextArea historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);

        // Append search history to the text area
        for (String entry : searchHistory) {
            historyTextArea.append(entry + "\n");
        }

        // Add the text area to the frame
        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        historyFrame.add(scrollPane, BorderLayout.CENTER);

        // Make the frame visible
        historyFrame.setVisible(true);

    }

    // Update weather image
    private void updateWeatherImage() {
        String weatherCondition = (String) weatherData.get("weather_condition");
        ImageIcon newIcon = null;
        switch (weatherCondition) {
            case "Clear":
                newIcon = loadImage("src/assets/clear.png");
                break;
            case "Cloudy":
                newIcon = loadImage("src/assets/cloudy.png");
                break;
            case "Rain":
                newIcon = loadImage("src/assets/rain.png");
                break;
            case "Snow":
                newIcon = loadImage("src/assets/snow.png");
                break;
        }

        // Set the new icon
        if (newIcon != null) {
            weatherConditionImage.setIcon(newIcon);
        } else {
            System.out.println("Failed to load image for weather condition: " + weatherCondition);
        }

    }

    // Used to create images in our gui components
    private ImageIcon loadImage(String resourcePath) {
        try {
            // read the image file from the path given
            BufferedImage image = ImageIO.read(new File(resourcePath));

            // returns an image icon so that our component can render it
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
}