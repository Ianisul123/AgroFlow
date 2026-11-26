import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/useAuth";
import {
  deleteFarm,
  getFarmById,
  getIrrigationZones,
  createIrrigationZone,
  updateFarm,
} from "../services/FarmService";

function FarmDetailsPage() {
  const { id } = useParams();
  const { isAdmin } = useAuth();
  const navigate = useNavigate();

  const [farm, setFarm] = useState(null);
  const [loadingFarm, setLoadingFarm] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [isUpdatingFarm, setIsUpdatingFarm] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const [name, setName] = useState("");
  const [location, setLocation] = useState("");

  const [irrigationZones, setIrrigationZones] = useState([]);
  const [loadingZones, setLoadingZones] = useState(true);
  const [zoneName, setZoneName] = useState("");
  const [cropType, setCropType] = useState("");
  const [areaSize, setAreaSize] = useState("");
  const [moistureThreshold, setMoistureThreshold] = useState("");
  const [zoneError, setZoneError] = useState("");
  const [zoneMessage, setZoneMessage] = useState("");
  const [validationErrors, setValidationErrors] = useState({});
  const [isAddingZone, setIsAddingZone] = useState(false);
  const [isCreatingZone, setIsCreatingZone] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoadingFarm(true);

    getFarmById(id)
      .then((data) => {
        if (!ignore) {
          setFarm(data);
          setName(data.name || "");
          setLocation(data.location || "");
          setLoadingFarm(false);
        }
      })
      .catch(() => {
        if (!ignore) {
          setError("Farm not found.");
          setLoadingFarm(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, [id]);

  async function loadZones() {
    try {
      const zones = await getIrrigationZones(id);
      setIrrigationZones(zones || []);
    } catch {
      setZoneError("Failed to load irrigation zones.");
    }
  }

  useEffect(() => {
    if (!id) {
      return;
    }

    let ignore = false;
    setLoadingZones(true);

    getIrrigationZones(id)
      .then((zones) => {
        if (!ignore) {
          setIrrigationZones(zones || []);
          setLoadingZones(false);
        }
      })
      .catch(() => {
        if (!ignore) {
          setZoneError("Failed to load irrigation zones.");
          setLoadingZones(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, [id]);

  useEffect(() => {
    if (!zoneMessage) {
      return;
    }

    const timer = setTimeout(() => {
      setZoneMessage("");
    }, 5000);

    return () => clearTimeout(timer);
  }, [zoneMessage]);

  useEffect(() => {
    if (!message) {
      return;
    }

    const timer = setTimeout(() => {
      setMessage("");
    }, 5000);

    return () => clearTimeout(timer);
  }, [message]);

  async function handleCreateZone(event) {
    event.preventDefault();
    setZoneError("");
    setZoneMessage("");

    const trimmedName = zoneName.trim();
    const trimmedCropType = cropType.trim();
    const parsedArea = parseFloat(areaSize);
    const parsedMoisture = parseFloat(moistureThreshold);

    const errors = {};

    if (!trimmedName) {
      errors.zoneName = "Zone name is required.";
    } else if (trimmedName.length > 100) {
      errors.zoneName = "Zone name must be 100 characters or less.";
    }

    if (!trimmedCropType) {
      errors.cropType = "Crop type is required.";
    } else if (trimmedCropType.length > 100) {
      errors.cropType = "Crop type must be 100 characters or less.";
    }

    if (Number.isNaN(parsedArea) || parsedArea <= 0) {
      errors.areaSize = "Area size must be greater than 0.";
    }

    if (Number.isNaN(parsedMoisture) || parsedMoisture < 0 || parsedMoisture > 100) {
      errors.moistureThreshold = "Moisture threshold must be between 0 and 100.";
    }

    if (Object.keys(errors).length > 0) {
      setValidationErrors(errors);
      return;
    }

    setValidationErrors({});
    setIsCreatingZone(true);

    try {
      await createIrrigationZone(id, {
        name: trimmedName,
        cropType: trimmedCropType,
        areaSize: parsedArea,
        moistureThreshold: parsedMoisture,
      });

      setZoneMessage("Irrigation zone was successfully created.");
      setZoneName("");
      setCropType("");
      setAreaSize("");
      setMoistureThreshold("");
      setIsAddingZone(false);
      await loadZones();
    } catch (err) {
      if (err.errors && err.errors.length > 0) {
        setZoneError(err.errors[0].message);
      } else if (err.message) {
        setZoneError(err.message);
      } else {
        setZoneError("Failed to create irrigation zone.");
      }
    } finally {
      setIsCreatingZone(false);
    }
  }

  function getZoneStatus(threshold) {
    if (threshold >= 60) {
      return { label: "Normal", color: "normal" };
    }

    if (threshold >= 35) {
      return { label: "Low", color: "low" };
    }

    return { label: "Critical", color: "critical" };
  }

  async function handleUpdate(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    setIsUpdatingFarm(true);

    try {
      await updateFarm(id, { name, location });
      const updatedFarm = await getFarmById(id);
      setFarm(updatedFarm);
      setIsEditing(false);
      setMessage("Farm was successfully updated.");
    } catch (err) {
      if (err.errors && err.errors.length > 0) {
        setError(err.errors[0].message);
      } else if (err.message) {
        setError(err.message);
      } else {
        setError("Something went wrong.");
      }
    } finally {
      setIsUpdatingFarm(false);
    }
  }

  async function handleDelete() {
    const confirmed = window.confirm("Are you sure you want to delete this farm?");

    if (!confirmed) {
      return;
    }

    setIsDeleting(true);

    try {
      await deleteFarm(id);
      navigate("/", { state: { notificationType: "FARM_DELETED" } });
    } catch {
      setError("Failed to delete farm.");
      setIsDeleting(false);
    }
  }

  return (
    <div className="app">
      <Navbar />

      <main className="details-page">
        <Link to="/" className="back-link">&lt; Back to farms</Link>

        {error && <p className="error-message">{error}</p>}
        {message && <p className="success-message">{message}</p>}

        {loadingFarm && <div className="loading-state">Loading farm details...</div>}

        {!loadingFarm && !farm && !error && (
          <p className="error-message">Farm not found.</p>
        )}

        {farm && !isEditing && (
          <>
            <div className="details-card">
              <div className="details-image"></div>

              <div className="details-content">
                <span className="status-badge">ACTIVE</span>
                <h1>{farm.name}</h1>
                <p className="farm-location">{farm.location || "No location"}</p>

                <div className="details-grid">
                  <div className="details-box">
                    <span className="meta-label">Farm ID</span>
                    <strong>{farm.id}</strong>
                  </div>

                  <div className="details-box">
                    <span className="meta-label">Location</span>
                    <strong>{farm.location || "Not available"}</strong>
                  </div>

                  {isAdmin && (
                    <div className="details-box">
                      <span className="meta-label">Owner</span>
                      <strong>{farm.ownerName || "Unassigned"}</strong>
                      {farm.ownerEmail && (
                        <span className="details-subtext">{farm.ownerEmail}</span>
                      )}
                    </div>
                  )}
                </div>

                <div className="farm-actions">
                  <button className="edit-btn" onClick={() => setIsEditing(true)}>
                    Edit
                  </button>
                  {isAdmin && (
                    <button
                      className="delete-btn"
                      onClick={handleDelete}
                      disabled={isDeleting}
                    >
                      {isDeleting ? "Deleting..." : "Delete"}
                    </button>
                  )}
                </div>

                {!isAdmin && (
                  <p className="admin-only-inline">
                    Delete actions are available only for admins.
                  </p>
                )}
              </div>
            </div>

            <section className="form-section zone-section">
              <div className="section-header">
                <div>
                  <h2>Irrigation Zones</h2>
                  <p>Manage all irrigation zones linked to this farm.</p>
                </div>
                <button
                  type="button"
                  className="add-zone-btn"
                  onClick={() => setIsAddingZone((prev) => !prev)}
                >
                  {isAddingZone ? "Close" : "+ Add Zone"}
                </button>
              </div>

              {zoneMessage && <div className="toast-message">{zoneMessage}</div>}

              {isAddingZone && (
                <form className="farm-form zone-form" onSubmit={handleCreateZone}>
                  <div className="form-row">
                    <div className="form-group">
                      <label>Zone name</label>
                      <input
                        type="text"
                        placeholder="e.g. Zone A"
                        value={zoneName}
                        onChange={(e) => setZoneName(e.target.value)}
                      />
                      {validationErrors.zoneName && (
                        <span className="field-error">{validationErrors.zoneName}</span>
                      )}
                    </div>

                    <div className="form-group">
                      <label>Crop type</label>
                      <input
                        type="text"
                        placeholder="e.g. Tomatoes"
                        value={cropType}
                        onChange={(e) => setCropType(e.target.value)}
                      />
                      {validationErrors.cropType && (
                        <span className="field-error">{validationErrors.cropType}</span>
                      )}
                    </div>
                  </div>

                  <div className="form-row">
                    <div className="form-group">
                      <label>Area size</label>
                      <input
                        type="number"
                        step="0.1"
                        placeholder="e.g. 12.5"
                        value={areaSize}
                        onChange={(e) => setAreaSize(e.target.value)}
                      />
                      {validationErrors.areaSize && (
                        <span className="field-error">{validationErrors.areaSize}</span>
                      )}
                    </div>

                    <div className="form-group">
                      <label>Moisture threshold</label>
                      <input
                        type="number"
                        step="1"
                        placeholder="e.g. 35"
                        value={moistureThreshold}
                        onChange={(e) => setMoistureThreshold(e.target.value)}
                      />
                      {validationErrors.moistureThreshold && (
                        <span className="field-error">{validationErrors.moistureThreshold}</span>
                      )}
                    </div>
                  </div>

                  <div className="form-actions zone-form-actions">
                    <button
                      type="button"
                      className="cancel-btn"
                      onClick={() => {
                        setZoneName("");
                        setCropType("");
                        setAreaSize("");
                        setMoistureThreshold("");
                        setZoneError("");
                        setValidationErrors({});
                        setIsAddingZone(false);
                      }}
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className="submit-btn"
                      disabled={isCreatingZone}
                    >
                      {isCreatingZone ? "Creating..." : "Create Zone"}
                    </button>
                  </div>

                  {zoneError && <p className="error-message">{zoneError}</p>}
                </form>
              )}

              {loadingZones && (
                <div className="loading-state">Loading irrigation zones...</div>
              )}

              {!loadingZones && irrigationZones.length === 0 && (
                <div className="empty-state">
                  <div className="empty-state-icon" aria-hidden="true">
                    <svg viewBox="0 0 64 64" width="48" height="48" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M32 4C21.6 4 13 12.6 13 23C13 31.4 18.5 38.6 26 40.8V56H38V40.8C45.5 38.6 51 31.4 51 23C51 12.6 42.4 4 32 4Z" fill="#D9F99D"/>
                      <path d="M32 14C26.5 14 22 18.5 22 24C22 29.5 26.5 34 32 34C37.5 34 42 29.5 42 24C42 18.5 37.5 14 32 14Z" fill="#4ADE80"/>
                      <path d="M20 45H44V50H20V45Z" fill="#A7F3D0"/>
                    </svg>
                  </div>
                  <h3>No irrigation zones created yet.</h3>
                  <p>Start by adding a new zone for this farm.</p>
                </div>
              )}

              {!loadingZones && irrigationZones.length > 0 && (
                <div className="zone-card-grid">
                  {irrigationZones.map((zone) => {
                    const status = getZoneStatus(zone.moistureThreshold);
                    return (
                      <div key={zone.id} className="zone-card">
                        <div className="zone-card-header">
                          <h3>{zone.name}</h3>
                          <span className={`zone-status-badge ${status.color}`}>
                            {status.label}
                          </span>
                        </div>
                        <p className="zone-label">{zone.cropType}</p>
                        <div className="zone-meta">
                          <span>{zone.areaSize} acres</span>
                          <span>{zone.moistureThreshold}% threshold</span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </section>
          </>
        )}

        {farm && isEditing && (
          <section className="form-section">
            <div className="form-header">
              <h2>Edit Farm</h2>
              <p>Update the farm information</p>
            </div>

            <form className="farm-form" onSubmit={handleUpdate}>
              <div className="form-row">
                <div className="form-group">
                  <label>Farm Name</label>
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label>Location</label>
                  <input
                    type="text"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="cancel-btn"
                  onClick={() => {
                    setIsEditing(false);
                    setName(farm.name || "");
                    setLocation(farm.location || "");
                    setError("");
                    setMessage("");
                  }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="submit-btn"
                  disabled={isUpdatingFarm}
                >
                  {isUpdatingFarm ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </form>
          </section>
        )}
      </main>
    </div>
  );
}

export default FarmDetailsPage;

