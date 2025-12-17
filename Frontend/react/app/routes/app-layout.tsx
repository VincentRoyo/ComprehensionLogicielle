import React from "react";
import {
    Link,
    Navigate,
    Outlet,
    useLocation,
} from "react-router";
import { useAuth } from "../auth/AuthContext";

export default function AppLayout() {
    const { user, isLoading, logout } = useAuth();
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

    return (
        <div className="min-h-screen flex flex-col font-sans">
            <header className="flex justify-between items-center px-6 py-3 border-b bg-white">
                <nav className="flex gap-4">
                    <Link to="/app/products" className="text-blue-600 hover:underline">
                        Produits
                    </Link>
                    <Link
                        to="/app/admin/users"
                        className="text-blue-600 hover:underline"
                    >
                        Utilisateurs
                    </Link>
                    <Link
                        to="/app/admin/products"
                        className="text-blue-600 hover:underline"
                    >
                        Admin Produits
                    </Link>
                </nav>
                <div className="flex items-center gap-3">
                    {user && (
                        <span className="text-sm text-gray-700">{user.email}</span>
                    )}
                    <button
                        className="border rounded px-3 py-1 text-sm hover:bg-gray-50"
                        onClick={logout}
                    >
                        Se déconnecter
                    </button>
                </div>
            </header>

            <main className="flex-1 p-6">
                <Outlet />
            </main>
        </div>
    );
}
