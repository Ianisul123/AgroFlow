import { useCallback, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import FarmGrid from "../components/FarmGrid";
import FarmForm from "../components/FarmForm";
import { createFarm, deleteFarm, getAllFarms } from "../services/FarmService";
import { useFarmNotifications } from "../hooks/useFarmNotifications";
import { useAuth } from "../context/useAuth";

const FARM_NOTIFICATION_MESSAGES = {
  FARM_CREATED: "Farm data changed in real time.",
  FARM_UPDATED: "Farm data changed in real time.",
  FARM_DELETED: "Farm data changed in real time.",
};

function buildFarmNotification(type, message) {
  return {
    type,
    message: message || FARM_NOTIFICATION_MESSAGES[type] || "Farm list updated in real time.",
  };
}

function FarmsPage() {
  const location = useLocation();
  const { isAdmin, user, token } = useAuth();
  const [farms, setFarms] = useState([]);
  const [loadingFarms, setLoadingFarms] = useState(true);
  const [farmsError, setFarmsError] = useState("");
  const [deleteError, setDeleteError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [notification, setNotification] = useState(() => {
    if (!location.state?.notificationType) {
      return null;
    }
    return buildFarmNotification(location.state.notificationType);
  });

  const pageTitle = isAdmin ? "All Farms" : "My Farms";
  const pageDescription = isAdmin
    ? "Admin overview of every farm registered in AgroFlow."
    : "Only farms linked to your account are shown here.";
  const scopeMessage = isAdmin
    ? "Admin view: you can see all users' farms."
    : `${user?.name || "This user"} can only see farms created by this account.`;

  const filteredFarms = farms.filter((farm) => {
    const query = searchQuery.toLowerCase();
    return (
      farm.name?.toLowerCase().includes(query) ||
      farm.location?.toLowerCase().includes(query) ||
      farm.ownerName?.toLowerCase().includes(query) ||
      farm.ownerEmail?.toLowerCase().includes(query)
    );
  });

  const loadFarms = useCallback(async () => {
    try {
      const data = await getAllFarms();
      setFarms(data);
      setFarmsError("");
    } catch {
      setFarmsError("Failed to load farms. Please try again.");
    } finally {
      setLoadingFarms(false);
    }
  }, []);

  const handleFarmNotification = useCallback((notification) => {
    if (
      notification.type === "FARM_CREATED" ||
      notification.type === "FARM_UPDATED" ||
      notification.type === "FARM_DELETED"
    ) {
      void loadFarms();
      setNotification(buildFarmNotification(notification.type, notification.message));
    }
  }, [loadFarms]);

  useFarmNotifications(handleFarmNotification, { token });

  useEffect(() => {
    if (!notification) {
      return;
    }

    const timer = setTimeout(() => {
      setNotification(null);
    }, 3000);

    return () => clearTimeout(timer);
  }, [notification]);

  useEffect(() => {
    if (!deleteError) {
      return;
    }

    const timer = setTimeout(() => {
      setDeleteError("");
    }, 5000);

    return () => clearTimeout(timer);
  }, [deleteError]);

  useEffect(() => {
    setLoadingFarms(true);
    void loadFarms();
  }, [loadFarms]);

  useEffect(() => {
    function refreshVisibleFarmList() {
      if (!document.hidden) {
        void loadFarms();
      }
    }

    document.addEventListener("visibilitychange", refreshVisibleFarmList);
    window.addEventListener("focus", refreshVisibleFarmList);

    return () => {
      document.removeEventListener("visibilitychange", refreshVisibleFarmList);
      window.removeEventListener("focus", refreshVisibleFarmList);
    };
  }, [loadFarms]);

  async function handleFarmCreated(farmData) {
    await createFarm(farmData);
    await loadFarms();
    setShowCreateForm(false);
  }

  async function handleFarmDeleted(farmId) {
    try {
      await deleteFarm(farmId);
      await loadFarms();
    } catch (err) {
      const message =
        err?.errors?.[0]?.message || err?.message || "Failed to delete farm.";
      setDeleteError(message);
    }
  }

  return (
    <div className="app">
      <Navbar />

      <main className="page-layout">
        <Sidebar farmCount={farms.length} label={pageTitle} />

        <section className="content">
          {notification && (
            <div className="realtime-notification">
              Realtime update: {notification.message}
            </div>
          )}

          <div className="page-header">
            <div>
              <h1>{pageTitle}</h1>
              <p>{pageDescription}</p>
            </div>

            <button
              className="create-btn"
              onClick={() => setShowCreateForm((prev) => !prev)}
            >
              {showCreateForm ? "Close Form" : "+ Create New Farm"}
            </button>
          </div>

          <div className="admin-only-notice">{scopeMessage}</div>

          {!isAdmin && (
            <div className="admin-only-notice">
              Delete actions are available only for admins.
            </div>
          )}

          {deleteError && <p className="error-message">{deleteError}</p>}

          <div className="farms-search-bar">
            <input
              type="text"
              className="search-input farms-search-input"
              placeholder="Search farms by name or location..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>

          {showCreateForm && (
            <FarmForm
              onFarmCreated={handleFarmCreated}
              onCancel={() => setShowCreateForm(false)}
            />
          )}

          {loadingFarms && (
            <div className="loading-state">Loading farms...</div>
          )}

          {farmsError && !loadingFarms && (
            <p className="error-message">{farmsError}</p>
          )}

          {!loadingFarms && !farmsError && (
            <FarmGrid
              farms={filteredFarms}
              onDelete={handleFarmDeleted}
              canDelete={isAdmin}
              showOwner={isAdmin}
              emptyTitle={
                isAdmin
                  ? "No farms in AgroFlow yet."
                  : "No farms linked to your account yet."
              }
              emptyMessage={
                isAdmin
                  ? "Create the first farm or ask a user to register one."
                  : "Create a farm and it will appear only in your account."
              }
            />
          )}
        </section>
      </main>
    </div>
  );
}

export default FarmsPage;
