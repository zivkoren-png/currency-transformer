import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://open.er-api.com/v6/latest/USD")).build();

        try {
            // 1. קבלת הנתונים מהשרת
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            // 2. חילוץ הנתונים ל-HashMap
            int startIndex = body.indexOf("\"rates\":{") + 9;
            int endIndex = body.indexOf("}", startIndex);
            String ratesString = body.substring(startIndex, endIndex);

            HashMap<String, Double> ratesMap = new HashMap<>();
            String[] pairs = ratesString.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                String currency = keyValue[0].replace("\"", "");
                double rate = Double.parseDouble(keyValue[1]);
                ratesMap.put(currency, rate);
            }

            // 3. בניית הממשק הגרפי - עיצוב Dark Mode קלאסי
            String[] allCurrencies = ratesMap.keySet().toArray(new String[0]);
            Arrays.sort(allCurrencies);

            // פלטת צבעים שחורה-אלגנטית
            Color darkBg = new Color(30, 30, 30);        // רקע שחור/אפור כהה מאוד
            Color componentBg = new Color(50, 50, 50);   // רקע מעט בהיר יותר לתיבות הטקסט והתפריטים
            Color textColor = new Color(230, 230, 230);  // טקסט בהיר
            Color accentColor = new Color(75, 175, 80);  // ירוק עדין לתוצאה

            JFrame frame = new JFrame("ממיר מטבעות");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(4, 2, 15, 15));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            mainPanel.setBackground(darkBg);

            Font mainFont = new Font("SansSerif", Font.PLAIN, 15);
            Font boldFont = new Font("SansSerif", Font.BOLD, 15);

            JLabel amountLabel = new JLabel("סכום להמרה:");
            amountLabel.setFont(mainFont);
            amountLabel.setForeground(textColor);

            JTextField amountField = new JTextField("100");
            amountField.setFont(boldFont);
            amountField.setBackground(componentBg);
            amountField.setForeground(textColor);
            amountField.setCaretColor(textColor); // צבע הסמן המהבהב
            amountField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(80, 80, 80)),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10) // שוליים פנימיים כדי שהטקסט לא יידבק לקצה
            ));

            JLabel fromLabel = new JLabel("מטבע מקור:");
            fromLabel.setFont(mainFont);
            fromLabel.setForeground(textColor);

            JComboBox<String> fromBox = new JComboBox<>(allCurrencies);
            fromBox.setFont(mainFont);
            fromBox.setBackground(componentBg);
            fromBox.setForeground(textColor);
            fromBox.setSelectedItem("USD");

            JLabel toLabel = new JLabel("מטבע יעד:");
            toLabel.setFont(mainFont);
            toLabel.setForeground(textColor);

            JComboBox<String> toBox = new JComboBox<>(allCurrencies);
            toBox.setFont(mainFont);
            toBox.setBackground(componentBg);
            toBox.setForeground(textColor);
            toBox.setSelectedItem("ILS");

            JButton convertButton = new JButton("בצע המרה");
            convertButton.setFont(boldFont);
            convertButton.setBackground(new Color(70, 70, 70));
            convertButton.setForeground(textColor);
            convertButton.setFocusPainted(false); // מבטל את המסגרת המכוערת שנוצרת בלחיצה
            convertButton.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));

            JLabel resultLabel = new JLabel("התוצאה תופיע כאן", SwingConstants.CENTER);
            resultLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            resultLabel.setForeground(accentColor);

            // 4. פעולת הכפתור (לוגיקה מתמטית)
            convertButton.addActionListener(e -> {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String fromCurrency = (String) fromBox.getSelectedItem();
                    String toCurrency = (String) toBox.getSelectedItem();

                    double fromRate = ratesMap.get(fromCurrency);
                    double toRate = ratesMap.get(toCurrency);

                    // הנוסחה שלנו: ממירים הכל לדולר, ואז למטבע היעד
                    double finalResult = (amount / fromRate) * toRate;

                    resultLabel.setText(String.format("%.2f %s = %.2f %s", amount, fromCurrency, finalResult, toCurrency));
                    resultLabel.setForeground(accentColor);
                } catch (NumberFormatException ex) {
                    resultLabel.setText("שגיאה: הכנס מספר חוקי");
                    resultLabel.setForeground(new Color(255, 85, 85)); // אדום עדין לשגיאות
                }
            });

            // 5. חיבור כל הרכיבים לחלון
            mainPanel.add(amountLabel);
            mainPanel.add(amountField);
            mainPanel.add(fromLabel);
            mainPanel.add(fromBox);
            mainPanel.add(toLabel);
            mainPanel.add(toBox);
            mainPanel.add(convertButton);
            mainPanel.add(resultLabel);

            frame.add(mainPanel);
            frame.setVisible(true);

        } catch (Exception e) {
            System.out.println("הייתה בעיה בתקשורת או בפענוח הנתונים: " + e.getMessage());
        }
    }
}
