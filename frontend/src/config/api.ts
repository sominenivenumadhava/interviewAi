/**
 * Single backend API origin for local development.
 * Frontend (Vite :5173) proxies /api → this origin.
 * There is no separate API gateway — Spring Boot IS the API.
 */
export const API_PORT = 8082;
export const API_ORIGIN = `http://localhost:${API_PORT}`;
export const BACKEND_DOWN_MESSAGE =
  `Cannot connect to the server. Make sure the backend API is running on port ${API_PORT}.`;
