
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LogTransformer {
    public static void main(String[] args) throws IOException {
        String path = args[0];
        InOutProperties properties = getProperties(path);
        String in = properties.getInFolder();
        String out = properties.getOutFolder();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            List<String> csvFiles = findCsvFiles(in);
            for (String filePath : csvFiles) {
                try {
                    processLog(filePath, out + filePath.substring(in.length()));
                } catch (IOException e) {
                    System.out.println("Cannot process " + filePath + " log.");
                }
            }
        }, 0, 1, TimeUnit.MINUTES);

    }

    private static List<String> findCsvFiles(String in) {
        File rootDir = new File(in);
        if (rootDir.isFile()) {
            return List.of();
        }
        List<File> result = new ArrayList<>(getCurrentCsvFiles(rootDir));
        ArrayList<File> dirs = new ArrayList<>(getDirs(rootDir));
        while (!dirs.isEmpty()) {
            File dir = dirs.remove(0);
            result.addAll(getCurrentCsvFiles(dir));
            dirs.addAll(getDirs(dir));
        }
        return result.stream().map(File::getPath).collect(Collectors.toList());
    }

    private static List<File> getDirs(File rootDir) {
        return Arrays.asList(rootDir.listFiles(File::isDirectory));
    }

    private static List<File> getCurrentCsvFiles(File rootDir) {
        return List.of(rootDir.listFiles((directory, fileName) -> fileName.endsWith(".csv")));
    }

    private static void processLog(String in, String out) throws IOException {
        new File(out).delete();

        Map<String, List<InLogEntry>> dateToEntries = Files.lines(Paths.get(in))
                .map(InLogEntry::parse)
                .flatMap(InLogEntry::splitDate)
                .collect(Collectors.groupingBy(InLogEntry::getDate));
        Map<String, List<OutLogEntry>> collect = dateToEntries.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> mergeEntries(e.getValue())));
        collect.forEach((key, value) -> writeToFile(key, value, out));
    }

    private static List<OutLogEntry> mergeEntries(List<InLogEntry> entries) {
        Map<LogEntryKey, Double> keyToAverage = entries.stream()
                .collect(Collectors.groupingBy(InLogEntry::getKey, Collectors.averagingInt(InLogEntry::getTime)));
        return keyToAverage.entrySet().stream()
                .map(e -> new OutLogEntry(
                        e.getKey().getUser(),
                        e.getKey().getPage(),
                        e.getValue().intValue()))
                .collect(Collectors.toList());
    }


    private static InOutProperties getProperties(String path) throws IOException {
        try (FileInputStream in = new FileInputStream(path)) {
            Properties p = new Properties();
            p.load(in);
            return new InOutProperties(p.getProperty("input"), p.getProperty("output"));
        }
    }

    private static void writeToFile(String date, List<OutLogEntry> entries, String path) {
        File file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(path), StandardOpenOption.APPEND)) {
            entries.sort(Comparator.comparing(OutLogEntry::getUser));
            bufferedWriter.write(date.toUpperCase());
            bufferedWriter.newLine();
            for (OutLogEntry entry : entries) {
                bufferedWriter.write(entry.toString());
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
