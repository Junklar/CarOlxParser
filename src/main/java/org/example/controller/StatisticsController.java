package org.example.controller;
import org.example.model.Car;
import org.example.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/overview")
    public String getCarStatistics(Model model) {

        long totalCars = statisticsService.getTotalCarsCount();
        model.addAttribute("totalCars", totalCars);


        BigDecimal averagePrice = statisticsService.getAverageCarPrice();
        model.addAttribute("averagePrice", averagePrice);


        Map<String, Long> carsByCountry = statisticsService.getCarsByCountry();
        model.addAttribute("carsByCountry", carsByCountry);


        List<Car> mostExpensiveCars = statisticsService.getMostExpensiveCars(5);
        model.addAttribute("mostExpensiveCars", mostExpensiveCars);

        return "statistics";
    }

    @GetMapping("/price-range")
    public String getCarsByPriceRange(Model model) {

        Map<String, Long> carsByPriceRange = statisticsService.getCarsByPriceRange();
        model.addAttribute("carsByPriceRange", carsByPriceRange);

        return "price-range-statistics";
    }
}
