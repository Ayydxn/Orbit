package me.ayydxn.orbit.utils.ultralight;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UltralightMemoryUsageInfo
{
    private static final long REFRESH_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(500L);

    private static final Pattern SECTION = Pattern.compile("^\\s*--\\s*(.+?)\\s*--\\s*$");
    private static final Pattern ENTRY = Pattern.compile("^\\s+([^:]+?):\\s*(.*)$");
    private static final Pattern SIZE = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(B|KB|MB|GB)\\b");
    private static final Pattern VRAM = Pattern.compile("VRAM:\\s*(\\d+(?:\\.\\d+)?)\\s*(B|KB|MB|GB)\\b");
    private static final Pattern COUNT = Pattern.compile("^(\\d+)");

    private static List<String> cachedLines = List.of();
    private static long lastRefreshNanos = 0L;

    private UltralightMemoryUsageInfo()
    {
    }

    public static synchronized List<String> getDebugMenuLines(Supplier<String> reportSupplier)
    {
        long now = System.nanoTime();
        if (cachedLines.isEmpty() || now - lastRefreshNanos >= REFRESH_INTERVAL_NANOS)
        {
            cachedLines = buildLines(reportSupplier.get());
            lastRefreshNanos = now;
        }

        return cachedLines;
    }

    private static List<String> buildLines(String reportText)
    {
        if (reportText == null || reportText.isBlank())
            return List.of("Ultralight Memory: Unavailable");

        Report report = Report.parse(reportText);
        if (report.isEmpty())
            return List.of("Ultralight Memory: Unavailable");

        long engineHeap = report.bytes("Process Memory", "bmalloc committed");
        long engineHeapInUse = report.bytes("WebCore Allocator (bmalloc/libpas)", "Live");
        long coreHeap = report.bytes("Process Memory", "mimalloc committed");

        long javaScript = report.bytes("JavaScript", "GC Object Space") + report.bytes("JavaScript", "Extra Memory");
        long liveObjects = report.count("JavaScript", "Live Objects");

        long resourceCache = 0;
        for (String label : new String[] { "Images", "CSS Stylesheets", "Scripts", "Fonts" })
            resourceCache += report.bytes("WebCore Resource Cache", label);

        // Decoded image data is reported separately from the encoded size, so it's added on top.
        resourceCache += report.parenthesizedBytes("WebCore Resource Cache", "Images", "decoded");

        long cachesAndPools = 0;
        for (String label : new String[] { "Font Cache", "Photon Cache", "Shadow Cache" })
            cachesAndPools += report.bytes("Caches", label);

        for (String label : new String[] { "Bitmap Pool", "Paint Pool", "Geometry Pool", "Path Tessellator" })
            cachesAndPools += report.bytes("Pools", label);

        long gpuMemory = report.vram("GPU Resources (VRAM estimated)", "Textures")
                + report.bytes("GPU Resources (VRAM estimated)", "Render Targets")
                + report.vram("GPU Resources (VRAM estimated)", "Geometry Buffers");

        long totalRam = engineHeap + coreHeap + cachesAndPools;

        long documents = report.count("JavaScript", "Live Documents");
        long canvases = report.count("HTML5 Canvas Elements", "Total");

        return List.of(
                "Ultralight Memory: %s RAM, %s VRAM (est.)".formatted(formatBytes(totalRam), formatBytes(gpuMemory)),
                "Web Engine Heap: %s (%s in use)".formatted(formatBytes(engineHeap), formatBytes(engineHeapInUse)),
                "Core Heap: %s".formatted(formatBytes(coreHeap)),
                "JavaScript: %s (%d objects)".formatted(formatBytes(javaScript), liveObjects),
                "Resource Cache: %s".formatted(formatBytes(resourceCache)),
                "Caches & Pools: %s".formatted(formatBytes(cachesAndPools)),
                "Documents: %d | Canvases: %d".formatted(documents, canvases)
        );
    }

    private static long toBytes(String number, String unit)
    {
        double value = Double.parseDouble(number);
        double multiplier = switch (unit)
        {
            case "KB" -> 1024.0;
            case "MB" -> 1024.0 * 1024.0;
            case "GB" -> 1024.0 * 1024.0 * 1024.0;
            default -> 1.0;
        };

        return Math.round(value * multiplier);
    }

    private static String formatBytes(long bytes)
    {
        if (bytes < 1024L)
            return bytes + " B";

        if (bytes < 1024L * 1024L)
            return String.format(Locale.ROOT, "%.0f KB", bytes / 1024.0);

        if (bytes < 1024L * 1024L * 1024L)
            return String.format(Locale.ROOT, "%.2f MB", bytes / (1024.0 * 1024.0));

        return String.format(Locale.ROOT, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private static final class Report
    {
        private final Map<String, Map<String, String>> sections = new HashMap<>();

        static Report parse(String text)
        {
            Report report = new Report();
            Map<String, String> currentSection = null;

            for (String line : text.split("\\R"))
            {
                Matcher sectionMatcher = SECTION.matcher(line);
                if (sectionMatcher.matches())
                {
                    currentSection = report.sections.computeIfAbsent(sectionMatcher.group(1), key -> new HashMap<>());
                    continue;
                }

                if (currentSection == null)
                    continue;

                Matcher entryMatcher = ENTRY.matcher(line);
                if (entryMatcher.matches())
                    currentSection.putIfAbsent(entryMatcher.group(1), entryMatcher.group(2));
            }

            return report;
        }

        boolean isEmpty()
        {
            return this.sections.isEmpty();
        }

        long bytes(String section, String label)
        {
            String value = this.value(section, label);
            if (value == null)
                return 0;

            Matcher matcher = SIZE.matcher(value);
            return matcher.find() ? toBytes(matcher.group(1), matcher.group(2)) : 0;
        }

        long vram(String section, String label)
        {
            String value = this.value(section, label);
            if (value == null)
                return 0;

            Matcher matcher = VRAM.matcher(value);
            return matcher.find() ? toBytes(matcher.group(1), matcher.group(2)) : 0;
        }

        long parenthesizedBytes(String section, String label, String key)
        {
            String value = this.value(section, label);
            if (value == null)
                return 0;

            Matcher matcher = Pattern.compile(Pattern.quote(key) + ":\\s*(\\d+(?:\\.\\d+)?)\\s*(B|KB|MB|GB)\\b").matcher(value);
            return matcher.find() ? toBytes(matcher.group(1), matcher.group(2)) : 0;
        }

        long count(String section, String label)
        {
            String value = this.value(section, label);
            if (value == null)
                return 0;

            Matcher matcher = COUNT.matcher(value.trim());
            return matcher.find() ? Long.parseLong(matcher.group(1)) : 0;
        }

        private String value(String section, String label)
        {
            Map<String, String> entries = this.sections.get(section);
            return entries != null ? entries.get(label) : null;
        }
    }
}
