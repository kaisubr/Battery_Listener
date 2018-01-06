package listener.battery;

import kernel.Kernel32;
import sample.Clock;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Battery_Listener, file created in listener.battery by Kailash Sub.
 */
public class CheckBattery {

    //Check every 1 minute...
    private List<BatteryListener> userCreatedListeners = new ArrayList<>();
    private boolean ACLineStatus = true, newACLineStatus;
    private String flag = "?", newFlag;
    private int life = -1, newLife;
    private String left = "?", newLeft;
    private String full = "?", newFull;

    public Clock clock = new Clock();
    public String latestResult = "?";

    public void addListener(BatteryListener newListener, int milliseconds) {
        if (userCreatedListeners.size() > 1) {
            System.out.println("WARNING: You have already instantiated a battery listener. Multiple listeners may result in unwanted consequences.");
        }

        userCreatedListeners.add(newListener);
        System.out.println("Battery listener " + userCreatedListeners.size() + " added.");

        //'attach' Task
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
                clock.tick(milliseconds);

                //notify anyone that may be interested.
                for (BatteryListener curListener : userCreatedListeners) {
                    if (newACLineStatus != ACLineStatus) {
                        curListener.lineStatusChanged(newACLineStatus);
                        ACLineStatus = newACLineStatus;
                    }
                    if (!newFlag.equals(flag)) {
                        curListener.batteryFlagChanged(newFlag);
                        flag = newFlag;
                    }
                    if (newLife != life) {
                        curListener.batteryLifeChanged(newLife);
                        life = newLife;
                    }
                    if (!newLeft.equals(left)) {
                        curListener.batteryLeftChanged(newLeft);
                        left = newLeft;
                    }
                    if (!newFull.equals(full)) {
                        curListener.batteryFullChanged(newFull);
                        full = newFull;
                    }

                    if (LocalDateTime.now().getHour() == 0 && LocalDateTime.now().getMinute() == 0) {
                        //onMidnight.
                        curListener.onMidnight();
                    }
                }
            }
        }, 0, milliseconds);
    }

    public void update() {
        Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
        Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);

        latestResult = batteryStatus.toString();
        String[] get = latestResult.split("\n");

        newACLineStatus = get[0].split(": ")[1].equals("Online");
        newFlag = get[1].split(": ")[1];
        newLife = Integer.valueOf(get[2].split(": ")[1].split("%")[0]);
        newLeft = get[3].split(": ")[1];
        newFull = get[4].split(": ")[1];

    }

    public List<BatteryListener> getUserCreatedListeners() {
        return userCreatedListeners;
    }

    public boolean isACLineStatus() {
        return ACLineStatus;
    }

    public String getFlag() {
        return flag;
    }

    public int getLife() {
        if (life == -1) return newLife;
        return life;
    }

    public String getLeft() {
        return left;
    }

    public String getFull() {
        return full;
    }

    public Clock getClock() {
        return clock;
    }

}
