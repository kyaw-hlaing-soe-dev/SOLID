package com.legacy.ecommerce.manager;

import java.util.Map;

public class ExecutiveSummaryHtmlFormatter {

    public String format(int totalOrders, int cancelledOrders, double totalRevenue,
                         Map<String, Double> revenueByCategory) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h1>Enterprise E-Commerce Executive Sales Summary</h1>");
        html.append("<p>Total Orders: ").append(totalOrders).append("</p>");
        html.append("<p>Cancelled Orders: ").append(cancelledOrders).append("</p>");
        html.append("<p>Total Net Revenue: $").append(String.format("%.2f", totalRevenue)).append("</p>");
        html.append("<h2>Category Breakdown</h2><ul>");
        for (Map.Entry<String, Double> entry : revenueByCategory.entrySet()) {
            html.append("<li>").append(entry.getKey()).append(": $")
                    .append(String.format("%.2f", entry.getValue())).append("</li>");
        }
        html.append("</ul></body></html>");
        return html.toString();
    }
}
