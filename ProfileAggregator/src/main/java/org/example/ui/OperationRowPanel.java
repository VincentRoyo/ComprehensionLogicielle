package org.example.ui;

import org.example.http.OperationType;

import javax.swing.*;
import java.awt.*;

public class OperationRowPanel extends JPanel {

    private final JComboBox<OperationType> operationTypeCombo;
    private final JSpinner countSpinner;
    private Runnable onRemove;

    public OperationRowPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        operationTypeCombo = new JComboBox<>(OperationType.values());
        countSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        JButton removeButton = new JButton("X");

        add(new JLabel("Op:"));
        add(operationTypeCombo);
        add(new JLabel("Count:"));
        add(countSpinner);
        add(removeButton);

        removeButton.addActionListener(e -> {
            if (onRemove != null) onRemove.run();
        });
    }

    public void setOnRemove(Runnable r) {
        this.onRemove = r;
    }

    public OperationType getOperationType() {
        return (OperationType) operationTypeCombo.getSelectedItem();
    }

    public int getCount() {
        return (Integer) countSpinner.getValue();
    }
}
