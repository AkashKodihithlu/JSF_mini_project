document.addEventListener("DOMContentLoaded", () => {
  // 1. Decorative Cursor Glow & Ring
  const glow = document.createElement("div");
  glow.className = "cursor-glow";
  const ring = document.createElement("div");
  ring.className = "cursor-ring";
  document.body.appendChild(glow);
  document.body.appendChild(ring);

  let mouseX = window.innerWidth / 2;
  let mouseY = window.innerHeight / 2;
  let ringX = mouseX;
  let ringY = mouseY;

  document.addEventListener("mousemove", (e) => {
    mouseX = e.clientX;
    mouseY = e.clientY;
    glow.style.left = mouseX + "px";
    glow.style.top = mouseY + "px";
  });

  function animateCursor() {
    ringX += (mouseX - ringX) * 0.01;
    ringY += (mouseY - ringY) * 0.01;
    ring.style.left = ringX + "px";
    ring.style.top = ringY + "px";
    requestAnimationFrame(animateCursor);
  }
  animateCursor();

  // Expand cursor ring on interactive elements
  const interactiveSelectors =
    "a, button, input, select, .hover-target, .role-card, .feature-card";
  document.addEventListener("mouseover", (e) => {
    if (e.target.closest(interactiveSelectors)) {
      document.body.classList.add("cursor-hover");
    }
  });
  document.addEventListener("mouseout", (e) => {
    if (e.target.closest(interactiveSelectors)) {
      document.body.classList.remove("cursor-hover");
    }
  });

  // 2. Subtle Spotlight Border Tracking on Cards
  document.querySelectorAll(".spotlight-card").forEach((card) => {
    card.addEventListener("mousemove", (e) => {
      const rect = card.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      card.style.setProperty("--mx", x + "px");
      card.style.setProperty("--my", y + "px");
    });
  });

  // 3. Scroll Reveal Observer
  const revealElements = document.querySelectorAll(".reveal");
  if ("IntersectionObserver" in window && revealElements.length > 0) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("visible");
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12 }
    );
    revealElements.forEach((el) => observer.observe(el));
  } else {
    revealElements.forEach((el) => el.classList.add("visible"));
  }
});
