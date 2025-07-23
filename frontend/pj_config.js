export const CITY_JSON = "counties.json";

// Default map center coordinates (US geographic center)
export const DEFAULT_MAP_CENTER = {
  lat: 37.3541,    // Santa Clara County latitude
  lng: -121.9552   // Santa Clara County longitude
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