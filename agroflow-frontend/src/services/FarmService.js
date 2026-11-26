import { getStoredAuthToken } from "./AuthService";

const API_BASE_URL = "http://localhost:8080/farms";

function buildHeaders(includeJson = false) {
  const headers = {};
  const token = getStoredAuthToken();

  if (includeJson) {
    headers["Content-Type"] = "application/json";
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return headers;
}

async function parseResponse(response, fallbackMessage) {
  const data = await response.json().catch(() => null);

  if (!response.ok) {
    throw data || { message: fallbackMessage };
  }

  return data;
}

export async function getAllFarms() {
  const response = await fetch(API_BASE_URL, {
    headers: buildHeaders(),
  });

  return parseResponse(response, "Failed to fetch farms.");
}

export async function getFarmById(id) {
  const response = await fetch(`${API_BASE_URL}/${id}`, {
    headers: buildHeaders(),
  });

  return parseResponse(response, "Failed to fetch farm.");
}

export async function createFarm(farmData) {
  const response = await fetch(API_BASE_URL, {
    method: "POST",
    headers: buildHeaders(true),
    body: JSON.stringify(farmData),
  });

  return parseResponse(response, "Failed to create farm.");
}

export async function getIrrigationZones(farmId) {
  const response = await fetch(`${API_BASE_URL}/${farmId}/irrigation-zones`, {
    headers: buildHeaders(),
  });

  return parseResponse(response, "Failed to fetch irrigation zones.");
}

export async function createIrrigationZone(farmId, zoneData) {
  const response = await fetch(`${API_BASE_URL}/${farmId}/irrigation-zones`, {
    method: "POST",
    headers: buildHeaders(true),
    body: JSON.stringify(zoneData),
  });

  return parseResponse(response, "Failed to create irrigation zone.");
}

export async function updateFarm(id, farmData) {
  const response = await fetch(`${API_BASE_URL}/${id}`, {
    method: "PUT",
    headers: buildHeaders(true),
    body: JSON.stringify(farmData),
  });

  return parseResponse(response, "Failed to update farm.");
}

export async function deleteFarm(id) {
  const response = await fetch(`${API_BASE_URL}/${id}`, {
    method: "DELETE",
    headers: buildHeaders(),
  });

  return parseResponse(response, "Failed to delete farm.");
}
