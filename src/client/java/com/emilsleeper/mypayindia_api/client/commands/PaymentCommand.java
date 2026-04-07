package com.emilsleeper.mypayindia_api.client.commands;

import com.emilsleeper.mypayindia_api.api.MyPayIndiaAPI;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

public class PaymentCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            var loginCommand = LiteralArgumentBuilder.<FabricClientCommandSource>literal("login")
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("username", StringArgumentType.word())
                    .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument("password", StringArgumentType.word())
                        .executes(PaymentCommand::executeLoginCommand)
                    )
                );

            loginCommand.requires(source -> true);

            var mypayindiaCommand = net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("mypayindia")
                .then(loginCommand)
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("logout")
                    .executes(context -> logoutCommand(context.getSource()))
                )
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("info")
                    .executes(context -> infoCommand(context.getSource()))
                )
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("balance")
                    .executes(context -> balanceCommand(context.getSource()))
                )
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("leaderboard")
                    .executes(context -> leaderboardCommand(context.getSource()))
                );

            var mpiCommand = net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("mpi")
                .then(loginCommand)
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("logout")
                    .executes(context -> logoutCommand(context.getSource()))
                )
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("info")
                    .executes(context -> infoCommand(context.getSource()))
                )
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("balance")
                    .executes(context -> balanceCommand(context.getSource()))
                )
                .then(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("leaderboard")
                    .executes(context -> leaderboardCommand(context.getSource()))
                );

            dispatcher.register(mypayindiaCommand);
            dispatcher.register(mpiCommand);
        });
    }

    private static int executeLoginCommand(CommandContext<FabricClientCommandSource> context) {
        String username = StringArgumentType.getString(context, "username");
        String password = StringArgumentType.getString(context, "password");

        if (context.getSource().getClient() != null && context.getSource().getClient().inGameHud != null) {
            try {
                context.getSource().getClient().inGameHud.getChatHud().resetScroll();
                context.getSource().getClient().inGameHud.getChatHud().addToMessageHistory("");
            } catch (Exception e) {
            }
        }

        return loginCommand(context.getSource(), username, password);
    }

    private static int loginCommand(FabricClientCommandSource source, String username, String password) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.login(username, password);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a✓ Successfully logged in!"));
                } else {
                    String message = response.has("message") ? response.get("message").getAsString() : "Login failed";
                    source.sendFeedback(Text.literal("§c✗ Login failed: " + message));
                }
            } catch (Exception e) {
                source.sendFeedback(Text.literal("§c✗ Error: " + e.getMessage()));
            }
        }).start();
        return 1;
    }

    private static int infoCommand(FabricClientCommandSource source) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.getInfo();
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a=== Account Information ==="));
                    source.sendFeedback(Text.literal("§6Username: §f" + response.get("username").getAsString()));
                    source.sendFeedback(Text.literal("§6Name: §f" + response.get("first_name").getAsString() + " " + response.get("last_name").getAsString()));
                    source.sendFeedback(Text.literal("§6Email: §f" + response.get("email").getAsString()));
                    source.sendFeedback(Text.literal("§6Balance: §f₹" + response.get("balance").getAsString()));
                    source.sendFeedback(Text.literal("§6Created: §f" + response.get("created").getAsString()));
                } else {
                    source.sendFeedback(Text.literal("§c✗ " + response.get("message").getAsString()));
                }
            } catch (Exception e) {
                source.sendFeedback(Text.literal("§c✗ Error: " + e.getMessage()));
            }
        }).start();
        return 1;
    }

    private static int balanceCommand(FabricClientCommandSource source) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.getInfo();
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§6Balance: §a₹" + response.get("balance").getAsString()));
                } else {
                    source.sendFeedback(Text.literal("§c✗ " + response.get("message").getAsString()));
                }
            } catch (Exception e) {
                source.sendFeedback(Text.literal("§c✗ Error: " + e.getMessage()));
            }
        }).start();
        return 1;
    }

    private static int leaderboardCommand(FabricClientCommandSource source) {
        new Thread(() -> {
            try {
                JsonObject response = MyPayIndiaAPI.getLeaderboard();
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    source.sendFeedback(Text.literal("§a=== Global Leaderboard ==="));
                    if (response.has("data") && response.get("data").isJsonArray()) {
                        var data = response.getAsJsonArray("data");
                        int count = Math.min(5, data.size());
                        for (int i = 0; i < count; i++) {
                            JsonObject user = data.get(i).getAsJsonObject();
                            source.sendFeedback(Text.literal("§6#" + (i + 1) + " " + user.get("username").getAsString() +
                                " - §a₹" + user.get("balance").getAsString()));
                        }
                    }
                } else {
                    source.sendFeedback(Text.literal("§c✗ " + response.get("message").getAsString()));
                }
            } catch (Exception e) {
                source.sendFeedback(Text.literal("§c✗ Error: " + e.getMessage()));
            }
        }).start();
        return 1;
    }

    private static int logoutCommand(FabricClientCommandSource source) {
        MyPayIndiaAPI.clearSessionCookie();
        source.sendFeedback(Text.literal("§a✓ Successfully logged out!"));
        return 1;
    }
}
