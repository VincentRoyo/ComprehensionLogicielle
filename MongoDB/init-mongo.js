const dbName = 'appdb';
db = db.getSiblingDB(dbName);

// Nettoyage (si tu veux repartir propre)
db.users.deleteMany({});
db.products.deleteMany({});

const users = [];
for (let i = 1; i <= 10; i++) {
  users.push({
    name: `User ${i}`,
    age: 20 + i,
    email: `user${i}@example.com`,
    password: `password${i}`
  });
}
db.users.insertMany(users);


const products = [];
const baseDate = new Date("2026-01-01T00:00:00Z");


for (let i = 1; i <= 100; i++) {
  const expiration = new Date(baseDate);
  expiration.setDate(baseDate.getDate() + i);

  // BigDecimal côté Java => on stocke un Decimal128 avec NumberDecimal
  const price = NumberDecimal((i * 1.5).toFixed(2));

  products.push({
    name: `Product ${i}`,
    price: price,
    expirationDate: expiration
  });
}

db.products.insertMany(products);

// Petite vérif
print("=== SEED DONE ===");
print("Users count:", db.users.countDocuments());
print("Products count:", db.products.countDocuments());
