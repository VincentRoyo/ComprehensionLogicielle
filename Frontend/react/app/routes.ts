import {
    type RouteConfig,
    route,
    index,
} from "@react-router/dev/routes";

export default [
    index("./routes/_index.tsx"),
    route("login", "./routes/login.tsx"),

    route("app", "./routes/app-layout.tsx", [
        index("./routes/app._index.tsx"),
        route("products", "./routes/app-products.tsx"),
        route("products/:id/edit", "./routes/app-edit-product.tsx"),
        route("me", "./routes/app-me.tsx"),
        route("admin/users", "./routes/app-admin-users.tsx"),
        route("admin/products", "./routes/app-admin-products.tsx"),
    ]),
] satisfies RouteConfig;
