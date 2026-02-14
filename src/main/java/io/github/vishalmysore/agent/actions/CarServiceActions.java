package io.github.vishalmysore.agent.actions;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Car service actions that can be performed by the agent
 * These actions are automatically discovered by Tools4AI and exposed to the AI
 */
@Agent(groupName = "CarServiceAgent", groupDescription = "AI agent that provides car information, comparisons, pricing, and booking services")
@Component
@Slf4j
public class CarServiceActions {

    private static final Map<String, CarInfo> CAR_DATABASE = new HashMap<>();

    static {
        // Initialize car database
        CAR_DATABASE.put("tesla-model-3", new CarInfo("Tesla Model 3", "Electric", 45000, 358, "★★★★★"));
        CAR_DATABASE.put("toyota-camry", new CarInfo("Toyota Camry", "Hybrid", 28000, 686, "★★★★☆"));
        CAR_DATABASE.put("bmw-x5", new CarInfo("BMW X5", "Gas", 62000, 449, "★★★★★"));
        CAR_DATABASE.put("honda-civic", new CarInfo("Honda Civic", "Gas", 24000, 714, "★★★★☆"));
        CAR_DATABASE.put("ford-mustang", new CarInfo("Ford Mustang", "Gas", 55000, 500, "★★★★★"));
    }

    @Action(description = "Get detailed information about a specific car model including type, price, range, and rating. Available models: tesla-model-3, toyota-camry, bmw-x5, honda-civic, ford-mustang")
    public String getCarInfo(String carModel) {

        log.info("Getting car info for: {}", carModel);

        CarInfo car = CAR_DATABASE.get(carModel.toLowerCase());
        if (car == null) {
            return "Car model not found. Available models: " + String.join(", ", CAR_DATABASE.keySet());
        }

        return String.format("🚗 %s\nType: %s\nPrice: $%,d\nRange: %d miles\nRating: %s",
                car.name, car.type, car.price, car.range, car.rating);
    }

    @Action(description = "Compare two car models side by side showing type, price, range, and ratings with a recommendation. Provide car model names like tesla-model-3, toyota-camry, bmw-x5, honda-civic, or ford-mustang")
    public String compareCars(String car1, String car2) {

        log.info("Comparing cars: {} vs {}", car1, car2);

        CarInfo carInfo1 = CAR_DATABASE.get(car1.toLowerCase());
        CarInfo carInfo2 = CAR_DATABASE.get(car2.toLowerCase());

        if (carInfo1 == null || carInfo2 == null) {
            return "One or both car models not found. Available: " + String.join(", ", CAR_DATABASE.keySet());
        }

        StringBuilder comparison = new StringBuilder();
        comparison.append("🚗 CAR COMPARISON 🚗\n\n");
        comparison.append(String.format("%-20s vs %-20s\n", carInfo1.name, carInfo2.name));
        comparison.append("─".repeat(45)).append("\n");
        comparison.append(String.format("Type:     %-15s vs %-15s\n", carInfo1.type, carInfo2.type));
        comparison.append(String.format("Price:    $%-14d vs $%-14d\n", carInfo1.price, carInfo2.price));
        comparison.append(String.format("Range:    %-15d vs %-15d miles\n", carInfo1.range, carInfo2.range));
        comparison.append(String.format("Rating:   %-15s vs %-15s\n", carInfo1.rating, carInfo2.rating));

        // Add recommendation
        comparison.append("\n💡 Recommendation: ");
        if (carInfo1.price < carInfo2.price && carInfo1.rating.equals(carInfo2.rating)) {
            comparison.append(carInfo1.name).append(" offers better value!");
        } else if (carInfo2.price < carInfo1.price && carInfo1.rating.equals(carInfo2.rating)) {
            comparison.append(carInfo2.name).append(" offers better value!");
        } else {
            comparison.append("Both are great choices depending on your priorities!");
        }

        return comparison.toString();
    }

    @Action(description = "Get pricing information for all cars of a specific type. Car types available: electric, hybrid, or gas. Returns a list of all cars of that type with their prices")
    public String getCarPricing(String carType) {

        log.info("Getting pricing for car type: {}", carType);

        StringBuilder result = new StringBuilder();
        result.append("💰 Pricing for ").append(carType).append(" cars:\n\n");

        CAR_DATABASE.values().stream()
                .filter(car -> car.type.equalsIgnoreCase(carType))
                .forEach(car -> result.append(String.format("%-20s: $%,d\n", car.name, car.price)));

        if (result.length() == 0) {
            return "No cars found for type: " + carType;
        }

        return result.toString();
    }

    @Action(description = "List all available car models in the database with their identifiers. Use this to see what cars you can query for more information")
    public String listCarTypes() {
        log.info("Listing all car types");

        return "Available car models:\n" + String.join("\n", CAR_DATABASE.keySet()) +
                "\n\nUse getCarInfo(carModel) to get details about any model.";
    }

    @Action(description = "Check the status of a car service booking by booking ID. Returns booking status, pickup date, location, and service details")
    public String getBookingStatus(String bookingId) {

        log.info("Checking booking status for: {}", bookingId);

        // Mock booking status
        return String.format("📋 Booking Status for %s:\n" +
                "Status: Confirmed ✓\n" +
                "Pickup Date: 2026-02-15\n" +
                "Location: Downtown Service Center\n" +
                "Service: Full Inspection & Oil Change", bookingId);
    }

    // Helper class to store car information
    private static class CarInfo {
        String name;
        String type;
        int price;
        int range;
        String rating;

        CarInfo(String name, String type, int price, int range, String rating) {
            this.name = name;
            this.type = type;
            this.price = price;
            this.range = range;
            this.rating = rating;
        }
    }
}
