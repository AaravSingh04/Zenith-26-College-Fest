# Zenith '26 College Fest Management System ⚡

A high-performance, responsive college fest management system featuring a student-facing event discovery & registration portal and a real-time admin command dashboard.

Built as a capstone project for **PM2-01 (How Software Works)** and **PM2-02 (Front-End, Back-End & Mobile)**.

---

## 🌟 Features

- **Student Event Portal (`index.html`)**:
  - Live discovery of all 14 flagship fest events across Technical, Cultural, Sports, Gaming, and Workshops.
  - Interactive search and category filtering powered directly by backend API query parameters (`?category=...&search=...`).
  - Dynamic seat capacity tracking: `available_seats` computed live on every request.
  - Prominent `FULL` badges when events reach capacity.
  - Interactive registration modal with client-side instant validation.
  - Robust server-side verification: blocks duplicate registrations, full events, and invalid inputs.

- **Admin Dashboard (`admin.html`)**:
  - 4 Real-time KPI stat cards: Total Events, Verified Registrations, Overall Capacity Utilization %, Upcoming Events.
  - Visual category breakdown charts.
  - In-demand events table automatically sorted by capacity utilization descending.
  - Visual alert highlight on events exceeding 90% capacity.
  - Student registrations list with instant event filtering.
  - **Stretch Goals Built-in**:
    - **Add New Event**: Admin can add new fest events directly from the dashboard.
    - **Cancel Registration**: Admin can cancel student registrations with confirmation and immediate seat recovery.

- **Architecture & Integrity**:
  - **Single Source of Truth**: Available seats are never stored in a separate column; computed live to prevent data drift.
  - **"Never Trust the Client"**: All registration constraints validated independently on the backend with appropriate HTTP status codes (200, 201, 400, 404, 409).
  - Seeded automatically on startup from `data/events_seed.xlsx`.

---

## 🛠️ Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Frontend** | HTML5, Tailwind CSS (Play CDN), Vanilla JavaScript | Responsive cards, modals, reactive UI, animations |
| **Backend** | Python 3.13, FastAPI, Uvicorn | REST APIs, validation, business logic, static file serving |
| **Database** | SQLite (`fest.db`) | Local persistent relational database |
| **Data Ingestion** | OpenPyXL | Spreadsheet seed data reading |
| **Testing** | Postman | End-to-end API contract testing |

---

## 📂 Project Structure

```
zenith26-fest-system/
├── backend/
│   ├── main.py              # FastAPI application, REST endpoints, static file mounting
│   ├── database.py          # SQLite connection and schema creation
│   ├── models.py            # Pydantic schemas (request/response models)
│   └── seed_data.py         # Ingestion script reading data/events_seed.xlsx into SQLite
├── frontend/
│   ├── index.html           # Student event portal & registration modal
│   ├── admin.html           # Admin dashboard & analytics
│   └── js/
│       ├── app.js           # Student portal client logic & API integration
│       └── admin.js         # Admin dashboard analytics & management logic
├── data/
│   └── events_seed.xlsx     # Companion spreadsheet with 14 seed events
├── .venv/                   # Python virtual environment
├── requirements.txt         # Project dependencies
├── zenith26-api.postman_collection.json # Ready-to-import Postman test collection
├── Postman-Testing-Commands-Zenith26.md  # 18 test cases specification
└── README.md                # Project documentation
```

---

## 🚀 How to Run the Application

### 1. Prerequisites
- Python 3.10+ installed on your computer.

### 2. Activate the Virtual Environment

**On Windows (PowerShell):**
```powershell
.\.venv\Scripts\Activate.ps1
```

**On macOS / Linux:**
```bash
source .venv/bin/activate
```

*(If setting up from scratch on a new machine, install dependencies using `pip install -r requirements.txt`)*

### 3. Start the Server

Run the following command from the `zenith26-fest-system` root folder:

```bash
uvicorn backend.main:app --reload
```

The server will automatically start at:
- **Student Event Portal:** [http://localhost:8000/](http://localhost:8000/)
- **Admin Dashboard:** [http://localhost:8000/admin.html](http://localhost:8000/admin.html)
- **Interactive Swagger API Docs:** [http://localhost:8000/docs](http://localhost:8000/docs)

---

## 📡 REST API Contract

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/api/events` | List events with live computed `available_seats`. Optional `?category=` & `?search=` | `200 OK` |
| `GET` | `/api/events/{id}` | Single event detail view | `200 OK`, `404 Not Found` |
| `POST` | `/api/events/{id}/register` | Register a student with server-side validation | `201 Created`, `400 Bad Request`, `409 Conflict`, `404 Not Found` |
| `GET` | `/api/registrations` | List all student registrations (optional `?event_id=`) | `200 OK` |
| `GET` | `/api/stats` | Live computed KPI statistics | `200 OK` |
| `POST` | `/api/events` | *(Stretch)* Admin creates a new event | `201 Created`, `400 Bad Request` |
| `DELETE` | `/api/registrations/{id}` | *(Stretch)* Admin cancels a registration | `200 OK`, `404 Not Found` |

---

## 🧪 Testing with Postman

1. Open Postman.
2. Click **Import** and select `zenith26-api.postman_collection.json`.
3. Run the requests to verify contract behavior against `http://localhost:8000`.
4. Refer to `Postman-Testing-Commands-Zenith26.md` for the full test script with all 18 cases.
