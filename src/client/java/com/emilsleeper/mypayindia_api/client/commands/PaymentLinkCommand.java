package com.emilsleeper.mypayindia_api.client.commands;

import com.emilsleeper.mypayindia_api.api.MyPayIndiaAPI;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.client.MinecraftClient;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;

/**
 * Payment Link commands for MyPayIndia API
 */
public class PaymentLinkCommand {

    private static final String BASE_URL = "https://mypayindia.com/pay?token=";

    public static void register() {
        // Register client commands using callback
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Main command: /mypayindia links
            var mypayindiaLinks = net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("mypayindia")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("links")
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("create")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("amount", DoubleArgumentType.doubleArg())
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("note", StringArgumentType.greedyString())
                                .executes(context -> createPaymentLink(
                                    context.getSource(),
                                    DoubleArgumentType.getDouble(context, "amount"),
                                    StringArgumentType.getString(context, "note")
                                ))
                            )
                            .executes(context -> createPaymentLink(
                                context.getSource(),
                                DoubleArgumentType.getDouble(context, "amount"),
                                null
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("list")
                        .executes(context -> listPaymentLinks(context.getSource()))
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("info")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("token", StringArgumentType.word())
                            .executes(context -> getPaymentLinkInfo(
                                context.getSource(),
                                StringArgumentType.getString(context, "token")
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("claim")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("link", StringArgumentType.greedyString())
                            .executes(context -> claimPaymentLink(
                                context.getSource(),
                                StringArgumentType.getString(context, "link")
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("cancel")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("link", StringArgumentType.string())
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("confirm")
                                .executes(context -> cancelPaymentLink(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "link"),
                                    true
                                ))
                            )
                            .executes(context -> cancelPaymentLink(
                                context.getSource(),
                                StringArgumentType.getString(context, "link"),
                                false
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("test")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("token", StringArgumentType.word())
                            .executes(context -> testPaymentLink(
                                context.getSource(),
                                StringArgumentType.getString(context, "token")
                            ))
                        )
                    )
                );

            // Alias command: /mpi links
            var mpiLinks = net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("mpi")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("links")
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("create")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("amount", DoubleArgumentType.doubleArg())
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("note", StringArgumentType.greedyString())
                                .executes(context -> createPaymentLink(
                                    context.getSource(),
                                    DoubleArgumentType.getDouble(context, "amount"),
                                    StringArgumentType.getString(context, "note")
                                ))
                            )
                            .executes(context -> createPaymentLink(
                                context.getSource(),
                                DoubleArgumentType.getDouble(context, "amount"),
                                null
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("list")
                        .executes(context -> listPaymentLinks(context.getSource()))
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("info")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("token", StringArgumentType.word())
                            .executes(context -> getPaymentLinkInfo(
                                context.getSource(),
                                StringArgumentType.getString(context, "token")
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("claim")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("link", StringArgumentType.greedyString())
                            .executes(context -> claimPaymentLink(
                                context.getSource(),
                                StringArgumentType.getString(context, "link")
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("cancel")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("link", StringArgumentType.string())
                            .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("confirm")
                                .executes(context -> cancelPaymentLink(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "link"),
                                    true
                                ))
                            )
                            .executes(context -> cancelPaymentLink(
                                context.getSource(),
                                StringArgumentType.getString(context, "link"),
                                false
                            ))
                        )
                    )
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("test")
                        .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("token", StringArgumentType.word())
                            .executes(context -> testPaymentLink(
                                context.getSource(),
                                StringArgumentType.getString(context, "token")
                            ))
                        )
                    )
                );

            dispatcher.register(mypayindiaLinks);
            dispatcher.register(mpiLinks);
        });
    }

    private static int createPaymentLink(FabricClientCommandSource source, double amount, String note) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.createPaymentLink(amount, note);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    String token = response.get("token").getAsString();
                    String linkUrl = response.get("link_url").getAsString();
                    double newBalance = response.get("new_balance").getAsDouble();

                    source.sendFeedback(Text.literal("§a✓ Payment Link created!"));
                    source.sendFeedback(Text.literal("§6Amount: §a₹" + amount));
                    source.sendFeedback(Text.literal("§6New Balance: §a₹" + newBalance));

                    // Make the link clickable - CORRECTED API
                    Text clickableLink = Text.literal("§6Link: §f" + linkUrl)
                        .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, linkUrl))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.literal("§eClick to open payment link"))));
                    source.sendFeedback(clickableLink);
                } else {
                    // Do nothing if API fails, just display the link as is
                }
            } catch (Exception e) {
                // Do nothing on error, just display the link as is
            }
        }).start();
        return 1;
    }

    private static int listPaymentLinks(FabricClientCommandSource source) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.listPaymentLinks();
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a=== Your Payment Links ==="));
                    if (response.has("data") && response.get("data").isJsonObject()) {
                        JsonObject dataObj = response.getAsJsonObject("data");
                        if (dataObj.has("links") && dataObj.get("links").isJsonArray()) {
                            var links = dataObj.getAsJsonArray("links");
                            if (links.size() == 0) {
                                source.sendFeedback(Text.literal("§7No payment links found."));
                                return;
                            }

                            for (int i = 0; i < links.size(); i++) {
                                JsonObject link = links.get(i).getAsJsonObject();
                                String token = link.get("token").getAsString();
                                double amount = link.get("amount").getAsDouble();
                                String status = link.get("status").getAsString();
                                String created = link.get("created").getAsString();

                                String coloredStatus = getColoredLinkStatus(status);
                                String linkUrl = BASE_URL + token;

                                Text clickableItem;
                                clickableItem = Text.literal("§a--- Link #" + (i + 1) + " --- §6Amount: §a₹" + amount + " §6Status: " + coloredStatus + " §6Created: §f" + created)
                                    .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mypayindia links info " + token))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal("§eClick for link details"))));

                                source.sendFeedback(clickableItem);
                            }
                        }
                    }
                } else {
                    // Do nothing if API fails, just display the link as is
                }
            } catch (Exception e) {
                // Do nothing on error, just display the link as is
            }
        }).start();
        return 1;
    }

    private static int getPaymentLinkInfo(FabricClientCommandSource source, String token) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.getPaymentLink(token);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a=== Payment Link Details ==="));
                    source.sendFeedback(Text.literal("§6Token: §f" + token));
                    source.sendFeedback(Text.literal("§6Amount: §a₹" + response.get("amount").getAsString()));
                    source.sendFeedback(Text.literal("§6Author: §f" + response.get("author_username").getAsString()));
                    source.sendFeedback(Text.literal("§6Author Name: §f" + response.get("author_name").getAsString()));
                    source.sendFeedback(Text.literal("§6Note: §f" + response.get("note").getAsString()));
                    source.sendFeedback(Text.literal("§6Created: §f" + response.get("created").getAsString()));

                    if (response.has("current_user_balance")) {
                        double balance = response.get("current_user_balance").getAsDouble();
                        boolean canAccept = response.get("can_accept").getAsBoolean();
                        source.sendFeedback(Text.literal("§6Your Balance: §a₹" + balance));
                        source.sendFeedback(Text.literal("§6Can Accept: " + (canAccept ? "§aYes" : "§cNo")));

                        if (canAccept) {
                            // CORRECTED API for claim button
                            String linkUrl = BASE_URL + token;
                            Text claimButton = Text.literal("§a[CLAIM LINK]")
                                .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mypayindia links claim \"" + linkUrl + "\""))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("§eClick to claim this payment link"))));
                            source.sendFeedback(claimButton);
                        }

                        // Add cancel button if the link is active
                        if (response.has("status") && "active".equals(response.get("status").getAsString())) {
                            String linkUrl = BASE_URL + token;
                            Text cancelButton = Text.literal("§c[CANCEL LINK]")
                                .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mypayindia links cancel " + linkUrl))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("§eClick to cancel this payment link"))));
                            source.sendFeedback(cancelButton);
                        }
                    }
                } else {
                    // Do nothing if API fails, just display the link as is
                }
            } catch (Exception e) {
                // Do nothing on error, just display the link as is
            }
        }).start();
        return 1;
    }

    private static int claimPaymentLink(FabricClientCommandSource source, String link) {
        new Thread(() -> {
            try {
                String token = link.startsWith(BASE_URL) ? link.substring(BASE_URL.length()) : link;
                JsonObject response = MyPayIndiaAPI.claimPaymentLink(token);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    JsonObject data = response.getAsJsonObject("data");
                    String transactionId = data.get("transaction_id").getAsString();
                    double amount = data.get("amount").getAsDouble();
                    double newBalance = data.get("new_balance").getAsDouble();
                    String fromUser = data.get("from_user").getAsString();

                    source.sendFeedback(Text.literal("§a✓ Payment link claimed successfully!"));
                    source.sendFeedback(Text.literal("§6From: §f" + fromUser));
                    source.sendFeedback(Text.literal("§6Amount: §a₹" + amount));
                    source.sendFeedback(Text.literal("§6Transaction ID: §f" + transactionId));
                    source.sendFeedback(Text.literal("§6New Balance: §a₹" + newBalance));
                } else {
                    // Do nothing if API fails, just display the link as is
                }
            } catch (Exception e) {
                // Do nothing on error, just display the link as is
            }
        }).start();
        return 1;
    }

    private static int cancelPaymentLink(FabricClientCommandSource source, String link, boolean confirm) {
        if (!confirm) {
            source.sendFeedback(Text.literal("§eAre you sure you want to cancel this payment link?"));
            Text confirmButton = Text.literal("§a[CONFIRM CANCEL]")
                .styled(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mypayindia links cancel " + link + " confirm"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§eClick to confirm cancellation"))));
            source.sendFeedback(confirmButton);
            return 1;
        }

        new Thread(() -> {
            try {
                String token = link.startsWith(BASE_URL) ? link.substring(BASE_URL.length()) : link;
                JsonObject response = MyPayIndiaAPI.cancelPaymentLink(token);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a✓ Payment link cancelled successfully!"));
                } else {
                    // Do nothing if API fails, just display the link as is
                }
            } catch (Exception e) {
                // Do nothing on error, just display the link as is
            }
        }).start();
        return 1;
    }

    private static int testPaymentLink(FabricClientCommandSource source, String token) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.getPaymentLink(token);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a=== Payment Link Test Details ==="));
                    source.sendFeedback(Text.literal("§6Token: §f" + token));
                    source.sendFeedback(Text.literal("§6Amount: §a₹" + response.get("amount").getAsString()));
                    source.sendFeedback(Text.literal("§6Author: §f" + response.get("author_username").getAsString()));
                    source.sendFeedback(Text.literal("§6Note: §f" + response.get("note").getAsString()));
                    source.sendFeedback(Text.literal("§6Created: §f" + response.get("created").getAsString()));
                    source.sendFeedback(Text.literal("§a✓ Payment link test completed!"));
                } else {
                    // Do nothing if API fails, just display the link as is
                }
            } catch (Exception e) {
                // Do nothing on error, just display the link as is
            }
        }).start();
        return 1;
    }

    public static void displayPaymentLinkInfo(String token) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.getPaymentLink(token);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§a=== Detected Payment Link ==="), false);
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§6Amount: §a₹" + response.get("amount").getAsString()), false);
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§6Author: §f" + response.get("author_username").getAsString()), false);
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§6Note: §f" + response.get("note").getAsString()), false);

                    if (response.has("current_user_balance")) {
                        double balance = response.get("current_user_balance").getAsDouble();
                        boolean canAccept = response.get("can_accept").getAsBoolean();
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§6Your Balance: §a₹" + balance), false);
                        MinecraftClient.getInstance().player.sendMessage(Text.literal("§6Can Accept: " + (canAccept ? "§aYes" : "§cNo")), false);

                        if (canAccept) {
                            String linkUrl = BASE_URL + token;
                            Text claimButton = Text.literal("§a[CLAIM LINK]")
                                .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mypayindia links claim \"" + linkUrl + "\""))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("§eClick to claim this payment link"))));
                            MinecraftClient.getInstance().player.sendMessage(claimButton, false);
                        }

                        // Add cancel button if the link is active
                        if (response.has("status") && "active".equals(response.get("status").getAsString())) {
                            String linkUrl = BASE_URL + token;
                            Text cancelButton = Text.literal("§c[CANCEL LINK]")
                                .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mypayindia links cancel " + linkUrl))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("§eClick to cancel this payment link"))));
                            MinecraftClient.getInstance().player.sendMessage(cancelButton, false);
                        }
                    }
                } else {
                    MinecraftClient.getInstance().player.sendMessage(Text.literal("§c✗ Detected invalid payment link."), false);
                }
            } catch (Exception e) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§c✗ Error detecting payment link: " + e.getMessage()), false);
            }
        }).start();
    }

    private static String getColoredLinkStatus(String status) {
        switch (status.toLowerCase()) {
            case "active":
                return "§a" + status;
            case "claimed":
                return "§c" + status;
            case "cancelled":
                return "§7" + status;
            default:
                return "§f" + status;
        }
    }
}
