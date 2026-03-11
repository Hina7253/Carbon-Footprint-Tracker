package com.example.LatestStable.service;

import com.example.LatestStable.config.AppConfig.CarbonConstants;
import com.example.LatestStable.model.ResourceType;
import org.springframework.stereotype.Service;

@Service
public class CarbonCalculatorService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CarbonCalculatorService.class);

    private final CarbonConstants carbonConstants;

    public CarbonCalculatorService(CarbonConstants carbonConstants) {
        this.carbonConstants = carbonConstants;
    }

    public double calculateCo2PerVisit(long totalBytes) {
        double gigabytes = totalBytes / 1_000_000_000.0;
        double energyKwh = gigabytes * carbonConstants.kwhPerGb();
        double co2Grams  = energyKwh * carbonConstants.gramsCo2PerKwh();
        log.debug("Carbon calc: {}B → {}kWh → {}gCO2",
                totalBytes, energyKwh, co2Grams);
        return co2Grams;
    }

    public double calculateEnergyKwh(long totalBytes) {
        double gigabytes = totalBytes / 1_000_000_000.0;
        return gigabytes * carbonConstants.kwhPerGb();
    }

    public double calculateAnnualCo2Kg(
            double co2PerVisitGrams, long monthlyVisits) {
        long annualVisits       = monthlyVisits * 12;
        double annualCo2Grams   = co2PerVisitGrams * annualVisits;
        return annualCo2Grams / 1000.0;
    }

    public double calculateResourceCo2(
            long sizeBytes, ResourceType resourceType) {
        double baseCo2    = calculateCo2PerVisit(sizeBytes);
        double multiplier = resourceType.getCarbonMultiplier();
        return baseCo2 * multiplier;
    }

    public String calculateGrade(double co2PerVisitGrams) {
        if (co2PerVisitGrams < 0.095) return "A";
        if (co2PerVisitGrams < 0.185) return "B";
        if (co2PerVisitGrams < 0.340) return "C";
        if (co2PerVisitGrams < 0.490) return "D";
        if (co2PerVisitGrams < 0.650) return "E";
        return "F";
    }

    public CarbonEquivalents calculateEquivalents(double yearlyKgCo2) {
        return new CarbonEquivalents(
                yearlyKgCo2 / 0.21,
                yearlyKgCo2 / 21.0,
                yearlyKgCo2 / 0.005,
                yearlyKgCo2 / 0.0002
        );
    }

    public double calculateOptimizationPotential(
            long sizeBytes, ResourceType type,
            boolean isCached, String contentType) {

        if (isCached) return 0.1;

        return switch (type) {
            case IMAGE -> {
                if (sizeBytes > 500_000) yield 0.7;
                if (sizeBytes > 200_000) yield 0.5;
                if (sizeBytes > 50_000)  yield 0.3;
                yield 0.1;
            }
            case VIDEO  -> sizeBytes > 1_000_000 ? 0.4 : 0.2;
            case SCRIPT -> {
                if (sizeBytes > 100_000) yield 0.6;
                if (sizeBytes > 50_000)  yield 0.4;
                yield 0.2;
            }
            case FONT   -> 0.3;
            case STYLE  -> 0.25;
            default     -> 0.1;
        };
    }

    public String getPerformanceCategory(double co2PerVisitGrams) {
        if (co2PerVisitGrams < 0.185) return "Clean";
        if (co2PerVisitGrams < 0.490) return "Average";
        return "Dirty";
    }

    public double calculatePercentileBetter(double co2PerVisitGrams) {
        if (co2PerVisitGrams < 0.095) return 90.0;
        if (co2PerVisitGrams < 0.185) return 75.0;
        if (co2PerVisitGrams < 0.340) return 50.0;
        if (co2PerVisitGrams < 0.490) return 35.0;
        if (co2PerVisitGrams < 0.650) return 20.0;
        return 10.0;
    }

    // ── Inner Class (record ki jagah) ─────────────────────────────
    public static class CarbonEquivalents {
        private final double kmDriven;
        private final double treesNeeded;
        private final double smartphoneCharges;
        private final double googleSearches;

        public CarbonEquivalents(
                double kmDriven, double treesNeeded,
                double smartphoneCharges, double googleSearches) {
            this.kmDriven          = kmDriven;
            this.treesNeeded       = treesNeeded;
            this.smartphoneCharges = smartphoneCharges;
            this.googleSearches    = googleSearches;
        }

        public double kmDriven()          { return kmDriven; }
        public double treesNeeded()       { return treesNeeded; }
        public double smartphoneCharges() { return smartphoneCharges; }
        public double googleSearches()    { return googleSearches; }
    }
}