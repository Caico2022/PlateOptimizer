package org.example;

import javax.swing.*;
import java.awt.*;

public class PlateVisualizer extends JPanel {
    Main.Plate plate;
    private final String mode;

    public PlateVisualizer(Main.Plate plate, String mode) {
        this.plate = plate;
        this.mode = mode;
        setPreferredSize(new Dimension(plate.width + 50, plate.height + 50));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Hintergrund
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, plate.width, plate.height);

        // === Jobs ===
        for (Main.Job job : plate.jobs) {
            // Füllung
            g2d.setColor(new Color(0, 0, 255, 120)); // halbtransparent blau
            g2d.fillRect(job.x, job.y, job.width, job.height);
            // Umrandung
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3f)); // dicke schwarze Umrandung
            g2d.drawRect(job.x, job.y, job.width, job.height);
            // Beschriftung
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Job " + job.id, job.x + 5, job.y + 15);
            g2d.drawString(job.width + "x" + job.height, job.x + 5, job.y + 30);
        }

        // === Freie Rechtecke ===
        if ("2".equals(mode)) {
            g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0));
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            for (int i = 0; i < plate.freeRects.size(); i++) {
                Main.FreeRectangle rect = plate.freeRects.get(i);
                // Füllung
                g2d.setColor(new Color(255, 0, 0, 50)); // hellrot
                g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
                // Umrandung
                g2d.setColor(Color.RED);
                g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
                // Beschriftung
                g2d.setColor(Color.RED.darker());
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.drawString("F" + i, rect.x + 5, rect.y + 15);
                g2d.drawString(rect.width + "x" + rect.height, rect.x + 5, rect.y + 30);
            }
        }

        // Reset Stroke
        g2d.setStroke(new BasicStroke(1f));
    }




    public static void showPlate(Main.Plate plate, String mode) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Plate Visualizer - " + plate.name);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new PlateVisualizer(plate, mode));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
