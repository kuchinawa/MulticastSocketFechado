package servidores;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServidorPrimordial {
    private final String multicastGroup = "225.17.1.18";
    private final int multicastPort = 55555;
    private final int dronePort = 44444;

    public void iniciarServidor() {
        try {
            DatagramSocket servidorSocket = new DatagramSocket(dronePort);
            MulticastSocket ms = new MulticastSocket();

            InetAddress group = InetAddress.getByName(multicastGroup);
            System.out.println("Servidor Primordial iniciado e ouvindo na porta " + dronePort);

            byte[] buffer = new byte[1024];

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    servidorSocket.receive(packet);

                    String dados = new String(packet.getData(), 0, packet.getLength());
                    InetAddress senderAddress = packet.getAddress();

                    System.out.println("Servidor Primordial recebeu de " + senderAddress.getHostAddress() + ": " + dados);

                    DatagramPacket multicastPacket = new DatagramPacket(packet.getData(), packet.getLength(), group, multicastPort);
                    ms.send(multicastPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 3, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ServidorPrimordial servidor = new ServidorPrimordial();
        servidor.iniciarServidor();
    }
}