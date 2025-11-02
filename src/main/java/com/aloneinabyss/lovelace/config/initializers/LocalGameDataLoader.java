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

import com.aloneinabyss.lovelace.pages.games.model.GameDetails;
import com.aloneinabyss.lovelace.pages.games.repository.GameDetailsRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class LocalGameDataLoader implements CommandLineRunner {

    private final GameDetailsRepository gameDetailsRepository;

    @Override
    public void run(String... args) {
        try {
            // Check if data already exists to avoid re-importing
            long count = gameDetailsRepository.count();
            if (count > 0) {
                log.info("✅ Local game data already loaded ({} games in database). Skipping import.", count);
                return;
            }

            log.info("📚 Starting local game data import...");
            long startTime = System.currentTimeMillis();

            // Get the CSV file
            File csvFile = getCSVFile();
            
            // Parse CSV using OpenCSV
            BufferedReader reader = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8));
            CsvToBean<GameDetails> csvToBean = new CsvToBeanBuilder<GameDetails>(reader)
                    .withType(GameDetails.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<GameDetails> games = csvToBean.parse();
            reader.close();

            log.info("📊 Parsed {} games from CSV file", games.size());

            // Save all games to MongoDB in batches
            int batchSize = 1000;
            int totalSaved = 0;
            
            for (int i = 0; i < games.size(); i += batchSize) {
                int end = Math.min(i + batchSize, games.size());
                List<GameDetails> batch = games.subList(i, end);
                gameDetailsRepository.saveAll(batch);
                totalSaved += batch.size();
                
                if (totalSaved % 10000 == 0 || totalSaved == games.size()) {
                    log.info("💾 Saved {}/{} games...", totalSaved, games.size());
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            log.info("✅ Local game data import completed successfully!");
            log.info("📊 Total games imported: {}", totalSaved);
            log.info("⏱️ Import duration: {} seconds", duration);
        } catch (Exception e) {
            log.error("❌ Error loading local game data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load local game data", e);
        }
    }

    private File getCSVFile() throws Exception {
        try {
            ClassPathResource resource = new ClassPathResource("local-game-data.csv");
            if (!resource.exists()) {
                throw new RuntimeException();
            }

            log.info("📁 Found CSV file on classpath");
            return resource.getFile();
        } catch (Exception e) {
            log.debug("CSV not found in classpath: {}", e.getMessage());
            throw new RuntimeException("CSV file not found locally.");
        }
    }
}
