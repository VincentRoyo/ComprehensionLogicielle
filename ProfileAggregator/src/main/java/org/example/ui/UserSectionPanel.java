package org.example.ui;

import org.example.http.ApiScenarioRequest;
import org.example.http.OperationType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserSectionPanel extends JPanel {

    private final JComboBox<String> emailCombo;
    private final JPasswordField passwordField;
    private final JPanel operationsPanel;
    private final List<OperationRowPanel> opRows = new ArrayList<>();
    private final Map<String, String> users;

    private final JButton removeUserButton;
    private final JButton addOperationButton;

    private Runnable onRemoveSection;

    public UserSectionPanel(Map<String, String> users) {
        this.users = users;

        setBorder(BorderFactory.createTitledBorder("User workload"));
        setLayout(new BorderLayout());

        // --- Top : user + password (masqué) + boutons ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Emails triés numériquement (user1, user2, ..., user10)
        String[] emails = users.keySet().stream()
                .sorted((e1, e2) -> {
                    int n1 = Integer.parseInt(e1.replaceAll("\\D+", ""));
                    int n2 = Integer.parseInt(e2.replaceAll("\\D+", ""));
                    return Integer.compare(n1, n2);
                })
                .toArray(String[]::new);

        emailCombo = new JComboBox<>(emails);
        passwordField = new JPasswordField(12);
        passwordField.setEditable(false);

        addOperationButton = new JButton("Ajouter opération");
        removeUserButton = new JButton("Supprimer user");

        top.add(new JLabel("User:"));
        top.add(emailCombo);
        top.add(new JLabel("Pass:"));
        top.add(passwordField);
        top.add(addOperationButton);
        top.add(removeUserButton);

        add(top, BorderLayout.NORTH);

        // --- Centre : liste d'opérations ---
        operationsPanel = new JPanel();
        operationsPanel.setLayout(new BoxLayout(operationsPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(operationsPanel);
        add(scroll, BorderLayout.CENTER);

        // Actions
        emailCombo.addActionListener(e -> updatePassword());
        removeUserButton.addActionListener(e -> {
            if (onRemoveSection != null) onRemoveSection.run();
        });
        addOperationButton.addActionListener(e -> addOperationRow());

        // init
        updatePassword();
        addOperationRow(); // une première ligne par défaut
    }

    private void updatePassword() {
        String email = (String) emailCombo.getSelectedItem();
        String pwd = users.getOrDefault(email, "");
        passwordField.setText(pwd);
    }

    private void addOperationRow() {
        // 2 étapes pour éviter la capture d'une variable non initialisée
        OperationRowPanel row = new OperationRowPanel();
        row.setOnRemove(() -> removeOperationRow(row));

        opRows.add(row);
        operationsPanel.add(row);
        operationsPanel.revalidate();
        operationsPanel.repaint();
    }

    private void removeOperationRow(OperationRowPanel row) {
        opRows.remove(row);
        operationsPanel.remove(row);
        operationsPanel.revalidate();
        operationsPanel.repaint();
    }

    public void setOnRemoveSection(Runnable r) {
        this.onRemoveSection = r;
    }

    public List<ApiScenarioRequest> toRequests() {
        String email = (String) emailCombo.getSelectedItem();
        String password = new String(passwordField.getPassword());

        List<ApiScenarioRequest> list = new ArrayList<>();
        for (OperationRowPanel row : opRows) {
            OperationType type = row.getOperationType();
            int count = row.getCount();
            list.add(new ApiScenarioRequest(email, password, type, count));
        }
        return list;
    }
}
