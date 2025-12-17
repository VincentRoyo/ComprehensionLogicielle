import React, {
    createContext, type ReactNode,
    useContext,
    useEffect,
    useState,
} from "react";

type User = {
    email: string;
};

type AuthContextType = {
    user: User | null;
    token: string | null;
    isLoading: boolean;
    login: (email: string, password: string) => Promise<void>;
    logout: () => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const STORAGE_TOKEN_KEY = "authToken";
const STORAGE_EMAIL_KEY = "authEmail";

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    // Récupération du token au démarrage
    useEffect(() => {
        const storedToken = localStorage.getItem(STORAGE_TOKEN_KEY);
        const storedEmail = localStorage.getItem(STORAGE_EMAIL_KEY);
        if (storedToken && storedEmail) {
            setToken(storedToken);
            setUser({ email: storedEmail });
        }
        setIsLoading(false);
    }, []);

    async function login(email: string, password: string) {
        const res = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
        });

        if (!res.ok) {
            throw new Error(`Login failed: HTTP ${res.status}`);
        }

        const data = await res.json();
        const jwt = data.token as string; // adapte si ton backend renvoie autre chose

        setToken(jwt);
        setUser({ email });

        localStorage.setItem(STORAGE_TOKEN_KEY, jwt);
        localStorage.setItem(STORAGE_EMAIL_KEY, email);
    }

    function logout() {
        setToken(null);
        setUser(null);
        localStorage.removeItem(STORAGE_TOKEN_KEY);
        localStorage.removeItem(STORAGE_EMAIL_KEY);
    }

    const value: AuthContextType = {
        user,
        token,
        isLoading,
        login,
        logout,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextType {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}
