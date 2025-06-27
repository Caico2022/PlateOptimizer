package org.example;

import java.util.*;

public class Main {

    static class Job {
        int id;
        int width, height;
        int x = -1, y = -1;  // Hier wird gespeichert, auf welcher Position dieser Job auf einer Platte platziert wurde (zur Kontrolle)
        Plate placedOn = null;


        public Job(int id, int width, int height) {
            this.id = id;
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            if (placedOn != null) {
                return "Job " + id + ": " + width + "mm x " + height + "mm -> " +
                        "Platte: " + placedOn.name +
                        ", Position: (" + x + ", " + y + ")";
            } else {
                return "Job " + id + ": " + width + "mm x " + height + "mm -> nicht platziert";
            }
        }
    }

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

        public boolean placeJobFFShelf(Job job) {
            // =============== First-Fit Shelf-Packing =============== //
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


        public boolean placeJobMaxRectsBestFit(Job job) {
            FreeRectangle bestRect = null;
            int bestScore = Integer.MAX_VALUE;

            System.out.println("\n\n============== Job " + job.id + " (" + job.width + "x" + job.height + ") ==============");

            // Durchsucht alle verfügbaren freien Rechtecke
            for (int i = 0; i < freeRects.size(); i++) {
                FreeRectangle rect = freeRects.get(i);
                System.out.println("  Prüfe FreeRect " + i + ": Startkoordinaten (x=" + rect.x + ", y=" + rect.y + "), Breite=" + rect.width + "mm, Höhe=" + rect.height + "mm");

                // Passt der Job in dieses freie Rechteck?
                if (job.width <= rect.width && job.height <= rect.height) {
                    System.out.println("    -> Passt!");
                    int leftoverHoriz = rect.width - job.width;
                    System.out.println("      Berechnung leftoverHoriz: rect.width (" + rect.width + ") - job.width (" + job.width + ") = " + leftoverHoriz);
                    int leftoverVert = rect.height - job.height;
                    System.out.println("      Berechnung leftoverVert: rect.height (" + rect.height + ") - job.height (" + job.height + ") = " + leftoverVert);
                    int shortSideFit = Math.min(leftoverHoriz, leftoverVert);
                    System.out.println("      Berechnung shortSideFit: min(leftoverHoriz (" + leftoverHoriz + "), leftoverVert (" + leftoverVert + ")) = " + shortSideFit);
                    System.out.println("      current bestScore = " + bestScore);

                    // Der "Best Fit" ist das leere Rechteck, worin der Job den kleinsten möglichen Abstand horizontal bzw. vertikal haben würde
                    if (shortSideFit < bestScore) {
                        bestScore = shortSideFit;
                        bestRect = rect;  // Merke dir das beste Rechteck
                        System.out.println("      -> Neuer Best-Fit gefunden! bestScore = " + bestScore);

                    }
                } else {
                    System.out.println("    -> Passt NICHT.");
                }
            }
            // Wenn kein passender freier Bereich gefunden wurde: Job passt nicht
            if (bestRect == null) {
                System.out.println("-> Kein passendes Rechteck gefunden für Job " + job.id);
                return false;
            }
            // Platziere den Job im besten gefundenen Rechteck
            job.x = bestRect.x;
            job.y = bestRect.y;
            job.placedOn = this;
            jobs.add(job);
            System.out.println("-> Platziert in (" + job.x + ", " + job.y + ") auf " + name);
            // Zerschneide das belegte Rechteck in neue freie Bereiche
            splitFreeRect(bestRect, job);
            return true;
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
        Scanner scanner = new Scanner(System.in);
        System.out.print("Algorithmus wählen (1 = First Fit, 2 = MaxRects): ");
        String mode = scanner.nextLine().trim();

        List<Job> jobs = Arrays.asList(
                new Job(1, 402, 480),
                new Job(2, 305, 222),
                new Job(3, 220, 573),
                new Job(4, 205, 153),
                new Job(5, 243, 188)
        );

        Plate plateA = new Plate("Plate A", 963, 650);

        for (Job job : jobs) {
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
        for (Job job : jobs) {
            System.out.println(job);
        }

        System.out.println("\n=== Used Plate ===");
        System.out.println(plateA.name + " hat " + plateA.jobs.size() + " Jobs.");
    }

}
