package com.emilsleeper.mypayindia_api.client.commands;

import com.emilsleeper.mypayindia_api.api.MyPayIndiaAPI;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

/**
 * Additional payment-related commands
 */
public class TransactionCommand {

    public static void register() {
        // Register client commands using callback
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Main command: /mypayindia transactions
            var mypayindiaTransactions = net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("mypayindia")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("transactions")
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("list")
                        .executes(context -> listTransactions(context.getSource()))
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("info")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("id", IntegerArgumentType.integer())
                            .executes(context -> getTransactionInfo(
                                context.getSource(),
                                IntegerArgumentType.getInteger(context, "id")
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("transfer")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("recipient", StringArgumentType.word())
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(context -> transferMoney(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "recipient"),
                                    String.valueOf(DoubleArgumentType.getDouble(context, "amount")),
                                    null
                                ))
                                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("note", StringArgumentType.string())
                                    .executes(context -> transferMoney(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "recipient"),
                                        String.valueOf(DoubleArgumentType.getDouble(context, "amount")),
                                        StringArgumentType.getString(context, "note")
                                    ))
                                )
                            )
                        )
                    )
                );

            // Alias command: /mpi transactions
            var mpiTransactions = net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("mpi")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("transactions")
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("list")
                        .executes(context -> listTransactions(context.getSource()))
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("info")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("id", IntegerArgumentType.integer())
                            .executes(context -> getTransactionInfo(
                                context.getSource(),
                                IntegerArgumentType.getInteger(context, "id")
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("transfer")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("recipient", StringArgumentType.word())
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("amount", DoubleArgumentType.doubleArg())
                                .executes(context -> transferMoney(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "recipient"),
                                    String.valueOf(DoubleArgumentType.getDouble(context, "amount")),
                                    null
                                ))
                            )
                        )
                    )
                );

            dispatcher.register(mypayindiaTransactions);
            dispatcher.register(mpiTransactions);
        });
    }

    private static int listTransactions(FabricClientCommandSource source) {
        new Thread(() -> {
            try {
                System.out.println("DEBUG: Calling getTransactionHistory()");
                JsonObject response = MyPayIndiaAPI.getTransactionHistory();
                System.out.println("DEBUG: getTransactionHistory() response: " + response);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§6§l=== Transaction History ==="));
                    if (response.has("data") && response.get("data").isJsonArray()) {
                        var data = response.getAsJsonArray("data");
                        int count = Math.min(10, data.size());
                        for (int i = 0; i < count; i++) {
                            JsonObject txn = data.get(i).getAsJsonObject();
                            int txnId = txn.get("id").getAsInt();
                            String sender = txn.get("sender_name").getAsString();
                            String target = txn.get("target_name").getAsString();
                            String amount = txn.get("amount").getAsString();
                            String status = txn.get("status").getAsString();

                            String coloredId = getColoredTransactionId(status, txnId);
                            Text clickableText = Text.literal(coloredId + " " + sender + " → " + target + ": §r§6₹" + amount)
                                .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mypayindia transactions info " + txnId))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("§eClick for transaction details"))));
                            
                            source.sendFeedback(clickableText);
                        }
                    }
                } else {
                    source.sendFeedback(Text.literal("§c✗ " + response.get("message").getAsString()));
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Exception in listTransactions: " + e.getMessage());
                e.printStackTrace();
                source.sendFeedback(Text.literal("§c✗ Error: " + e.getMessage()));
            }
        }).start();
        return 1;
    }

    private static int getTransactionInfo(FabricClientCommandSource source, int transactionId) {
        new Thread(() -> {
            try {
                System.out.println("DEBUG: Calling getTransaction(" + transactionId + ")");
                JsonObject response = MyPayIndiaAPI.getTransaction(transactionId);
                System.out.println("DEBUG: getTransaction() response: " + response);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§6§l=== " + getColoredStatus(response.get("status").getAsString()).replace(response.get("status").getAsString(), "")+"§lTransaction Details §6§l==="));
                    source.sendFeedback(Text.literal("§6§lID: §r§o" + response.get("id").getAsString() + " - " + response.get("transaction_id").getAsString()));
                    source.sendFeedback(Text.literal("§6§lFrom: §r§7§n" + response.get("sender_name").getAsString()));
                    source.sendFeedback(Text.literal("§6§lTo: §r§7§n" + response.get("target_name").getAsString()));
                    source.sendFeedback(Text.literal("§6§lAmount: §r§6₹" + response.get("amount").getAsString()));
                    source.sendFeedback(Text.literal("§6§lStatus: " + getColoredStatus(response.get("status").getAsString()).substring(0, 1).toUpperCase() + getColoredStatus(response.get("status").getAsString()).substring(1)));
                    source.sendFeedback(Text.literal("§6§lDate: §r§o" + response.get("created").getAsString()));
                } else {
                    source.sendFeedback(Text.literal("§c✗ " + response.get("message").getAsString()));
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Exception in getTransactionInfo: " + e.getMessage());
                e.printStackTrace();
                source.sendFeedback(Text.literal("§c✗ Error: " + e.getMessage()));
            }
        }).start();
        return 1;
    }

    private static int transferMoney(FabricClientCommandSource source, String recipient, String amount, String note) {
        new Thread(() -> {
            try {
                System.out.println("DEBUG: Calling transfer(" + amount + ", " + recipient + ", " + note + ")");
                JsonObject response = MyPayIndiaAPI.transfer(amount, recipient, note);
                System.out.println("DEBUG: transfer() response: " + response);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a✓ Transfer successful!"));
                    source.sendFeedback(Text.literal("§6Amount: §a₹" + response.get("amount").getAsString()));
                    source.sendFeedback(Text.literal("§6New Balance: §a₹" + response.get("new_balance").getAsString()));
                } else {
                    source.sendFeedback(Text.literal("§c✗ Transfer failed: " + response.get("message").getAsString()));
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Exception in transferMoney: " + e.getMessage());
                e.printStackTrace();
                source.sendFeedback(Text.literal("§c✗ Error: " + e.getMessage()));
            }
        }).start();
        return 1;
    }

    private static String getColoredStatus(String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
                return "§a" + status;
            case "pending":
                return "§e" + status;
            case "rejected":
                return "§c" + status;
            default:
                return "§f" + status; // Default to white for unknown statuses
        }
    }

    private static String getColoredTransactionId(String status, int txnId) {
        String color;
        switch (status.toLowerCase()) {
            case "confirmed":
                color = "§a"; // Green
                break;
            case "pending":
                color = "§e"; // Yellow
                break;
            case "rejected":
                color = "§c"; // Red
                break;
            default:
                color = "§f"; // White for unknown statuses
        }
        return color + "#" + txnId;
    }
}
