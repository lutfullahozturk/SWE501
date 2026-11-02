# 🗺️ Turkey Navigation System (Dijkstra Shortest Path & Map Visualization)

An interactive 2D navigation and shortest-path visualization application for cities across Turkey. Built in **Java** utilizing **StdDraw**, this application parses geographic coordinates, constructs a dynamic undirected graph based on geometric proximity, renders detailed country borders, and computes optimal routes using **Dijkstra's Algorithm**.

---

## ✨ Features

* **Interactive Map GUI:** Click to select source and destination cities with visual highlights.
* **Geographical Boundary Rendering:** Cleaned 301-point polygon rendering for Turkey's borders.
* **Parametric Graph Construction:** Configurable $K$-Nearest Neighbor ($K$-NN) edge creation via `connectionCount`.
* **Shortest Path Computation:** High-performance pathfinding backed by Dijkstra's Algorithm and a Min-Priority Queue.
* **Flicker-Free Canvas:** Event-driven rendering engine (`needsRedraw` state system) eliminating UI flickering and high CPU usage.
* **Robust File IO:** Multi-format parser resilient to Unicode BOM, whitespace variations, and regional decimal separators (comma vs. period).

---

## 🛠️ Project Structure

The project follows a standard structured layout:

```text
name_surname/
├── code/
│   └── LutfullahOzturk.java       # Core application & GUI logic
├── report/
│   └── report.pdf                 # Academic assignment report
└── misc/
    ├── cityCoordinates.txt        # City longitude/latitude data
    └── turkeyBorders.txt          # Cleaned country boundary coordinates
```

---

## 🚀 Getting Started

### Prerequisites

* **Java Development Kit (JDK 8 or higher)**
* `StdDraw` library (included or compiled together)

### Running the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/turkey-navigation-system.git
   cd turkey-navigation-system
   ```

2. Compile the Java source files from the project root:
   ```bash
   javac code/LutfullahOzturk.java
   ```

3. Run the application:
   ```bash
   java code.LutfullahOzturk
   ```

---

## ⚙️ How It Works

### 1. Graph Construction ($K$-NN)
Each city represents a node in an undirected graph. Cities are connected to their $K$ closest neighbors based on Euclidean distance:
$$d = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}$$

The parameter `connectionCount` controls graph density (default $K=2$). Duplicate undirected edges are automatically filtered using custom string comparisons.

### 2. Pathfinding (Dijkstra)
When both source and destination cities are selected:
* Dijkstra's algorithm evaluates distances and computes the shortest route.
* The total travel distance and ordered city sequence are output to the console and drawn highlighted in red on the canvas.

---

## 📄 License

This project was developed for educational purposes as part of the Data Structures & Algorithms coursework.
