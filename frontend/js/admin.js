/**
 * Zenith '26 College Fest Management System
 * Admin Dashboard JavaScript: Analytics, Event Monitoring, and Registration Management
 */

let allEvents = [];
let allRegistrations = [];
let currentTab = "events"; // 'events' | 'registrations'
let pendingDeleteRegId = null;

// DOM Elements
const adminLoading = document.getElementById("admin-loading");
const adminError = document.getElementById("admin-error");
const adminErrorText = document.getElementById("admin-error-text");
const adminRetryBtn = document.getElementById("admin-retry-btn");
const dashboardContent = document.getElementById("dashboard-content");
const refreshDataBtn = document.getElementById("refresh-data-btn");
const refreshIcon = document.getElementById("refresh-icon");

// Stat Elements
const statTotalEvents = document.getElementById("stat-total-events");
const statTotalRegistrations = document.getElementById("stat-total-registrations");
const statUtilizationPct = document.getElementById("stat-utilization-pct");
const statCapacityRatio = document.getElementById("stat-capacity-ratio");
const statUtilizationBar = document.getElementById("stat-utilization-bar");
const statUpcomingEvents = document.getElementById("stat-upcoming-events");
const categoriesBreakdown = document.getElementById("categories-breakdown");

// Tabs
const tabEventsBtn = document.getElementById("tab-events-btn");
const tabRegistrationsBtn = document.getElementById("tab-registrations-btn");
const tabEventsView = document.getElementById("tab-events-view");
const tabRegistrationsView = document.getElementById("tab-registrations-view");
const badgeTotalRegs = document.getElementById("badge-total-regs");

// Tables
const adminEventsTbody = document.getElementById("admin-events-tbody");
const adminRegistrationsTbody = document.getElementById("admin-registrations-tbody");
const filterRegEvent = document.getElementById("filter-reg-event");
const filteredRegCount = document.getElementById("filtered-reg-count");
const regsEmptyState = document.getElementById("regs-empty-state");
const regsTableContainer = document.getElementById("regs-table-container");

// Add Event Modal
const openAddEventBtn = document.getElementById("open-add-event-btn");
const addEventModal = document.getElementById("add-event-modal");
const closeAddEventBtn = document.getElementById("close-add-event-btn");
const addEventForm = document.getElementById("add-event-form");
const addEventAlert = document.getElementById("add-event-alert");

// Delete Reg Modal
const deleteRegModal = document.getElementById("delete-reg-modal");
const cancelDeleteBtn = document.getElementById("cancel-delete-btn");
const confirmDeleteBtn = document.getElementById("confirm-delete-btn");

// Toast
const adminToast = document.getElementById("admin-toast");
const adminToastIcon = document.getElementById("admin-toast-icon");
const adminToastText = document.getElementById("admin-toast-text");

// Category badge colors
const categoryBadgeClasses = {
  Technical: "bg-blue-50 text-blue-700 border-blue-200",
  Cultural: "bg-pink-50 text-pink-700 border-pink-200",
  Sports: "bg-emerald-50 text-emerald-700 border-emerald-200",
  Gaming: "bg-purple-50 text-purple-700 border-purple-200",
  Workshop: "bg-amber-50 text-amber-800 border-amber-200",
};

document.addEventListener("DOMContentLoaded", () => {
  setupEventListeners();
  loadAllAdminData();
  setupAiAdminFeatures();
});

function setupEventListeners() {
  refreshDataBtn.addEventListener("click", () => {
    rotateRefreshIcon();
    loadAllAdminData();
  });
  adminRetryBtn.addEventListener("click", loadAllAdminData);

  // Tab switching
  tabEventsBtn.addEventListener("click", () => switchTab("events"));
  tabRegistrationsBtn.addEventListener("click", () => switchTab("registrations"));

  // Event dropdown filter for registrations
  filterRegEvent.addEventListener("change", (e) => {
    loadRegistrations(e.target.value ? parseInt(e.target.value, 10) : null);
  });

  // Add Event Modal handlers
  openAddEventBtn.addEventListener("click", () => {
    addEventAlert.classList.add("hidden");
    addEventForm.reset();
    addEventModal.classList.remove("hidden");
  });
  closeAddEventBtn.addEventListener("click", () => {
    addEventModal.classList.add("hidden");
  });
  addEventModal.addEventListener("click", (e) => {
    if (e.target === addEventModal) addEventModal.classList.add("hidden");
  });
  addEventForm.addEventListener("submit", handleAddEventSubmit);

  // Delete Registration Modal handlers
  cancelDeleteBtn.addEventListener("click", () => {
    deleteRegModal.classList.add("hidden");
    pendingDeleteRegId = null;
  });
  confirmDeleteBtn.addEventListener("click", handleConfirmDeleteReg);
}

function rotateRefreshIcon() {
  refreshIcon.classList.add("animate-spin");
  setTimeout(() => refreshIcon.classList.remove("animate-spin"), 800);
}

function switchTab(tab) {
  currentTab = tab;
  if (tab === "events") {
    tabEventsBtn.className = "border-b-2 border-indigo-600 pb-3 text-sm font-bold text-indigo-600 transition-colors flex items-center space-x-2";
    tabRegistrationsBtn.className = "border-b-2 border-transparent pb-3 text-sm font-bold text-slate-500 hover:text-slate-800 transition-colors flex items-center space-x-2";
    tabEventsView.classList.remove("hidden");
    tabRegistrationsView.classList.add("hidden");
  } else {
    tabEventsBtn.className = "border-b-2 border-transparent pb-3 text-sm font-bold text-slate-500 hover:text-slate-800 transition-colors flex items-center space-x-2";
    tabRegistrationsBtn.className = "border-b-2 border-indigo-600 pb-3 text-sm font-bold text-indigo-600 transition-colors flex items-center space-x-2";
    tabEventsView.classList.add("hidden");
    tabRegistrationsView.classList.remove("hidden");
  }
}

/**
 * Load all dashboard data concurrently
 */
async function loadAllAdminData() {
  adminLoading.classList.remove("hidden");
  adminError.classList.add("hidden");
  dashboardContent.classList.add("hidden");

  try {
    const [statsRes, eventsRes, regsRes] = await Promise.all([
      fetch("/api/stats"),
      fetch("/api/events"),
      fetch("/api/registrations"),
    ]);

    if (!statsRes.ok || !eventsRes.ok || !regsRes.ok) {
      throw new Error("One or more dashboard endpoints failed to respond.");
    }

    const stats = await statsRes.json();
    const events = await eventsRes.json();
    const registrations = await regsRes.json();

    allEvents = events;
    allRegistrations = registrations;

    renderStats(stats);
    renderCategoryBreakdown(stats.events_by_category);
    renderEventsTable(events);
    populateEventFilterDropdown(events);
    renderRegistrationsTable(registrations);

    adminLoading.classList.add("hidden");
    dashboardContent.classList.remove("hidden");
  } catch (err) {
    console.error("Admin dashboard fetch error:", err);
    adminErrorText.textContent = err.message || "Failed to load admin analytics.";
    adminLoading.classList.add("hidden");
    adminError.classList.remove("hidden");
  }
}

/**
 * Render Core Stat Cards
 */
function renderStats(stats) {
  statTotalEvents.textContent = stats.total_events;
  statTotalRegistrations.textContent = stats.total_registrations;
  statUtilizationPct.textContent = `${stats.overall_utilization_pct}%`;
  statCapacityRatio.textContent = `${stats.total_registrations} / ${stats.total_capacity}`;
  statUtilizationBar.style.width = `${Math.min(100, stats.overall_utilization_pct)}%`;
  statUpcomingEvents.textContent = stats.upcoming_events_count;
  badgeTotalRegs.textContent = stats.total_registrations;
}

/**
 * Render Category Breakdown
 */
function renderCategoryBreakdown(catDict) {
  categoriesBreakdown.innerHTML = "";
  const categories = Object.keys(catDict);

  if (categories.length === 0) {
    categoriesBreakdown.innerHTML = `<p class="text-xs text-slate-400">No category data available.</p>`;
    return;
  }

  categories.forEach((cat) => {
    const count = catDict[cat];
    const badgeColor = categoryBadgeClasses[cat] || "bg-slate-50 text-slate-700 border-slate-200";

    const card = document.createElement("div");
    card.className = `p-3 rounded-xl border ${badgeColor} flex flex-col justify-between`;
    card.innerHTML = `
      <span class="text-xs font-bold">${cat}</span>
      <div class="mt-2 flex items-baseline justify-between">
        <span class="text-xl font-black">${count}</span>
        <span class="text-[10px] uppercase font-semibold opacity-75">${count === 1 ? "event" : "events"}</span>
      </div>
    `;
    categoriesBreakdown.appendChild(card);
  });
}

/**
 * Render Events Table (sorted by utilization descending, highlighting > 90%)
 */
function renderEventsTable(events) {
  adminEventsTbody.innerHTML = "";

  // Sort by utilization percentage descending
  const sortedEvents = [...events].sort((a, b) => {
    const utilA = a.capacity > 0 ? ((a.capacity - a.available_seats) / a.capacity) * 100 : 0;
    const utilB = b.capacity > 0 ? ((b.capacity - b.available_seats) / b.capacity) * 100 : 0;
    return utilB - utilA;
  });

  sortedEvents.forEach((ev) => {
    const registered = ev.capacity - ev.available_seats;
    const utilPct = ev.capacity > 0 ? Math.round((registered / ev.capacity) * 100) : 0;
    const isCritical = utilPct >= 90;
    const isFull = ev.available_seats === 0;
    const catBadge = categoryBadgeClasses[ev.category] || "bg-slate-50 text-slate-700 border-slate-200";

    const tr = document.createElement("tr");
    tr.className = `hover:bg-slate-50/80 transition-colors ${isCritical ? "bg-amber-50/40" : ""}`;

    tr.innerHTML = `
      <td class="py-3.5 px-4 font-bold text-slate-900">
        <div class="flex items-center space-x-2">
          <span class="text-lg">${ev.icon || "⚡"}</span>
          <span>${escapeHtml(ev.title)}</span>
        </div>
      </td>
      <td class="py-3.5 px-4">
        <span class="inline-block px-2 py-0.5 rounded text-[11px] font-bold border ${catBadge}">
          ${ev.category}
        </span>
      </td>
      <td class="py-3.5 px-4 text-xs text-slate-600">
        ${ev.event_date} <br><span class="text-slate-400">${ev.event_time}</span>
      </td>
      <td class="py-3.5 px-4 text-xs text-slate-600">
        ${escapeHtml(ev.venue)}
      </td>
      <td class="py-3.5 px-4 text-center font-bold text-xs text-slate-800">
        ${registered} / ${ev.capacity}
      </td>
      <td class="py-3.5 px-4 text-center">
        <div class="inline-flex items-center space-x-1.5">
          <span class="font-extrabold text-xs ${
            isCritical ? "text-rose-600" : utilPct > 50 ? "text-indigo-600" : "text-slate-700"
          }">
            ${utilPct}%
          </span>
          <div class="w-12 bg-slate-200 rounded-full h-1.5 overflow-hidden hidden sm:block">
            <div class="${isCritical ? "bg-rose-500" : "bg-indigo-600"} h-1.5 rounded-full" style="width: ${utilPct}%"></div>
          </div>
        </div>
      </td>
      <td class="py-3.5 px-4 text-right">
        ${
          isFull
            ? `<span class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-black bg-rose-600 text-white shadow-sm">
                FULL
               </span>`
            : isCritical
            ? `<span class="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-black bg-amber-500 text-white shadow-sm animate-pulse">
                &gt;90% ALMOST FULL
               </span>`
            : `<span class="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                Available
               </span>`
        }
      </td>
    `;
    adminEventsTbody.appendChild(tr);
  });
}

/**
 * Populate Event Filter Dropdown
 */
function populateEventFilterDropdown(events) {
  const currentVal = filterRegEvent.value;
  filterRegEvent.innerHTML = `<option value="">All Events (${allRegistrations.length})</option>`;
  events.forEach((ev) => {
    const opt = document.createElement("option");
    opt.value = ev.id;
    opt.textContent = `${ev.icon || ""} ${ev.title}`;
    filterRegEvent.appendChild(opt);
  });
  if (currentVal) filterRegEvent.value = currentVal;
}

/**
 * Fetch registrations with optional event_id filter
 */
async function loadRegistrations(eventId = null) {
  try {
    const url = eventId ? `/api/registrations?event_id=${eventId}` : "/api/registrations";
    const res = await fetch(url);
    if (!res.ok) throw new Error("Failed to fetch registrations");
    const data = await res.json();
    renderRegistrationsTable(data);
  } catch (err) {
    showAdminToast("⚠️", "Could not filter registrations");
  }
}

/**
 * Render Registrations Table (with delete registration action)
 */
function renderRegistrationsTable(registrations) {
  adminRegistrationsTbody.innerHTML = "";
  filteredRegCount.textContent = `Showing ${registrations.length} ${
    registrations.length === 1 ? "registration" : "registrations"
  }`;

  if (registrations.length === 0) {
    regsEmptyState.classList.remove("hidden");
    regsTableContainer.classList.add("hidden");
    return;
  }

  regsEmptyState.classList.add("hidden");
  regsTableContainer.classList.remove("hidden");

  registrations.forEach((reg) => {
    const tr = document.createElement("tr");
    tr.className = "hover:bg-slate-50/80 transition-colors";
    tr.innerHTML = `
      <td class="py-3 px-4 font-bold text-slate-900">${escapeHtml(reg.student_name)}</td>
      <td class="py-3 px-4 text-xs font-mono text-slate-600">${escapeHtml(reg.roll_number)}</td>
      <td class="py-3 px-4 text-xs text-slate-600">${escapeHtml(reg.email)}</td>
      <td class="py-3 px-4 text-xs text-slate-500">${escapeHtml(reg.phone || "—")}</td>
      <td class="py-3 px-4 text-xs font-semibold text-indigo-700">${escapeHtml(reg.event_title)}</td>
      <td class="py-3 px-4 text-xs text-slate-400 font-mono">${escapeHtml(reg.registered_at)}</td>
      <td class="py-3 px-4 text-center">
        <button 
          data-reg-id="${reg.id}" 
          class="delete-reg-btn text-xs font-bold text-rose-600 hover:text-rose-800 bg-rose-50 hover:bg-rose-100 px-2.5 py-1 rounded-lg border border-rose-200 transition-all active:scale-95"
        >
          Cancel
        </button>
      </td>
    `;
    adminRegistrationsTbody.appendChild(tr);
  });

  // Attach delete buttons
  adminRegistrationsTbody.querySelectorAll(".delete-reg-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const regId = parseInt(btn.getAttribute("data-reg-id"), 10);
      promptDeleteRegistration(regId);
    });
  });
}

/**
 * Stretch Goal 1: Handle Admin Adding a New Event
 */
async function handleAddEventSubmit(e) {
  e.preventDefault();
  addEventAlert.classList.add("hidden");

  const title = document.getElementById("new-event-title").value.trim();
  const category = document.getElementById("new-event-category").value;
  const icon = document.getElementById("new-event-icon").value.trim() || "🎉";
  const description = document.getElementById("new-event-desc").value.trim();
  const event_date = document.getElementById("new-event-date").value.trim();
  const event_time = document.getElementById("new-event-time").value.trim();
  const venue = document.getElementById("new-event-venue").value.trim();
  const capacity = parseInt(document.getElementById("new-event-capacity").value, 10);
  const fee = parseInt(document.getElementById("new-event-fee").value, 10) || 0;

  if (!title || !category || !event_date || !event_time || !venue || isNaN(capacity) || capacity <= 0) {
    addEventAlert.textContent = "Please fill in all required fields and ensure capacity is greater than 0.";
    addEventAlert.classList.remove("hidden");
    return;
  }

  try {
    const res = await fetch("/api/events", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        title,
        category,
        icon,
        description,
        event_date,
        event_time,
        venue,
        capacity,
        fee,
      }),
    });

    const data = await res.json();
    if (res.status === 201) {
      addEventModal.classList.add("hidden");
      showAdminToast("🎉", `Event '${title}' created successfully!`);
      await loadAllAdminData();
    } else {
      addEventAlert.textContent = data.detail || "Failed to create event.";
      addEventAlert.classList.remove("hidden");
    }
  } catch (err) {
    addEventAlert.textContent = "Network error. Could not connect to API.";
    addEventAlert.classList.remove("hidden");
  }
}

/**
 * Stretch Goal 2: Handle Cancel Registration
 */
function promptDeleteRegistration(regId) {
  pendingDeleteRegId = regId;
  deleteRegModal.classList.remove("hidden");
}

async function handleConfirmDeleteReg() {
  if (!pendingDeleteRegId) return;

  try {
    const res = await fetch(`/api/registrations/${pendingDeleteRegId}`, {
      method: "DELETE",
    });

    if (res.ok) {
      deleteRegModal.classList.add("hidden");
      showAdminToast("🗑️", `Registration #${pendingDeleteRegId} cancelled.`);
      pendingDeleteRegId = null;
      await loadAllAdminData();
    } else {
      const err = await res.json();
      showAdminToast("⚠️", err.detail || "Failed to cancel registration.");
      deleteRegModal.classList.add("hidden");
    }
  } catch (err) {
    showAdminToast("❌", "Network error when cancelling registration.");
    deleteRegModal.classList.add("hidden");
  }
}

function showAdminToast(icon, message) {
  adminToastIcon.textContent = icon;
  adminToastText.textContent = message;
  adminToast.classList.remove("hidden", "translate-y-4");
  adminToast.classList.add("translate-y-0");

  setTimeout(() => {
    adminToast.classList.add("translate-y-4");
    setTimeout(() => adminToast.classList.add("hidden"), 300);
  }, 3000);
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

// ==============================================================================
// Zenith AI Admin Features (OpenAI Integration)
// ==============================================================================
let latestGeneratedPlan = null;

function setupAiAdminFeatures() {
  const runInsightsBtn = document.getElementById("run-ai-insights-btn");
  const insightsBtnIcon = document.getElementById("ai-insights-btn-icon");
  const insightsBtnText = document.getElementById("ai-insights-btn-text");
  const insightsContainer = document.getElementById("ai-insights-container");

  const openPlannerBtn = document.getElementById("open-ai-planner-btn");
  const plannerModal = document.getElementById("ai-planner-modal");
  const closePlannerBtn = document.getElementById("close-ai-planner-btn");
  const plannerForm = document.getElementById("ai-planner-form");
  const plannerResults = document.getElementById("ai-planner-results");
  const planSpinner = document.getElementById("ai-plan-spinner");
  const planBtnText = document.getElementById("ai-plan-btn-text");
  const planSubmitBtn = document.getElementById("ai-plan-submit-btn");
  const planCopyToFormBtn = document.getElementById("ai-plan-copy-to-form-btn");

  const draftDescBtn = document.getElementById("ai-generate-desc-btn");
  const descStatus = document.getElementById("ai-desc-status");
  const newEventTitle = document.getElementById("new-event-title");
  const newEventCategory = document.getElementById("new-event-category");
  const newEventDesc = document.getElementById("new-event-desc");

  // --- 1. AI Analytics & Insights ---
  if (runInsightsBtn && insightsContainer) {
    runInsightsBtn.addEventListener("click", async () => {
      insightsBtnIcon.textContent = "⏳";
      insightsBtnText.textContent = "Analyzing metrics...";
      runInsightsBtn.disabled = true;

      try {
        const res = await fetch("/api/ai/analytics-insights", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
        });

        if (res.ok) {
          const data = await res.json();
          const formatted = escapeHtml(data.insights)
            .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
            .replace(/\n/g, "<br/>");

          insightsContainer.innerHTML = `
            <div class="space-y-2 w-full animate-fade-in">
              <div class="leading-relaxed text-slate-200">
                ${formatted}
              </div>
              <div class="pt-2 mt-2 border-t border-white/10 flex flex-wrap items-center justify-between gap-2 text-[10px] text-slate-400">
                <span>Verified: ${data.total_events_analyzed} events, ${data.total_registrations_analyzed} registrations</span>
                <span class="italic">${escapeHtml(data.data_limitations)}</span>
              </div>
            </div>
          `;
          showAdminToast("✨", "AI Insights generated successfully.");
        } else {
          const err = await res.json().catch(() => ({ detail: "Unknown error" }));
          let msg = err.detail || "Failed to generate AI insights.";
          if (res.status === 503) {
            msg = "⚠️ OPENAI_API_KEY is not configured in Vercel / server environment.";
          }
          insightsContainer.innerHTML = `
            <div class="text-rose-400 p-2">
              <strong>Error:</strong> ${escapeHtml(msg)}
            </div>
          `;
          showAdminToast("⚠️", msg);
        }
      } catch (err) {
        insightsContainer.innerHTML = `<p class="text-rose-400">Network error while contacting AI service.</p>`;
        showAdminToast("❌", "Network error when requesting AI insights.");
      } finally {
        insightsBtnIcon.textContent = "⚡";
        insightsBtnText.textContent = "Generate AI Analysis";
        runInsightsBtn.disabled = false;
      }
    });
  }

  // --- 2. AI Event Description Generator ---
  if (draftDescBtn && newEventDesc) {
    draftDescBtn.addEventListener("click", async () => {
      const title = newEventTitle.value.trim();
      const category = newEventCategory.value;

      if (!title) {
        descStatus.className = "text-[11px] font-semibold text-rose-600 mt-1 block";
        descStatus.textContent = "⚠️ Please enter an Event Title first to generate a description.";
        newEventTitle.focus();
        return;
      }

      draftDescBtn.disabled = true;
      descStatus.className = "text-[11px] font-medium text-indigo-600 mt-1 flex items-center gap-1.5";
      descStatus.innerHTML = `
        <span class="inline-block animate-spin rounded-full h-3 w-3 border-2 border-indigo-600 border-t-transparent"></span>
        Generating description with OpenAI...
      `;
      descStatus.classList.remove("hidden");

      try {
        const res = await fetch("/api/ai/generate-description", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            title: title,
            category: category,
            target_audience: "College Fest Participants",
            tone: "Energetic and professional",
          }),
        });

        if (res.ok) {
          const data = await res.json();
          newEventDesc.value = data.description;
          descStatus.className = "text-[11px] font-semibold text-emerald-600 mt-1 block";
          descStatus.textContent = "✨ Draft generated! You can review and edit it before creating the event.";
          showAdminToast("✨", "AI Description draft generated.");
        } else {
          const err = await res.json().catch(() => ({ detail: "Unknown error" }));
          let msg = err.detail || "Failed to generate description.";
          if (res.status === 503) {
            msg = "OPENAI_API_KEY is not configured in server environment.";
          }
          descStatus.className = "text-[11px] font-semibold text-rose-600 mt-1 block";
          descStatus.textContent = `⚠️ ${msg}`;
        }
      } catch (err) {
        descStatus.className = "text-[11px] font-semibold text-rose-600 mt-1 block";
        descStatus.textContent = "❌ Network error contacting AI generator.";
      } finally {
        draftDescBtn.disabled = false;
      }
    });
  }

  // --- 3. AI Event Planner Modal ---
  if (openPlannerBtn && plannerModal) {
    openPlannerBtn.addEventListener("click", () => {
      plannerModal.classList.remove("hidden");
    });

    closePlannerBtn?.addEventListener("click", () => {
      plannerModal.classList.add("hidden");
    });

    plannerModal.addEventListener("click", (e) => {
      if (e.target === plannerModal) plannerModal.classList.add("hidden");
    });

    plannerForm?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const title = document.getElementById("ai-plan-title").value.trim();
      const category = document.getElementById("ai-plan-category").value;
      const attendance = parseInt(document.getElementById("ai-plan-attendance").value, 10) || 100;
      const venue = document.getElementById("ai-plan-venue").value.trim();
      const objectives = document.getElementById("ai-plan-objectives").value.trim();

      planSpinner.classList.remove("hidden");
      planBtnText.textContent = "Synthesizing Event Plan with OpenAI...";
      planSubmitBtn.disabled = true;

      try {
        const res = await fetch("/api/ai/plan-event", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            title,
            category,
            expected_attendance: attendance,
            venue_type: venue,
            objectives,
          }),
        });

        if (res.ok) {
          const plan = await res.json();
          latestGeneratedPlan = {
            title,
            category,
            venue,
            capacity: attendance,
            plan,
          };
          renderAiPlan(plan);
          plannerResults.classList.remove("hidden");
          showAdminToast("📋", "AI Event Plan generated!");
        } else {
          const err = await res.json().catch(() => ({ detail: "Unknown error" }));
          let msg = err.detail || "Failed to generate event plan.";
          if (res.status === 503) {
            msg = "OPENAI_API_KEY is not configured in server environment.";
          }
          showAdminToast("⚠️", msg);
        }
      } catch (err) {
        showAdminToast("❌", "Network error when requesting event plan.");
      } finally {
        planSpinner.classList.add("hidden");
        planBtnText.textContent = "Generate Comprehensive Event Plan";
        planSubmitBtn.disabled = false;
      }
    });

    // Copy to Add Event Form handoff
    planCopyToFormBtn?.addEventListener("click", () => {
      if (!latestGeneratedPlan) return;

      plannerModal.classList.add("hidden");
      addEventModal.classList.remove("hidden");

      newEventTitle.value = latestGeneratedPlan.title;
      newEventCategory.value = latestGeneratedPlan.category;
      document.getElementById("new-event-venue").value = latestGeneratedPlan.venue || "Auditorium";
      document.getElementById("new-event-capacity").value = latestGeneratedPlan.capacity || 100;

      if (latestGeneratedPlan.plan?.marketing_ideas?.length) {
        newEventDesc.value = latestGeneratedPlan.plan.marketing_ideas[0];
      }

      showAdminToast("✨", "Plan details copied into Add Event form for review!");
    });
  }

  function renderAiPlan(plan) {
    const checklistEl = document.getElementById("ai-plan-checklist");
    const scheduleEl = document.getElementById("ai-plan-schedule");
    const resourcesEl = document.getElementById("ai-plan-resources");
    const budgetEl = document.getElementById("ai-plan-budget");
    const marketingEl = document.getElementById("ai-plan-marketing");
    const disclaimerEl = document.getElementById("ai-plan-disclaimer");

    // Checklist
    if (checklistEl && plan.checklist) {
      checklistEl.innerHTML = Object.entries(plan.checklist)
        .map(([phase, tasks]) => `
          <div class="mb-2">
            <strong class="text-slate-800 uppercase text-[10px] tracking-wider block mb-1">${escapeHtml(phase.replace(/_/g, " "))}</strong>
            <ul class="space-y-1">
              ${(Array.isArray(tasks) ? tasks : []).map(t => `<li class="flex items-center gap-1.5 text-slate-600"><span>◽</span> ${escapeHtml(t)}</li>`).join("")}
            </ul>
          </div>
        `).join("");
    }

    // Schedule
    if (scheduleEl && Array.isArray(plan.tentative_schedule)) {
      scheduleEl.innerHTML = plan.tentative_schedule
        .map(s => `
          <div class="flex items-start justify-between py-1 border-b border-slate-200/60 last:border-0">
            <span class="font-bold text-slate-700 w-24 shrink-0">${escapeHtml(s.time_slot || "")}</span>
            <span class="text-slate-600 flex-1">${escapeHtml(s.activity || "")}</span>
          </div>
        `).join("");
    }

    // Resources
    if (resourcesEl && Array.isArray(plan.resources_and_equipment)) {
      resourcesEl.innerHTML = plan.resources_and_equipment
        .map(r => `<li>${escapeHtml(r)}</li>`)
        .join("");
    }

    // Budget
    if (budgetEl && Array.isArray(plan.estimated_budget)) {
      budgetEl.innerHTML = plan.estimated_budget
        .map(b => `
          <div class="flex items-center justify-between py-1 border-b border-slate-200/60 last:border-0">
            <span class="text-slate-700">${escapeHtml(b.item || "")}</span>
            <span class="font-bold text-slate-900">${escapeHtml(b.cost_estimate || "")}</span>
          </div>
        `).join("");
    }

    // Marketing
    if (marketingEl && Array.isArray(plan.marketing_ideas)) {
      marketingEl.innerHTML = plan.marketing_ideas
        .map(m => `<p class="mb-1">📢 ${escapeHtml(m)}</p>`)
        .join("");
    }

    // Disclaimer
    if (disclaimerEl) {
      disclaimerEl.textContent = plan.disclaimer || "Note: This is an AI-generated draft to assist organizers. Checklists and budgets are editable estimates.";
    }
  }
}

