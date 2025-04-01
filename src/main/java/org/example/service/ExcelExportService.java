package org.example.service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Car;
import org.example.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    @Autowired
    private CarRepository carRepository;

    public byte[] exportCarsToExcel() {
        List<Car> cars = carRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Автомобілі");

            String[] headers = {"ID", "Назва автомобіля", "Місто", "Ціна (UAH)", "Валюта", "Дата парсингу", "Посилання"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Car car : cars) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(car.getId());
                row.createCell(1).setCellValue(car.getTitle());
                row.createCell(2).setCellValue(car.getCountry());
                row.createCell(3).setCellValue(car.getPrice().doubleValue());

                row.createCell(4).setCellValue(car.getCurrency());
                row.createCell(5).setCellValue(car.getParseDate() != null ? car.getParseDate().toString() : "");
                row.createCell(6).setCellValue("https://www.olx.ua" + car.getUrl()); // Додаємо повну лінку

            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
