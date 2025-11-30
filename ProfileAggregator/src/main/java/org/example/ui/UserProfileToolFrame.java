package org.example.ui;

import org.example.UserProfileAggregatorService;
import org.example.http.ApiScenarioRequest;
import org.example.http.ApiWorkloadRunner;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserProfileToolFrame extends JFrame {

    private final JTextField baseUrlField;
    private final JTabbedPane tabbedPane;
    private final JTextArea outputArea;

    private final java.util.List<UserWorkloadPanel> userPanels = new ArrayList<>();

    private static final Map<String, String> SEEDED_USERS = Map.of(
            "user1@example.com", "password1",
            "user2@example.com", "password2",
            "user3@example.com", "password3",
            "user4@example.com", "password4",
            "user5@example.com", "password5",
            "user6@example.com", "password6",
            "user7@example.com", "password7",
            "user8@example.com", "password8",
            "user9@example.com", "password9",
            "user10@example.com", "password10"
    );

    public UserProfileToolFrame() {
        super("API Workload & User Profiles");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // TOP: base URL + boutons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        baseUrlField = new JTextField("http://localhost:8080/api", 30);
        JButton runScenarioButton = new JButton("Exécuter le scénario");
        JButton aggregateButton = new JButton("Agréger les profils");

        topPanel.add(new JLabel("Base URL:"));
        topPanel.add(baseUrlField);
        topPanel.add(runScenarioButton);
        topPanel.add(aggregateButton);

        // CENTRE: onglets par user
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        SEEDED_USERS.keySet().stream()
                .sorted((e1, e2) -> {
                    int n1 = Integer.parseInt(e1.replaceAll("\\D+", ""));
                    int n2 = Integer.parseInt(e2.replaceAll("\\D+", ""));
                    return Integer.compare(n1, n2);
                })
                .forEach(email -> {
                    String pwd = SEEDED_USERS.get(email);
                    UserWorkloadPanel panel = new UserWorkloadPanel(email, pwd);
                    userPanels.add(panel);
                    tabbedPane.addTab(email, panel);
                });

        // BAS: output JSON
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Profils agrégés (JSON)"));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(outputScroll, BorderLayout.SOUTH);

        // Actions
        runScenarioButton.addActionListener(e -> runScenario());
        aggregateButton.addActionListener(e -> aggregateProfiles());
    }

    private void runScenario() {
        new Thread(() -> {
            try {
                String baseUrl = baseUrlField.getText().trim();
                ApiWorkloadRunner runner = new ApiWorkloadRunner(baseUrl);

                List<ApiScenarioRequest> allRequests = new ArrayList<>();
                for (UserWorkloadPanel panel : userPanels) {
                    allRequests.addAll(panel.toRequests());
                }

                appendOutput("Exécution du scénario...\n");
                runner.runScenario(allRequests);
                appendOutput("Scénario terminé.\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                appendOutput("Erreur lors de l'exécution du scénario: " + ex.getMessage() + "\n");
            }
        }).start();
    }

    private void aggregateProfiles() {
        new Thread(() -> {
            try {
                appendOutput("Agrégation des profils à partir des logs...\n");
                UserProfileAggregatorService service = new UserProfileAggregatorService();
                String json = service.aggregateAndWriteProfiles();
                appendOutput("Agrégation terminée. Résultat écrit dans profiles.json\n");
                outputArea.setText(json);
            } catch (Exception ex) {
                ex.printStackTrace();
                appendOutput("Erreur lors de l'agrégation: " + ex.getMessage() + "\n");
            }
        }).start();
    }

    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> outputArea.append(text));
    }
}
