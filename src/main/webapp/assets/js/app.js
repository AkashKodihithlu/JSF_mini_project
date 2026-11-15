document.addEventListener("DOMContentLoaded", () => {
    const mapElement = document.getElementById("map");
    if (mapElement && typeof L !== "undefined") {
        const map = L.map("map").setView([13.2150, 74.9960], 13);
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        L.marker([13.2150, 74.9960])
            .addTo(map)
            .bindPopup("<b>District Relief Control Center</b><br>Active monitoring zone.")
            .openPopup();
    }
});
