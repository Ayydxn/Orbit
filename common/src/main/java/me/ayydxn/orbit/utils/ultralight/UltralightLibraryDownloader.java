package me.ayydxn.orbit.utils.ultralight;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UltralightLibraryDownloader
{
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static void downloadAndExtract(Path targetDirectory) throws IOException
    {
        UltralightPlatform ultralightPlatform = UltralightPlatform.getCurrentPlatform();
        OrbitMod.LOGGER.info("Downloading Ultralight binaries for {}...", ultralightPlatform.getName());

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
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .header("User-Agent", "Orbit-UltralightDownloader")
                .GET()
                .build();

        try
        {
            HttpResponse<InputStream> httpResponse = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            if (httpResponse.statusCode() != 200)
                throw new IOException("Failed to download file: HTTP " + httpResponse.statusCode());

            try (InputStream inputStream = httpResponse.body(); OutputStream outputStream = Files.newOutputStream(destination))
            {
                inputStream.transferTo(outputStream);
            }

            OrbitMod.LOGGER.info("Download complete!");
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();

            throw new IOException("Download interrupted", exception);
        }
    }

    private static void extractBinaries(Path ultralightSDKPath, Path targetDirectory, UltralightPlatform platform) throws IOException
    {
        OrbitMod.LOGGER.info("Extracting Ultralight binaries...");

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(ultralightSDKPath)))
        {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                if (zipEntry.isDirectory())
                    continue;

                String filename = StringUtils.substringAfterLast(zipEntry.getName(), "/");
                if (filename.isEmpty())
                    filename = StringUtils.substringAfterLast(zipEntry.getName(), "\\");

                if (platform.getLibraryFiles().contains(filename))
                {
                    Path libraryOutputPath = targetDirectory.resolve(filename);

                    try (OutputStream outputStream = Files.newOutputStream(libraryOutputPath))
                    {
                        zipInputStream.transferTo(outputStream);
                    }
                }
            }
        }

        OrbitMod.LOGGER.info("Successfully extracted Ultralight binaries!");
    }
}
