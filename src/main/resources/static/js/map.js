// Enhanced Map Initialization with Futuristic Features
class SmartMap {
    constructor(containerId = 'map') {
        this.containerId = containerId;
        this.map = null;
        this.markers = [];
        this.routes = [];
        this.userLocation = null;
        this.init();
    }

    init() {
        // Initialize map with modern styling
        this.map = L.map(this.containerId, {
            zoomControl: true,
            attributionControl: true,
            scrollWheelZoom: true,
            doubleClickZoom: true,
            boxZoom: true,
            keyboard: true,
            dragging: true,
            touchZoom: true
        }).setView([9.9312, 76.2673], 13);

        // Add custom tile layer with better styling
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
            maxZoom: 19,
            subdomains: ['a', 'b', 'c']
        }).addTo(this.map);

        // Add welcome marker with enhanced popup
        this.addWelcomeMarker();
        
        // Add user location tracking
        this.enableLocationTracking();
        
        // Add map event listeners
        this.addEventListeners();
    }

    addWelcomeMarker() {
        const welcomeMarker = L.marker([9.9312, 76.2673], {
            icon: this.createCustomIcon('🚀', '#00e6d8')
        }).addTo(this.map);
        
        welcomeMarker.bindPopup(`
            <div style="text-align: center; padding: 10px;">
                <h3 style="color: #00e6d8; margin: 0 0 10px 0;">🚀 Smart Travel Assistant</h3>
                <p style="margin: 0; color: #666;">Welcome to the future of travel planning!</p>
            </div>
        `).openPopup();
        
        this.markers.push(welcomeMarker);
    }

    createCustomIcon(emoji, color = '#00e6d8') {
        return L.divIcon({
            html: `
                <div style="
                    background: linear-gradient(135deg, ${color}, #6fffe4);
                    border: 2px solid #fff;
                    border-radius: 50%;
                    width: 40px;
                    height: 40px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 18px;
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
                ">
                    ${emoji}
                </div>
            `,
            className: 'custom-marker',
            iconSize: [40, 40],
            iconAnchor: [20, 20],
            popupAnchor: [0, -20]
        });
    }

    enableLocationTracking() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    this.userLocation = [position.coords.latitude, position.coords.longitude];
                    this.addUserLocationMarker();
                },
                (error) => {
                    console.log('Location access denied or unavailable');
                },
                {
                    enableHighAccuracy: true,
                    timeout: 10000,
                    maximumAge: 300000
                }
            );
        }
    }

    addUserLocationMarker() {
        if (this.userLocation) {
            const userMarker = L.marker(this.userLocation, {
                icon: this.createCustomIcon('📍', '#00ff88')
            }).addTo(this.map);
            
            userMarker.bindPopup(`
                <div style="text-align: center; padding: 10px;">
                    <h4 style="color: #00ff88; margin: 0 0 5px 0;">📍 Your Location</h4>
                    <p style="margin: 0; color: #666;">We found you here!</p>
                </div>
            `);
            
            this.markers.push(userMarker);
        }
    }

    addEventListeners() {
        // Add click event to show coordinates
        this.map.on('click', (e) => {
            const { lat, lng } = e.latlng;
            console.log(`Clicked at: ${lat}, ${lng}`);
        });

        // Add zoom event
        this.map.on('zoomend', () => {
            console.log(`Zoom level: ${this.map.getZoom()}`);
        });
    }

    addRoute(coordinates, options = {}) {
        const defaultOptions = {
            color: '#00e6d8',
            weight: 4,
            opacity: 0.8,
            dashArray: null
        };
        
        const routeOptions = { ...defaultOptions, ...options };
        
        const route = L.polyline(coordinates, routeOptions).addTo(this.map);
        this.routes.push(route);
        
        // Fit map to route bounds
        this.map.fitBounds(route.getBounds(), { padding: [20, 20] });
        
        return route;
    }

    addMarker(lat, lng, popupText, emoji = '📍') {
        const marker = L.marker([lat, lng], {
            icon: this.createCustomIcon(emoji)
        }).addTo(this.map);
        
        if (popupText) {
            marker.bindPopup(popupText);
        }
        
        this.markers.push(marker);
        return marker;
    }

    clearMarkers() {
        this.markers.forEach(marker => this.map.removeLayer(marker));
        this.markers = [];
    }

    clearRoutes() {
        this.routes.forEach(route => this.map.removeLayer(route));
        this.routes = [];
    }

    setView(lat, lng, zoom = 13) {
        this.map.setView([lat, lng], zoom);
    }

    getCenter() {
        return this.map.getCenter();
    }

    getZoom() {
        return this.map.getZoom();
    }
}

// Initialize the smart map when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Only initialize if map container exists
    if (document.getElementById('map')) {
        window.smartMap = new SmartMap('map');
    }
});

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = SmartMap;
}