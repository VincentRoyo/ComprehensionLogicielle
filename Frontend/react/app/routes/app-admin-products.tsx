import React, { useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import {useNavigate} from "react-router";

type Product = {
    id: number;
    name: string;
    price: number;
};

export default function AdminProductsPage() {
    const { token } = useAuth();
    const [products, setProducts] = useState<Product[]>([]);
    const [name, setName] = useState("");
    const [price, setPrice] = useState<string>("");
    const [error, setError] = useState<string | null>(null);
    const [deletingId, setDeletingId] = useState<number | null>(null);
    const navigate = useNavigate();

    async function fetchProducts() {
        setError(null);
        try {
            const res = await fetch("/api/products", {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            setProducts(data);
        } catch (err: any) {
            setError(err.message ?? "Erreur lors du chargement des produits");
        }
    }

    useEffect(() => {
        fetchProducts();
    }, []);

    async function handleCreateProduct(e: React.FormEvent) {
        e.preventDefault();
        setError(null);

        const priceValue = parseFloat(price);
        if (isNaN(priceValue)) {
            setError("Prix invalide");
            return;
        }

        try {
            const res = await fetch("/api/products", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ name, price: priceValue }),
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            setName("");
            setPrice("");
            await fetchProducts();
        } catch (err: any) {
            setError(err.message ?? "Erreur lors de la création du produit");
        }
    }

    async function handleDelete(productId: number) {
        setError(null);

        const ok = window.confirm("Supprimer ce produit ?");
        if (!ok) return;

        setDeletingId(productId);
        try {
            const res = await fetch(`/api/products/${productId}`, {
                method: "DELETE",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                throw new Error(`HTTP ${res.status}`);
            }

            setProducts(prev => prev.filter(p => p.id !== productId));
        } catch (err: any) {
            setError(err.message ?? "Erreur lors de la suppression du produit");
        } finally {
            setDeletingId(null);
        }
    }

    function handleEdit(productId: number) {
        navigate(`/app/products/${productId}/edit`);
    }

    return (
        <section>
            <h1 className="text-2xl font-bold mb-4">Gestion des produits</h1>

            <form
                onSubmit={handleCreateProduct}
                className="flex flex-wrap gap-3 mb-6"
            >
                <input
                    className="border rounded px-2 py-1"
                    type="text"
                    placeholder="Nom du produit"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    required
                />
                <input
                    className="border rounded px-2 py-1"
                    type="number"
                    placeholder="Prix"
                    min={0}
                    step="0.01"
                    value={price}
                    onChange={e => setPrice(e.target.value)}
                    required
                />
                <button className="bg-blue-600 text-white rounded px-4 py-1">
                    Créer
                </button>
            </form>

            {error && <p className="text-red-600 mb-4">{error}</p>}

            <ul className="space-y-2">
                {products.map(p => (
                    <li
                        key={p.id}
                        className="border rounded px-3 py-2 flex justify-between items-center gap-3"
                    >
                        <div className="flex flex-col">
                            <span>{p.name}</span>
                            <span className="text-sm text-gray-600">{p.price.toFixed(2)} €</span>
                        </div>

                        <div className="flex gap-2">
                            <button
                                className="bg-gray-700 text-white rounded px-3 py-1"
                                type="button"
                                onClick={() => handleEdit(p.id)}
                            >
                                Modifier
                            </button>

                            <button
                                className="bg-red-600 text-white rounded px-3 py-1 disabled:opacity-50"
                                type="button"
                                onClick={() => handleDelete(p.id)}
                                disabled={deletingId === p.id}
                            >
                                {deletingId === p.id ? "Suppression..." : "Supprimer"}
                            </button>
                        </div>
                    </li>
                ))}
            </ul>
        </section>
    );
}
