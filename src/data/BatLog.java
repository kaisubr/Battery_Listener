package data;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Battery_Listener, file created in data by Kailash Sub.
 */
public class BatLog {
    private File f;
    String path;
    BufferedWriter writer;

    public BatLog(String s) throws IOException {
        path = s;
        writer = new BufferedWriter(new FileWriter(path, true));
        //"C:\\Users\\plsub\\Desktop\\Saved Files\\apps\\Battery_Listener\\src\\data\\log.txt"
    }

    public void newEvent(String event) {

        System.out.println("~ logging ~");
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();
        String current = dateFormat.format(date);
        try {

            writer.append("[" + (current) + ("]"));
            writer.newLine();
            writer.append((event));
            writer.newLine();
            writer.newLine();

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("~ logging done ~");
        }
    }

    public void whenWriteStringUsingBufferedWritter_thenCorrect() throws IOException {

    }

    public void dashedLine() {
        try {
            writer.newLine();
            writer.append("-----------------------------");
            writer.newLine();

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newLine() {
        try {
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
