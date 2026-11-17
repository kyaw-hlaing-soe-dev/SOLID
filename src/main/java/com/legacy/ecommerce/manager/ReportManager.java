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
    private final ReportCsvFormatter csvFormatter = new ReportCsvFormatter();
    private final ExecutiveSummaryHtmlFormatter htmlFormatter = new ExecutiveSummaryHtmlFormatter();

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

    public String exportSalesReportToCsv() {
        return csvFormatter.format(database.getOrders().values());
    }

    public String generateExecutiveSummaryHtml() {
        return htmlFormatter.format(
                getTotalOrdersCount(),
                getCancelledOrdersCount(),
                calculateTotalRevenue(),
                getRevenueByCategory());
    }

    public void emailDailySalesReportToAdmin(String adminEmail) {
        String html = generateExecutiveSummaryHtml();
        notificationService.sendEmail(adminEmail, "Daily Executive Sales Report", html);
    }
}
