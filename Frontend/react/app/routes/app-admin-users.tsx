import React, { useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";

type UserDto = {
    id: number;
    email: string;
};

export default function AdminUsersPage() {
    const { token } = useAuth();
    const [users, setUsers] = useState<UserDto[]>([]);
    const [newEmail, setNewEmail] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [error, setError] = useState<string | null>(null);

    async function fetchUsers() {
        setError(null);
        try {
            const res = await fetch("/api/users", {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            setUsers(data);
        } catch (err: any) {
            setError(err.message ?? "Erreur lors du chargement des utilisateurs");
        }
    }

    useEffect(() => {
        fetchUsers().then();
    }, []);

    async function handleCreateUser(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        try {
            const res = await fetch("/api/users", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ email: newEmail, password: newPassword }),
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            setNewEmail("");
            setNewPassword("");
            await fetchUsers();
        } catch (err: any) {
            setError(err.message ?? "Erreur lors de la création d’utilisateur");
        }
    }

    return (
        <section>
            <h1 className="text-2xl font-bold mb-4">Gestion des utilisateurs</h1>

    <form
    onSubmit={handleCreateUser}
    className="flex flex-wrap gap-3 mb-6"
    >
    <input
        className="border rounded px-2 py-1"
    type="email"
    placeholder="Email"
    value={newEmail}
    onChange={e => setNewEmail(e.target.value)}
    required
    />
    <input
        className="border rounded px-2 py-1"
    type="password"
    placeholder="Mot de passe"
    value={newPassword}
    onChange={e => setNewPassword(e.target.value)}
    required
    />
    <button className="bg-blue-600 text-white rounded px-4 py-1">
        Créer
        </button>
        </form>

    {error && <p className="text-red-600 mb-4">{error}</p>}

        <ul className="space-y-1">
        {users.map(u => (
                <li key={u.id} className="border rounded px-3 py-1">
                {u.email}
                </li>
    ))}
        </ul>
        </section>
    );
    }
