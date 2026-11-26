import FarmCard from "./FarmCard";

function FarmGrid({ farms, onDelete, canDelete, showOwner, emptyTitle, emptyMessage }) {
  return (
    <section className="farm-grid-section">
      {farms.length === 0 ? (
        <div className="empty-state">
          <h3>{emptyTitle || "No farms found."}</h3>
          <p>{emptyMessage || "Create a farm to start tracking irrigation data."}</p>
        </div>
      ) : (
        <div className="farm-grid">
          {farms.map((farm) => (
            <FarmCard
              key={farm.id}
              farm={farm}
              onDelete={onDelete}
              canDelete={canDelete}
              showOwner={showOwner}
            />
          ))}
        </div>
      )}
    </section>
  );
}

export default FarmGrid;
