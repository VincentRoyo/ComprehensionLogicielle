import React, { useEffect, useState } from "react";
import { useAuth } from "../auth/AuthContext";

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

            <ul className="space-y-1">
                {products.map(p => (
                    <li
                        key={p.id}
                        className="border rounded px-3 py-1 flex justify-between"
                    >
                        <span>{p.name}</span>
                        <span>{p.price.toFixed(2)} €</span>
                    </li>
                ))}
            </ul>
        </section>
    );
}
