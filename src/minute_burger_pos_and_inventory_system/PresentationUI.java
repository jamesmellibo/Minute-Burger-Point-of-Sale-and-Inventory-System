package minute_burger_pos_and_inventory_system;

import java.awt.*;
import javax.swing.*;

/** Reusable dialogs and kiosk sizing; no NetBeans generated GUI code is modified. */
public final class PresentationUI {
    private PresentationUI() {}
    public static void fullscreen(JFrame frame) {
        if (frame.isDisplayable()) return;
        frame.setUndecorated(true);
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        frame.setBounds(bounds);
    }
    public static void showSuccess(Component parent, String heading, String person, String role, String description) {
        JPanel panel = new JPanel(new BorderLayout(0, 17));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
        JLabel title = new JLabel("✓  " + heading);
        title.setFont(new Font("Segoe UI", Font.BOLD, 29));
        title.setForeground(new Color(23, 129, 70));
        panel.add(title, BorderLayout.NORTH);
        JPanel detail = new JPanel(new GridLayout(3, 1, 0, 12));
        detail.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("Welcome, " + person + "!");
        JLabel roleLabel = new JLabel("Account role: " + role);
        JLabel descLabel = new JLabel(description);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        descLabel.setForeground(new Color(88, 94, 102));
        detail.add(nameLabel); detail.add(roleLabel); detail.add(descLabel);
        panel.add(detail, BorderLayout.CENTER);
        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, new Object[]{"CONTINUE TO DASHBOARD"}, "CONTINUE TO DASHBOARD");
        JDialog dialog = pane.createDialog(parent, "Minute Burger | " + heading);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        dialog.dispose();
    }
}
