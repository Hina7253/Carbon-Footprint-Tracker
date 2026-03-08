package com.example.LatestStable;

import com.example.LatestStable.service.CarbonCalculatorService;
import com.example.LatestStable.service.CarbonCalculatorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class CarbonCalculatorServiceTest {

    // We can create this directly since it has no Spring dependencies
    // We just need to pass the CarbonConstants manually
    private final CarbonCalculatorService calculator =
            new CarbonCalculatorService(
                    new com.example.LatestStable.config.AppConfig.CarbonConstants(
                            0.06,  // kwhPerGb
                            490.0  // gramsCo2PerKwh
                    )
            );

    @Test
    @DisplayName("1 MB page should produce approximately 0.02994 grams of CO2")
    void testCo2CalculationFor1MbPage() {
        long oneMegabyte = 1_000_000; // 1 million bytes = 1 MB

        double co2 = calculator.calculateCo2PerVisit(oneMegabyte);

        // Expected: 0.001 GB × 0.06 kWh/GB × 490 gCO2/kWh = 0.02940
        // Allow small floating-point tolerance (±0.001)
        assertEquals(0.02940, co2, 0.001,
                "1 MB page should produce ~0.029g of CO2");
    }

    @Test
    @DisplayName("Grade A should be assigned for very light pages")
    void testGradeAForLightPage() {
        // A grade requires < 0.095g per visit
        // A 200KB page produces very little CO2
        double veryLowCo2 = 0.05;
        String grade = calculator.calculateGrade(veryLowCo2);
        assertEquals("A", grade, "0.05g CO2 should earn grade A");
    }

    @Test
    @DisplayName("Grade F should be assigned for very heavy pages")
    void testGradeFForHeavyPage() {
        double highCo2 = 1.5; // 1.5g per visit = very polluting
        String grade = calculator.calculateGrade(highCo2);
        assertEquals("F", grade, "1.5g CO2 should earn grade F");
    }

    @Test
    @DisplayName("Annual CO2 should scale correctly with traffic")
    void testAnnualCo2Calculation() {
        double co2PerVisit = 0.5; // grams
        long monthlyVisits = 10_000;

        double annualKg = calculator.calculateAnnualCo2Kg(co2PerVisit, monthlyVisits);

        // Expected: 0.5g × 10,000 × 12 = 60,000g = 60 kg
        assertEquals(60.0, annualKg, 0.01,
                "0.5g × 10,000 visits/month should equal 60kg CO2/year");
    }

    @Test
    @DisplayName("Performance category 'Clean' for low CO2 sites")
    void testPerformanceCategoryClean() {
        String category = calculator.getPerformanceCategory(0.1);
        assertEquals("Clean", category);
    }

    @Test
    @DisplayName("Large images should have high optimization potential")
    void testLargeImageOptimizationPotential() {
        long largeImage = 600_000; // 600 KB

        double potential = calculator.calculateOptimizationPotential(
                largeImage,
                com.example.LatestStable.model.ResourceType.IMAGE,
                false,
                "image/jpeg"
        );

        // 600KB image should have 70% optimization potential
        assertEquals(0.7, potential, 0.01);
    }

    @Test
    @DisplayName("Cached resources should have low optimization potential")
    void testCachedResourceLowOptimizationPotential() {
        double potential = calculator.calculateOptimizationPotential(
                1_000_000, // 1 MB (large but cached)
                com.example.LatestStable.model.ResourceType.IMAGE,
                true, // isCached = true
                "image/png"
        );

        // Even large cached resources have low potential (0.1)
        assertEquals(0.1, potential, 0.01,
                "Cached resource should have low optimization potential");
    }
}