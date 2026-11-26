import { useState } from "react";

function FarmForm({ onFarmCreated, onCancel }) {
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage("");
    setError("");
    setIsSubmitting(true);

    try {
      await onFarmCreated({ name, location });
      setMessage("Farm was successfully created.");
      setName("");
      setLocation("");
    } catch (err) {
      if (err.errors && err.errors.length > 0) {
        setError(err.errors[0].message);
      } else if (err.message) {
        setError(err.message);
      } else {
        setError("Something went wrong.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="form-section">
      <div className="form-header">
        <h2>Create New Farm</h2>
        <p>Add a farm to the current user account</p>
      </div>

      <form className="farm-form" onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="form-group">
            <label>Farm Name</label>
            <input
              type="text"
              placeholder="e.g. Golden Harvest"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Location</label>
            <input
              type="text"
              placeholder="City, State"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
            />
          </div>
        </div>

        <div className="form-actions">
          <button
            type="button"
            className="cancel-btn"
            onClick={onCancel}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="submit-btn"
            disabled={isSubmitting}
          >
            {isSubmitting ? "Creating..." : "Add Farm"}
          </button>
        </div>

        {message && <p className="success-message">{message}</p>}
        {error && <p className="error-message">{error}</p>}
      </form>
    </section>
  );
}

export default FarmForm;

