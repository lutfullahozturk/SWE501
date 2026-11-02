package code;
import java.io.File;
import java.util.*;

public class LutfullahOzturk {

    static int connectionCount = 2; // Her şehir için en yakın kaç komşu bağlanacak

    // Şehir veri modeli
    static class City {
        String name;
        double lon; // X ekseni (Boylam)
        double lat; // Y ekseni (Enlem)
        List<City> neighbors = new ArrayList<>();

        City(String name, double lon, double lat) {
            this.name = name;
            this.lon = lon;
            this.lat = lat;
        }

        // İki şehir arasındaki harita üzeri mesafe (Öklid/Coğrafi)
        double distanceTo(City other) {
            double dx = this.lon - other.lon;
            double dy = this.lat - other.lat;
            return Math.sqrt(dx * dx + dy * dy);
        }

        // Gerçek dünya mesafesi (Haversine formülü - km cinsinden)
        double realDistanceTo(City other) {
            double R = 6371; // Dünya yarıçapı (km)
            double dLat = Math.toRadians(other.lat - this.lat);
            double dLon = Math.toRadians(other.lon - this.lon);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(other.lat)) *
                            Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c;
        }
    }

    private static List<City> cities = new ArrayList<>();
    private static City startCity = null;
    private static City endCity = null;
    private static List<City> shortestPath = new ArrayList<>();
    private static double totalDistance = 0.0;
    private static List<double[]> borderPoints = new ArrayList<>();

    public static void main(String[] args) {
        // 1. Şehir koordinatlarını dosyadan oku
        loadCities("misc/cityCoordinates.txt");
        loadBorders("misc/turkeyBorders.txt");

        if (cities.isEmpty()) {
            System.err.println("Şehir verileri yüklenemedi!");
            return;
        }

        // 2. Her şehri en yakın 2 komşusuna bağla
        buildGraph();

        // 3. Canvas ve koordinat ölçeğini ayarla (Türkiye sınırlarına uygun)
        StdDraw.setCanvasSize(1000, 500);
        StdDraw.setXscale(25.5, 45.0); // Türkiye Boylam aralığı
        StdDraw.setYscale(35.5, 42.5); // Türkiye Enlem aralığı
        StdDraw.enableDoubleBuffering();

        // 4. Ana Çizim ve Etkileşim Döngüsü
        boolean needsRedraw = true; // İlk açılışta çizilmesi için true

        while (true) {
            // Fare tıklamalarını kontrol et
            if (StdDraw.isMousePressed()) {
                double mouseX = StdDraw.mouseX();
                double mouseY = StdDraw.mouseY();

                City clicked = getClosestCity(mouseX, mouseY);
                if (clicked != null) {
                    if (startCity == null || (startCity != null && endCity != null)) {
                        startCity = clicked;
                        endCity = null;
                        shortestPath.clear();
                        totalDistance = 0.0;
                        needsRedraw = true; // Değişiklik oldu, tekrar çiz
                    } else if (endCity == null && clicked != startCity) {
                        endCity = clicked;
                        findShortestPath();
                        needsRedraw = true; // Değişiklik oldu, tekrar çiz
                    }
                    StdDraw.pause(200); // Tıklama gecikmesi
                }
            }

            // Sadece bir değişiklik olduğunda ekranı yeniden çiz (Yanıp sönmeyi önler)
            if (needsRedraw) {
                drawMap();
                StdDraw.show();
                needsRedraw = false; // Çizildi, bir sonraki tıklamaya kadar bekle
            }

            StdDraw.pause(50); // İşlemciyi yormamak için kısa bir bekleme
        }
    }

    private static void loadBorders(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Sınır dosyası bulunamadı, sınırlar çizilmeyecek.");
            return;
        }

        try (Scanner scanner = new Scanner(file, "UTF-8")) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    double lon = Double.parseDouble(parts[0].replace(",", "."));
                    double lat = Double.parseDouble(parts[1].replace(",", "."));
                    borderPoints.add(new double[] { lon, lat });
                }
            }
            System.out.println(borderPoints.size() + " sınır noktası yüklendi!");
        } catch (Exception e) {
            System.err.println("Sınır dosyası okunurken hata: " + e.getMessage());
        }
    }

    // cityCoordinates.txt dosyasını okur
    private static void loadCities(String filename) {
        File file = new File(filename);
        System.out.println("Aranan dosya tam yolu: " + file.getAbsolutePath());
        System.out.println("Dosya mevcut mu?: " + file.exists() + ", Boyut: " + file.length() + " bayt");

        try (Scanner scanner = new Scanner(file, "UTF-8")) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String name = parts[0];
                    double lat = Double.parseDouble(parts[1].replace(",", "."));
                    double lon = Double.parseDouble(parts[2].replace(",", "."));
                    cities.add(new City(name, lon, lat));
                }
            }
            System.out.println(cities.size() + " şehir başarıyla yüklendi!");
        } catch (Exception e) {
            System.err.println("Veri okunurken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Her şehri kendisine en yakın 2 şehre bağlar
    private static void buildGraph() {
        for (City c1 : cities) {
            List<City> sortedByDist = new ArrayList<>(cities);
            sortedByDist.remove(c1);
            sortedByDist.sort(Comparator.comparingDouble(c1::distanceTo));

            // En yakın 2 komşuyu ekle (karşılıklı bağlantı)
            for (int i = 0; i < Math.min(connectionCount, sortedByDist.size()); i++) {
                City neighbor = sortedByDist.get(i);
                if (!c1.neighbors.contains(neighbor)) {
                    c1.neighbors.add(neighbor);
                }
                if (!neighbor.neighbors.contains(c1)) {
                    neighbor.neighbors.add(c1);
                }
            }
        }
    }

    // Dijkstra Algoritması ile En Kısa Yol
    private static void findShortestPath() {
        if (startCity == null || endCity == null)
            return;

        Map<City, Double> dist = new HashMap<>();
        Map<City, City> prev = new HashMap<>();
        PriorityQueue<City> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        for (City c : cities) {
            dist.put(c, Double.POSITIVE_INFINITY);
        }
        dist.put(startCity, 0.0);
        pq.add(startCity);

        while (!pq.isEmpty()) {
            City current = pq.poll();

            if (current == endCity)
                break;

            for (City neighbor : current.neighbors) {
                double newDist = dist.get(current) + current.realDistanceTo(neighbor);
                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    prev.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        // Yolu geriye doğru oluştur
        shortestPath.clear();
        City curr = endCity;
        if (prev.containsKey(curr) || curr == startCity) {
            while (curr != null) {
                shortestPath.add(0, curr);
                curr = prev.get(curr);
            }
            totalDistance = dist.get(endCity);
        }
    }

    // Fare tıklamasına en yakın şehri bulur
    private static City getClosestCity(double x, double y) {
        City closest = null;
        double minDst = Double.MAX_VALUE;
        for (City c : cities) {
            double dst = Math.hypot(c.lon - x, c.lat - y);
            if (dst < minDst && dst < 0.4) { // 0.4 tolerans yarıçapı
                minDst = dst;
                closest = c;
            }
        }
        return closest;
    }

    // Ekran Çizimleri
    private static void drawMap() {
        StdDraw.clear(StdDraw.WHITE);

        if (!borderPoints.isEmpty()) {
            StdDraw.setPenColor(StdDraw.DARK_GRAY);
            StdDraw.setPenRadius(0.003); // Kalınlığı buradan ayarlayabilirsin
            for (int i = 0; i < borderPoints.size() - 1; i++) {
                double[] p1 = borderPoints.get(i);
                double[] p2 = borderPoints.get(i + 1);
                StdDraw.line(p1[0], p1[1], p2[0], p2[1]);
            }
        }

        // 1. Tüm Kenarları (Yolları) Çiz (Gri)
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.setPenRadius(0.002);
        Set<String> drawnEdges = new HashSet<>();
        for (City c : cities) {
            for (City n : c.neighbors) {
                String edgeKey = c.name.compareTo(n.name) < 0 ? c.name + "-" + n.name : n.name + "-" + c.name;
                if (!drawnEdges.contains(edgeKey)) {
                    StdDraw.line(c.lon, c.lat, n.lon, n.lat);
                    drawnEdges.add(edgeKey);
                }
            }
        }

        // 2. En Kısa Yolu Çiz (Mavi)
        if (shortestPath.size() > 1) {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius(0.006);
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                City c1 = shortestPath.get(i);
                City c2 = shortestPath.get(i + 1);
                StdDraw.line(c1.lon, c1.lat, c2.lon, c2.lat);
            }
        }

        // 3. Şehir Düğümlerini Çiz (Kırmızı / Mavi)
        for (City c : cities) {
            if (c == startCity || c == endCity) {
                StdDraw.setPenColor(StdDraw.BLUE);
                StdDraw.setPenRadius(0.025);
            } else {
                StdDraw.setPenColor(StdDraw.RED);
                StdDraw.setPenRadius(0.015);
            }
            StdDraw.point(c.lon, c.lat);

            // Şehir isimleri
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
            StdDraw.text(c.lon, c.lat + 0.12, c.name);
        }

        // 4. Sol Alt Köşeye Toplam Mesafeyi Yazdır
        if (endCity != null && totalDistance > 0) {
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
            StdDraw.textLeft(26.0, 36.0, String.format("Distance: %.1f km", totalDistance));
        }
    }
}