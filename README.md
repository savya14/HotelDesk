# 🏨 HotelDesk

> A JavaFX 25 hotel management application for front-desk operations — manage rooms, register guests, book and checkout with automated billing and a live dashboard. No database required.

![JavaFX](https://img.shields.io/badge/JavaFX-25-blue?style=flat-square)
![Maven](https://img.shields.io/badge/Build-Maven-red?style=flat-square&logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [How to Use](#how-to-use)
- [Architecture](#architecture)
- [Contributing](#contributing)

---

## Overview

HotelDesk is a standalone desktop application built with JavaFX that automates the core operations of a hotel front desk. It replaces manual, paper-based processes with a clean tab-based GUI covering room inventory, guest registration, bookings, checkout with automatic bill generation, and a real-time operations dashboard.

All data is stored in memory using JavaFX `ObservableList` collections — no database, no external services, just run and go.

---

## Features

### 🛏 Room Management
- Add rooms with number, type (Single / Double / Deluxe), and price per day
- View all rooms in a sortable table
- Filter to show available rooms only
- Live status badge — green **Available**, red **Occupied** — updates instantly on every booking and checkout

### 👤 Customer Management
- Register guests with name and 10-digit contact number
- Assign an available room at registration time
- Assigned Room column updates automatically across the app when a booking is made
- Live occupancy percentage widget

### 📋 Booking & Checkout
- Select a customer and an available room to create a booking in one click
- Room card selector refreshes automatically to show only currently available rooms
- Checkout computes the bill automatically: `nights × price per day` (minimum 1 night)
- Full bill summary shown in a confirmation dialog on checkout
- Booking history table with Active / Checked Out status

### 📊 Dashboard
- Five live metric cards: Total Rooms, Available, Occupied, Active Bookings, Revenue
- All numbers update in real time — no manual refresh needed
- Powered by `ListChangeListener` bindings on the observable collections

---

## Screenshots

> _Add your own screenshots here after running the app._

| Dashboard | Room Management | Booking & Checkout |
|-----------|----------------|-------------------|
| _(screenshot)_ | _(screenshot)_ | _(screenshot)_ |

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| JavaFX | 25 | UI framework |
| Maven | 3.8+ | Build and dependency management |
| javafx-maven-plugin | 0.0.8 | `mvn javafx:run` launcher |

No third-party libraries. No database. Pure JavaFX.

---

## Project Structure

```
src/main/java/com/hotel/
│
├── Main.java                     # Entry point — wires managers → views → Stage
│
├── model/
│   ├── RoomType.java             # Enum: SINGLE, DOUBLE, DELUXE
│   ├── Room.java                 # POJO with BooleanProperty (live availability binding)
│   ├── Customer.java             # POJO with IntegerProperty (live room assignment binding)
│   └── Booking.java              # POJO with checkout() — computes bill, records dates
│
├── manager/
│   ├── RoomManager.java          # ObservableList<Room> with extractor, CRUD, filter, stats
│   ├── CustomerManager.java      # ObservableList<Customer> with extractor, ID sequencing
│   └── BookingManager.java       # book() + checkout() logic, revenue calculation
│
└── view/
    ├── RoomView.java             # Rooms tab — form + filtered TableView
    ├── CustomerView.java         # Customers tab — registration form + table
    ├── BookingView.java          # Bookings tab — book panel + checkout panel + history table
    └── DashboardView.java        # Dashboard tab — live metric cards
```

---

## Getting Started

### Prerequisites

- [JDK 21+](https://adoptium.net) — Temurin 21 LTS recommended
- [IntelliJ IDEA Community](https://www.jetbrains.com/idea/) — free edition
- Maven is bundled with IntelliJ, no separate install needed

### Clone the Repository

```bash
git clone https://github.com/your-username/hotel-management.git
cd hotel-management
```

Also update your `pom.xml` to use JavaFX 25:

```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <javafx.version>25</javafx.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>
</dependencies>
```

### Run with Maven

```bash
mvn javafx:run
```

### Run in IntelliJ

1. Open IntelliJ → **File → Open** → select the project folder
2. Wait for Maven to download dependencies (first run only)
3. Open the **Maven** panel on the right → **Plugins → javafx → javafx:run**
4. Double-click `javafx:run`

The app launches with **6 pre-loaded sample rooms** so you can test immediately.

---

## How to Use

### Add a Room
1. Go to the **Rooms** tab
2. Enter a room number, select a type, enter the price per day
3. Click **Add Room** — it appears in the table instantly

### Register a Guest
1. Go to the **Customers** tab
2. Fill in the guest's name and 10-digit contact number
3. Optionally assign an available room
4. Click **Register Customer**

### Book a Room
1. Go to the **Bookings** tab
2. Select a customer from the dropdown
3. Click a room card to select it
4. Click **Book Now** — the room status flips to Occupied across the entire app

### Checkout a Guest
1. Go to the **Bookings** tab
2. Select the active booking from the Checkout dropdown
3. Click **Checkout** — a bill summary dialog shows the total
4. The room is automatically released back to Available

### View the Dashboard
Switch to the **Dashboard** tab at any time — all stats update live as you work.

---

## Architecture

The app follows a clean three-layer architecture:

```
Presentation  →  view/        (JavaFX controls, no business logic)
Business      →  manager/     (ObservableLists, all invariants enforced here)
Data          →  model/       (POJOs with JavaFX properties for reactive binding)
```

### Key Design: Observable Extractor Pattern

The core pattern that makes the UI reactive without extra code:

```java
FXCollections.observableArrayList(
    r -> new Observable[]{ r.availableProperty() }
)
```

This tells JavaFX to fire a list `UPDATE` event whenever a room's `available` property changes — not just when rooms are added or removed. Every bound `TableView` column re-renders automatically on every book and checkout with zero additional code.

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

1. Fork the repository
2. Create your feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---
