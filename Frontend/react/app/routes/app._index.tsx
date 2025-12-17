import React from "react";
import { Navigate } from "react-router";

export default function AppIndexRedirect() {
    return <Navigate to="/app/products" replace />;
}
