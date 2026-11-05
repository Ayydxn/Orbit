package me.ayydxn.orbit.utils.ultralight;

import be.stef.arcana.Arcana;
import be.stef.arcana.ArcanaEntry;
import be.stef.arcana.ArchiveItem;
import be.stef.arcana.ArchiveReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ayydxn.orbit.OrbitMod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class UltralightLibraryDownloader
{
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static void downloadAndExtract(Path targetDirectory) throws IOException
    {
        UltralightPlatform ultralightPlatform = UltralightPlatform.getCurrentPlatform();
        OrbitMod.LOGGER.info("Downloading and extracting Ultralight binaries for {}...", ultralightPlatform.getName());

        Files.createDirectories(targetDirectory);
        Path tempSDKFile = Files.createTempFile("ultralight-sdk-" + ultralightPlatform.getShortName() + "-", ".7z");

        try
        {
            UltralightLibraryDownloader.downloadFile(ultralightPlatform.getSDKDownloadURL(), tempSDKFile);
            UltralightLibraryDownloader.extractBinaries(tempSDKFile, targetDirectory, ultralightPlatform);
        }
        catch (Exception exception)
        {
            OrbitMod.LOGGER.error("Failed to download or extract Ultralight binaries!", exception);

            throw exception;
        }
        finally
        {
            FileUtils.deleteQuietly(tempSDKFile.toFile());
        }
    }

    private static void downloadFile(String urlString, Path destination) throws IOException
    {
        HttpRequest jsonHTTPRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("User-Agent", "Orbit-UltralightDownloader")
                .header("Accept", "application/json")
                .GET()
                .build();

        try
        {
            HttpResponse<InputStream> jsonResponse = HTTP_CLIENT.send(jsonHTTPRequest, HttpResponse.BodyHandlers.ofInputStream());
            if (jsonResponse.statusCode() != 200)
                throw new IOException("Failed to get SDK download info: HTTP " + jsonResponse.statusCode());

            try (InputStream jsonInputStream = jsonResponse.body())
            {
                // TODO: (Ayydxn) In the future, we take the SDK's checksum from this and do some validation to make sure it's legit.
                JsonObject downloadInfo = JsonParser.parseString(new String(jsonInputStream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();

                HttpRequest sdkArchiveHTTPRequest = HttpRequest.newBuilder()
                        .uri(URI.create(downloadInfo.get("url").getAsString()))
                        .header("User-Agent", "Orbit-UltralightDownloader")
                        .header("Accept", "application/octet-stream")
                        .GET()
                        .build();

                HttpResponse<InputStream> sdkArchiveResponse = HTTP_CLIENT.send(sdkArchiveHTTPRequest, HttpResponse.BodyHandlers.ofInputStream());
                if (sdkArchiveResponse.statusCode() != 200)
                    throw new IOException("Failed to download SDK archive: HTTP " + sdkArchiveResponse.statusCode());

                try (InputStream sdkInputStream = sdkArchiveResponse.body(); OutputStream outputStream = Files.newOutputStream(destination))
                {
                    sdkInputStream.transferTo(outputStream);
                }
            }
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();

            throw new IOException("Download interrupted", exception);
        }
    }

    // TODO: (Ayydxn) Move back to using Commons Compress ASAP.
    //  1.20.1 uses a version of Commons Compress that doesn't support the 7z format, which prevents it from extracting the Ultralight libraries properly.
    //  This is a temporary workaround until we update to a newer version of the game which (hopefully) uses a newer version of Commons Compress with that support.
    private static void extractBinaries(Path ultralightSDKPath, Path targetDirectory, UltralightPlatform platform) throws IOException
    {
        try (ArchiveReader archiveReader = Arcana.read(ultralightSDKPath))
        {
            for (ArchiveItem archiveItem : archiveReader)
            {
                ArcanaEntry currentEntry = archiveItem.getEntry();
                if (currentEntry.isDirectory())
                    continue;

                String filename = StringUtils.substringAfterLast(currentEntry.getName(), "/");
                if (filename.isEmpty())
                    filename = StringUtils.substringAfterLast(currentEntry.getName(), "\\");

                if (platform.getLibraryFiles().contains(filename))
                {
                    Path outputPath = targetDirectory.resolve(filename);

                    try (InputStream inputStream = archiveItem.openStream())
                    {
                        Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }
}
