class LogEntryKey {
    private final String user;
    private final String page;

    public LogEntryKey(String user, String page) {
        this.user = user;
        this.page = page;
    }

    public String getUser() {
        return user;
    }

    public String getPage() {
        return page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntryKey that = (LogEntryKey) o;

        if (!user.equals(that.user)) return false;
        return page.equals(that.page);
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + page.hashCode();
        return result;
    }
}
