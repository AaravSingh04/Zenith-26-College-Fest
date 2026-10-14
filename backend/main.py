from datetime import date, datetime
from pathlib import Path
from typing import List, Optional
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Query, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

from backend.database import get_db_connection, init_db
from backend.models import (
    EventDetail,
    EventIn,
    EventOut,
    RegistrationDetailOut,
    RegistrationIn,
    RegistrationOut,
    StatsOut,
)
from backend.seed_data import seed_events

BASE_DIR = Path(__file__).resolve().parent.parent
FRONTEND_DIR = BASE_DIR / "frontend"


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: ensure tables exist and seed events from data/events_seed.xlsx
    init_db()
    seed_events()
    yield


app = FastAPI(
    title="Zenith '26 College Fest Management System",
    description="API for student registration and admin management for Zenith '26 Fest.",
    version="1.0.0",
    lifespan=lifespan,
)

# Enable CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Helper: fetch available seats for an event
def get_event_with_seats(event_row: dict, conn) -> dict:
    event_id = event_row["id"]
    cursor = conn.execute(
        "SELECT COUNT(*) as reg_count FROM registrations WHERE event_id = ?;",
        (event_id,),
    )
    reg_count = cursor.fetchone()["reg_count"]
    event_dict = dict(event_row)
    event_dict["available_seats"] = max(0, event_dict["capacity"] - reg_count)
    return event_dict


# -----------------------------------------------------------------------------
# API Endpoints
# -----------------------------------------------------------------------------


@app.get("/api/events", response_model=List[EventOut])
def list_events(
    category: Optional[str] = Query(None, description="Filter by event category"),
    search: Optional[str] = Query(
        None, description="Search event title (case-insensitive)"
    ),
):
    """
    List events with available_seats computed live as capacity minus registrations.
    Supports optional category filter and title search query parameters.
    """
    conn = get_db_connection()
    query = "SELECT * FROM events WHERE 1=1"
    params = []

    if category and category.strip() and category.strip().lower() != "all":
        query += " AND LOWER(category) = LOWER(?)"
        params.append(category.strip())

    if search and search.strip():
        query += " AND LOWER(title) LIKE LOWER(?)"
        params.append(f"%{search.strip()}%")

    query += " ORDER BY event_date ASC, event_time ASC, id ASC"

    cursor = conn.execute(query, params)
    rows = cursor.fetchall()

    results = []
    for r in rows:
        results.append(get_event_with_seats(r, conn))

    conn.close()
    return results


@app.get("/api/events/{event_id}", response_model=EventDetail)
def get_event(event_id: int):
    """Returns details for a single event, including computed available_seats."""
    conn = get_db_connection()
    cursor = conn.execute("SELECT * FROM events WHERE id = ?;", (event_id,))
    row = cursor.fetchone()
    if not row:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with ID {event_id} not found",
        )

    event_detail = get_event_with_seats(row, conn)
    conn.close()
    return event_detail


@app.post(
    "/api/events/{event_id}/register",
    response_model=RegistrationOut,
    status_code=status.HTTP_201_CREATED,
)
def register_for_event(event_id: int, reg_in: RegistrationIn):
    """
    Registers a student for an event with strict server-side validation:
    1. event_id must exist (404)
    2. student_name, roll_number, email must not be empty (400)
    3. email must contain '@' (400)
    4. event must not be at capacity already (409 'Event is full')
    5. same email must not already be registered for this event (409 'Already registered')
    """
    conn = get_db_connection()

    # 1. Check if event exists
    event_cursor = conn.execute("SELECT * FROM events WHERE id = ?;", (event_id,))
    event_row = event_cursor.fetchone()
    if not event_row:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with ID {event_id} not found",
        )

    # 2. Validate non-empty fields
    student_name = reg_in.student_name.strip() if reg_in.student_name else ""
    roll_number = reg_in.roll_number.strip() if reg_in.roll_number else ""
    email = reg_in.email.strip() if reg_in.email else ""
    phone = reg_in.phone.strip() if reg_in.phone else None

    if not student_name:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Student name is required and cannot be empty",
        )
    if not roll_number:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Roll number is required and cannot be empty",
        )
    if not email:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email is required and cannot be empty",
        )

    # 3. Validate email format
    if "@" not in email or email.startswith("@") or email.endswith("@"):
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid email address format: must contain '@'",
        )

    # 4. Check available capacity
    count_cursor = conn.execute(
        "SELECT COUNT(*) as reg_count FROM registrations WHERE event_id = ?;",
        (event_id,),
    )
    current_reg_count = count_cursor.fetchone()["reg_count"]
    if current_reg_count >= event_row["capacity"]:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Event is full",
        )

    # 5. Check duplicate registration (same email for this event)
    dup_cursor = conn.execute(
        "SELECT id FROM registrations WHERE event_id = ? AND LOWER(email) = LOWER(?);",
        (event_id, email),
    )
    if dup_cursor.fetchone():
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Already registered",
        )

    # Insert registration
    registered_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    insert_cursor = conn.execute(
        """
        INSERT INTO registrations (event_id, student_name, roll_number, email, phone, registered_at)
        VALUES (?, ?, ?, ?, ?, ?);
        """,
        (event_id, student_name, roll_number, email, phone, registered_at),
    )
    conn.commit()
    new_reg_id = insert_cursor.lastrowid
    conn.close()

    return {
        "id": new_reg_id,
        "event_id": event_id,
        "student_name": student_name,
        "registered_at": registered_at,
    }


@app.get("/api/registrations", response_model=List[RegistrationDetailOut])
def list_registrations(
    event_id: Optional[int] = Query(None, description="Filter by event_id")
):
    """
    Returns registrations, including the event title for the admin dashboard.
    Optionally filters by event_id.
    """
    conn = get_db_connection()
    query = """
        SELECT r.id, r.event_id, e.title as event_title, r.student_name,
               r.roll_number, r.email, r.phone, r.registered_at
        FROM registrations r
        JOIN events e ON r.event_id = e.id
    """
    params = []
    if event_id is not None:
        query += " WHERE r.event_id = ?"
        params.append(event_id)

    query += " ORDER BY r.id DESC;"

    cursor = conn.execute(query, params)
    rows = cursor.fetchall()
    results = [dict(r) for r in rows]
    conn.close()
    return results


@app.get("/api/stats", response_model=StatsOut)
def get_stats():
    """
    Computes dashboard statistics live from the database:
    - total_events
    - total_registrations
    - total_capacity
    - overall_utilization_pct (total_registrations / total_capacity * 100)
    - upcoming_events_count (event_date today or later)
    - events_by_category (count per category)
    """
    conn = get_db_connection()

    # Total events and capacity
    event_stats = conn.execute(
        "SELECT COUNT(*) as total_events, COALESCE(SUM(capacity), 0) as total_capacity FROM events;"
    ).fetchone()
    total_events = event_stats["total_events"]
    total_capacity = event_stats["total_capacity"]

    # Total registrations
    reg_stats = conn.execute(
        "SELECT COUNT(*) as total_registrations FROM registrations;"
    ).fetchone()
    total_registrations = reg_stats["total_registrations"]

    # Overall utilization percentage
    overall_utilization_pct = 0.0
    if total_capacity > 0:
        overall_utilization_pct = round(
            (total_registrations / total_capacity) * 100, 1
        )

    # Upcoming events count: events with event_date >= today
    today_str = date.today().isoformat()
    upcoming = conn.execute(
        "SELECT COUNT(*) as count FROM events WHERE event_date >= ?;",
        (today_str,),
    ).fetchone()
    upcoming_events_count = upcoming["count"]

    # Events by category
    cat_cursor = conn.execute(
        "SELECT category, COUNT(*) as count FROM events GROUP BY category ORDER BY count DESC;"
    )
    events_by_category = {row["category"]: row["count"] for row in cat_cursor.fetchall()}

    conn.close()

    return {
        "total_events": total_events,
        "total_registrations": total_registrations,
        "total_capacity": total_capacity,
        "overall_utilization_pct": overall_utilization_pct,
        "upcoming_events_count": upcoming_events_count,
        "events_by_category": events_by_category,
    }


# Route alias in case frontend or external client calls /api/dashboard/stats
@app.get("/api/dashboard/stats", response_model=StatsOut, include_in_schema=False)
def get_dashboard_stats():
    return get_stats()


@app.get("/favicon.ico", include_in_schema=False)
def get_favicon():
    from fastapi import Response
    # Simple SVG favicon representing fest lightning bolt
    svg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><text y=".9em" font-size="90">⚡</text></svg>'
    return Response(content=svg, media_type="image/svg+xml")




# Stretch Goal 1: POST /api/events (Admin adds a new event)
@app.post(
    "/api/events", response_model=EventOut, status_code=status.HTTP_201_CREATED
)
def create_event(event_in: EventIn):
    """Admin endpoint to create a new event with server-side validation."""
    title = event_in.title.strip() if event_in.title else ""
    category = event_in.category.strip() if event_in.category else ""
    event_date = event_in.event_date.strip() if event_in.event_date else ""
    event_time = event_in.event_time.strip() if event_in.event_time else ""
    venue = event_in.venue.strip() if event_in.venue else ""

    if not title:
        raise HTTPException(status_code=400, detail="Event title is required")
    if not category:
        raise HTTPException(status_code=400, detail="Category is required")
    if not event_date:
        raise HTTPException(status_code=400, detail="Event date is required")
    if not event_time:
        raise HTTPException(status_code=400, detail="Event time is required")
    if not venue:
        raise HTTPException(status_code=400, detail="Venue is required")
    if event_in.capacity <= 0:
        raise HTTPException(
            status_code=400, detail="Capacity must be greater than zero"
        )

    conn = get_db_connection()
    cursor = conn.execute(
        """
        INSERT INTO events (title, category, description, event_date, event_time, venue, capacity, fee, icon)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
        """,
        (
            title,
            category,
            event_in.description.strip() if event_in.description else None,
            event_date,
            event_time,
            venue,
            event_in.capacity,
            event_in.fee,
            event_in.icon.strip() if event_in.icon else "🎉",
        ),
    )
    conn.commit()
    new_id = cursor.lastrowid
    row = conn.execute("SELECT * FROM events WHERE id = ?;", (new_id,)).fetchone()
    res = get_event_with_seats(row, conn)
    conn.close()
    return res


# Stretch Goal 2: DELETE /api/registrations/{registration_id} (Admin cancels registration)
@app.delete("/api/registrations/{registration_id}")
def cancel_registration(registration_id: int):
    """Admin endpoint to cancel/delete a registration."""
    conn = get_db_connection()
    row = conn.execute(
        "SELECT * FROM registrations WHERE id = ?;", (registration_id,)
    ).fetchone()
    if not row:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Registration with ID {registration_id} not found",
        )

    conn.execute("DELETE FROM registrations WHERE id = ?;", (registration_id,))
    conn.commit()
    conn.close()
    return {"message": "Registration cancelled successfully"}


# -----------------------------------------------------------------------------
# Frontend Static Files Serving
# -----------------------------------------------------------------------------


@app.get("/", include_in_schema=False)
def serve_root():
    return FileResponse(FRONTEND_DIR / "index.html")


@app.get("/admin.html", include_in_schema=False)
def serve_admin():
    return FileResponse(FRONTEND_DIR / "admin.html")


# Mount frontend js files and static assets
if (FRONTEND_DIR / "js").exists():
    app.mount("/js", StaticFiles(directory=FRONTEND_DIR / "js"), name="js")

app.mount("/", StaticFiles(directory=FRONTEND_DIR, html=True), name="frontend")
