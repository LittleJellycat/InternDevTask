class OutLogEntry {
    private String user;
    private String page;
    private int time;

    OutLogEntry(String user, String page, int time) {
        this.user = user;
        this.page = page;
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return user + "," + page + "," + time;
    }
}
