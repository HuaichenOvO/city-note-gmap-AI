export const CITY_JSON = "counties.json";

// Default map center coordinates (US geographic center)
export const DEFAULT_MAP_CENTER = {
  lat: 39.8283,  // US geographic center latitude
  lng: -98.5795  // US geographic center longitude
};

// Default zoom level
export const DEFAULT_MAP_ZOOM = 7;

// Pagination settings
export const PAGINATION_CONFIG = {
  defaultPage: 0,
  defaultPageSize: 10
};

// API endpoints (if needed)
export const API_CONFIG = {
  baseUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080'
};