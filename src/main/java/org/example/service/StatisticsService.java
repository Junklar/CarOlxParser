package org.example.service;
import org.example.model.Car;
import org.example.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private CarRepository carRepository;

    public long getTotalCarsCount() {
        return carRepository.count();
    }

    public BigDecimal getAverageCarPrice() {
        List<Car> cars = carRepository.findAll();
        if (cars.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalPrice = cars.stream()
                .map(Car::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrice.divide(BigDecimal.valueOf(cars.size()), 2, RoundingMode.HALF_UP);
    }

    public Map<String, Long> getCarsByCountry() {
        return carRepository.findAll().stream()
                .collect(Collectors.groupingBy(Car::getCountry, Collectors.counting()));
    }

    public List<Car> getMostExpensiveCars(int limit) {
        return carRepository.findAll().stream()
                .sorted((c1, c2) -> c2.getPrice().compareTo(c1.getPrice()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getCarsByPriceRange() {
        List<Car> cars = carRepository.findAll();
        return cars.stream()
                .collect(Collectors.groupingBy(car -> {
                    BigDecimal price = car.getPrice();
                    if (price.compareTo(BigDecimal.valueOf(500)) < 0) return "Budget (0-500$)";
                    if (price.compareTo(BigDecimal.valueOf(1000)) < 0) return "Mid-range (500-1000$)";
                    return "Luxury (1000$+)";
                }, Collectors.counting()));
    }
}