package com.emilsleeper.mypayindia_api;

import com.emilsleeper.mypayindia_api.client.commands.PaymentCommand;
import com.emilsleeper.mypayindia_api.client.commands.TransactionCommand;
import com.emilsleeper.mypayindia_api.client.commands.PaymentLinkCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MypayindiaAPIClient implements ClientModInitializer {
    private static final Pattern PAYMENT_LINK_PATTERN = Pattern.compile("https://mypayindia\\.com/pay(?:/link)?\\?token=([a-zA-Z0-9]+)");

    @Override
    public void onInitializeClient() {
        // Register all client commands
        PaymentCommand.register();
        TransactionCommand.register();
        PaymentLinkCommand.register();

        // Register chat event to detect payment
        /* ClientReceiveMessageEvents.GAME_MESSAGE.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String messageText = message.getString();
            Matcher matcher = PAYMENT_LINK_PATTERN.matcher(messageText);
            if (matcher.find()) {
                String token = matcher.group(1);
                // Show special display for payment link
                PaymentLinkCommand.displayPaymentLinkInfo(token);
            }
        }); */
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String messageText = message.getString();
            Matcher matcher = PAYMENT_LINK_PATTERN.matcher(messageText);
            if (matcher.find()) {
                String token = matcher.group(1);
                // Show special display for payment link
                PaymentLinkCommand.displayPaymentLinkInfo(token);
            }
        });
    }
}
