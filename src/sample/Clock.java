package sample;

/**
 * Battery_Listener, file created in sample by Kailash Sub.
 * Stopwatch time values are always in milliseconds!
 */
public class Clock {
    private double time;
    private double startTime;
    public static double EPSILON = 4.88E-04;

    /**
     * Stopwatch times are always in milliseconds!
     * @param startTime offset in milliseconds.
     */
    public Clock(double startTime) {
        time = startTime;
        this.startTime = startTime;
    }

    public Clock() {
        time = 0;
    }

    public double getTime() {
        return time;
    }

    public double getStartTime() {
        return startTime;
    }

    public void tick() {
        time++;
    }

    public void tick(double ms) {
        time += ms;
    }

    public void tickSeconds(double sec) {
        time += 1000 * sec;
    }

    public void tickMinutes(double min) {
        time += 60_000 * min;
    }

    public void reset(double startTime) {
        time = startTime;
    }

    public static double toSeconds(double milliseconds) {
        return (milliseconds/1000);
    }

    public static double toMinutes(double milliseconds) {
        return (milliseconds/60_000);
    }
}
