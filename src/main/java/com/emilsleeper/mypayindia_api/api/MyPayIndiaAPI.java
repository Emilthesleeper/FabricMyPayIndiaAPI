package com.emilsleeper.mypayindia_api.api;

import com.google.gson.JsonObject;
import java.io.IOException;

/**
 * MyPayIndia API wrapper with all available endpoints
 */
public class MyPayIndiaAPI {

    // ==================== Authentication ====================

    /**
     * Login and start a session with the server
     * @param username Username
     * @param password Password
     * @return Response containing success status
     */
    public static JsonObject login(String username, String password) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);
        return ApiClient.post("/login", body);
    }

    // ==================== Account Information ====================

    /**
     * Get information about the logged in account
     * @return Account information (username, email, balance, etc.)
     */
    public static JsonObject getInfo() throws IOException {
        return ApiClient.get("/info");
    }

    /**
     * Get global leaderboard
     * @return Array of users with their balance and creation date
     */
    public static JsonObject getLeaderboard() throws IOException {
        return ApiClient.get("/leaderboard");
    }

    // ==================== Transactions ====================

    /**
     * Get transaction history for logged in account
     * @return Array of transactions with details
     */
    public static JsonObject getTransactionHistory() throws IOException {
        return ApiClient.get("/transaction_history");
    }

    /**
     * Get information about a specific transaction by ID
     * @param id Transaction ID
     * @return Transaction details
     */
    public static JsonObject getTransaction(int id) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("id", id);
        return ApiClient.post("/transaction", body);
    }

    /**
     * Get information about a specific transaction by transaction ID
     * @param transactionId Transaction ID (e.g., "TXN-1234567890-09af")
     * @return Transaction details
     */
    public static JsonObject getTransactionByTransactionId(String transactionId) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("transaction_id", transactionId);
        return ApiClient.post("/transaction", body);
    }

    /**
     * Transfer money to recipient
     * @param amount Amount to transfer (e.g., "123.45")
     * @param recipientUsername Recipient username (not ID!)
     * @param note Optional note for the transfer
     * @return Transfer response with new balance
     */
    public static JsonObject transfer(String amount, String recipientUsername, String note) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("amount", amount);
        body.addProperty("recipient", recipientUsername);
        if (note != null && !note.isEmpty()) {
            body.addProperty("note", note);
        }
        return ApiClient.post("/transfer", body);
    }

    /**
     * Transfer money to recipient without note
     * @param amount Amount to transfer (e.g., "123.45")
     * @param recipientUsername Recipient username (not ID!)
     * @return Transfer response with new balance
     */
    public static JsonObject transfer(String amount, String recipientUsername) throws IOException {
        return transfer(amount, recipientUsername, null);
    }

    // ==================== Payment Links ====================

    /**
     * Create a payment link to share money with anyone who has a MyPayIndia account
     * @param amount Amount for the link (e.g., 123.45)
     * @param note Optional note for the payment
     * @return Payment link details with token and URL
     */
    public static JsonObject createPaymentLink(double amount, String note) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("amount", amount);
        if (note != null && !note.isEmpty()) {
            body.addProperty("note", note);
        }
        return ApiClient.post("/create_payment_link", body);
    }

    /**
     * Create a payment link without note
     * @param amount Amount for the link (e.g., 123.45)
     * @return Payment link details with token and URL
     */
    public static JsonObject createPaymentLink(double amount) throws IOException {
        return createPaymentLink(amount, null);
    }

    /**
     * Get information about a payment link by its token
     * @param token Payment link token
     * @return Payment link details
     */
    public static JsonObject getPaymentLink(String token) throws IOException {
        return ApiClient.get("/get_payment_link?token=" + token);
    }

    /**
     * List payment links for logged in account
     * @return Array of payment links with their details and status
     */
    public static JsonObject listPaymentLinks() throws IOException {
        return ApiClient.get("/list_payment_links");
    }

    /**
     * Claim a payment link
     * @param token Payment link token
     * @return Claim response with transaction details
     */
    public static JsonObject claimPaymentLink(String token) throws IOException {
        return ApiClient.get("/claim_payment_link?token=" + token);
    }

    /**
     * Cancel a payment link
     * @param token Payment link token
     * @return Cancel response with refund details
     */
    public static JsonObject cancelPaymentLink(String token) throws IOException {
        return ApiClient.get("/cancel_payment_link?token=" + token);
    }

    // ==================== Merchant Payments ====================

    /**
     * Create a payment transaction for merchant
     * @param merchantKey Merchant key
     * @param amount Amount to charge (e.g., 123.45)
     * @param orderId Unique order identifier
     * @return Payment URL for user to complete transaction
     */
    public static JsonObject createTransaction(String merchantKey, double amount, String orderId) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("merchant_key", merchantKey);
        body.addProperty("amount", amount);
        body.addProperty("order_id", orderId);
        return ApiClient.post("/pay/create_transaction", body);
    }

    // ==================== Team Information ====================

    /**
     * Get team members with their details
     * @return Array of team members with name, role, image, join date, and socials
     */
    public static JsonObject getTeam() throws IOException {
        return ApiClient.get("/team");
    }

    // ==================== Session Management ====================

    /**
     * Get the current session cookie
     * @return PHPSESSID cookie or null if not logged in
     */
    public static String getSessionCookie() {
        return ApiClient.getSessionCookie();
    }

    /**
     * Set the session cookie manually (e.g., to restore a previous session)
     * @param cookie PHPSESSID cookie value
     */
    public static void setSessionCookie(String cookie) {
        ApiClient.setSessionCookie(cookie);
    }

    /**
     * Clear the session cookie (logout)
     */
    public static void clearSessionCookie() {
        ApiClient.clearSessionCookie();
    }
}
