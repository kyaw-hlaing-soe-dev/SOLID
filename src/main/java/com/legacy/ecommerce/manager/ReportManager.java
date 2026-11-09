package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.data.LegacyDatabase;
import com.legacy.ecommerce.model.Order;
import com.legacy.ecommerce.model.OrderItem;
import com.legacy.ecommerce.model.OrderStatus;
import com.legacy.ecommerce.model.Product;

import java.util.*;

/**
 * Legacy ReportManager.
 * Mixes sales analytics, calculations, CSV formatting, HTML report generation,
 * and automated email dispatching to company executives.
 */
public class ReportManager {
    private final LegacyDatabase database = LegacyDatabase.getInstance();
    private final NotificationService notificationService = new NotificationService();

    public double calculateTotalRevenue() {
        double total = 0.0;
        for (Order order : database.getOrders().values()) {
            if (order.getStatus() != OrderStatus.CANCELLED) {
                total += order.getTotalAmount();
            }
        }
        return total;
    }

    public int getTotalOrdersCount() {
        return database.getOrders().size();
    }

    public int getCancelledOrdersCount() {
        int count = 0;
        for (Order order : database.getOrders().values()) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                count++;
            }
        }
        return count;
    }

    public Map<String, Integer> getTopSellingProducts(int limit) {
        Map<String, Integer> productSales = new HashMap<>();

        for (Order order : database.getOrders().values()) {
            if (order.getStatus() != OrderStatus.CANCELLED) {
                for (OrderItem item : order.getItems()) {
                    productSales.put(item.getProductName(),
                            productSales.getOrDefault(item.getProductName(), 0) + item.getQuantity());
                }
            }
        }

        // Sort descending by quantity
        List<Map.Entry<String, Integer>> list = new ArrayList<>(productSales.entrySet());
        list.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        Map<String, Integer> topProducts = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (count++ >= limit) break;
            topProducts.put(entry.getKey(), entry.getValue());
        }
        return topProducts;
    }

    public Map<String, Double> getRevenueByCategory() {
        Map<String, Double> categoryRevenue = new HashMap<>();

        for (Order order : database.getOrders().values()) {
            if (order.getStatus() != OrderStatus.CANCELLED) {
                for (OrderItem item : order.getItems()) {
                    Product prod = database.getProducts().get(item.getProductId());
                    String category = (prod != null) ? prod.getCategory() : "Uncategorized";
                    categoryRevenue.put(category,
                            categoryRevenue.getOrDefault(category, 0.0) + item.getLineTotal());
                }
            }
        }
        return categoryRevenue;
    }

    // --- CSV Report Generation (SRP Violation) ---
    public String exportSalesReportToCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("Order ID,User ID,Status,Subtotal,Discount,Tax,Shipping,Total,Created At\n");
        for (Order o : database.getOrders().values()) {
            csv.append(o.getId()).append(",")
               .append(o.getUserId()).append(",")
               .append(o.getStatus()).append(",")
               .append(String.format("%.2f", o.getSubtotal())).append(",")
               .append(String.format("%.2f", o.getDiscountAmount())).append(",")
               .append(String.format("%.2f", o.getTaxAmount())).append(",")
               .append(String.format("%.2f", o.getShippingCost())).append(",")
               .append(String.format("%.2f", o.getTotalAmount())).append(",")
               .append(o.getCreatedAt()).append("\n");
        }
        return csv.toString();
    }

    // --- HTML Report Generation & Emailing (SRP Violation) ---
    public String generateExecutiveSummaryHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h1>Enterprise E-Commerce Executive Sales Summary</h1>");
        html.append("<p>Total Orders: ").append(getTotalOrdersCount()).append("</p>");
        html.append("<p>Cancelled Orders: ").append(getCancelledOrdersCount()).append("</p>");
        html.append("<p>Total Net Revenue: $").append(String.format("%.2f", calculateTotalRevenue())).append("</p>");
        html.append("<h2>Category Breakdown</h2><ul>");
        for (Map.Entry<String, Double> entry : getRevenueByCategory().entrySet()) {
            html.append("<li>").append(entry.getKey()).append(": $").append(String.format("%.2f", entry.getValue())).append("</li>");
        }
        html.append("</ul></body></html>");
        return html.toString();
    }

    public void emailDailySalesReportToAdmin(String adminEmail) {
        String html = generateExecutiveSummaryHtml();
        notificationService.sendEmail(adminEmail, "Daily Executive Sales Report", html);
    }
}
