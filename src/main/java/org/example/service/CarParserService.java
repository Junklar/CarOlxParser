package org.example.service;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Car;
import org.example.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CarParserService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private MonobankAPIService monobankAPIService;

    @Value("${car.parser.url}")
    private String parserUrl;

    private static final Pattern USD_PATTERN = Pattern.compile("\\$|USD|доларів|долл", Pattern.CASE_INSENSITIVE);
    private static final Pattern EUR_PATTERN = Pattern.compile("€|EUR|євро", Pattern.CASE_INSENSITIVE);

    public List<Car> parseCars() {
        List<Car> parsedCars = new ArrayList<>();

        try {
            Document document = Jsoup.connect(parserUrl).get();
            Elements carElements = document.select("div.css-qfzx1y");

            for (Element carElement : carElements) {
                Car car = new Car();

                String carTitle = carElement.select("h4").text();
                String carUrl = carElement.select("a").attr("href").trim();
                String carPrice = carElement.select("p[data-testid='ad-price']").text().trim();
                String carLocation = carElement.select("p[data-testid='location-date']").text().trim();

                System.out.println("Заголовок: " + carTitle);
                System.out.println("Посилання: " + "https://www.olx.ua" + carUrl);
                System.out.println("Ціна: " + carPrice);
                System.out.println("Записано місто: " + carLocation);

                String currency = detectCurrency(carPrice);
                BigDecimal originalPrice = parsePrice(carPrice);
                BigDecimal priceInUAH = monobankAPIService.convertToUAH(originalPrice, currency);

                car.setTitle(carTitle);
                car.setCountry(carLocation);
                car.setPrice(priceInUAH);
                car.setOriginalPrice(originalPrice);
                car.setCurrency(currency);
                car.setUrl(carUrl);
                car.setParseDate(LocalDate.now());

                carRepository.save(car);
                parsedCars.add(car);
            }

        } catch (IOException e) {
            log.error("Помилка під час парсингу автомобілів: ", e);
        }

        return parsedCars;
    }

    private String detectCurrency(String priceText) {
        if (USD_PATTERN.matcher(priceText).find()) {
            return "USD";
        } else if (EUR_PATTERN.matcher(priceText).find()) {
            return "EUR";
        }
        return "UAH";
    }

    private BigDecimal parsePrice(String priceText) {
        try {
            return new BigDecimal(priceText.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
