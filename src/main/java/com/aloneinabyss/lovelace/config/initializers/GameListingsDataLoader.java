package com.aloneinabyss.lovelace.config.initializers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.aloneinabyss.lovelace.pages.games.model.GameListing;
import com.aloneinabyss.lovelace.pages.games.repository.GameListingRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class GameListingsDataLoader implements CommandLineRunner {

    private final GameListingRepository gameListingRepository;

    @Override
    public void run(String... args) {
        try {
            // Check if data already exists to avoid re-importing
            long count = gameListingRepository.count();
            if (count > 0) {
                log.info("✅ Game listings data already loaded ({} listings in database). Skipping import.", count);
                return;
            }

            log.info("📚 Starting game listings data import...");
            long startTime = System.currentTimeMillis();

            // Get the CSV file
            File csvFile = getCSVFile();
            
            // Parse CSV using OpenCSV
            BufferedReader reader = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8));
            CsvToBean<GameListing> csvToBean = new CsvToBeanBuilder<GameListing>(reader)
                    .withType(GameListing.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<GameListing> listings = csvToBean.parse();
            reader.close();

            log.info("📊 Parsed {} game listings from CSV file", listings.size());

            // Save all listings to MongoDB
            gameListingRepository.saveAll(listings);

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            log.info("✅ Game listings data import completed successfully!");
            log.info("📊 Total listings imported: {}", listings.size());
            log.info("⏱️ Import duration: {} seconds", duration);
        } catch (Exception e) {
            log.error("❌ Error loading game listings data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load game listings data", e);
        }
    }

    private File getCSVFile() throws Exception {
        try {
            ClassPathResource resource = new ClassPathResource("game-listings.csv");
            if (!resource.exists()) {
                throw new RuntimeException();
            }

            log.info("📁 Found game listings CSV file on classpath");
            return resource.getFile();
        } catch (Exception e) {
            log.debug("Game listings CSV not found in classpath: {}", e.getMessage());
            throw new RuntimeException("Game listings CSV file not found locally.");
        }
    }
}
