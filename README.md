# Waste Collection & Municipal Complaint System
### Smart City Municipal Corporation Desktop Application

[![Java](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://www.oracle.com/java/)
[![UI](https://img.shields.io/badge/UI-Swing%20%2B%20AWT-blue.svg)]()
[![Database](https://img.shields.io/badge/Database-MySQL%208.0-blue.svg)](https://www.mysql.com/)
[![Architecture](https://img.shields.io/badge/Architecture-MVC%20%2F%20Layered-brightgreen.svg)]()
[![License](https://img.shields.io/badge/License-MIT-green.svg)]()

A production-grade, architectural desktop software application designed for a modern **Smart City Municipal Corporation**. The system manages citizen waste-related complaints, computes an intelligent **AI-assisted priority score (0–100)**, automatically **routes tasks area-wise to collection crews based on real-time workload**, and provides **real-time command center dashboards** for municipal staff, field crews, and citizens.

---

## 🌟 Key Features

### 1. Role-Based Access Control (RBAC)
- **Citizen Portal**:
  - Register new account & secure sign in with SHA-256 password hashing.
  - Submit municipal waste complaints with live AI priority score preview.
  - Area and Ward dropdowns with landmark address.
  - Real-time condition flags: Public Health Hazard, Garbage Overflow, Public Landmark / Sensitive Zone.
  - Track complaint tickets with a visual 4-step progress timeline (`NEW` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED`).
  - Interactive 5-star service rating and feedback submission for resolved complaints.
- **Municipal Admin / Staff Control Center**:
  - Real-time KPI Stat Cards: Total Complaints, Pending Action, In Progress, Resolved, Critical Risk, Active Crews.
  - Interactive Vector Graphics2D charts:
    - **Category Share** (Donut Chart)
    - **Area Density** (Horizontal Bar Chart)
    - **7-Day Influx Trend** (Line Chart with smooth area fill)
  - Full complaint operations: multi-criteria search, live filters (Status, Priority, Area, Category), sorting, manual crew assignment/reassignment, and status progression.
  - **Area Intelligence Hotspot Analyzer**: Identifies high-risk wards, complaint density, backlog clusters, and average resolution turnaround hours.
  - Collection Crew Management: Crew roster, live active workload counts, availability toggling, and new crew dispatching.
- **Collection Crew Field Dashboard**:
  - Today's assigned tasks filtered by active status (`ASSIGNED`, `IN_PROGRESS`).
  - Action buttons: Accept & Start Work (moves status to `IN_PROGRESS`), Mark Resolved with resolution notes and proof photo path.
  - Crew workload warning meters.

### 2. AI-Assisted Complaint Priority Engine
A modular rule-based intelligence engine (`IPriorityEngine`) that generates an objective score between **0 and 100**:
- **90 – 100 ➔ CRITICAL** (4-hour resolution target)
- **70 – 89 ➔ HIGH** (12-hour resolution target)
- **40 – 69 ➔ MEDIUM** (24-hour resolution target)
- **0 – 39 ➔ LOW** (48-hour resolution target)

**Evaluation Factors**:
- Base category severity (Dead animal = 30, Overflow = 25, Drainage = 25, Illegal dumping = 20, etc.)
- Public health hazard flag (`+30`)
- Garbage overflow condition (`+25`)
- Illegal dumping violation (`+20`)
- Sensitive / high footfall zone (Hospitals, schools, metro stations) (`+15`)
- Ward complaint cluster frequency (multiple tickets in same ward in 48h) (`+10` to `+20`)
- Aging backlog escalation (unresolved > 24h: `+15`, > 48h: `+25`)
- Outputs a transparent, human-readable scoring reason breakdown for auditability.

### 3. Intelligent Area-Wise Smart Routing
- Detects the complaint's Area and Ward.
- Queries active, available collection crews in the exact ward first; falls back to the parent area if ward crews are at capacity.
- Evaluates real-time active task queues and assigns the complaint to the least occupied crew.
- Computes targeted resolution deadlines based on priority and queue depth.
- Automatically creates assignment records and audit status history.

---

## 🏛️ System Architecture

The application strictly enforces separation of concerns through an enterprise layered architecture:

```
[UI Layer (Swing + AWT)]
   ├── LoginFrame.java
   ├── CitizenDashboard.java
   ├── AdminDashboard.java
   ├── CrewDashboard.java
   └── components/ (RoundedPanel, ModernButton, StatCard, StyledTable, CustomChartPanel, etc.)
          │
          ▼
[Service & Intelligence Layer]
   ├── ComplaintService.java
   ├── PriorityEngine.java (AI Scoring Engine)
   ├── AssignmentService.java (Smart Routing Engine)
   └── AnalyticsService.java
          │
          ▼
[Data Access Layer (JDBC DAO)]
   ├── ComplaintDAO.java
   ├── CrewDAO.java
   ├── UserDAO.java
   ├── AreaDAO.java
   ├── StatusHistoryDAO.java
   ├── FeedbackDAO.java
   └── AnalyticsDAO.java
          │
          ▼
[Database Layer (MySQL 8.0)]
   └── smart_city_waste (Normalized tables, foreign keys, indexes)
```

---

## 🗄️ Database Design (MySQL)

Database Name: `smart_city_waste`

### Entity-Relationship (ER) Overview
- **`users`**: Stores municipal staff, citizens, and crew leaders with SHA-256 password hash, salt, phone, and role enum (`ADMIN`, `CITIZEN`, `CREW`).
- **`areas`**: Municipal administrative zones (e.g., Mansarovar, Malviya Nagar, Civil Lines).
- **`wards`**: Administrative wards belonging to an area with foreign key `area_id`.
- **`crews`**: Field collection units with leader name, vehicle registration number, assigned ward/area, current workload count, availability (`AVAILABLE`, `BUSY`, `ON_LEAVE`), and optional user login.
- **`complaints`**: Primary ticket record containing code (`CMP-YYYY-XXXX`), citizen details, area/ward, category, description, status, priority, priority score, priority reason, flags, assigned crew, deadline, resolution notes, and timestamps.
- **`complaint_assignments`**: Assignment audit logs recording which crew was dispatched, by whom, and timestamp.
- **`complaint_status_history`**: Comprehensive status progression audit trail.
- **`feedback`**: Citizen ratings (1–5 stars) and reviews for resolved complaints.
- **`notifications`**: Real-time municipal system alerts.

---

## 📁 Project Directory Structure

```
java project/
├── bin/                          # Compiled bytecode .class files
├── config/
│   └── db.properties             # Configurable database credentials
├── database/
│   ├── schema.sql                # Complete MySQL DDL schema
│   └── seed.sql                  # Realistic sample municipal data (Jaipur)
├── lib/
│   └── mysql-connector-j-8.3.0.jar # MySQL JDBC Driver
├── src/
│   ├── config/
│   │   ├── DBConnection.java     # Thread-safe JDBC connection manager
│   │   └── DatabaseInitializer.java # Automated schema & seed runner
│   ├── model/
│   │   ├── Enums.java            # UserRole, ComplaintStatus, PriorityLevel, Category
│   │   ├── User.java
│   │   ├── Area.java
│   │   ├── Ward.java
│   │   ├── Crew.java
│   │   ├── Complaint.java
│   │   ├── ComplaintAssignment.java
│   │   ├── StatusHistory.java
│   │   ├── Feedback.java
│   │   └── Notification.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── AreaDAO.java
│   │   ├── CrewDAO.java
│   │   ├── ComplaintDAO.java
│   │   ├── StatusHistoryDAO.java
│   │   ├── FeedbackDAO.java
│   │   └── AnalyticsDAO.java
│   ├── service/
│   │   ├── PriorityEngine.java   # Rule-based AI scoring
│   │   ├── AssignmentService.java # Smart routing engine
│   │   ├── ComplaintService.java # Lifecycle coordinator
│   │   └── AnalyticsService.java # KPI & chart aggregations
│   ├── ui/
│   │   ├── components/
│   │   │   ├── RoundedPanel.java
│   │   │   ├── ModernButton.java
│   │   │   ├── StatCard.java
│   │   │   ├── StyledTable.java
│   │   │   ├── StatusBadge.java
│   │   │   ├── PriorityBadge.java
│   │   │   ├── SearchField.java
│   │   │   ├── SidebarButton.java
│   │   │   ├── CustomChartPanel.java (Donut, Bar, Trend charts)
│   │   │   └── NotificationToast.java
│   │   ├── LoginFrame.java
│   │   ├── CitizenDashboard.java
│   │   ├── AdminDashboard.java
│   │   └── CrewDashboard.java
│   ├── utils/
│   │   ├── SecurityUtils.java    # SHA-256 + Salt hashing
│   │   ├── ValidationUtils.java  # Indian mobile, email, input validation
│   │   ├── SessionManager.java   # Session state
│   │   └── UIUtils.java          # Design tokens, color palette, typography
│   ├── Main.java                 # Application launcher
│   └── TestRunner.java           # Automated 31-test verification harness
├── compile.bat                   # 1-click Windows compilation script
├── run.bat                       # 1-click Windows execution script
├── compile.ps1                   # PowerShell compilation script
├── run.ps1                       # PowerShell execution script
└── README.md                     # Technical documentation
```

---

## 🚀 Setup & Execution Guide

### Prerequisites
1. **Java JDK 17+** (Developed and verified on Java 25 LTS).
2. **MySQL Server 8.0+** running locally on port `3306`.
3. The MySQL JDBC Driver is bundled in `lib/mysql-connector-j-8.3.0.jar`.

### Step 1: Database Setup
You can set up the database using either of the two methods below:

#### Method A: 1-Click Auto-Setup (Recommended)
1. Verify `config/db.properties` has your MySQL `root` password (default is `root`).
2. Run the application (or `TestRunner`). The application includes an automated Database Initializer that executes `database/schema.sql` and `database/seed.sql` automatically!
3. Alternatively, open the **"Database Setup"** tab on the Login screen and click **"Run Auto-Setup & Seed"**.

#### Method B: MySQL Workbench / CLI
1. Open MySQL Workbench.
2. Open and execute `database/schema.sql`.
3. Open and execute `database/seed.sql`.

---

### Step 2: Compile the Project

From the project root folder in Command Prompt or PowerShell:

```bat
compile.bat
```
or via PowerShell:
```powershell
.\compile.ps1
```

---

### Step 3: Run the Application

```bat
run.bat
```
or via PowerShell:
```powershell
.\run.ps1
```

---

### Step 4: Run Automated Tests

To run the complete automated test suite (verifying security hashing, validations, AI priority calculation, DB seeding, and smart routing):

```bat
java -cp "bin;lib/*" TestRunner
```

---

## 🔑 Default Login Credentials

| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Municipal Admin** | `admin` | `admin123` | Full command dashboard, KPIs, assign crews, area intelligence |
| **Area Supervisor** | `supervisor1` | `admin123` | Inspect ward operations, reassign crews, update tickets |
| **Citizen** | `rahul` | `password123` | Submit complaints, live priority preview, track ticket, rate service |
| **Citizen** | `priya` | `password123` | Alternate citizen account with ongoing complaints |
| **Collection Crew** | `crew_mansarovar1` | `crew123` | View assigned field tasks, accept work, submit resolution notes |
| **Collection Crew** | `crew_malviya1` | `crew123` | Field unit for Malviya Nagar |

*(Citizens can also self-register with any mobile number and password via the "Register here" link on the login screen).*

---

## 🛡️ Security Best Practices
- **No Plaintext Passwords**: Passwords are cryptographically salted using random 16-byte hex salts and hashed via **SHA-256** with constant-time comparison to prevent timing attacks.
- **SQL Injection Prevention**: 100% of database interactions utilize parameterized `PreparedStatement` queries. No string concatenation for SQL queries.
- **Input Sanitization & Validation**: Indian mobile format validation (`^(?:\\+91|91|0)?[6-9]\\d{9}$`), email format checks, and character length bounds.
- **Thread Safety & Non-blocking UI**: Heavy database queries, status updates, and auto-routing executions are offloaded to background threads using `SwingWorker`.

---

## 🔮 Future Enhancements
- Integration of a computer vision AI model for automatic waste classification and overflow volume estimation from uploaded photos.
- Real-time GPS vehicle tracking of collection trucks rendered on a GIS map canvas.
- Citizen SMS / WhatsApp notification integration via municipal telecom gateways.
- IoT smart bin sensor integration for automated threshold-based complaint generation.
