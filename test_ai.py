import os
import json
from unittest.mock import MagicMock, patch
from backend.ai_service import (
    execute_assistant_chat,
    execute_description_gen,
    execute_event_plan,
    execute_analytics_insights,
    check_rate_limit,
    AssistantChatRequest,
    DescriptionGenRequest,
    EventPlanRequest,
)

def run_ai_tests():
    print("=== 1. Testing execute_assistant_chat Grounding ===")
    mock_chat_completion = MagicMock()
    mock_chat_completion.choices = [MagicMock()]
    mock_chat_completion.choices[0].message.content = "There are 2 events scheduled: Code Sprint 2.0 and RoboWars."

    with patch("backend.ai_service.get_openai_client") as mock_client:
        client_instance = MagicMock()
        mock_client.return_value = client_instance
        client_instance.chat.completions.create.return_value = mock_chat_completion
        
        events = [
            {"id": 1, "title": "Code Sprint", "category": "Technical", "date": "2026-11-14", "venue": "Lab 1", "available_seats": 50, "capacity": 50, "fee": 0}
        ]
        req = AssistantChatRequest(message="What events are there?")
        resp = execute_assistant_chat(req, events)
        print("Assistant Response:", resp.response)
        assert resp.source_events_count == 1
        assert "Code Sprint" in resp.response

    print("\n=== 2. Testing execute_description_gen JSON Parsing ===")
    mock_desc_completion = MagicMock()
    mock_desc_completion.choices = [MagicMock()]
    mock_desc_completion.choices[0].message.content = json.dumps({
        "description": "An intense coding sprint challenge.",
        "promo_caption": "Ready to test your algorithmic skills? Join now!",
        "social_media_caption": "#Zenith26 #CodeSprint #TechFest"
    })

    with patch("backend.ai_service.get_openai_client") as mock_client:
        client_instance = MagicMock()
        mock_client.return_value = client_instance
        client_instance.chat.completions.create.return_value = mock_desc_completion
        
        req = DescriptionGenRequest(title="Code Sprint", category="Technical")
        resp = execute_description_gen(req)
        print("Description:", resp.description)
        assert resp.promo_caption == "Ready to test your algorithmic skills? Join now!"

    print("\n=== 3. Testing execute_event_plan JSON Parsing ===")
    mock_plan_completion = MagicMock()
    mock_plan_completion.choices = [MagicMock()]
    mock_plan_completion.choices[0].message.content = json.dumps({
        "checklist": {"pre_event": ["Book computer lab", "Arrange volunteer judges"]},
        "tentative_schedule": [{"time_slot": "10:00 AM", "activity": "Opening Ceremony"}],
        "resources_and_equipment": ["High-speed LAN", "Projector", "Extension boards"],
        "marketing_ideas": ["Post on Discord channel", "Print posters for cafeteria"],
        "estimated_budget": [{"item": "Trophies & Certificates", "cost_estimate": "₹3,500"}],
        "disclaimer": "Organizer planning estimate. Review before confirming."
    })

    with patch("backend.ai_service.get_openai_client") as mock_client:
        client_instance = MagicMock()
        mock_client.return_value = client_instance
        client_instance.chat.completions.create.return_value = mock_plan_completion
        
        req = EventPlanRequest(title="RoboWars", category="Technical")
        resp = execute_event_plan(req)
        print("Checklist tasks:", resp.checklist["pre_event"])
        assert len(resp.tentative_schedule) == 1
        assert resp.estimated_budget[0]["cost_estimate"] == "₹3,500"

    print("\n=== 4. Testing execute_analytics_insights Grounding ===")
    mock_insights_completion = MagicMock()
    mock_insights_completion.choices = [MagicMock()]
    mock_insights_completion.choices[0].message.content = "Technical events have reached 92% capacity. Consider opening additional lab sections."

    with patch("backend.ai_service.get_openai_client") as mock_client:
        client_instance = MagicMock()
        mock_client.return_value = client_instance
        client_instance.chat.completions.create.return_value = mock_insights_completion
        
        stats_data = {
            "total_events": 1,
            "total_registrations": 46,
            "total_capacity": 50,
            "overall_utilization_pct": 92.0,
            "events_by_category": {"Technical": 1}
        }
        resp = execute_analytics_insights(events, stats_data)
        print("Insights:", resp.insights)
        assert resp.total_events_analyzed == 1
        assert resp.total_registrations_analyzed == 46

    print("\n=== 5. Testing Sliding Window Rate Limiting ===")
    # 20 requests allowed per minute per IP
    test_ip = "192.168.1.99"
    for i in range(20):
        check_rate_limit(test_ip)
    
    rate_limited = False
    try:
        check_rate_limit(test_ip)
    except Exception as e:
        if getattr(e, "status_code", None) == 429:
            rate_limited = True
    assert rate_limited, "Rate limiting should have raised 429 on 21st request!"
    print("Rate limiting 429 triggered successfully on 21st request within 60s!")

    print("\nALL OPENAI SERVICE LOGIC TESTS PASSED 100%!")

if __name__ == "__main__":
    run_ai_tests()
