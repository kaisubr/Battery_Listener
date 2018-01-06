package listener.battery;

/**
 * Battery_Listener, file created in listener.battery by Kailash Sub.
 */
public interface BatteryListener {
    /**
     * @param newACLineStatus true if plugged in, false if battery removed.
     */
    void lineStatusChanged(boolean newACLineStatus);

    /**
     * flag example: High, more than 66 percent
     * @param newFlag flag
     */
    void batteryFlagChanged(String newFlag);

    /**
     * Battery life = percent
     * @param newLife percent left
     */
    void batteryLifeChanged(int newLife);

    /**
     * Time till reaches zero.
     * @param newLeft time until % reaches 0, in seconds.
     */
    void batteryLeftChanged(String newLeft);

    /**
     * Dependent on batteryLife
     * @param newFull true if 100%, false if != 100%.
     */
    void batteryFullChanged(String newFull);

    /**
     * If it is onMidnight.
     */
    void onMidnight();
}
