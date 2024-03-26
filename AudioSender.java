import javax.sound.sampled.*;
import java.io.*;
import java.net.*;


public class AudioSender {

    private InetAddress clientIP;
    private DatagramSocket sendingSocket;
    private AudioFormat audioFormat;
    private TargetDataLine line;
    private DataLine.Info info;
    private boolean running;
    private int packetSplitSize;

    private void initializeSocket() throws SocketException {
        clientIP = clientIP;
        sendingSocket = new DatagramSocket();
    }

    private void initializeLine() throws LineUnavailableException {
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
        line.start();
    }

    private void sendBuffer(byte[] buffer) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, 50000);
        try {
            System.out.println(buffer.length);
            sendingSocket.send(packet);
        } catch (IOException e) {
            System.out.println(e);
            return;
        }
    }

    private byte[][] splitBuffer(byte[] buffer) {
        int bufferSize = buffer.length;
        int splitSize = bufferSize / packetSplitSize;
        byte[][] splitBuffers = new byte[packetSplitSize][splitSize];

        for (int i = 0; i < packetSplitSize; i++) {
            System.arraycopy(buffer, i * splitSize, splitBuffers[i], 0, splitSize);
        }

        return splitBuffers;
    }

    public void run() {
        while (running) {
            byte[] buffer = new byte[4096];
            byte[][] audioDataArray = new byte[50][];
            System.out.println("sending ... " + buffer.length);
            line.read(buffer, 0, buffer.length);
            sendBuffer(buffer);
        }
    }

    public AudioSender() {

        try {
            clientIP = InetAddress.getByName("172.21.0.78"); // use this IP for client
        } catch (UnknownHostException e) {
            System.exit(0);
        }

        audioFormat = new AudioFormat(44100, 16, 2, true, true);
        info = new DataLine.Info(TargetDataLine.class, audioFormat);
        packetSplitSize = 2;

        try {
            initializeSocket();
        } catch (SocketException e) {
        }

        try {
        initializeLine();
        } catch (LineUnavailableException e) {
        }

        running = true;


    }

    public static void main(String[] args) {

        AudioSender audioSender = new AudioSender();
        audioSender.run();

    }
}
