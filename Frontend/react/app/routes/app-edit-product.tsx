import React from "react";
import { useNavigate, useParams } from "react-router";
import { useAuth } from "../auth/AuthContext";

type Product = {
    id: number;
    name: string;
    price: number;
};

export default function EditProductPage() {
    const { token } = useAuth();
    const navigate = useNavigate();
    const params = useParams();

    const productId = params.id;

    const [name, setName] = React.useState("");
    const [price, setPrice] = React.useState<string>("");
    const [error, setError] = React.useState<string | null>(null);
    const [isLoading, setIsLoading] = React.useState(true);
    const [isSaving, setIsSaving] = React.useState(false);

    React.useEffect(() => {
        let cancelled = false;

        (async () => {
            try {
                setError(null);
                setIsLoading(true);

                const res = await fetch(`/api/products/${productId}`, {
                    headers: { Authorization: `Bearer ${token}` },
                });
                if (!res.ok) throw new Error(`HTTP ${res.status}`);

                const p: Product = await res.json();
                if (cancelled) return;

                setName(p.name ?? "");
                setPrice(String(p.price ?? ""));
            } catch (e: any) {
                if (!cancelled) setError(e.message ?? "Erreur lors du chargement du produit");
            } finally {
                if (!cancelled) setIsLoading(false);
            }
        })();

        return () => {
            cancelled = true;
        };
    }, [productId, token]);

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError(null);

        const parsedPrice = Number(price);
        if (!name.trim()) {
            setError("Le nom est obligatoire");
            return;
        }
        if (!Number.isFinite(parsedPrice) || parsedPrice < 0) {
            setError("Le prix doit être un nombre positif");
            return;
        }

        setIsSaving(true);
        try {
            const res = await fetch(`/api/products/${productId}`, {
                method: "PUT", // ou "PATCH" selon ton API
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ name: name.trim(), price: parsedPrice }),
            });

            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            // retour à la liste
            navigate("/app/products");
        } catch (e: any) {
            setError(e.message ?? "Erreur lors de la sauvegarde");
        } finally {
            setIsSaving(false);
        }
    }

    if (isLoading) {
        return (
           <p>Chargement...</p>
        );
    }

    return (
        <section className="max-w-md">
            <h1 className="text-2xl font-bold mb-4">Modifier le produit</h1>

            {error && <p className="text-red-600 mb-4">{error}</p>}

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="flex flex-col gap-1">
                    <label className="text-sm">Nom</label>
                    <input
                        className="border rounded px-2 py-1"
                        type="text"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        placeholder="Nom du produit"
                    />
                </div>

                <div className="flex flex-col gap-1">
                    <label className="text-sm">Prix</label>
                    <input
                        className="border rounded px-2 py-1"
                        type="number"
                        min={0}
                        step="0.01"
                        value={price}
                        onChange={e => setPrice(e.target.value)}
                    />
                </div>

                <div className="flex gap-2">
                    <button
                        className="bg-blue-600 text-white rounded px-4 py-2 disabled:opacity-50"
                        type="submit"
                        disabled={isSaving}
                    >
                        {isSaving ? "Enregistrement..." : "Enregistrer"}
                    </button>

                    <button
                        className="bg-gray-700 text-white rounded px-4 py-2"
                        type="button"
                        onClick={() => navigate("/app/products")}
                    >
                        Annuler
                    </button>
                </div>
            </form>
        </section>
    );


}
