package org.example.controller;

import org.example.service.CarParserService;
import org.example.service.ExcelExportService;
import org.example.service.MonobankAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CarController {

    @Autowired
    private CarParserService carParserService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private MonobankAPIService monobankAPIService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("cars", carParserService.parseCars());
        model.addAttribute("exchangeRates", monobankAPIService.getExchangeRates());
        return "index";
    }

    @GetMapping("/parse")
    public String parseCars() {
        carParserService.parseCars();
        return "redirect:/";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCarsToExcel() {
        byte[] excelContent = excelExportService.exportCarsToExcel();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cars.xlsx")
                .body(excelContent);
    }
}