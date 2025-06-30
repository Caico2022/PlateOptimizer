package org.example;

import java.util.*;

public class Main {

    public static final boolean DEBUG = true;
    public static final boolean RotateJobs = true;
    public static final boolean AllAlgorithms = true;

    static class FreeRectangle {
        int x, y, width, height;

        public FreeRectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }


    static class Plate {
        String name;
        int width, height;
        int currentX = 0, currentY = 0;
        int shelfHeight = 0;
        List<Job> jobs = new ArrayList<>();
        List<FreeRectangle> freeRects = new ArrayList<>();

        public Plate(String name, int width, int height) {
            this.name = name;
            this.width = width;
            this.height = height;
            freeRects.add(new FreeRectangle(0, 0, width, height));
        }

        // =============== First-Fit Shelf-Packing =============== //
        public boolean placeJobFFShelf(Job job) {
            // First-Fit Algorithmus, aber jede Zeile wird als ein "Regal mit gleichmäßiger Höhe" betrachtet.
            // Ein neuer Job, der zwar in Regal 2 unter einem bereits platzierten Job von Regal 1 passen würde, könnte möglicherweise nicht platziert werden,
            // weil der höchste Job von Regal 1 die Höhe des Regals bestimmt.
            //
            // passt Job in aktuelle Zeile?
            if (currentX + job.width <= width) {
                // Speichern der Koordinaten auf der Platte (Abstand vom linken Rand und oberen Rand) der linken, oberen Ecke des Jobs.
                job.x = currentX;
                job.y = currentY;
                currentX += job.width;
                shelfHeight = Math.max(shelfHeight, job.height);  // aktualisiere die Höhe des "Regals"
            } else {
                // Die neue Zeile wird dort angefangen, wo der höchste, bereits platzierte Job, endet.
                currentY += shelfHeight;
                // passt der Job von der Höhe her überhaupt in das neue "Regal"?
                if (currentY + job.height > height) {
                    return false;
                }
                currentX = 0;
                shelfHeight = job.height;
                job.x = currentX;
                job.y = currentY;
                currentX += job.width;
            }

            job.placedOn = this;
            jobs.add(job);
            return true;
        }


        static class BestFitResult {
            public FreeRectangle bestRect;
            int bestScore = Integer.MAX_VALUE;
            boolean useRotated = false;
            int bestWidth = -1;
            int bestHeight = -1;
        }

        public boolean placeJobMaxRectsBestFit(Job job) {
            BestFitResult result = new BestFitResult();

            System.out.println("\n\n============== Job " + job.id + " (" + job.width + "x" + job.height + ") ==============");

            // Durchsucht alle verfügbaren freien Rechtecke
            for (int i = 0; i < freeRects.size(); i++) {
                FreeRectangle rect = freeRects.get(i);
                System.out.println("  Prüfe FreeRect " + i + ": Startkoordinaten (x=" + rect.x + ", y=" + rect.y + "), Breite=" + rect.width + "mm, Höhe=" + rect.height + "mm");

                // Originalposition testen
                testAndUpdateBestFit(job.width, job.height, rect, false, result);
                // Gedrehte Position testen
                if (RotateJobs) testAndUpdateBestFit(job.height, job.width, rect, true, result);

            }
            // Wenn kein passender freier Bereich gefunden wurde: Job passt nicht
            if (result.bestRect == null) {
                System.out.println("-> Kein passendes Rechteck gefunden für Job " + job.id);
                return false;
            }

            // Aktuellen Job speichern
            if (result.useRotated) {
                System.out.println("-> Job wird GEDREHT platziert! (" + job.width + "x" + job.height + " → " + result.bestWidth + "x" + result.bestHeight + ")");
                job.rotated = true;
            } else {
                System.out.println("-> Job wird in Originalausrichtung platziert.");
            }
            job.width = result.bestWidth;
            job.height = result.bestHeight;
            job.x = result.bestRect.x;
            job.y = result.bestRect.y;
            job.placedOn = this;
            jobs.add(job);

            System.out.println("-> Platziert in (" + job.x + ", " + job.y + ") auf " + name);

            splitFreeRect(result.bestRect, job);

            // ===== Zwischenschritte visualisieren ===== //
            if (DEBUG) {
                PlateVisualizer.showPlate(this, "2");
                // Wichtig: Mit Pause, sonst läuft das Programm zu schnell durch und es wird in jedem Fenster nur das Endergebnis angezeigt.
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            return true;
        }

        private void testAndUpdateBestFit(int testWidth, int testHeight, FreeRectangle rect, boolean rotated, BestFitResult result) {
            if (testWidth <= rect.width && testHeight <= rect.height) {
                String ausrichtung = rotated ? "GEDREHTE Ausrichtung" : "Originalausrichtung";
                int leftoverHoriz = rect.width - testWidth;
                int leftoverVert = rect.height - testHeight;
                int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                System.out.println("    -> Passt in " + ausrichtung + "!");
                System.out.println("       Berechnung leftoverHoriz: " + rect.width + " - " + testWidth + " = " + leftoverHoriz);
                System.out.println("       Berechnung leftoverVert: " + rect.height + " - " + testHeight + " = " + leftoverVert);
                System.out.println("       shortSideFit = " + shortSideFit + ", aktueller bestScore = " + result.bestScore);
                // Kriterium für "Best Fit": Das Rechteck, worin der Job den kleinsten Abstand entweder vertikal ODER horizontal zum nächsten freien Rechteck oder zum Rand hat.
                // Weitere Möglichkeit für "Best Fit": durchschnittlicher Abstand vertikal UND horizontal zum jeweiligen nächsten freien Rechteck oder zum Rand.
                if (shortSideFit < result.bestScore) {
                    result.bestScore = shortSideFit;
                    result.bestRect = rect;
                    result.useRotated = rotated;
                    result.bestWidth = testWidth;
                    result.bestHeight = testHeight;
                    System.out.println("       -> Neuer Best-Fit (" + ausrichtung + ")!");
                }
            } else {
                System.out.println("    -> Passt NICHT in " + (rotated ? "GEDREHTER" : "Original") + " Ausrichtung.");
            }
        }


        private void splitFreeRect(FreeRectangle rect, Job job) {
            System.out.println("\n--- splitFreeRect aufgerufen ---");
            System.out.println("Belegtes Rechteck: Start(" + rect.x + ", " + rect.y + "), Breite=" + rect.width + "mm, Höhe=" + rect.height + "mm");
            System.out.println("Jobgröße: Breite=" + job.width + "mm, Höhe=" + job.height + "mm");
            freeRects.remove(rect);
            System.out.println("Entferne belegtes Rechteck aus freien Bereichen.");
            // Neuer freier Bereich rechts neben dem Job
            if (job.width < rect.width) {
                // Leere Rechtecke werden sich nicht überschneiden, weil die ersten zwei gesplitteten Rechtecke (vom ersten Job) sich natürlicherweise nicht überschneiden.
                // → Neue leere Rechtecke beziehen sich nicht auf den Rand der Platte, sondern auf die Größe des Rechtecks, in dem der Job platziert wurde (rect.width - job.width)
                FreeRectangle newRectRight = new FreeRectangle(rect.x + job.width, rect.y,rect.width - job.width, job.height);
                freeRects.add(newRectRight);
                System.out.println("Füge freien Bereich rechts hinzu: Start(" + newRectRight.x + ", " + newRectRight.y + "), Breite=" + newRectRight.width + "mm, Höhe=" + newRectRight.height + "mm");
            }
            // Neuer freier Bereich unterhalb des Jobs
            if (job.height < rect.height) {
                FreeRectangle newRectBelow = new FreeRectangle(rect.x, rect.y + job.height, rect.width, rect.height - job.height);
                freeRects.add(newRectBelow);
                System.out.println("Füge freien Bereich unten hinzu: Start(" + newRectBelow.x + ", " + newRectBelow.y + "), Breite=" + newRectBelow.width + "mm, Höhe=" + newRectBelow.height + "mm");
            }
            System.out.println("\nAktuelle freie Rechtecke:");
            for (int i = 0; i < freeRects.size(); i++) {
                FreeRectangle r = freeRects.get(i);
                System.out.println("  FreeRect " + i + ": Start(" + r.x + ", " + r.y + "), Breite=" + r.width + "mm, Höhe=" + r.height + "mm");
            }
        }
    }


    public static void main(String[] args) {
        List<Job> jobs = Arrays.asList(
                new Job(1, 402, 480),
                new Job(2, 305, 222),
                new Job(3, 220, 573),
                new Job(4, 205, 153),
                new Job(5, 243, 188),
                new Job(6, 243,188),
                new Job(7,205,153)
        );
        Plate plateA = new Plate("Plate A", 963, 650);

        String mode;
        Scanner scanner = new Scanner(System.in);
        if (AllAlgorithms) {
            System.out.print("Algorithmus wählen (1 = First Fit, 2 = MaxRects): ");
            mode = scanner.nextLine().trim();
        } else mode = "2";

        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            boolean placed;
            if (mode.equals("1")) {
                placed = plateA.placeJobFFShelf(job);
            } else {
                placed = plateA.placeJobMaxRectsBestFit(job);
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
        PlateVisualizer.showPlate(plateA, mode);
    }

}