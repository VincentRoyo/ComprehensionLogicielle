import React from "react";
import { Navigate, Outlet, useLocation } from "react-router";
import { useAuth } from "../auth/AuthContext";

export default function ProtectedLayout() {
    const { user, isLoading } = useAuth();
    const location = useLocation();

    if (isLoading) {
        return <div className="p-4">Chargement...</div>;
    }

    if (!user) {
        return (
            <Navigate
                to="/login"
                replace
                state={{ from: location }}
            />
        );
    }

    return <Outlet />;
}