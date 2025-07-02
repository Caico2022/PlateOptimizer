package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlateVisualizer extends JPanel {
    Plate plate;
    MaxRectBF maxRectBF;
    private final String mode;

    public PlateVisualizer(Plate plate, String mode, MaxRectBF maxRectBF) {
        this.plate = plate;
        this.mode = mode;
        this.maxRectBF = maxRectBF;
        // Panelgröße (Panel innerhalb des J-Frame-Fensters) initialisieren
        setPreferredSize(new Dimension(plate.width + 50, plate.height + 50));
    }

    // Bemalt das Panel
    // wird automatisch vom PlateVisualizer aufgerufen
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;  // Bessere Grafikfunktionen

        // Hintergrund
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, plate.width, plate.height);

        // === Jobs ===
        List<Job> jobs = plate.jobs;
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            if (job.rotated) {
                g2d.setColor(new Color(0, 180, 0, 120));
            } else {
                g2d.setColor(new Color(0, 0, 255, 120));
            }
            // Füllung
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
            if (job.rotated) g2d.drawString("(gedreht)", job.x + 5, job.y + 45);
            g2d.drawString("Order: " + job.placementOrder, job.x + 5, job.y + 60);
        }

        // === Freie Rechtecke ===
        if ("2".equals(mode)) {
            // Strichlinie für die Umrandung definieren
            g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6}, 0));
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            for (int i = 0; i < maxRectBF.freeRects.size(); i++) {
                MaxRectBF.FreeRectangle rect = maxRectBF.freeRects.get(i);
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
        // Reset Stroke (für eventuelle spätere Zeichnungen)
        g2d.setStroke(new BasicStroke(1f));
    }


    // Öffnet ein Swing-Fenster (Framework javax.swing), dass die aktuell definierten Zeichnungen visualisiert.
    // J-Frame ist eine Klasse innerhalb des Frameworks, die das Fenster erzeugt.
    public static void showPlate(Plate plate, String mode, MaxRectBF maxRectBF) {
        // GUI-Thread starten
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Plate Visualizer - " + plate.name);  // Initialisiert das Fenster
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Beendet das Programm (auch die main), wenn das Fenster geschlossen wird
            frame.getContentPane().add(new PlateVisualizer(plate, mode, maxRectBF));  // Füge das Panel in das J-Frame-Fenster ein
            frame.pack();  // Fenster auf optimale Größe bringen. Entspricht setPreferredSize(new Dimension(plate.width + 50, plate.height + 50));
            frame.setLocationRelativeTo(null);  // Fenster zentrieren
            frame.setVisible(true);  // Hier ruft Swing automatisch die Methode paintComponent(Graphics g) auf
        });
    }
}
