/**
 * Zenith '26 College Fest Management System
 * Frontend Application Script: Student Event Portal
 */

let currentEvents = [];
let selectedCategory = "All";
let searchQuery = "";
let searchDebounceTimeout = null;

// Category Badge Color Palette
const categoryColors = {
  Technical: "bg-blue-50 text-blue-700 border-blue-200",
  Cultural: "bg-pink-50 text-pink-700 border-pink-200",
  Sports: "bg-emerald-50 text-emerald-700 border-emerald-200",
  Gaming: "bg-purple-50 text-purple-700 border-purple-200",
  Workshop: "bg-amber-50 text-amber-800 border-amber-200",
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
    const feeText = event.fee > 0 ? `₹${event.fee}` : "Free";
    const seatsRatio = `${event.available_seats} / ${event.capacity} seats left`;

    const card = document.createElement("div");
    card.className = `group bg-white rounded-2xl border ${
      isFull ? "border-slate-300 opacity-90" : "border-slate-200/90 hover:border-indigo-400"
    } shadow-sm hover:shadow-xl transition-all duration-300 flex flex-col overflow-hidden animate-fade-in`;
    card.style.animationDelay = `${idx * 40}ms`;

    card.innerHTML = `
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
                    event.available_seats <= 5 ? "text-amber-600 font-bold" : "text-emerald-600"
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
