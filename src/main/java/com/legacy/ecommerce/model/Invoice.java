package com.legacy.ecommerce.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy Invoice entity.
 * Mixes data attributes with text/HTML document formatting logic.
 */
public class Invoice {
    private String invoiceNumber;
    private String orderId;
    private String customerName;
    private String customerEmail;
    private String billingAddress;
    private List<OrderItem> lineItems = new ArrayList<>();
    private double subtotal;
    private double discountAmount;
    private double taxAmount;
    private double shippingCost;
    private double grandTotal;
    private LocalDateTime issuedDate;

    public Invoice() {
        this.issuedDate = LocalDateTime.now();
    }

    public Invoice(String invoiceNumber, String orderId, String customerName, String customerEmail, String billingAddress) {
        this.invoiceNumber = invoiceNumber;
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.billingAddress = billingAddress;
        this.issuedDate = LocalDateTime.now();
    }

    // --- Embedded formatting logic (SRP Violation) ---

    public String generatePlainText() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("                  COMMERCIAL INVOICE                 \n");
        sb.append("====================================================\n");
        sb.append("Invoice Number: ").append(invoiceNumber).append("\n");
        sb.append("Order Reference: ").append(orderId).append("\n");
        sb.append("Date:           ").append(issuedDate).append("\n");
        sb.append("Customer:       ").append(customerName).append(" (").append(customerEmail).append(")\n");
        sb.append("Billing Address:").append(billingAddress).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-20s %-8s %-10s %-10s\n", "Item", "Qty", "Unit Price", "Total"));
        sb.append("----------------------------------------------------\n");
        for (OrderItem item : lineItems) {
            sb.append(String.format("%-20s %-8d $%-9.2f $%-9.2f\n",
                    item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()));
        }
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("Subtotal:       $%.2f\n", subtotal));
        sb.append(String.format("Discount:      -$%.2f\n", discountAmount));
        sb.append(String.format("Tax:            $%.2f\n", taxAmount));
        sb.append(String.format("Shipping:       $%.2f\n", shippingCost));
        sb.append(String.format("Grand Total:    $%.2f\n", grandTotal));
        sb.append("====================================================\n");
        sb.append("Thank you for your business!\n");
        return sb.toString();
    }

    public String generateHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='invoice-box'><h1>Invoice ").append(invoiceNumber).append("</h1>");
        sb.append("<p>Order: ").append(orderId).append(" | Date: ").append(issuedDate).append("</p>");
        sb.append("<p>Customer: ").append(customerName).append("</p>");
        sb.append("<table border='1'><tr><th>Item</th><th>Qty</th><th>Price</th><th>Total</th></tr>");
        for (OrderItem item : lineItems) {
            sb.append("<tr><td>").append(item.getProductName()).append("</td>")
              .append("<td>").append(item.getQuantity()).append("</td>")
              .append("<td>$").append(String.format("%.2f", item.getUnitPrice())).append("</td>")
              .append("<td>$").append(String.format("%.2f", item.getLineTotal())).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("<p>Grand Total: $").append(String.format("%.2f", grandTotal)).append("</p></div>");
        return sb.toString();
    }

    // --- Getters and Setters ---

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public List<OrderItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<OrderItem> lineItems) {
        this.lineItems = lineItems;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDateTime issuedDate) {
        this.issuedDate = issuedDate;
    }
}
