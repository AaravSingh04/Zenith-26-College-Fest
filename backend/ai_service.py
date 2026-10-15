import os
import time
from typing import Any, Dict, List, Optional
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass
from fastapi import HTTPException, Request
from pydantic import BaseModel, Field

# In-memory IP-based rate limiting (sliding window: max requests per minute)
_RATE_LIMIT_STORE: Dict[str, List[float]] = {}
RATE_LIMIT_MAX_REQUESTS = 20
RATE_LIMIT_WINDOW_SECONDS = 60.0


def check_rate_limit(client_ip: str) -> None:
    """Enforces basic rate limiting per IP address to protect against API abuse."""
    now = time.time()
    timestamps = _RATE_LIMIT_STORE.get(client_ip, [])
    # Remove timestamps older than the window
    valid_timestamps = [t for t in timestamps if now - t < RATE_LIMIT_WINDOW_SECONDS]
    
    if len(valid_timestamps) >= RATE_LIMIT_MAX_REQUESTS:
        raise HTTPException(
            status_code=429,
            detail="Rate limit exceeded. Please wait a minute before making more AI requests."
        )
    
    valid_timestamps.append(now)
    _RATE_LIMIT_STORE[client_ip] = valid_timestamps


def get_openai_client():
    """
    Returns an initialized OpenAI client.
    Reads the key exclusively from the OPENAI_API_KEY environment variable.
    Raises an informative 503 error if the key is not set.
    """
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key or not api_key.strip():
        raise HTTPException(
            status_code=503,
            detail=(
                "OpenAI API key is not configured. "
                "Please configure OPENAI_API_KEY in your server environment settings."
            )
        )
    try:
        from openai import OpenAI
        return OpenAI(api_key=api_key.strip())
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to initialize OpenAI client: {str(e)}"
        )


def handle_openai_exception(e: Exception) -> None:
    """Translates raw OpenAI SDK exceptions into clear, actionable HTTPExceptions."""
    err_msg = str(e)
    err_type = type(e).__name__

    if "invalid_api_key" in err_msg or "Incorrect API key" in err_msg or "AuthenticationError" in err_type:
        raise HTTPException(
            status_code=401,
            detail=(
                "OpenAI Authentication Failed: The API key provided was rejected as invalid or expired by OpenAI. "
                "Please verify that your key is active and copied correctly from https://platform.openai.com/api-keys."
            )
        )
    elif "insufficient_quota" in err_msg or "RateLimitError" in err_type:
        raise HTTPException(
            status_code=429,
            detail=(
                "OpenAI Quota Exceeded: Your OpenAI account has exhausted its available credits or reached its usage limit. "
                "Please check your credit balance at https://platform.openai.com/account/billing."
            )
        )
    raise HTTPException(
        status_code=502,
        detail=f"OpenAI service error: {err_msg}"
    )


# ==============================================================================
# Pydantic Request & Response Schemas
# ==============================================================================

class ChatMessage(BaseModel):
    role: str = Field(pattern="^(user|assistant|system)$")
    content: str = Field(min_length=1, max_length=2000)


class AssistantChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=1000)
    history: Optional[List[ChatMessage]] = Field(default_factory=list)


class AssistantChatResponse(BaseModel):
    response: str
    source_events_count: int


class DescriptionGenRequest(BaseModel):
    title: str = Field(min_length=2, max_length=120)
    category: str = Field(min_length=2, max_length=50)
    keywords: Optional[str] = Field(default="", max_length=300)
    target_audience: Optional[str] = Field(default="College Students", max_length=100)
    tone: Optional[str] = Field(default="Exciting & Engaging", max_length=50)


class DescriptionGenResponse(BaseModel):
    description: str
    promo_caption: str
    social_media_caption: str


class EventPlanRequest(BaseModel):
    title: str = Field(min_length=2, max_length=120)
    category: str = Field(min_length=2, max_length=50)
    expected_attendance: Optional[int] = Field(default=100, ge=10, le=5000)
    venue_type: Optional[str] = Field(default="Campus Auditorium / Hall", max_length=100)
    objectives: Optional[str] = Field(default="", max_length=500)


class EventPlanResponse(BaseModel):
    checklist: Dict[str, List[str]]
    tentative_schedule: List[Dict[str, str]]
    resources_and_equipment: List[str]
    marketing_ideas: List[str]
    estimated_budget: List[Dict[str, Any]]
    disclaimer: str


class AnalyticsInsightsResponse(BaseModel):
    insights: str
    total_events_analyzed: int
    total_registrations_analyzed: int
    data_limitations: str


# ==============================================================================
# Business Logic Implementations
# ==============================================================================

def execute_assistant_chat(req: AssistantChatRequest, db_events: List[Dict[str, Any]]) -> AssistantChatResponse:
    """Answers student questions grounded exclusively in real database event data."""
    client = get_openai_client()

    # Format real database events as ground truth
    events_summary = []
    for ev in db_events:
        seats = ev.get("available_seats", 0)
        cap = ev.get("capacity", 0)
        fee = ev.get("fee", 0)
        fee_str = "Free" if fee == 0 else f"Rs. {fee}"
        status_str = "FULL / SOLD OUT" if seats <= 0 else f"{seats}/{cap} seats available"
        events_summary.append(
            f"• [{ev.get('id')}] {ev.get('icon', '⚡')} {ev.get('title')} ({ev.get('category')})\n"
            f"  Date & Time: {ev.get('event_date')} at {ev.get('event_time')}\n"
            f"  Venue: {ev.get('venue')} | Registration Fee: {fee_str}\n"
            f"  Availability: {status_str}\n"
            f"  Description: {ev.get('description', 'N/A')}"
        )

    context_text = "\n\n".join(events_summary)

    system_prompt = (
        "You are the official Zenith '26 College Fest AI Assistant. "
        "Your role is to help students, participants, and visitors with accurate information about the college fest.\n\n"
        "HERE IS THE VERIFIED, LIVE DATABASE OF COLLEGE FEST EVENTS:\n"
        "--------------------------------------------------\n"
        f"{context_text}\n"
        "--------------------------------------------------\n\n"
        "STRICT GROUNDING RULES:\n"
        "1. Answer using ONLY the verified events data provided above.\n"
        "2. If an event is full or has 0 available seats, inform the user clearly.\n"
        "3. NEVER fabricate, assume, or invent events, venues, dates, or timings that are not in the list.\n"
        "4. If a user asks about an event not listed, politely inform them that it is not scheduled in the official fest itinerary.\n"
        "5. NEVER claim that a user's registration has been processed or confirmed. Explain that they must register through the 'View Details' -> 'Register' form on the portal.\n"
        "6. Keep responses clear, polite, friendly, and well-structured using bullet points when describing multiple events.\n"
        "7. If requested data is missing from the database, explain the limitation honestly."
    )

    messages = [{"role": "system", "content": system_prompt}]

    # Include recent conversation history (up to last 6 messages)
    if req.history:
        for hist in req.history[-6:]:
            messages.append({"role": hist.role, "content": hist.content})

    messages.append({"role": "user", "content": req.message})

    try:
        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=messages,
            temperature=0.3,
            max_tokens=600,
        )
        answer = completion.choices[0].message.content or "No response generated."
        return AssistantChatResponse(
            response=answer,
            source_events_count=len(db_events)
        )
    except Exception as e:
        handle_openai_exception(e)


def execute_description_gen(req: DescriptionGenRequest) -> DescriptionGenResponse:
    """Generates promotional event copy for organizers to review and edit."""
    client = get_openai_client()

    prompt = (
        f"Event Title: {req.title}\n"
        f"Category: {req.category}\n"
        f"Target Audience: {req.target_audience}\n"
        f"Tone: {req.tone}\n"
        f"Key Highlights / Notes: {req.keywords or 'Standard college fest competition/showcase'}\n\n"
        "Please generate 3 pieces of content for college fest organizers:\n"
        "1. Full Event Description: 2-3 engaging, well-written paragraphs suitable for the event website.\n"
        "2. Promo Caption: A punchy 1-2 sentence teaser for flyers/banners.\n"
        "3. Social Media Caption: Ready-to-post Instagram/WhatsApp broadcast caption with relevant fest hashtags (#Zenith26, etc.).\n\n"
        "Return your response in clean JSON format with exactly these three keys: 'description', 'promo_caption', and 'social_media_caption'."
    )

    try:
        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {
                    "role": "system",
                    "content": "You are a creative marketing copywriter specializing in college cultural, technical, and sports festivals. Output valid JSON only."
                },
                {"role": "user", "content": prompt}
            ],
            response_format={"type": "json_object"},
            temperature=0.7,
            max_tokens=750,
        )
        import json
        raw = completion.choices[0].message.content or "{}"
        data = json.loads(raw)
        return DescriptionGenResponse(
            description=data.get("description", "Exciting college fest event."),
            promo_caption=data.get("promo_caption", f"Join us for {req.title} at Zenith '26!"),
            social_media_caption=data.get("social_media_caption", f"Get ready for {req.title}! #Zenith26")
        )
    except Exception as e:
        handle_openai_exception(e)


def execute_event_plan(req: EventPlanRequest) -> EventPlanResponse:
    """Creates a comprehensive, tentative event planning toolkit for organizers."""
    client = get_openai_client()

    prompt = (
        f"Event Title: {req.title}\n"
        f"Category: {req.category}\n"
        f"Expected Attendance: {req.expected_attendance}\n"
        f"Venue Type: {req.venue_type}\n"
        f"Specific Goals / Theme: {req.objectives or 'Successful engagement and seamless coordination'}\n\n"
        "Generate a structured event plan for organizers in valid JSON format with these exact keys:\n"
        "- checklist: object with arrays 'pre_event' (5 tasks), 'event_day' (5 tasks), and 'post_event' (3 tasks).\n"
        "- tentative_schedule: array of objects with 'time_slot' (e.g. '09:00 AM - 09:30 AM') and 'activity'.\n"
        "- resources_and_equipment: array of strings listing required equipment, technical gear, and volunteer roles.\n"
        "- marketing_ideas: array of 4 creative marketing ideas to maximize attendance.\n"
        "- estimated_budget: array of objects with 'item_name', 'estimated_cost_inr' (number or string), and 'note'."
    )

    try:
        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {
                    "role": "system",
                    "content": (
                        "You are an expert college festival organizing consultant. "
                        "Produce realistic, practical, actionable event plans. Output valid JSON only."
                    )
                },
                {"role": "user", "content": prompt}
            ],
            response_format={"type": "json_object"},
            temperature=0.5,
            max_tokens=1100,
        )
        import json
        raw = completion.choices[0].message.content or "{}"
        data = json.loads(raw)
        return EventPlanResponse(
            checklist=data.get("checklist", {
                "pre_event": ["Confirm venue", "Finalize rules", "Assign volunteers"],
                "event_day": ["Setup AV", "Open check-in desk", "Coordinate judges"],
                "post_event": ["Announce winners", "Issue certificates", "Venue cleanup"]
            }),
            tentative_schedule=data.get("tentative_schedule", [
                {"time_slot": "10:00 AM - 10:30 AM", "activity": "Participant Reporting & Kit Distribution"},
                {"time_slot": "10:30 AM - 01:00 PM", "activity": "Main Competition Round"},
                {"time_slot": "01:00 PM - 01:30 PM", "activity": "Evaluation & Valedictory"}
            ]),
            resources_and_equipment=data.get("resources_and_equipment", ["Audio/Mic system", "Registration desk", "Certificates"]),
            marketing_ideas=data.get("marketing_ideas", ["Classroom announcements", "Instagram teaser reels"]),
            estimated_budget=data.get("estimated_budget", [{"item_name": "Prizes / Mementos", "estimated_cost_inr": "5000", "note": "Estimated"}]),
            disclaimer="All budget figures, schedules, and checklists are tentative planning estimates. Final arrangements should be verified with the college fest committee."
        )
    except Exception as e:
        handle_openai_exception(e)


def execute_analytics_insights(events_data: List[Dict[str, Any]], stats_data: Dict[str, Any]) -> AnalyticsInsightsResponse:
    """Analyzes real registration and capacity data to give actionable insights."""
    client = get_openai_client()

    # Prepare privacy-safe, aggregate statistical summary
    total_events = stats_data.get("total_events", len(events_data))
    total_regs = stats_data.get("total_registrations", 0)
    total_cap = stats_data.get("total_capacity", 0)
    util_pct = stats_data.get("overall_utilization_pct", 0.0)
    cat_breakdown = stats_data.get("events_by_category", {})

    high_demand_events = []
    low_demand_events = []
    for ev in events_data:
        cap = ev.get("capacity", 1)
        avail = ev.get("available_seats", cap)
        registered = cap - avail
        rate = (registered / cap) * 100 if cap > 0 else 0
        info = f"{ev.get('title')} ({ev.get('category')}): {registered}/{cap} filled ({rate:.1f}%)"
        if rate >= 90 or avail <= 0:
            high_demand_events.append(info)
        elif rate < 20:
            low_demand_events.append(info)

    data_summary = (
        f"Total Events: {total_events}\n"
        f"Total Capacity: {total_cap}\n"
        f"Total Registrations: {total_regs}\n"
        f"Overall Utilization: {util_pct:.1f}%\n"
        f"Categories: {cat_breakdown}\n"
        f"Events >= 90% Capacity: {', '.join(high_demand_events) if high_demand_events else 'None yet'}\n"
        f"Events < 20% Capacity: {', '.join(low_demand_events[:5]) if low_demand_events else 'None'}\n"
    )

    prompt = (
        "Here is the aggregate verified attendance and registration data for Zenith '26 College Fest:\n\n"
        f"{data_summary}\n\n"
        "Generate a structured, professional executive summary for the fest organizers covering:\n"
        "1. Fest Performance Overview: Key strengths and current momentum.\n"
        "2. High Demand Highlights: Events nearing or at capacity.\n"
        "3. Actionable Recommendations: Specific suggestions to boost turnout in under-registered events/categories.\n"
        "4. Data Limitations Notice: Explicitly note what data is missing (e.g. ticket revenue if fee=0, on-spot vs advance registrations, physical gate check-in attendance).\n\n"
        "Do NOT hallucinate fictional registration numbers or metrics. Use only the exact figures provided above."
    )

    try:
        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {
                    "role": "system",
                    "content": "You are a university analytics consultant. Produce accurate, data-backed insights without inventing figures."
                },
                {"role": "user", "content": prompt}
            ],
            temperature=0.3,
            max_tokens=650,
        )
        answer = completion.choices[0].message.content or "No insights generated."
        return AnalyticsInsightsResponse(
            insights=answer,
            total_events_analyzed=total_events,
            total_registrations_analyzed=total_regs,
            data_limitations="Insights are derived exclusively from advance registration database records. Actual physical footfall and walk-in ticket sales are not tracked in this dataset."
        )
    except Exception as e:
        handle_openai_exception(e)
