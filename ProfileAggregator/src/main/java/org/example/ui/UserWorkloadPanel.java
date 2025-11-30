package org.example.ui;

import org.example.http.ApiScenarioRequest;
import org.example.http.OperationType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class UserWorkloadPanel extends JPanel {

    private final String email;
    private final String password;

    private final JPanel operationsPanel;
    private final java.util.List<OperationRowPanel> opRows = new ArrayList<>();

    public UserWorkloadPanel(String email, String password) {
        this.email = email;
        this.password = password;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // TOP: info user + bouton ajouter op
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel userLabel = new JLabel("User: " + email + "  (pwd masqué)");
        JButton addOpButton = new JButton("Ajouter une opération");

        top.add(userLabel);
        top.add(addOpButton);

        add(top, BorderLayout.NORTH);

        // CENTRE: liste des opérations
        operationsPanel = new JPanel();
        operationsPanel.setLayout(new BoxLayout(operationsPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(operationsPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("Workload de " + email));
        add(scroll, BorderLayout.CENTER);

        // Actions
        addOpButton.addActionListener(e -> addOperationRow());

        // Une première ligne par défaut
        addOperationRow();
    }

    private void addOperationRow() {
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

    public java.util.List<ApiScenarioRequest> toRequests() {
        java.util.List<ApiScenarioRequest> list = new ArrayList<>();
        for (OperationRowPanel row : opRows) {
            OperationType type = row.getOperationType();
            int count = row.getCount();
            list.add(new ApiScenarioRequest(email, password, type, count));
        }
        return list;
    }
}
