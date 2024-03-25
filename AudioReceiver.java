import javax.sound.sampled.*;
import java.io.*;
import java.net.*;


public class AudioReceiver {

    private InetAddress clientIP;
    private DatagramSocket receivingSocket;
    private AudioFormat audioFormat;
    private SourceDataLine line;
    private boolean running;
    private byte[][] structBuffer;
    private int structBufferIncrement;

    private void initializeSocket() throws SocketException {
        clientIP = clientIP;
        receivingSocket = new DatagramSocket(50000);
        receivingSocket.setSoTimeout(500);
    }

    private void initializeLine() throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(audioFormat);
        line.open(audioFormat);
        line.start();
    }

    private byte[] reconstructBuffer(byte[][] splitBuffers) {
        int splitSize = splitBuffers[0].length;
        int bufferSize = splitSize * 2;
        byte[] buffer = new byte[bufferSize];

        for (int i = 0; i < 2; i++) {
            System.arraycopy(splitBuffers[i], 0, buffer, i * splitSize, splitSize);
        }

        return buffer;
    }

    public void run() {
        while (running) {

            byte[] buffer = new byte[22050];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 22050);


            try {
                receivingSocket.receive(packet);
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e2) {
                continue;
            }


            if (structBufferIncrement < 2) {
                structBuffer[structBufferIncrement] = buffer;
                structBufferIncrement++;
                System.out.println("Increment!");
            }

            if (structBufferIncrement == 2) {
                // Get the default system audio line
                System.out.println("Play!");
                //
                byte[] audioData = reconstructBuffer(structBuffer);


                // Iterate over each byte array in the array
                //line.drain();
                line.write(audioData, 0, audioData.length);

                // Stop and close the audio line

                structBuffer = new byte[2][];
                structBufferIncrement = 0;
            }


        }
    }

    public AudioReceiver() {

        try { //// DONT TEST IT ON LOCALHOST YOU IDIOT
            clientIP = InetAddress.getByName("172.21.0.230"); // use this IP for client
        } catch (UnknownHostException e) {
            System.exit(0);
        }

        audioFormat = new AudioFormat(44100, 16, 2, true, true);

        try {
            initializeSocket();
        } catch (SocketException e) {
        }

        try {
            initializeLine();
        } catch (LineUnavailableException e) {
        }

        structBuffer = new byte[2][];

        running = true;


    }

    public static void main(String[] args) {

        AudioReceiver audioReceiver = new AudioReceiver();
        audioReceiver.run();

    }
}
