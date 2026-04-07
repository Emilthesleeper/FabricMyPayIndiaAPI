# MyPayIndia API Implementation - Zusammenfassung

## ✅ Was wurde implementiert

### 1. **Web API Funktionen** (`src/main/java`)
   - **ApiClient.java**: Low-level HTTP-Client mit automatischem Cookie-Management
   - **MyPayIndiaAPI.java**: High-level API mit allen 13 Endpoints

### 2. **Minecraft Commands** (`src/client/java`)
   - **PaymentCommand.java**: Login, Info, Balance, Leaderboard
   - **TransactionCommand.java**: Transaction History, Details, Transfer
   - **PaymentLinkCommand.java**: Payment Links erstellen, verwalten, anfordern
   - **MypayindiaAPIClient.java**: Command-Registrierung

### 3. **Automatisches Session-Management**
   - PHPSESSID wird automatisch gespeichert nach Login
   - Cookie wird bei allen Requests automatisch mitgesendet
   - Manuelles Speichern/Laden des Cookies möglich

---

## 📁 Projektstruktur

```
MyPayIndia API/
├── src/
│   ├── main/
│   │   └── java/com/emilsleeper/mypayindia_api/
│   │       └── api/
│   │           ├── ApiClient.java          ← HTTP-Client
│   │           └── MyPayIndiaAPI.java       ← API-Wrapper
│   │
│   └── client/
│       └── java/com/emilsleeper/mypayindia_api/
│           ├── MypayindiaAPIClient.java     ← Client Init
│           └── client/commands/
│               ├── PaymentCommand.java      ← /mypayindia login/info/balance/leaderboard
│               ├── TransactionCommand.java  ← /mypayindia transactions ...
│               └── PaymentLinkCommand.java  ← /mypayindia links ...
│
├── build.gradle                  ← UPDATED (Gson hinzugefügt)
├── API_USAGE.md                  ← Dokumentation
├── EXAMPLES.md                   ← Code-Beispiele
└── IMPLEMENTATION_SUMMARY.md     ← Diese Datei
```

---

## 🔌 Verfügbare API-Endpoints

### Authentifizierung
- ✅ `login(username, password)`

### Account
- ✅ `getInfo()`
- ✅ `getLeaderboard()`

### Transaktionen
- ✅ `getTransactionHistory()`
- ✅ `getTransaction(id)`
- ✅ `getTransactionByTransactionId(transactionId)`
- ✅ `transfer(amount, recipientId, note)`

### Payment Links
- ✅ `createPaymentLink(amount, note)`
- ✅ `getPaymentLink(token)`
- ✅ `listPaymentLinks()`
- ✅ `claimPaymentLink(token)`
- ✅ `cancelPaymentLink(token)`

### Merchant
- ✅ `createTransaction(merchantKey, amount, orderId)`

### Team
- ✅ `getTeam()`

---

## 📝 Verfügbare Minecraft Commands

### `/mypayindia` Commands (und `/mpi` als Alias)
```
/mypayindia login <username> <password>  - Einloggen (nicht in History gespeichert)
/mypayindia info                          - Account-Informationen
/mypayindia balance                       - Kontostand
/mypayindia leaderboard                   - Top 5 Leaderboard

/mypayindia transactions list             - Transaction History (klickbar!)
/mypayindia transactions info <id>        - Transaction Details
/mypayindia transactions transfer <username> <amount> - Geld transferieren

/mypayindia links create <amount> [note]  - Payment Link erstellen
/mypayindia links list                    - Payment Links auflisten
/mypayindia links info <token>            - Link-Details anzeigen
/mypayindia links claim <token>           - Link anfordern
/mypayindia links cancel <token>          - Link stornieren
```

### Klickbare Transaction List
Die Transaction History zeigt jetzt **klickbare Einträge** an:
- **Hover**: "Click for transaction details"
- **Click**: Führt automatisch `/mypayindia transactions info <id>` aus

### Transfer korrigiert
Der Transfer-Befehl verwendet jetzt **Usernamen** statt IDs:
```
/mypayindia transactions transfer JohnDoe 50.00
```
Statt der alten falschen Syntax mit IDs.

---

## 🚀 Verwendung

### Server-seitig (Main)
```java
import com.emilsleeper.mypayindia_api.api.MyPayIndiaAPI;

JsonObject response = MyPayIndiaAPI.login("user", "pass");
if (response.get("success").getAsBoolean()) {
    JsonObject info = MyPayIndiaAPI.getInfo();
    System.out.println(info.get("balance"));
}
```

### Client-seitig (Commands)
```java
// Commands sind automatisch registriert
// Einfach im Spiel verwenden: /mypay login username password
```

---

## 🔐 Session-Verwaltung

```java
// Session speichern
String cookie = MyPayIndiaAPI.getSessionCookie();

// Session später wiederherstellen
MyPayIndiaAPI.setSessionCookie(cookie);

// Session löschen (Logout)
MyPayIndiaAPI.clearSessionCookie();
```

---

## 🛠️ Dependencies
- ✅ Gson 2.10.1 (JSON)
- ✅ Fabric API
- ✅ Fabric Loader

---

## ❓ Häufige Fragen

**F: Wo speichere ich den Session-Cookie?**
A: Du kannst ihn in einer Config-Datei speichern oder in NBT-Tags. Siehe `EXAMPLES.md` für Code-Beispiele.

**F: Wie handle ich Errors?**
A: Jede Antwort hat ein `success`-Feld. Nutze `response.get("message")` für Fehlermeldungen.

**F: Kann ich neue Commands hinzufügen?**
A: Ja! Erstelle eine neue Klasse in `client/commands/`, implementiere `register()` und rufe sie in `MypayindiaAPIClient` auf.

**F: Funktioniert es auf dem Server?**
A: Die API-Funktionen ja, aber Commands nur auf dem Client. Für Server-Commands brauchst du Fabric-Server-API.

---

## 📚 Weitere Ressourcen
- `API_USAGE.md` - Detaillierte API-Dokumentation
- `EXAMPLES.md` - Code-Beispiele für alle Funktionen
- https://mypayindia.com/dev - Offizielle API-Dokumentation
