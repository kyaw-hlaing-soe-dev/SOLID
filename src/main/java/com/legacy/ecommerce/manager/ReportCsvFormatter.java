package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.model.Order;

import java.util.Collection;

public class ReportCsvFormatter {

    public String format(Collection<Order> orders) {
        StringBuilder csv = new StringBuilder();
        csv.append("Order ID,User ID,Status,Subtotal,Discount,Tax,Shipping,Total,Created At\n");
        for (Order o : orders) {
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
}
