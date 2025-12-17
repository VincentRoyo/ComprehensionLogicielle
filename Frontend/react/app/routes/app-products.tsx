import React, { useState } from "react";
import { useAuth } from "../auth/AuthContext";

type Product = {
    id: number;
    name: string;
    price: number;
};

export default function ProductsPage() {
    const { token } = useAuth();

    const [nameFilter, setNameFilter] = useState("");
    const [minPrice, setMinPrice] = useState<string>("");
    const [products, setProducts] = useState<Product[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    async function handleSearch(e: React.FormEvent) {
        e.preventDefault();
        setError(null);
        setIsLoading(true);

        const params = new URLSearchParams();
        if (nameFilter.trim() !== "") params.set("name", nameFilter.trim());
        if (minPrice.trim() !== "") params.set("minPrice", minPrice.trim());

        const url = `/api/products?${params.toString()}`;

        try {
            const res = await fetch(url, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            setProducts(data);
        } catch (err: any) {
            setError(err.message ?? "Erreur lors de la récupération des produits");
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <section>
            <h1 className="text-2xl font-bold mb-4">Recherche de produits</h1>

            <form
                onSubmit={handleSearch}
                className="flex flex-wrap gap-4 items-end mb-6"
            >
                <div className="flex flex-col gap-1">
                    <label className="text-sm">Nom</label>
                    <input
                        className="border rounded px-2 py-1"
                        type="text"
                        value={nameFilter}
                        onChange={e => setNameFilter(e.target.value)}
                        placeholder="Nom du produit"
                    />
                </div>

                <div className="flex flex-col gap-1">
                    <label className="text-sm">Prix minimal</label>
                    <input
                        className="border rounded px-2 py-1"
                        type="number"
                        min={0}
                        value={minPrice}
                        onChange={e => setMinPrice(e.target.value)}
                    />
                </div>

                <button
                    className="bg-blue-600 text-white rounded px-4 py-2 disabled:opacity-50"
                    type="submit"
                    disabled={isLoading}
                >
                    {isLoading ? "Recherche..." : "Rechercher"}
                </button>
            </form>

            {error && <p className="text-red-600 mb-4">{error}</p>}

            <ul className="space-y-2">
                {products.map(p => (
                    <li
                        key={p.id}
                        className="border rounded px-3 py-2 flex justify-between"
                    >
                        <span>{p.name}</span>
                        <span>{p.price.toFixed(2)} €</span>
                    </li>
                ))}
            </ul>
        </section>
    );
}
