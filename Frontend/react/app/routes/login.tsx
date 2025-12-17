import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router";
import { useAuth } from "../auth/AuthContext";

export default function LoginPage() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation() as any;

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        try {
            await login(email, password);
            const redirectTo = location.state?.from?.pathname ?? "/app/products";
            navigate(redirectTo, { replace: true });
        } catch (err: any) {
            setError(err.message ?? "Erreur lors de la connexion");
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <main className="max-w-md mx-auto mt-24 p-6 border rounded-lg shadow-sm">
            <h1 className="text-2xl font-bold mb-4">Connexion</h1>
            <form onSubmit={handleSubmit} className="flex flex-col gap-3">
                <label className="flex flex-col gap-1">
                    <span>Email</span>
                    <input
                        className="border rounded px-2 py-1"
                        type="email"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                    />
                </label>

                <label className="flex flex-col gap-1">
                    <span>Mot de passe</span>
                    <input
                        className="border rounded px-2 py-1"
                        type="password"
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        required
                    />
                </label>

                {error && <p className="text-red-600 text-sm">{error}</p>}

                <button
                    className="mt-2 bg-blue-600 text-white rounded px-3 py-2 disabled:opacity-50"
                    type="submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? "Connexion..." : "Se connecter"}
                </button>
            </form>
        </main>
    );
}
