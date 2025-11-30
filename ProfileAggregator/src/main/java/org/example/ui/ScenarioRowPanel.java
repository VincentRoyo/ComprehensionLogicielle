package org.example.ui;

import org.example.http.OperationType;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.Map;

public class ScenarioRowPanel extends JPanel {

    private final JComboBox<String> userEmailCombo;
    private final JPasswordField passwordField;
    private final JComboBox<OperationType> operationTypeCombo;
    private final JSpinner countSpinner;
    private final JButton removeButton;

    private final Map<String, String> users;
    private Runnable onRemove;

    public ScenarioRowPanel(Map<String, String> users, Runnable onRemove) {
        this.users = users;
        this.onRemove = onRemove;

        setLayout(new FlowLayout(FlowLayout.LEFT));

        // La liste des emails = les clés de la map
        String[] emails = users.keySet().stream()
                .sorted(Comparator.comparingInt(e ->
                        Integer.parseInt(
                                e.replace("user", "").replace("@example.com", "")
                        )
                ))
                .toArray(String[]::new);

        this.userEmailCombo = new JComboBox<>(emails);


        this.passwordField = new JPasswordField("", 12);
        this.operationTypeCombo = new JComboBox<>(OperationType.values());
        this.countSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        this.removeButton = new JButton("X");

        add(new JLabel("User:"));
        add(userEmailCombo);

        add(new JLabel("Op:"));
        add(operationTypeCombo);

        add(new JLabel("Count:"));
        add(countSpinner);

        add(removeButton);

        removeButton.addActionListener(e -> {
            if (onRemove != null) onRemove.run();
        });

        userEmailCombo.addActionListener(e -> updatePassword());
        updatePassword(); // init
    }

    private void updatePassword() {
        String email = (String) userEmailCombo.getSelectedItem();
        passwordField.setText(users.getOrDefault(email, ""));
    }

    public void setOnRemove(Runnable onRemove) {
        this.onRemove = onRemove;
    }

    public String getEmail() {
        return (String) userEmailCombo.getSelectedItem();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public OperationType getOperationType() {
        return (OperationType) operationTypeCombo.getSelectedItem();
    }

    public int getCount() {
        return (Integer) countSpinner.getValue();
    }
}

