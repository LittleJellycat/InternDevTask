import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

class InLogEntry {
    private int timestamp;
    private String user;
    private String page;
    private int time;

    private InLogEntry(int timestamp, String user, String page, int time) {
        this.timestamp = timestamp;
        this.user = user;
        this.page = page;
        this.time = time;
    }

    public static InLogEntry parse(String s) {
        String[] entries = s.split(",");
        return new InLogEntry(Integer.parseInt(entries[0]), entries[1], entries[2], Integer.parseInt(entries[3]));
    }

    public Stream<InLogEntry> splitDate() {
        if (getDate().equals(getDate((long) (timestamp + getTime())))) {
            return Stream.of(this);
        } else {
            final int secondsPerDay = (int) TimeUnit.DAYS.toSeconds(1);
            List<InLogEntry> entryList = new ArrayList<>();
            int firstDaySpent = secondsPerDay - timestamp % secondsPerDay;
            entryList.add(new InLogEntry(timestamp, user, page, firstDaySpent));
            int timeLeft = getTime() - firstDaySpent;
            int newTimeStamp = timestamp + firstDaySpent;
            while (timeLeft > 0) {
                entryList.add(new InLogEntry(newTimeStamp, user, page, Math.min(timeLeft, secondsPerDay)));
                newTimeStamp += secondsPerDay;
                timeLeft -= secondsPerDay;
            }
            return entryList.stream();
        }
    }

    public String getDate() {
        return getDate(timestamp);
    }

    private String getDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        return sdf.format(timestamp * 1000);
    }

    public int getTime() {
        return time;
    }

    public LogEntryKey getKey() {
        return new LogEntryKey(user, page);
    }
}
