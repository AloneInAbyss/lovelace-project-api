package com.aloneinabyss.lovelace.config.initializers;

import com.aloneinabyss.lovelace.pages.games.model.BGGGameDetails;
import com.aloneinabyss.lovelace.pages.games.repository.BGGGameDetailsRepository;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class BGGGameDataLoader implements CommandLineRunner {

    private final BGGGameDetailsRepository bggGameDetailsRepository;
    
    @Value("${bgg.csv.url}")
    private String csvUrl;
    
    @Value("${bgg.csv.local.path}")
    private String csvLocalPath;
    
    @Value("${bgg.csv.delete-after-import}")
    private boolean deleteAfterImport;

    @Override
    public void run(String... args) {
        try {
            // Check if data already exists to avoid re-importing
            long count = bggGameDetailsRepository.count();
            if (count > 0) {
                log.info("✅ BGG game data already loaded ({} games in database). Skipping import.", count);
                return;
            }

            log.info("📚 Starting BGG game data import...");
            long startTime = System.currentTimeMillis();

            // Get the CSV file (download if needed)
            File csvFile = getOrDownloadCSVFile();
            
            // Parse CSV using OpenCSV
            BufferedReader reader = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8));
            CsvToBean<BGGGameDetails> csvToBean = new CsvToBeanBuilder<BGGGameDetails>(reader)
                    .withType(BGGGameDetails.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            List<BGGGameDetails> games = csvToBean.parse();
            reader.close();

            log.info("📊 Parsed {} games from CSV file", games.size());

            // Save all games to MongoDB in batches
            int batchSize = 1000;
            int totalSaved = 0;
            
            for (int i = 0; i < games.size(); i += batchSize) {
                int end = Math.min(i + batchSize, games.size());
                List<BGGGameDetails> batch = games.subList(i, end);
                bggGameDetailsRepository.saveAll(batch);
                totalSaved += batch.size();
                
                if (totalSaved % 10000 == 0 || totalSaved == games.size()) {
                    log.info("💾 Saved {}/{} games...", totalSaved, games.size());
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            log.info("✅ BGG game data import completed successfully!");
            log.info("📊 Total games imported: {}", totalSaved);
            log.info("⏱️ Import duration: {} seconds", duration);

            // Delete CSV file after import if configured
            if (deleteAfterImport && csvFile.exists()) {
                if (csvFile.delete()) {
                    log.info("🗑️ CSV file deleted after successful import");
                } else {
                    log.warn("⚠️ Failed to delete CSV file: {}", csvFile.getAbsolutePath());
                }
            }

        } catch (Exception e) {
            log.error("❌ Error loading BGG game data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load BGG game data", e);
        }
    }

    private File getOrDownloadCSVFile() throws Exception {
        Path localPath = Paths.get(csvLocalPath);
        
        // Check if file exists locally
        if (Files.exists(localPath)) {
            log.info("📁 Using existing CSV file: {}", localPath.toAbsolutePath());
            return localPath.toFile();
        }
        
        // Try to load from classpath
        try {
            ClassPathResource resource = new ClassPathResource(csvLocalPath);
            if (resource.exists()) {
                log.info("📁 Using CSV file from classpath: {}", csvLocalPath);
                // Copy to local file system for consistent handling
                Files.copy(resource.getInputStream(), localPath);
                return localPath.toFile();
            }
        } catch (Exception e) {
            log.debug("CSV not found in classpath: {}", e.getMessage());
        }
        
        // Download from URL if configured
        if (csvUrl != null && !csvUrl.trim().isEmpty()) {
            log.info("📥 Downloading CSV from: {}", csvUrl);
            downloadCSVFile(csvUrl, localPath);
            return localPath.toFile();
        }
        
        throw new RuntimeException(
            "CSV file not found locally and no download URL configured. " +
            "Please either:\n" +
            "  1. Place the CSV file at: " + localPath.toAbsolutePath() + "\n" +
            "  2. Or configure the download URL with property: bgg.csv.url"
        );
    }

    private void downloadCSVFile(String url, Path destination) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        log.info("⏬ Downloading file...");
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException(
                "Failed to download CSV file. HTTP Status: " + response.statusCode()
            );
        }
        
        // Save to file
        try (InputStream in = response.body()) {
            Files.copy(in, destination);
        }
        
        log.info("✅ CSV file downloaded successfully to: {}", destination.toAbsolutePath());
    }
}
