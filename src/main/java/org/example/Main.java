package org.example;

import java.util.*;

public class Main {

    public static final boolean DEBUG = true;  // Zwischenschritte auf GUI aktivieren/deaktivieren.
    public static final boolean allAlgorithms = true;  // Userabfrage nach Wunschalgorithmus aktivieren/deaktivieren.

    public static final boolean rotateJobs = true;  // Bislang nur für MaxRectBestFit-Algorithmus.
    public static final boolean sortJobs = true;  // Bislang nur für MaxRectBestFit-Algorithmus.

    public static void sortJobsBySizeDescending(List<Job> jobs) {
        jobs.sort((j1, j2) -> {
            int area1 = j1.width * j1.height;
            int area2 = j2.width * j2.height;
            return Integer.compare(area2, area1);
        });
        System.out.println("Jobs sortiert nach Fläche absteigend:");
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            int area = job.width * job.height;
            System.out.printf("%d: Job ID=%d, Größe=%dx%d, Fläche=%d\n", i + 1, job.id, job.width, job.height, area);
        }
    }

    public static void main(String[] args) {
        List<Job> jobs = Arrays.asList(
                new Job(0, 402, 480),
                new Job(1, 305, 222),
                new Job(2, 220, 561),
                new Job(3, 205, 153),
                new Job(4, 243, 188),
                new Job(5, 243,188),
                new Job(6,205,153)
        );
        if (sortJobs) sortJobsBySizeDescending(jobs);

        Plate plateA = new Plate("Plate A", 963, 650);
        MaxRectBF maxRectBF = new MaxRectBF(plateA);

        String mode;
        Scanner scanner = new Scanner(System.in);
        if (allAlgorithms) {
            System.out.print("Algorithmus wählen (1 = First Fit, 2 = MaxRects): ");
            mode = scanner.nextLine().trim();
        } else mode = "2";

        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            boolean placed;
            if (mode.equals("1")) {
                placed = plateA.placeJobFFShelf(job);
            } else {
                placed = maxRectBF.placeJobMaxRectsBestFit(job);
            }
            if (!placed) {
                System.out.println("Job " + job.id + " konnte nicht platziert werden.");
            }
        }

        System.out.println("\n=== Job Placement ===");
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            System.out.println(job);
        }
        System.out.println("\n=== Used Plate ===");
        System.out.println(plateA.name + " hat " + plateA.jobs.size() + " Jobs.");

        // Visualisieren
        PlateVisualizer.showPlate(plateA, mode, maxRectBF);
    }

}