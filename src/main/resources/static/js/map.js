// Initialize OpenStreetMap
var map = L.map('map').setView([9.9312, 76.2673], 13); // Example: Kochi, Kerala

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
}).addTo(map);

// Add a marker
var marker = L.marker([9.9312, 76.2673]).addTo(map);
marker.bindPopup("Welcome to Smart Travel Assistant!").openPopup();
