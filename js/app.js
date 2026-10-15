/**
 * Zenith '26 College Fest Management System
 * Frontend Application Script: Student Event Portal
 */

let currentEvents = [];
let selectedCategory = "All";
let searchQuery = "";
let searchDebounceTimeout = null;

// Category Badge Color Palette with Vibrant Modern Gradients
const categoryColors = {
  Technical: "bg-cyan-50 text-cyan-800 border-cyan-200",
  Cultural: "bg-pink-50 text-pink-800 border-pink-200",
  Sports: "bg-emerald-50 text-emerald-800 border-emerald-200",
  Gaming: "bg-purple-50 text-purple-800 border-purple-200",
  Workshop: "bg-amber-50 text-amber-900 border-amber-200",
};

const categoryAccents = {
  Technical: "from-cyan-500 via-blue-500 to-indigo-600",
  Cultural: "from-pink-500 via-rose-500 to-purple-600",
  Sports: "from-emerald-400 via-teal-500 to-cyan-600",
  Gaming: "from-violet-500 via-purple-600 to-pink-500",
  Workshop: "from-amber-400 via-orange-500 to-rose-500",
};

// DOM Elements
const eventsGrid = document.getElementById("events-grid");
const loadingState = document.getElementById("loading-state");
const errorState = document.getElementById("error-state");
const errorMessage = document.getElementById("error-message");
const emptyState = document.getElementById("empty-state");
const retryBtn = document.getElementById("retry-btn");
const searchInput = document.getElementById("search-input");
const clearSearchBtn = document.getElementById("clear-search-btn");
const resultsCount = document.getElementById("results-count");
const resetAllBtn = document.getElementById("reset-all-btn");
const clearFiltersBtn = document.getElementById("clear-filters-btn");
const categoryButtons = document.querySelectorAll(".filter-btn");

// Modal Elements
const eventModal = document.getElementById("event-modal");
const closeModalBtn = document.getElementById("close-modal-btn");
const registrationForm = document.getElementById("registration-form");
const formEventId = document.getElementById("form-event-id");
const modalIcon = document.getElementById("modal-icon");
const modalTitle = document.getElementById("modal-title");
const modalCategory = document.getElementById("modal-category");
const modalDate = document.getElementById("modal-date");
const modalTime = document.getElementById("modal-time");
const modalVenue = document.getElementById("modal-venue");
const modalFee = document.getElementById("modal-fee");
const modalDescription = document.getElementById("modal-description");
const modalSeatsBadge = document.getElementById("modal-seats-badge");
const modalSeatsProgress = document.getElementById("modal-seats-progress");
const modalAlert = document.getElementById("modal-alert");
const alertIcon = document.getElementById("alert-icon");
const alertText = document.getElementById("alert-text");
const submitRegBtn = document.getElementById("submit-reg-btn");
const btnSpinner = document.getElementById("btn-spinner");
const btnText = document.getElementById("btn-text");

// Form Inputs
const inputName = document.getElementById("student-name");
const inputRoll = document.getElementById("student-roll");
const inputEmail = document.getElementById("student-email");
const inputPhone = document.getElementById("student-phone");

// Inline error elements
const errorName = document.getElementById("error-student-name");
const errorRoll = document.getElementById("error-student-roll");
const errorEmail = document.getElementById("error-student-email");

// Toast
const toast = document.getElementById("toast");
const toastIcon = document.getElementById("toast-icon");
const toastMessage = document.getElementById("toast-message");

// Initialize on Load
document.addEventListener("DOMContentLoaded", () => {
  setupEventListeners();
  fetchEvents();
  setupAiAssistant();
});

function setupEventListeners() {
  // Category buttons
  categoryButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      const category = btn.getAttribute("data-category");
      setCategory(category);
    });
  });

  // Search input with debounce
  searchInput.addEventListener("input", (e) => {
    clearTimeout(searchDebounceTimeout);
    searchQuery = e.target.value.trim();
    clearSearchBtn.classList.toggle("hidden", searchQuery.length === 0);

    searchDebounceTimeout = setTimeout(() => {
      fetchEvents();
    }, 280);
  });

  clearSearchBtn.addEventListener("click", () => {
    searchInput.value = "";
    searchQuery = "";
    clearSearchBtn.classList.add("hidden");
    fetchEvents();
  });

  resetAllBtn.addEventListener("click", resetFilters);
  clearFiltersBtn.addEventListener("click", resetFilters);
  retryBtn.addEventListener("click", () => fetchEvents());

  // Modal close handlers
  closeModalBtn.addEventListener("click", closeModal);
  eventModal.addEventListener("click", (e) => {
    if (e.target === eventModal) closeModal();
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !eventModal.classList.contains("hidden")) {
      closeModal();
    }
  });

  // Form submit
  registrationForm.addEventListener("submit", handleRegistrationSubmit);
}

function setCategory(category) {
  selectedCategory = category;
  categoryButtons.forEach((btn) => {
    const isCurrent = btn.getAttribute("data-category") === category;
    if (isCurrent) {
      btn.className = "filter-btn active-category px-3.5 py-1.5 rounded-xl text-xs sm:text-sm font-semibold transition-all shadow-sm whitespace-nowrap bg-indigo-600 text-white";
    } else {
      btn.className = "filter-btn px-3.5 py-1.5 rounded-xl text-xs sm:text-sm font-semibold transition-all whitespace-nowrap bg-slate-100 hover:bg-slate-200 text-slate-700";
    }
  });
  fetchEvents();
}

function resetFilters() {
  searchInput.value = "";
  searchQuery = "";
  clearSearchBtn.classList.add("hidden");
  setCategory("All");
}

/**
 * Fetches events from GET /api/events with query parameters
 */
async function fetchEvents() {
  showLoading(true);
  hideError();
  hideEmpty();

  const params = new URLSearchParams();
  if (selectedCategory && selectedCategory !== "All") {
    params.append("category", selectedCategory);
  }
  if (searchQuery) {
    params.append("search", searchQuery);
  }

  const url = `/api/events${params.toString() ? "?" + params.toString() : ""}`;

  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Server responded with HTTP ${response.status}`);
    }
    const events = await response.json();
    currentEvents = events;
    renderEvents(events);
    updateMetrics(events);
  } catch (err) {
    console.error("Error fetching events:", err);
    showError(err.message || "Failed to load events from the server.");
  } finally {
    showLoading(false);
  }
}

function updateMetrics(events) {
  // Update result count text
  const count = events.length;
  resultsCount.textContent = `Showing ${count} ${count === 1 ? "event" : "events"}${
    selectedCategory !== "All" ? ` in ${selectedCategory}` : ""
  }${searchQuery ? ` matching "${searchQuery}"` : ""}`;

  const hasFilterActive = selectedCategory !== "All" || searchQuery.length > 0;
  resetAllBtn.classList.toggle("hidden", !hasFilterActive);

  // Update hero counter if viewing all
  if (selectedCategory === "All" && !searchQuery) {
    const heroCount = document.getElementById("hero-events-count");
    if (heroCount) heroCount.textContent = count;
  }
}

/**
 * Render Event Cards inside #events-grid
 */
function renderEvents(events) {
  eventsGrid.innerHTML = "";

  if (events.length === 0) {
    showEmpty();
    return;
  }

  events.forEach((event, idx) => {
    const isFull = event.available_seats <= 0;
    const catColor = categoryColors[event.category] || "bg-slate-100 text-slate-700 border-slate-200";
    const catAccent = categoryAccents[event.category] || "from-indigo-500 to-purple-600";
    const feeText = event.fee > 0 ? `₹${event.fee}` : "Free";
    const seatsRatio = `${event.available_seats} / ${event.capacity} seats left`;
    const fillPct = Math.min(100, Math.round(((event.capacity - event.available_seats) / event.capacity) * 100));

    const card = document.createElement("div");
    card.className = `group bg-white rounded-2xl border ${
      isFull ? "border-slate-300 opacity-95" : "border-slate-200/90 hover:border-indigo-400"
    } shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 flex flex-col overflow-hidden animate-fade-in`;
    card.style.animationDelay = `${idx * 40}ms`;

    card.innerHTML = `
      <!-- Top Colorful Category Stripe -->
      <div class="h-1.5 w-full bg-gradient-to-r ${catAccent}"></div>

      <div class="p-5 flex-1 flex flex-col justify-between">
        
        <div>
          <!-- Header: Category + Capacity Badge -->
          <div class="flex items-center justify-between gap-2 mb-3">
            <span class="inline-flex items-center px-2.5 py-1 rounded-lg text-[11px] font-bold border ${catColor}">
              ${event.category}
            </span>
            ${
              isFull
                ? `<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-black bg-rose-600 text-white tracking-wider shadow-sm animate-pulse">
                    FULL
                   </span>`
                : `<span class="text-xs font-semibold ${
                    event.available_seats <= 5 ? "text-amber-600 font-bold" : "text-emerald-600 font-bold"
                  }">
                    ${seatsRatio}
                   </span>`
            }
          </div>

          <!-- Title & Icon -->
          <div class="flex items-start space-x-3 mb-2.5">
            <span class="text-2xl p-2 bg-slate-50 group-hover:bg-indigo-50 rounded-xl transition-colors shrink-0">
              ${event.icon || "⚡"}
            </span>
            <div>
              <h3 class="text-base font-bold text-slate-900 group-hover:text-indigo-600 transition-colors line-clamp-1">
                ${escapeHtml(event.title)}
              </h3>
              <p class="text-xs text-slate-500 line-clamp-2 mt-1">
                ${escapeHtml(event.description || "Exciting fest event. Register now!")}
              </p>
            </div>
          </div>

          <!-- Mini Seat Meter -->
          <div class="w-full bg-slate-100 rounded-full h-1.5 mt-2 overflow-hidden">
            <div class="h-1.5 rounded-full transition-all duration-500 ${isFull ? 'bg-rose-500' : 'bg-gradient-to-r from-emerald-500 to-teal-500'}" style="width: ${fillPct}%"></div>
          </div>
        </div>

        <!-- Event Meta Details -->
        <div class="mt-4 pt-3 border-t border-slate-100 space-y-1.5 text-xs text-slate-600">
          <div class="flex items-center justify-between">
            <span class="flex items-center space-x-1.5 text-slate-500">
              <span>📅</span>
              <span>${event.event_date}</span>
              <span class="text-slate-300">•</span>
              <span>${event.event_time}</span>
            </span>
            <span class="font-extrabold ${event.fee > 0 ? "text-indigo-600" : "text-emerald-600"}">
              ${feeText}
            </span>
          </div>

          <div class="flex items-center text-slate-500 truncate">
            <span class="mr-1.5">📍</span>
            <span class="truncate">${escapeHtml(event.venue)}</span>
          </div>
        </div>

      </div>

      <!-- Action Footer -->
      <div class="p-3 bg-slate-50/80 border-t border-slate-100 flex items-center justify-between">
        <span class="text-[11px] font-medium text-slate-500">
          ${isFull ? "Registration Closed" : "Open for students"}
        </span>
        <button 
          data-id="${event.id}" 
          class="view-details-btn inline-flex items-center space-x-1.5 px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all ${
            isFull
              ? "bg-slate-200 text-slate-700 hover:bg-slate-300"
              : "bg-indigo-600 text-white hover:bg-indigo-700 shadow-sm shadow-indigo-200 active:scale-95"
          }"
        >
          <span>View Details</span>
          <span>→</span>
        </button>
      </div>
    `;

    eventsGrid.appendChild(card);
  });

  // Attach button click handlers
  eventsGrid.querySelectorAll(".view-details-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = parseInt(btn.getAttribute("data-id"), 10);
      openEventDetails(id);
    });
  });
}

/**
 * Open Event Detail & Registration Modal
 */
async function openEventDetails(eventId) {
  clearFormErrors();
  hideModalAlert();
  registrationForm.reset();

  // Find local or fetch from GET /api/events/{eventId}
  try {
    const response = await fetch(`/api/events/${eventId}`);
    if (!response.ok) {
      throw new Error(`Event not found (HTTP ${response.status})`);
    }
    const event = await response.json();

    formEventId.value = event.id;
    modalIcon.textContent = event.icon || "⚡";
    modalTitle.textContent = event.title;
    modalCategory.textContent = event.category;
    modalDate.textContent = event.event_date;
    modalTime.textContent = event.event_time;
    modalVenue.textContent = event.venue;
    modalFee.textContent = event.fee > 0 ? `₹${event.fee}` : "Free";
    modalDescription.textContent = event.description || "No description provided.";

    const isFull = event.available_seats <= 0;
    const filledPercent = Math.min(
      100,
      Math.round(((event.capacity - event.available_seats) / event.capacity) * 100)
    );

    modalSeatsBadge.textContent = isFull
      ? "EVENT IS FULL (0 seats left)"
      : `${event.available_seats} of ${event.capacity} seats left`;
    modalSeatsBadge.className = `font-bold ${isFull ? "text-rose-600" : "text-indigo-600"}`;
    modalSeatsProgress.style.width = `${filledPercent}%`;
    modalSeatsProgress.className = `h-2 rounded-full transition-all duration-500 ${
      isFull ? "bg-rose-600" : filledPercent > 80 ? "bg-amber-500" : "bg-indigo-600"
    }`;

    // Disable registration if full
    if (isFull) {
      submitRegBtn.disabled = true;
      btnText.textContent = "Event is Full";
      showModalAlert("warning", "Registration is closed because this event has reached maximum capacity.");
    } else {
      submitRegBtn.disabled = false;
      btnText.textContent = "Confirm Registration";
    }

    eventModal.classList.remove("hidden");
    document.body.style.overflow = "hidden";
  } catch (err) {
    showToast("⚠️", err.message || "Failed to load event details");
  }
}

function closeModal() {
  eventModal.classList.add("hidden");
  document.body.style.overflow = "auto";
  clearFormErrors();
  hideModalAlert();
}

/**
 * Handle Registration Form Submit
 */
async function handleRegistrationSubmit(e) {
  e.preventDefault();
  clearFormErrors();
  hideModalAlert();

  const eventId = parseInt(formEventId.value, 10);
  const studentName = inputName.value.trim();
  const rollNumber = inputRoll.value.trim();
  const email = inputEmail.value.trim();
  const phone = inputPhone.value.trim() || null;

  // Client-Side Validation Checks
  let hasError = false;
  if (!studentName) {
    showFieldError(errorName, "Please enter your full name");
    hasError = true;
  }
  if (!rollNumber) {
    showFieldError(errorRoll, "Please enter your roll / student number");
    hasError = true;
  }
  if (!email) {
    showFieldError(errorEmail, "Please enter your email address");
    hasError = true;
  } else if (!email.includes("@") || email.startsWith("@") || email.endsWith("@")) {
    showFieldError(errorEmail, "Please enter a valid email address containing '@'");
    hasError = true;
  }

  if (hasError) return;

  // Submit POST to /api/events/{event_id}/register
  setSubmitting(true);

  try {
    const response = await fetch(`/api/events/${eventId}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        student_name: studentName,
        roll_number: rollNumber,
        email: email,
        phone: phone,
      }),
    });

    const data = await response.json();

    if (response.status === 201) {
      // Success!
      showModalAlert(
        "success",
        `🎉 Registration Confirmed! You are registered for ${modalTitle.textContent}. Confirmation ID: #${data.id}`
      );
      showToast("🎉", `Registration successful for ${studentName}!`);

      // Refresh event list to update available seats
      await fetchEvents();

      // Automatically close modal after brief delay
      setTimeout(() => {
        closeModal();
      }, 1600);
    } else {
      // 400, 404, or 409: Show exact error message sent from server
      const serverError = data.detail || "Registration failed. Please check your information.";
      showModalAlert("error", serverError);
    }
  } catch (err) {
    showModalAlert("error", "Network connection failed. Could not reach server.");
  } finally {
    setSubmitting(false);
  }
}

function setSubmitting(isSubmitting) {
  submitRegBtn.disabled = isSubmitting;
  btnSpinner.classList.toggle("hidden", !isSubmitting);
  btnText.textContent = isSubmitting ? "Registering..." : "Confirm Registration";
}

function showFieldError(el, message) {
  el.textContent = message;
  el.classList.remove("hidden");
}

function clearFormErrors() {
  [errorName, errorRoll, errorEmail].forEach((el) => {
    el.textContent = "";
    el.classList.add("hidden");
  });
}

function showModalAlert(type, message) {
  modalAlert.classList.remove("hidden", "bg-red-50", "border-red-200", "text-red-800", "bg-emerald-50", "border-emerald-200", "text-emerald-800", "bg-amber-50", "border-amber-200", "text-amber-800");

  if (type === "success") {
    modalAlert.classList.add("bg-emerald-50", "border-emerald-200", "text-emerald-800");
    alertIcon.textContent = "✅";
  } else if (type === "warning") {
    modalAlert.classList.add("bg-amber-50", "border-amber-200", "text-amber-800");
    alertIcon.textContent = "⚠️";
  } else {
    modalAlert.classList.add("bg-red-50", "border-red-200", "text-red-800");
    alertIcon.textContent = "❌";
  }

  alertText.textContent = message;
}

function hideModalAlert() {
  modalAlert.classList.add("hidden");
}

function showLoading(isLoading) {
  loadingState.classList.toggle("hidden", !isLoading);
  if (isLoading) {
    eventsGrid.classList.add("hidden");
  } else {
    eventsGrid.classList.remove("hidden");
  }
}

function showError(msg) {
  errorMessage.textContent = msg;
  errorState.classList.remove("hidden");
  eventsGrid.classList.add("hidden");
}

function hideError() {
  errorState.classList.add("hidden");
}

function showEmpty() {
  emptyState.classList.remove("hidden");
  eventsGrid.classList.add("hidden");
}

function hideEmpty() {
  emptyState.classList.add("hidden");
}

function showToast(icon, message) {
  toastIcon.textContent = icon;
  toastMessage.textContent = message;
  toast.classList.remove("hidden", "translate-y-4");
  toast.classList.add("translate-y-0");

  setTimeout(() => {
    toast.classList.add("translate-y-4");
    setTimeout(() => toast.classList.add("hidden"), 300);
  }, 3500);
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
// Zenith AI Event Assistant (OpenAI Integration)
// ==============================================================================
let aiChatHistory = [];
let aiChatDrawerOpen = false;

function setupAiAssistant() {
  // Drawer Elements
  const launcherBtn = document.getElementById("open-ai-chat-btn");
  const chatDrawer = document.getElementById("ai-chat-drawer");
  const closeBtn = document.getElementById("ai-close-chat-btn");
  const clearBtn = document.getElementById("ai-clear-chat-btn");
  const chatForm = document.getElementById("ai-chat-form");
  const userInput = document.getElementById("ai-user-input");
  const sendBtn = document.getElementById("ai-send-btn");
  const messagesContainer = document.getElementById("ai-chat-messages");
  const typingIndicator = document.getElementById("ai-typing-indicator");
  const quickPrompts = document.querySelectorAll(".ai-chip");

  // In-Page Copilot Elements
  const inpageForm = document.getElementById("inpage-chat-form");
  const inpageInput = document.getElementById("inpage-chat-input");
  const inpageSendBtn = document.getElementById("inpage-chat-send-btn");
  const inpageStream = document.getElementById("inpage-chat-stream");
  const inpageTyping = document.getElementById("inpage-chat-typing");
  const inpageChips = document.querySelectorAll(".inpage-ai-chip");

  // Shared Helper to format text with Markdown bold, bullets, and line breaks
  function formatAiMarkdown(text) {
    return escapeHtml(text)
      .replace(/\*\*(.*?)\*\*/g, "<strong class='text-white font-bold'>$1</strong>")
      .replace(/^[•\-\*]\s+(.*)$/gm, "<div class='flex items-start gap-1.5 ml-1 my-0.5'><span class='text-cyan-400'>•</span><span>$1</span></div>")
      .replace(/\n/g, "<br/>");
  }

  // --- 1. Drawer Logic ---
  if (launcherBtn && chatDrawer) {
    function toggleDrawer(open) {
      aiChatDrawerOpen = typeof open === "boolean" ? open : !aiChatDrawerOpen;
      chatDrawer.classList.toggle("hidden", !aiChatDrawerOpen);
      if (aiChatDrawerOpen && userInput) {
        userInput.focus();
        if (messagesContainer) messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    }

    launcherBtn.addEventListener("click", () => toggleDrawer());
    closeBtn?.addEventListener("click", () => toggleDrawer(false));

    clearBtn?.addEventListener("click", () => {
      aiChatHistory = [];
      if (messagesContainer) {
        messagesContainer.innerHTML = `
          <div class="flex items-start gap-2.5">
            <div class="w-6 h-6 rounded-lg bg-indigo-100 text-indigo-700 flex items-center justify-center text-xs shrink-0 font-bold">✨</div>
            <div class="bg-slate-100 text-slate-800 rounded-2xl rounded-tl-sm p-3.5 leading-relaxed max-w-[85%] border border-slate-200/60 shadow-xs">
              👋 Chat cleared! How can I help you explore Zenith '26 today?
            </div>
          </div>
        `;
      }
    });

    quickPrompts.forEach((chip) => {
      chip.addEventListener("click", () => {
        const promptText = chip.getAttribute("data-prompt");
        if (promptText && userInput && chatForm) {
          userInput.value = promptText;
          chatForm.dispatchEvent(new Event("submit"));
        }
      });
    });

    chatForm?.addEventListener("submit", async (e) => {
      e.preventDefault();
      const query = userInput.value.trim();
      if (!query) return;

      appendDrawerMessage("user", query);
      userInput.value = "";
      if (sendBtn) sendBtn.disabled = true;
      typingIndicator?.classList.remove("hidden");
      if (messagesContainer) messagesContainer.scrollTop = messagesContainer.scrollHeight;

      try {
        const res = await fetch("/api/ai/assistant", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            message: query,
            history: aiChatHistory.slice(-6),
          }),
        });

        if (res.ok) {
          const data = await res.json();
          appendDrawerMessage("assistant", data.response);
          aiChatHistory.push({ role: "user", content: query });
          aiChatHistory.push({ role: "assistant", content: data.response });
        } else {
          const errData = await res.json().catch(() => ({ detail: "Unknown error" }));
          let errMsg = errData.detail || "Unable to reach the AI assistant.";
          if (res.status === 503) {
            errMsg = "⚠️ OpenAI API key is not yet configured in server environment. Please set OPENAI_API_KEY in your Vercel Project Settings → Environment Variables.";
          } else if (res.status === 429) {
            errMsg = "⏳ Rate limit reached (20 requests/minute). Please wait a moment before asking another question.";
          }
          appendDrawerMessage("assistant", errMsg, true);
        }
      } catch (err) {
        appendDrawerMessage(
          "assistant",
          "❌ Network error: Could not connect to the AI Assistant service. Please verify server connectivity.",
          true
        );
      } finally {
        typingIndicator?.classList.add("hidden");
        if (sendBtn) sendBtn.disabled = false;
        if (messagesContainer) messagesContainer.scrollTop = messagesContainer.scrollHeight;
        userInput.focus();
      }
    });
  }

  function appendDrawerMessage(role, text, isError = false) {
    if (!messagesContainer) return;
    const bubble = document.createElement("div");
    if (role === "user") {
      bubble.className = "flex items-start justify-end gap-2";
      bubble.innerHTML = `
        <div class="bg-gradient-to-r from-violet-600 to-indigo-600 text-white rounded-2xl rounded-tr-sm p-3 leading-relaxed max-w-[85%] shadow-sm font-medium">
          ${escapeHtml(text)}
        </div>
      `;
    } else {
      bubble.className = "flex items-start gap-2.5 animate-fade-in";
      const bgStyle = isError
        ? "bg-rose-50 text-rose-800 border-rose-200"
        : "bg-slate-100 text-slate-800 border-slate-200/60";
      const icon = isError ? "⚠️" : "✨";
      const iconBg = isError ? "bg-rose-100 text-rose-700" : "bg-indigo-100 text-indigo-700";

      const formatted = escapeHtml(text)
        .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
        .replace(/\n/g, "<br/>");

      bubble.innerHTML = `
        <div class="w-6 h-6 rounded-lg ${iconBg} flex items-center justify-center text-xs shrink-0 font-bold">${icon}</div>
        <div class="${bgStyle} rounded-2xl rounded-tl-sm p-3.5 leading-relaxed max-w-[85%] border shadow-xs">
          ${formatted}
        </div>
      `;
    }
    messagesContainer.appendChild(bubble);
  }

  // --- 2. In-Page Copilot Chat Logic ---
  if (inpageForm && inpageStream) {
    inpageChips.forEach((chip) => {
      chip.addEventListener("click", () => {
        const prompt = chip.getAttribute("data-prompt");
        if (prompt && inpageInput) {
          inpageInput.value = prompt;
          inpageForm.dispatchEvent(new Event("submit"));
        }
      });
    });

    inpageForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const query = inpageInput.value.trim();
      if (!query) return;

      appendInpageMessage("user", query);
      inpageInput.value = "";
      if (inpageSendBtn) inpageSendBtn.disabled = true;
      inpageTyping?.classList.remove("hidden");
      inpageStream.scrollTop = inpageStream.scrollHeight;

      try {
        const res = await fetch("/api/ai/assistant", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            message: query,
            history: aiChatHistory.slice(-6),
          }),
        });

        if (res.ok) {
          const data = await res.json();
          appendInpageMessage("assistant", data.response);
          aiChatHistory.push({ role: "user", content: query });
          aiChatHistory.push({ role: "assistant", content: data.response });
        } else {
          const errData = await res.json().catch(() => ({ detail: "Unknown error" }));
          let errMsg = errData.detail || "Unable to reach the AI assistant.";
          if (res.status === 503) {
            errMsg = "⚠️ OpenAI API key is not configured in server environment. Please set OPENAI_API_KEY in Vercel Settings → Environment Variables.";
          } else if (res.status === 429) {
            errMsg = "⏳ Rate limit reached (20 requests/minute). Please wait a moment before sending another query.";
          }
          appendInpageMessage("assistant", errMsg, true);
        }
      } catch (err) {
        appendInpageMessage(
          "assistant",
          "❌ Network error: Could not reach the Zenith AI Copilot. Please check your internet connection.",
          true
        );
      } finally {
        inpageTyping?.classList.add("hidden");
        if (inpageSendBtn) inpageSendBtn.disabled = false;
        inpageStream.scrollTop = inpageStream.scrollHeight;
        inpageInput.focus();
      }
    });

    function appendInpageMessage(role, text, isError = false) {
      const bubble = document.createElement("div");
      if (role === "user") {
        bubble.className = "flex items-start justify-end gap-3 animate-fade-in";
        bubble.innerHTML = `
          <div class="bg-gradient-to-r from-violet-600 via-indigo-600 to-cyan-500 text-white rounded-2xl rounded-tr-sm px-4 py-3 text-xs sm:text-sm font-semibold max-w-[85%] sm:max-w-[75%] shadow-lg shadow-indigo-500/20">
            ${escapeHtml(text)}
          </div>
        `;
      } else {
        bubble.className = "flex items-start gap-3 animate-fade-in";
        const bgStyle = isError
          ? "bg-rose-950/80 text-rose-200 border-rose-800"
          : "bg-slate-800/90 text-slate-200 border-slate-700/80";
        const iconBg = isError
          ? "bg-rose-500/30 text-rose-300 border-rose-500/40"
          : "bg-gradient-to-tr from-violet-600 to-indigo-600 text-white shadow-md shadow-violet-500/20";
        const icon = isError ? "⚠️" : "✨";

        const formatted = formatAiMarkdown(text);

        bubble.innerHTML = `
          <div class="w-8 h-8 rounded-xl ${iconBg} border border-white/10 flex items-center justify-center text-sm font-black shrink-0">
            ${icon}
          </div>
          <div class="${bgStyle} rounded-2xl rounded-tl-sm p-4 text-xs leading-relaxed max-w-[90%] sm:max-w-[85%] border shadow-md">
            ${formatted}
          </div>
        `;
      }
      inpageStream.appendChild(bubble);
    }
  }
}

