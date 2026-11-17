package com.legacy.ecommerce.manager;

import com.legacy.ecommerce.model.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class UserCsvExporter {

    public String exportUsersToCsv(Collection<User> users) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Name,Email,Tier,LoyaltyPoints,Active\n");
        for (User user : users) {
            csv.append(user.toCsvRow()).append("\n");
        }
        return csv.toString();
    }

    public void exportUsersToFile(Collection<User> users, String filePath) throws IOException {
        String csvContent = exportUsersToCsv(users);
        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(csvContent);
        }
    }
}
