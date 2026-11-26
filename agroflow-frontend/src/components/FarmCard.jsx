import { useNavigate } from "react-router-dom";

function FarmCard({ farm, onDelete, canDelete, showOwner }) {
  const navigate = useNavigate();

  function handleViewDetails() {
    navigate(`/farms/${farm.id}`);
  }

  async function handleDelete() {
    const confirmed = window.confirm("Are you sure you want to delete this farm?");

    if (!confirmed) {
      return;
    }

    await onDelete(farm.id);
  }

  return (
    <div className="farm-card">
      <div className="farm-image">
        <span className="status-badge">ACTIVE</span>
      </div>

      <div className="farm-card-body">
        <h3>{farm.name}</h3>
        <p className="farm-location">{farm.location || "No location"}</p>

        {showOwner && (
          <div className="farm-owner-pill">
            <span>Owner</span>
            <strong>{farm.ownerName || "Unassigned"}</strong>
            {farm.ownerEmail && <small>{farm.ownerEmail}</small>}
          </div>
        )}
        <div className="farm-actions">
          <button className="edit-btn" onClick={handleViewDetails}>
            View Details
          </button>
          {canDelete && (
            <button className="delete-btn" onClick={handleDelete}>
              Delete
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default FarmCard;
