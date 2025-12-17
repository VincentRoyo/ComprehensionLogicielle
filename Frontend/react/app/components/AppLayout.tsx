import React from 'react';
import { useAuth } from '../auth/AuthContext';
import {Link, Outlet} from "react-router";

export const AppLayout: React.FC = () => {
    const { user, logout } = useAuth();

    return (
        <div style={{ fontFamily: 'sans-serif' }}>
            <header
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    padding: '12px 24px',
                    borderBottom: '1px solid #ddd',
                }}
            >
                <nav style={{ display: 'flex', gap: 12 }}>
                    <Link to="/app/products">Produits</Link>
                    <Link to="/app/admin/users">Utilisateurs</Link>
                    <Link to="/app/admin/products">Admin Produits</Link>
                </nav>
                <div>
                    {user ? (
                        <>
                            <span style={{ marginRight: 16 }}>{user.email}</span>
                            <button onClick={logout}>Se déconnecter</button>
                        </>
                    ) : null}
                </div>
            </header>

            <main style={{ padding: 24 }}>
                <Outlet />
            </main>
        </div>
    );
};
