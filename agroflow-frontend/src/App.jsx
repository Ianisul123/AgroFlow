import { Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import AuthPage from "./pages/AuthPage";
import FarmsPage from "./pages/FarmsPage";
import FarmDetailsPage from "./pages/FarmDetailsPage";
import "./index.css";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<AuthPage mode="login" />} />
      <Route path="/register" element={<AuthPage mode="register" />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <FarmsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/farms/:id"
        element={
          <ProtectedRoute>
            <FarmDetailsPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default App;
