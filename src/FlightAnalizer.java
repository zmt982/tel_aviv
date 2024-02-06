import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FlightAnalizer {
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(new File("src/resources/tickets.json"));
            JsonNode tickets = rootNode.get("tickets");

            Map<String, Integer> minFlightTimes = new HashMap<>();
            Map<String, Double> priceStats = new HashMap<>();

            for (JsonNode ticket : tickets) {
                String from = ticket.get("origin_name").asText();
                String to = ticket.get("destination_name").asText();
                String carrier = ticket.get("carrier").asText();
                int duration = calculateDuration(ticket.get("departure_time").asText(),
                        ticket.get("arrival_time").asText());
                double price = (ticket.get("price").asDouble());

                if ((from.equals("Владивосток") && to.equals("Тель-Авив")) || (from.equals("Тель-Авив")
                        && to.equals("Владивосток"))) {
                    if (!minFlightTimes.containsKey(carrier) || duration < minFlightTimes.get(carrier)) {
                        minFlightTimes.put(carrier, duration);
                    }

                    if (!priceStats.containsKey(carrier)) {
                        priceStats.put(carrier, price);
                    } else {
                        double avgPrice = priceStats.get(carrier);
                        priceStats.put(carrier, avgPrice + (price - avgPrice) / 2);
                    }
                }
            }

            System.out.println("Минимальное время полета между городами Владивосток и Тель-Авик для каждого" +
                    " авиаперевозчика:");
            minFlightTimes.forEach((carrier, time) -> System.out.println(carrier + ": " + time + " минут"));

            System.out.println("\nРазница между средней ценой и медианой для полета между городами Владивосток и" +
                    " Тель-Авив:");
            priceStats.forEach((carrier, avgPrice) -> System.out.println(carrier + ": " + Math.abs(avgPrice -
                    calculateMedianPrice(priceStats, carrier))));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int calculateDuration(String departureTime, String arrivalTime) {
        String[] departureParts = departureTime.split(":");
        String[] arrivalParts = arrivalTime.split(":");
        int depatureHour = Integer.parseInt(departureParts[0]);
        int departureMinute = Integer.parseInt(departureParts[1]);
        int arrivalHour = Integer.parseInt(arrivalParts[0]);
        int arrivalMinute = Integer.parseInt(arrivalParts[1]);

        return (arrivalHour - depatureHour) * 60 + (arrivalMinute - departureMinute);
    }

    private static double calculateMedianPrice(Map<String, Double> prices, String carrier) {
        double[] carrierPrices = prices.values().stream().mapToDouble(Double::doubleValue).toArray();
        Arrays.sort(carrierPrices);
        int length = carrierPrices.length;
        if (length % 2 == 0) {
            return (carrierPrices[length / 2 - 1] + carrierPrices[length / 2]) / 2;
        } else {
            return carrierPrices[length / 2];
        }
    }
}