# MyPayIndia API Minecraft Mod

## Projektstruktur

```
src/
├── main/java/com/emilsleeper/mypayindia_api/
│   └── api/
│       ├── ApiClient.java          # Low-level HTTP client mit Cookie-Management
│       └── MyPayIndiaAPI.java       # High-level API wrapper mit allen Endpoints
│
└── client/java/com/emilsleeper/mypayindia_api/
    ├── MypayindiaAPIClient.java     # Client mod initializer
    └── client/commands/
        └── PaymentCommand.java      # Beispiel Minecraft Commands
```

## Verwendung

### 1. API Funktionen (Server-seitig) - `src/main/java`

Alle API-Funktionen sind in der `MyPayIndiaAPI` Klasse verfügbar:

```java
import com.emilsleeper.mypayindia_api.api.MyPayIndiaAPI;
import com.google.gson.JsonObject;

// Login
JsonObject loginResponse = MyPayIndiaAPI.login("username", "password");

// Account Info abrufen
JsonObject info = MyPayIndiaAPI.getInfo();

// Leaderboard abrufen
JsonObject leaderboard = MyPayIndiaAPI.getLeaderboard();

// Geld transferieren
JsonObject transfer = MyPayIndiaAPI.transfer("100.00", 41, "Optional note");

// Payment Link erstellen
JsonObject paymentLink = MyPayIndiaAPI.createPaymentLink(50.00, "Some note");

// Payment Link anfordern
JsonObject claimedLink = MyPayIndiaAPI.claimPaymentLink("token123");

// Session Cookie abrufen/speichern
String cookie = MyPayIndiaAPI.getSessionCookie();
MyPayIndiaAPI.setSessionCookie(cookie);
```

### 2. Minecraft Commands (Client-seitig) - `src/client/java`

Die Commands müssen in der `MypayindiaAPIClient` registriert werden:

```java
// MypayindiaAPIClient.java
public void onInitializeClient() {
    PaymentCommand.register();
}
```

#### Verfügbare Commands:

- `/mypay login <username> <password>` - Einloggen
- `/mypay info` - Account-Informationen
- `/mypay balance` - Kontostand anzeigen
- `/mypay leaderboard` - Top 5 Leaderboard

### 3. Session-Management

Die PHPSESSID wird automatisch gespeichert nach dem Login:

```java
// Nach erfolgreichem Login
String sessionCookie = MyPayIndiaAPI.getSessionCookie();
// Speichere dies für später: config.yml, NBT-Tag, etc.

// Session später wiederherstellen
MyPayIndiaAPI.setSessionCookie(sessionCookie);

// Logout
MyPayIndiaAPI.clearSessionCookie();
```

## Verfügbare API-Endpoints

### Authentication
- `login(username, password)` - Einloggen

### Account Information
- `getInfo()` - Account-Informationen
- `getLeaderboard()` - Global Leaderboard
- `getTransactionHistory()` - Transaction History

### Transactions
- `getTransaction(id)` - Spezifische Transaction
- `getTransactionByTransactionId(transactionId)` - Transaction by ID
- `transfer(amount, recipientUsername, note)` - Geldtransfer (Username statt ID!)
- `transfer(amount, recipientUsername)` - Geldtransfer ohne Note

### Payment Links
- `createPaymentLink(amount, note)` - Payment Link erstellen
- `createPaymentLink(amount)` - Payment Link ohne Note
- `getPaymentLink(token)` - Payment Link Informationen
- `listPaymentLinks()` - Alle Payment Links auflisten
- `claimPaymentLink(token)` - Payment Link anfordern
- `cancelPaymentLink(token)` - Payment Link stornieren

### Merchant
- `createTransaction(merchantKey, amount, orderId)` - Merchant Payment

### Team
- `getTeam()` - Team-Informationen

## Error Handling

```java
try {
    JsonObject response = MyPayIndiaAPI.login("user", "pass");
    
    if (response.has("success") && response.get("success").getAsBoolean()) {
        // Erfolgreich
        String message = response.get("message").getAsString();
    } else {
        // Fehler
        String error = response.get("message").getAsString();
        System.err.println("API Error: " + error);
    }
} catch (IOException e) {
    System.err.println("Connection Error: " + e.getMessage());
}
```

## Dependencies

- **Gson 2.10.1** - JSON Verarbeitung
- **Fabric API** - Minecraft Mod Framework
- **Fabric Loader** - Mod Loader

Diese werden automatisch von Gradle heruntergeladen.
