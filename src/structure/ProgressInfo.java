package structure;

public final class ProgressInfo {

    private long total;
    private long current;
    private long secondLeft;

    public ProgressInfo() {
        reset();
    }

    public void reset() {
        total = 0;
        current = 0;
        secondLeft = 0;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSecondLeft() {
        return secondLeft;
    }

    public void setSecondLeft(long secondLeft) {
        this.secondLeft = secondLeft;
    }

    public int getProgressValue() {
        return (int) (total > 0 ? current * 100 / total : 0);
    }

    public String getTimeLeft() {
        long hours = secondLeft / 3600;
        long minutes = (secondLeft % 3600) / 60;
        long seconds = (secondLeft % 3600) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
