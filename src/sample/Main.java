package sample;

import data.BatLog;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import listener.battery.BatteryListener;
import listener.battery.CheckBattery;
import listener.texting.MailGTextingClient;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {
    Stage mPrimaryStage;
    Scene mScene;
    TrayIcon trayIcon;
    boolean isTray;
    MailGTextingClient mgtc;
    public BatLog mLog;

    public static final String APP_NAME = "Bat Echo";
    public static final double APP_VERSION = 1.1;
    public static final String APP_DETAIL = APP_NAME.concat(" v" + APP_VERSION);
    public static final String LOG_PATH = "C:/Users/plsub/Desktop/Saved Files/apps/Battery_Listener/src/data/log.txt";

    @Override
    public void start(Stage primaryStage) throws Exception{
        //"C:\\Users\\plsub\\Desktop\\Saved Files\\apps\\Battery_Listener\\src\\data\\log.txt";
        try {
            mLog = new BatLog("C:\\Users\\plsub\\Desktop\\Saved Files\\apps\\Battery_Listener\\src\\data\\log.txt");

            mPrimaryStage = primaryStage;
            Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
            mPrimaryStage.initStyle(StageStyle.UTILITY);
            mScene = new Scene(root, 1, 1);
            mPrimaryStage.setScene(mScene);
            mPrimaryStage.setResizable(false);
            mPrimaryStage.show();
            //mPrimaryStage.hide();

            mLog.dashedLine();
            mLog.newEvent(APP_DETAIL + " has completely set up and is running.");

            addAppToTaskbar();
            checkForUpdate();
            mgtc = new MailGTextingClient();

        } catch (Throwable e) {
            eCaught(null, e);
            //e.printStackTrace();
        }
    }

    final int MS_TICK = 60_000;
    private void checkForUpdate() {
        CheckBattery checkBattery = new CheckBattery();
        Clock unPluggedOffsetClock = new Clock(checkBattery.getClock().getTime());

        checkBattery.addListener(new BatteryListener() {
            @Override
            public void lineStatusChanged(boolean newACLineStatus) {
                System.out.println(newACLineStatus);

                if (newACLineStatus) {
                    trayIcon.displayMessage("Charging (" + checkBattery.getLife() + "% remaining)", "Connection to AC Line was established.", TrayIcon.MessageType.INFO);
                    unPluggedOffsetClock.reset(checkBattery.getClock().getTime());

                    //Log the event.
                    mLog.newEvent("Charging (" + checkBattery.getLife() + "% remaining, ... " + checkBattery.latestResult.replace("\n", " | "));

                    mgtc.sendMesesage("Charging (" + checkBattery.getLife() + "% remaining)", "The latest results are shown below.\n" + checkBattery.latestResult);

                } else {
                    trayIcon.displayMessage("Not charging! (" + checkBattery.getLife() + "% remaining)", "Connection to AC Line was lost!", TrayIcon.MessageType.WARNING);
                    unPluggedOffsetClock.tick(MS_TICK);

                    //Log the event.
                    mLog.newEvent("Discharging (" + checkBattery.getLife() + "% remaining), ... " + checkBattery.latestResult.replace("\n", " | "));

                    mgtc.sendMesesage("[!] Discharging (" + checkBattery.getLife() + "% remaining)", "The latest results are shown below.\n" + checkBattery.latestResult +
                            "\n\nThe computer was discharging for " + (Clock.toMinutes(unPluggedOffsetClock.getTime() - unPluggedOffsetClock.getStartTime())) + " minutes.");

                    if ((Clock.toMinutes(unPluggedOffsetClock.getTime() - unPluggedOffsetClock.getStartTime())) % 60 == 0) {
                        //over multiple of hour, unplugged
                        mgtc.sendMesesage("Discharged for " + Clock.toMinutes(unPluggedOffsetClock.getTime() - unPluggedOffsetClock.getStartTime()) + " minutes."
                                + "(" + checkBattery.getLife() + "%)", "The latest results are shown below.\n" + checkBattery.latestResult);
                    }

                    //over 4 hours... shutdown countdown (1 hour).
                    if ((Clock.toMinutes(unPluggedOffsetClock.getTime() - unPluggedOffsetClock.getStartTime())) > 240) {
                        mgtc.sendMesesage("CRITICAL NOTICE", "Your computer has been discharging for over 4 hours. Reply Y to shut down now. Reply N to keep the computer awake. Shut down will commence in one hour.");

                        //send api text message, if reply YES to shutdown, shutdown.
                        //...
                        String input = "...";
                        if (input.equals("Y")) {
                            forceShutDown();
                        } else if (input.equals("N")) {
                            //leave on.
                            //thread.stop() or something.
                        } else {
                            //unknown input. repeat and extend countdown...
                        }
                    }

                }
            }

            @Override
            public void batteryFlagChanged(String newFlag) {
                //Log the event.
                mLog.newEvent("Flag update (" + checkBattery.getLife() + "% remaining), ... " + checkBattery.latestResult.replace("\n", " | "));

                System.out.println(newFlag);
                trayIcon.displayMessage("Battery " + newFlag.split(",")[0].toLowerCase(), "Flag update: " + newFlag, TrayIcon.MessageType.INFO);
                mgtc.sendMesesage("Flag update (" + checkBattery.getLife() + "% remaining)", checkBattery.latestResult);
            }

            @Override
            public void batteryLifeChanged(int newLife) {
                System.out.println(newLife);
                if (newLife < 10 && !checkBattery.isACLineStatus()) {
                    //shutdown. forced.
                    trayIcon.displayMessage("A forced shutdown has been initiated.", "This procedure should not be stopped without valid reason.", TrayIcon.MessageType.INFO);
                    forceShutDown();
                }
            }

            @Override
            public void batteryLeftChanged(String newLeft) {
                System.out.println(newLeft);
            }

            @Override
            public void batteryFullChanged(String newFull) {
                System.out.println(newFull);
            }

            @Override
            public void onMidnight() {
                mLog.newEvent("It's midnight, ... " + checkBattery.latestResult.replace("\n", " | "));
                final int[] min = {3}; //3 minutes, 180 seconds

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Battery Listener");
                    alert.setHeaderText("Shut down? It's midnight.");
                    alert.setContentText("Press OK to shut down now. Press Cancel to postpone the shutdown for tomorrow.\n" +
                            "In " + min[0] + " minutes, the computer will automatically shut down.");


                    Timeline idle = new Timeline(new KeyFrame(Duration.minutes(min[0]), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            //auto init after 3 min
                            alert.setResult(ButtonType.OK);
                            alert.hide();
                        }
                    }));

                    idle.setCycleCount(1);
                    idle.play();

                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.get() == ButtonType.OK){
                        System.out.println("OK (shut down)");
                        forceShutDown();
                    } else if (result.get() == ButtonType.CANCEL) {
                        //skip this shutdown.
                        mLog.newEvent("The shutdown was cancelled, ... " + checkBattery.latestResult.replace("\n", " | "));
                    }


                });


            }

            private void forceShutDown() {
                //Log the event.
                mLog.newEvent("Computer is shutting down. " + checkBattery.latestResult.replace("\n", " | "));

                mgtc.sendMesesage("Computer has shut down", "This email notifies you that the computer was shut down. This is an irreversible process, unless you are at your computer and Windows prompts you to save any unsaved files.");
                Runtime runtime = Runtime.getRuntime();
                try {
                    Process proc = runtime.exec("shutdown -s -t 0");
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                    //failed. text device about failure.
                }
            }
        }, MS_TICK); //check every minute.
    }

    private void addAppToTaskbar() {
        if (!SystemTray.isSupported()) {
            System.out.println("[i] System tray not supported!");
            return;
        }

        PopupMenu popupMenu = new PopupMenu();
//        TrayIcon trayIcon = new TrayIcon(new ImageIcon(
//                "src/images/sp-search/ic_search_white_24dp/ic_search_white_24dp/android/drawable-xxhdpi/ic_search_white_24dp.png").getImage());
        trayIcon = new TrayIcon(new ImageIcon(
                "img/bat_ste.png", "Battery Listener").getImage());
        SystemTray systemTray = SystemTray.getSystemTray();
        //popup menu
        Font newFont = new Font("Arial", Font.PLAIN, 27);

        MenuItem exitItem = new MenuItem("Exit"); exitItem.setFont(newFont.deriveFont(Font.BOLD));

        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);

        try {
            systemTray.add(trayIcon);
            trayIcon.displayMessage("Listening for battery events.", "Application is minimized to taskbar.", TrayIcon.MessageType.INFO);
            isTray = true;
        } catch (AWTException e) {
            isTray = false;
            System.out.println("TrayIcon could not be added.");
        }

        exitItem.addActionListener((l) -> {
            Platform.runLater(this::exitConf);
        });
    }

    private void exitConf() {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Battery Listener");
//        alert.setHeaderText("Exit?");
//        alert.setContentText("Are you sure you want to stop listening to battery events? " +
//                "Note: the program uses an optimized algorithm to listen to battery event changes, so it has little to no impact on RAM, GPU, CPU usage, or BIOS time. " +
//                "If you would like, open your startup folder and edit configurations there.");
//
//        Optional<ButtonType> result = alert.showAndWait();
//
//        if (result.get() == ButtonType.OK){
//            System.exit(0);
//        }
        mLog.newEvent("Battery listener was forcefully exited!");
        mgtc.sendMesesage("Forceful exit", "Battery listener was forcefully exited.");

        System.exit(0);
    }

    public static void main(String[] args) {

        //Thread.setDefaultUncaughtExceptionHandler(Main::eCaught);

        launch(args);
    }

    private static void eCaught(Thread t, Throwable e) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
        String filename = "C:/Users/plsub/Desktop/Saved Files/apps/Battery_Listener/src/crashlogs/"+sdf.format(cal.getTime())+".txt";
        File f = new File(filename);

        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
                return;
            }
        }

        PrintStream writer;
        try {
            writer = new PrintStream(filename, "UTF-8");
            writer.println(e.getClass() + ": " + e.getMessage());
            for (int i = 0; i < e.getStackTrace().length; i++) {
                writer.println(e.getStackTrace()[i].toString());
            }

        } catch (FileNotFoundException | UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}
