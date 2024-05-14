package com.tel_aviv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Analysis {
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(new File("C:/workspace/tel_aviv/src/main/resources/tickets.json"));
            JsonNode tickets = rootNode.get("tickets");

            Map<String, Integer> minFlightTimes = new HashMap<>();
            List<Integer> prices = new ArrayList<>();
            extractData(tickets, minFlightTimes, prices);
            System.out.println("Минимальное время полета между городами Владивосток и Тель-Авив для каждого авиаперевозчика:");
            minFlightTimes.forEach((carrier, duration) -> System.out.println(carrier + ": " + duration + " минут"));
            System.out.println("\nРазница между средней ценой и медианой для полета между городами Владивосток и Тель-Авив: " + calculateDifference(prices));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void extractData(JsonNode tickets, Map<String, Integer> minFlightTimes, List<Integer> prices) {
        for (JsonNode ticket : tickets) {
            String from = ticket.get("origin_name").asText();
            String to = ticket.get("destination_name").asText();
            String carrier = ticket.get("carrier").asText();
            String strDepartureDate = ticket.get("departure_date").asText();
            String strDepartureTime = ticket.get("departure_time").asText();
            String strArrivalDate = ticket.get("arrival_date").asText();
            String strArrivalTime = ticket.get("arrival_time").asText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
            LocalTime departureTime = LocalTime.parse(strDepartureDate + " " + strDepartureTime, formatter);
            LocalTime arrivalTime = LocalTime.parse(strArrivalDate + " " + strArrivalTime, formatter);
            Duration duration = Duration.between(departureTime, arrivalTime);
            int price = ticket.get("price").asInt();

            if ((from.equals("Владивосток") && to.equals("Тель-Авив"))
                    || (from.equals("Тель-Авив") && to.equals("Владивосток"))) {
                if (!minFlightTimes.containsKey(carrier)
                        || (int) duration.toMinutes() < minFlightTimes.get(carrier)) {
                    minFlightTimes.put(carrier, (int) duration.toMinutes());
                }
                prices.add(price);
            }
        }
    }

    private static int calculateDifference(List<Integer> prices) {
        Collections.sort(prices);
        int averagePrice = (int) prices.stream().mapToInt(Integer::intValue).average().orElse(0);
        int median;
        if (prices.size() % 2 == 0) {
            median = (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2;
        } else {
            median = prices.get(prices.size() / 2);
        }
        return averagePrice - median;
    }

}