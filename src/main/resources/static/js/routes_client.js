// lightweight client-side routing + LLM helper
async function geocodeORS(place, apiKey){
  const resp = await fetch(`https://api.openrouteservice.org/geocode/search?api_key=${apiKey}&text=${encodeURIComponent(place)}`);
  if (!resp.ok) throw new Error('Geocode API error: '+resp.status);
  return await resp.json();
}

async function getDirectionsORS(startLon, startLat, endLon, endLat, apiKey){
  const resp = await fetch(`https://api.openrouteservice.org/v2/directions/driving-car?api_key=${apiKey}&start=${startLon},${startLat}&end=${endLon},${endLat}`);
  if (!resp.ok) throw new Error('Directions API error: '+resp.status);
  return await resp.json();
}

export async function planAndDraw(map, start, end, apiKey){
  const s = await geocodeORS(start, apiKey);
  const e = await geocodeORS(end, apiKey);
  if (!s.features || !s.features.length || !e.features || !e.features.length) throw new Error('geocode failed');
  const sCoord = s.features[0].geometry.coordinates; // [lon,lat]
  const eCoord = e.features[0].geometry.coordinates;
  const route = await getDirectionsORS(sCoord[0],sCoord[1],eCoord[0],eCoord[1],apiKey);
  if (!route || !route.features || !route.features.length) throw new Error('route response invalid');
  const coords = route.features[0].geometry.coordinates.map(c=>[c[1],c[0]]);
  // draw
  if (window._currentRouteLayer) map.removeLayer(window._currentRouteLayer);
  window._currentRouteLayer = L.polyline(coords,{color:'#00e6d8'}).addTo(map);
  map.fitBounds(window._currentRouteLayer.getBounds());
  return {coords, route};
}
