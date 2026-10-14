# Postman Testing Guide: Zenith '26 College Fest Management System
**Companion to:** PM2-01 (How Software Works) & PM2-02 (Front-End, Back-End & Mobile)  
**Base URL:** `http://localhost:8000`

---

## 1. Quick Postman Setup

1. Open **Postman** (Desktop App or Web Agent).
2. Click **Import** (top left) and select:
   `zenith26-api.postman_collection.json`
3. Verify that the variable `baseUrl` is set to `http://localhost:8000`.
4. Make sure your FastAPI backend server is running:
   ```bash
   uvicorn backend.main:app --reload
   ```

---

## 2. Test Suite: All 18 API Test Cases

### Folder 1: Events Endpoints

#### Request 1: GET All Events
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/events`
- **Expected Status:** `200 OK`
- **What to verify:** An array of 14 event objects. Each event contains `available_seats`, `capacity`, `category`, `fee`, `venue`, `event_date`, `event_time`, `icon`.

#### Request 2: GET Events Filtered by Category
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/events?category=Technical`
- **Expected Status:** `200 OK`
- **What to verify:** All returned events have `"category": "Technical"`.

#### Request 3: GET Events Filtered by Cultural Category
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/events?category=Cultural`
- **Expected Status:** `200 OK`
- **What to verify:** All returned events have `"category": "Cultural"`.

#### Request 4: GET Events Search by Title
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/events?search=Code`
- **Expected Status:** `200 OK`
- **What to verify:** Returns "Code Sprint 2.0".

#### Request 5: GET Events Search Non-existent
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/events?search=NonExistentKeywordXYZ`
- **Expected Status:** `200 OK`
- **What to verify:** Returns empty list `[]`.

#### Request 6: GET Single Event by Valid ID
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/events/1`
- **Expected Status:** `200 OK`
- **Expected Body snippet:**
  ```json
  {
    "id": 1,
    "title": "Code Sprint 2.0",
    "category": "Technical",
    "capacity": 100,
    "available_seats": 100
  }
  ```

#### Request 7: GET Single Event by Non-existent ID
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/events/999`
- **Expected Status:** `404 Not Found`
- **Expected Response:** `{"detail": "Event with ID 999 not found"}`

---

### Folder 2: Registrations Endpoints

#### Request 8: POST Register for Event (Success)
- **Method:** `POST`
- **URL:** `{{baseUrl}}/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Aditi Sharma",
    "roll_number": "26CS104",
    "email": "aditi.sharma@college.edu",
    "phone": "9876543210"
  }
  ```
- **Expected Status:** `201 Created`
- **Expected Response:** Contains `id`, `event_id`: 1, `student_name`: "Aditi Sharma", `registered_at`.

#### Request 9: Verify Seat Count Decreased
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/events/1`
- **Expected Status:** `200 OK`
- **What to verify:** `available_seats` decreased from 100 to 99!

#### Request 10: POST Register - Missing Student Name (400)
- **Method:** `POST`
- **URL:** `{{baseUrl}}/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "",
    "roll_number": "26CS104",
    "email": "test@college.edu",
    "phone": "9876543210"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Expected Detail:** `"Student name is required and cannot be empty"`

#### Request 11: POST Register - Missing Roll Number (400)
- **Method:** `POST`
- **URL:** `{{baseUrl}}/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Rohan Mehra",
    "roll_number": "",
    "email": "rohan@college.edu"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Expected Detail:** `"Roll number is required and cannot be empty"`

#### Request 12: POST Register - Invalid Email without @ (400)
- **Method:** `POST`
- **URL:** `{{baseUrl}}/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Rohan Mehra",
    "roll_number": "26CS105",
    "email": "rohan-without-at"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Expected Detail:** `"Invalid email address format: must contain '@'"`

#### Request 13: POST Register - Duplicate Registration for Same Event (409)
- **Method:** `POST`
- **URL:** `{{baseUrl}}/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Aditi Sharma",
    "roll_number": "26CS104",
    "email": "aditi.sharma@college.edu",
    "phone": "9876543210"
  }
  ```
- **Expected Status:** `409 Conflict`
- **Expected Detail:** `"Already registered"`

#### Request 14: POST Register - Non-existent Event ID (404)
- **Method:** `POST`
- **URL:** `{{baseUrl}}/api/events/999/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Aditi Sharma",
    "roll_number": "26CS104",
    "email": "unique@college.edu"
  }
  ```
- **Expected Status:** `404 Not Found`

#### Request 15: GET All Registrations
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/registrations`
- **Expected Status:** `200 OK`
- **What to verify:** Array includes `event_title` joined from events table.

#### Request 16: GET Registrations Filtered by Event ID
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/registrations?event_id=1`
- **Expected Status:** `200 OK`

---

### Folder 3: Analytics & Admin Stats

#### Request 17: GET Dashboard Stats
- **Method:** `GET`
- **URL:** `{{baseUrl}}/api/stats`
- **Expected Status:** `200 OK`
- **Expected Keys:**
  ```json
  {
    "total_events": 14,
    "total_registrations": 1,
    "total_capacity": 1447,
    "overall_utilization_pct": 0.1,
    "upcoming_events_count": 14,
    "events_by_category": {
      "Technical": 4,
      "Cultural": 5,
      "Sports": 2,
      "Gaming": 1,
      "Workshop": 2
    }
  }
  ```

---

### Folder 4: Stretch Endpoints

#### Request 18: POST Admin Add Event (Stretch)
- **Method:** `POST`
- **URL:** `{{baseUrl}}/api/events`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "title": "Hackathon 24hr Blitz",
    "category": "Technical",
    "description": "Non-stop overnight hackathon.",
    "event_date": "2026-11-14",
    "event_time": "09:00 AM",
    "venue": "Innovation Hub",
    "capacity": 50,
    "fee": 0,
    "icon": "🚀"
  }
  ```
- **Expected Status:** `201 Created`
