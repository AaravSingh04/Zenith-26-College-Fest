import os
import re
import sqlite3
import urllib.parse
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple, Union

# Database file location in the project root for SQLite fallback
BASE_DIR = Path(__file__).resolve().parent.parent

# On Vercel serverless environments, root filesystem is read-only; use /tmp
if os.getenv("VERCEL"):
    DB_PATH = Path("/tmp") / "fest.db"
    ORIGINAL_DB = BASE_DIR / "fest.db"
    if ORIGINAL_DB.exists() and not DB_PATH.exists():
        import shutil
        try:
            shutil.copy2(ORIGINAL_DB, DB_PATH)
        except Exception:
            pass
else:
    DB_PATH = BASE_DIR / "fest.db"

# PostgreSQL connection string from environment (Vercel Postgres, Supabase, Neon, Railway, etc.)
DATABASE_URL = (
    os.getenv("DATABASE_URL")
    or os.getenv("POSTGRES_URL")
    or os.getenv("POSTGRES_PRISMA_URL")
    or os.getenv("POSTGRES_URL_NON_POOLING")
)


def is_postgres() -> bool:
    """Returns True if a PostgreSQL connection URL is configured."""
    url = (
        os.getenv("DATABASE_URL")
        or os.getenv("POSTGRES_URL")
        or os.getenv("POSTGRES_PRISMA_URL")
        or os.getenv("POSTGRES_URL_NON_POOLING")
    )
    return bool(url and ("postgres://" in url or "postgresql://" in url))


class PostgresCursorWrapper:
    """Wraps a psycopg2/pg8000 cursor to behave like a sqlite3 cursor with dictionary rows."""

    def __init__(self, conn, real_cursor):
        self.conn = conn
        self.cursor = real_cursor
        self.lastrowid = None

    def execute(self, query: str, params: Optional[Union[Tuple, List, Dict]] = None):
        sql = query
        # Convert SQLite parameter placeholder (?) to PostgreSQL (%s)
        if params is not None and not isinstance(params, dict):
            sql = sql.replace("?", "%s")

        is_insert = sql.strip().upper().startswith("INSERT INTO")
        has_returning = "RETURNING" in sql.upper()

        if is_insert and not has_returning:
            # Append RETURNING id so cursor.lastrowid is automatically populated
            clean_sql = sql.rstrip("; \t\r\n") + " RETURNING id;"
            if params is not None:
                self.cursor.execute(clean_sql, tuple(params))
            else:
                self.cursor.execute(clean_sql)
            try:
                row = self.cursor.fetchone()
                if row:
                    if isinstance(row, dict):
                        self.lastrowid = row.get("id")
                    elif hasattr(row, "__getitem__"):
                        self.lastrowid = row[0]
            except Exception:
                pass
            return self
        else:
            if params is not None:
                self.cursor.execute(sql, tuple(params))
            else:
                self.cursor.execute(sql)
            return self

    def fetchone(self) -> Optional[Dict[str, Any]]:
        try:
            row = self.cursor.fetchone()
            if row is None:
                return None
            return dict(row)
        except Exception:
            return None

    def fetchall(self) -> List[Dict[str, Any]]:
        try:
            rows = self.cursor.fetchall()
            return [dict(r) for r in rows]
        except Exception:
            return []

    def close(self):
        try:
            self.cursor.close()
        except Exception:
            pass


class PostgresConnectionWrapper:
    """Wraps a PostgreSQL connection to behave like sqlite3.Connection with row_factory."""

    def __init__(self, real_conn):
        self.conn = real_conn

    def cursor(self):
        try:
            import psycopg2.extras
            real_cur = self.conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
        except Exception:
            real_cur = self.conn.cursor()
        return PostgresCursorWrapper(self, real_cur)

    def execute(self, query: str, params: Optional[Union[Tuple, List, Dict]] = None):
        cur = self.cursor()
        return cur.execute(query, params)

    def commit(self):
        self.conn.commit()

    def rollback(self):
        try:
            self.conn.rollback()
        except Exception:
            pass

    def close(self):
        try:
            self.conn.close()
        except Exception:
            pass


def _connect_postgres(url: str):
    """Connect to PostgreSQL using psycopg2 or pg8000."""
    normalized_url = url
    if normalized_url.startswith("postgres://"):
        normalized_url = normalized_url.replace("postgres://", "postgresql://", 1)

    # 1. Try psycopg2
    try:
        import psycopg2
        import psycopg2.extras
        conn = psycopg2.connect(normalized_url)
        return PostgresConnectionWrapper(conn)
    except Exception as e_psycopg:
        # 2. Fallback to pg8000 (pure python)
        try:
            import pg8000
            parsed = urllib.parse.urlparse(normalized_url)
            conn = pg8000.connect(
                user=parsed.username,
                password=parsed.password,
                host=parsed.hostname,
                port=parsed.port or 5432,
                database=parsed.path.lstrip("/"),
                ssl_context=True,
            )
            return PostgresConnectionWrapper(conn)
        except Exception:
            raise e_psycopg


def get_db_connection():
    """Provides a database connection (PostgreSQL if configured in environment, otherwise SQLite)."""
    url = (
        os.getenv("DATABASE_URL")
        or os.getenv("POSTGRES_URL")
        or os.getenv("POSTGRES_PRISMA_URL")
        or os.getenv("POSTGRES_URL_NON_POOLING")
    )
    if url and ("postgres://" in url or "postgresql://" in url):
        return _connect_postgres(url)
    else:
        conn = sqlite3.connect(DB_PATH)
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA foreign_keys = ON;")
        return conn


def init_db() -> None:
    """Initializes the database schema for both PostgreSQL and SQLite."""
    conn = get_db_connection()
    cursor = conn.cursor()

    if is_postgres():
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS events (
                id SERIAL PRIMARY KEY,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT,
                event_date TEXT NOT NULL,
                event_time TEXT NOT NULL,
                venue TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                fee INTEGER NOT NULL DEFAULT 0,
                icon TEXT
            );
            """
        )
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS registrations (
                id SERIAL PRIMARY KEY,
                event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
                student_name TEXT NOT NULL,
                roll_number TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT,
                registered_at TEXT NOT NULL,
                UNIQUE(event_id, email)
            );
            """
        )
    else:
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT,
                event_date TEXT NOT NULL,
                event_time TEXT NOT NULL,
                venue TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                fee INTEGER NOT NULL DEFAULT 0,
                icon TEXT
            );
            """
        )
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS registrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
                student_name TEXT NOT NULL,
                roll_number TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT,
                registered_at TEXT NOT NULL,
                UNIQUE(event_id, email)
            );
            """
        )

    conn.commit()
    conn.close()


if __name__ == "__main__":
    init_db()
    db_type = "PostgreSQL" if is_postgres() else f"SQLite ({DB_PATH})"
    print(f"Database initialized successfully using {db_type}")
