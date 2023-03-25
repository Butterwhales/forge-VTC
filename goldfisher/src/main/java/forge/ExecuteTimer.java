package forge;

import java.util.concurrent.TimeUnit;

public class ExecuteTimer {
    private long start;
    private long end;

    public ExecuteTimer() {
        reset();
        start = System.currentTimeMillis();
    }

    public void end() {
        end = System.currentTimeMillis();
    }

    public long duration() {
        return (end - start);
    }

    public void reset() {
        start = 0;
        end = 0;
    }

    @Override
    public String toString() {
        long millis = duration();
        return String.format("%02d:%03d (s, ms)",
                TimeUnit.MILLISECONDS.toSeconds(millis),
                millis - TimeUnit.MILLISECONDS.toSeconds(millis));
    }
}
