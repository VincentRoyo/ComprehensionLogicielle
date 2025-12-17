import React from "react";
import { useAuth } from "../auth/AuthContext";

type MeDto = {
    id?: string;
    email?: string;
    name?: string;
};

export default function MePage() {
    const { token } = useAuth();

    const [me, setMe] = React.useState<MeDto | null>(null);
    const [error, setError] = React.useState<string | null>(null);
    const [loading, setLoading] = React.useState(true);

    React.useEffect(() => {
        let cancelled = false;

        (async () => {
            try {
                setError(null);
                setLoading(true);

                const res = await fetch("/api/users/me", {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                if (!res.ok) throw new Error(`HTTP ${res.status}`);

                const data: MeDto = await res.json();
                if (!cancelled) setMe(data);
            } catch (e: any) {
                if (!cancelled) setError(e.message ?? "Erreur lors du chargement du profil");
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [token]);

    return (
        <section className="max-w-md">
        <h1 className="text-2xl font-bold mb-4">Mon profil</h1>

    {loading && <p>Chargement…</p>}
        {error && <p className="text-red-600">{error}</p>}

            {!loading && !error && me && (
                <div className="border rounded p-4 space-y-2">
                <div className="flex justify-between">
                <span className="text-gray-600">ID</span>
                    <span className="font-mono">{me.id ?? "-"}</span>
                </div>

                <div className="flex justify-between">
            <span className="text-gray-600">Email</span>
                <span>{me.email ?? "-"}</span>
                </div>

                {me.name && (
                    <div className="flex justify-between">
                    <span className="text-gray-600">Pseudo</span>
                        <span>{me.name}</span>
                        </div>
                )}
                </div>
            )}
            </section>
        );
        }
