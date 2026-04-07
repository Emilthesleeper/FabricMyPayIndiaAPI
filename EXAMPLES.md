# Implementierungs-Beispiele

## Quick Start

### 1. Login
```java
JsonObject response = MyPayIndiaAPI.login("myusername", "mypassword");

if (response.get("success").getAsBoolean()) {
    System.out.println("Login erfolgreich!");
    String cookie = MyPayIndiaAPI.getSessionCookie();
    // Speichere den Cookie für späteren Gebrauch
}
```

### 2. Account-Informationen abrufen
```java
JsonObject info = MyPayIndiaAPI.getInfo();

if (info.get("success").getAsBoolean()) {
    String username = info.get("username").getAsString();
    String balance = info.get("balance").getAsString();
    System.out.println("Benutzer: " + username + ", Balance: ₹" + balance);
}
```

### 3. Geld transferieren
```java
JsonObject transfer = MyPayIndiaAPI.transfer("50.00", "JohnDoe", "Vielen Dank!");

if (transfer.get("success").getAsBoolean()) {
    System.out.println("Transfer erfolgreich!");
    System.out.println("Neue Balance: ₹" + transfer.get("new_balance").getAsString());
}
```

### 4. Payment Link erstellen
```java
JsonObject paymentLink = MyPayIndiaAPI.createPaymentLink(100.00, "Spende");

if (paymentLink.get("success").getAsBoolean()) {
    String token = paymentLink.get("token").getAsString();
    String link = paymentLink.get("link_url").getAsString();
    System.out.println("Payment Link: " + link);
}
```

### 5. Payment Link anfordern
```java
JsonObject claimed = MyPayIndiaAPI.claimPaymentLink("token123");

if (claimed.get("success").getAsBoolean()) {
    JsonObject data = claimed.getAsJsonObject("data");
    System.out.println("₹" + data.get("amount").getAsString() + " von " + data.get("from_user").getAsString());
}
```

## Minecraft Commands

### Payment Commands
```
/mypayindia login <username> <password>  - Einloggen (nicht in History gespeichert)
/mypayindia info                          - Account-Informationen
/mypayindia balance                       - Kontostand
/mypayindia leaderboard                   - Top 5 Leaderboard
```

### Transaction Commands
```
/mypayindia transactions list              - Transaction History (klickbar!)
/mypayindia transactions info <id>         - Transaction Details
/mypayindia transactions transfer <username> <amount> - Geld transferieren
```

### Payment Link Commands
```
/mypayindia links create <amount> [note]   - Payment Link erstellen
/mypayindia links list                     - Payment Links auflisten
/mypayindia links info <token>             - Link-Details anzeigen
/mypayindia links claim <token>            - Link anfordern
/mypayindia links cancel <token>           - Link stornieren
```

### Aliases
```
/mpi login <username> <password>           - Gleiche wie /mypayindia
/mpi transactions transfer JohnDoe 50.00   - Gleiche wie /mypayindia
/mpi links create 100.00 "Thanks!"         - Gleiche wie /mypayindia
```

## Session-Persistierung

### Session speichern
```java
// Nach erfolgreichem Login
String cookie = MyPayIndiaAPI.getSessionCookie();

// In Config speichern (Beispiel mit JSON)
JsonObject config = new JsonObject();
config.addProperty("session_cookie", cookie);
Files.write(Paths.get("config.json"), gson.toJson(config).getBytes());
```

### Session laden
```java
// Beim Starten des Mods
JsonObject config = gson.fromJson(Files.readString(Paths.get("config.json")), JsonObject.class);
String cookie = config.get("session_cookie").getAsString();
MyPayIndiaAPI.setSessionCookie(cookie);

// Teste, ob Session noch gültig ist
JsonObject info = MyPayIndiaAPI.getInfo();
if (!info.get("success").getAsBoolean()) {
    // Session expired, neu einloggen
    MyPayIndiaAPI.login("username", "password");
}
```

## Error Handling

```java
public static void safeTransfer(String amount, int recipient) {
    try {
        JsonObject response = MyPayIndiaAPI.transfer(amount, recipient);
        
        if (!response.has("success")) {
            System.err.println("Invalid response format");
            return;
        }
        
        if (response.get("success").getAsBoolean()) {
            System.out.println("✓ Erfolgreich transferiert");
        } else {
            String message = response.get("message").getAsString();
            System.err.println("✗ Fehler: " + message);
            
            // Spezifische Fehlermeldungen
            if (message.contains("Insufficient balance")) {
                System.err.println("Nicht genug Guthaben");
            } else if (message.contains("Unauthorized")) {
                System.err.println("Session abgelaufen, bitte neu einloggen");
                MyPayIndiaAPI.clearSessionCookie();
            }
        }
    } catch (IOException e) {
        System.err.println("Netzwerkfehler: " + e.getMessage());
    }
}
```

## Custom Commands hinzufügen

```java
// Neue Datei: src/client/java/com/emilsleeper/mypayindia_api/client/commands/LinkCommand.java

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class LinkCommand {
    public static void register() {
        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("links")
                .then(ClientCommandManager.literal("create")
                    .then(ClientCommandManager.argument("amount", DoubleArgumentType.doubleArg())
                        .executes(context -> createLink(
                            context.getSource(),
                            DoubleArgumentType.getDouble(context, "amount")
                        ))
                    )
                )
                .then(ClientCommandManager.literal("list")
                    .executes(context -> listLinks(context.getSource()))
                )
        );
    }
    
    private static int createLink(FabricClientCommandSource source, double amount) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.createPaymentLink(amount);
                if (response.get("success").getAsBoolean()) {
                    String link = response.get("link_url").getAsString();
                    source.sendFeedback(Text.literal("§aLink erstellt: " + link));
                } else {
                    source.sendFeedback(Text.literal("§c✗ Fehler: " + response.get("message").getAsString()));
                }
            } catch (Exception e) {
                source.sendFeedback(Text.literal("§c✗ Fehler: " + e.getMessage()));
            }
        }).start();
        return 1;
    }
    
    private static int listLinks(FabricClientCommandSource source) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.listPaymentLinks();
                if (response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a=== Meine Payment Links ==="));
                    var links = response.getAsJsonArray("data");
                    for (int i = 0; i < links.size(); i++) {
                        JsonObject link = links.get(i).getAsJsonObject();
                        source.sendFeedback(Text.literal("§6₹" + link.get("amount").getAsString() + 
                            " - Status: " + link.get("status").getAsString()));
                    }
                }
            } catch (Exception e) {
                source.sendFeedback(Text.literal("§c✗ Fehler: " + e.getMessage()));
            }
        }).start();
        return 1;
    }
}
```

Dann in `MypayindiaAPIClient` registrieren:
```java
LinkCommand.register();
```
