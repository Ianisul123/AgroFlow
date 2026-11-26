function Sidebar({ farmCount, label = "Farms" }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-card active-sidebar-item">
        <span>{label}</span>
        <span className="count-badge">{farmCount}</span>
      </div>
    </aside>
  );
}

export default Sidebar;
