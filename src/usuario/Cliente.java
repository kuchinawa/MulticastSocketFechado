package usuario;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Cliente {
    private final String multicastGroup = "225.17.1.18";
    private final int multicastPort = 55555;

    private List<String> dadosRecebidos = new ArrayList<>();

    public void iniciarCliente() {
        try {
            MulticastSocket ms = new MulticastSocket(multicastPort);
            System.out.println("Receptor " +
                    InetAddress.getLocalHost() +
                    " escutando na porta " +
                    ms.getLocalPort());

            InetAddress multicastIP = InetAddress.getByName(multicastGroup);
            InetSocketAddress grupo = new InetSocketAddress(multicastIP, multicastPort);
            //NetworkInterface interfaceRede = NetworkInterface.getByName("Ethernet");



            System.out.println(multicastIP);
            System.out.println(grupo);
            //System.out.println(interfaceRede);


            ms.joinGroup(grupo.getAddress());
            System.out.println("Receptor " +
                    InetAddress.getLocalHost() +
                    " entrou no grupo: " +
                    grupo);

            byte[] bufferRecepcao = new byte[1024];


            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    DatagramPacket pacoteRecepcao =
                            new DatagramPacket(
                                    bufferRecepcao,
                                    bufferRecepcao.length);
                    ms.receive(pacoteRecepcao);
                    String dados = new String(
                            bufferRecepcao,
                            0,
                            pacoteRecepcao.getLength());
                    synchronized (dadosRecebidos) {
                        dadosRecebidos.add(dados);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, 0, 3, TimeUnit.SECONDS);


            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                System.out.println("Escolha uma opção:");
                System.out.println("1. Ver dados recebidos");
                System.out.println("2. Ver temperatura");
                System.out.println("3. Ver umidade");
                System.out.println("4. Sair");

                int opcao = scanner.nextInt();

                switch (opcao) {
                    case 1:
                        synchronized (dadosRecebidos) {
                            System.out.println("Dados recebidos:");
                            dadosRecebidos.forEach(System.out::println);
                        }
                        break;

                    case 2:
                        synchronized (dadosRecebidos) {
                            System.out.println("Temperaturas recebidas:");
                            dadosRecebidos.stream()
                                    .filter(p -> p.contains("Temp:"))
                                    .map(p -> p.split(", "))
                                    .filter(partes -> partes.length > 0 && partes[0].startsWith("Temp:"))
                                    .forEach(parte -> System.out.println(parte[0]));
                        }
                        break;

                    case 3:
                        synchronized (dadosRecebidos) {
                            System.out.println("Umidades recebidas:");
                            dadosRecebidos.stream()
                                    .filter(p -> p.contains("Umidade:"))
                                    .map(p -> p.split(", "))
                                    .filter(partes -> partes.length > 1 && partes[1].startsWith("Umidade:"))
                                    .forEach(parte -> System.out.println(parte[1]));
                        }
                        break;

                    case 4:
                        running = false;
                        ms.leaveGroup(grupo.getAddress());
                        ms.close();
                        scanner.close();
                        System.out.println("Saindo...");
                        break;

                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            }

            ms.leaveGroup(grupo.getAddress());
            ms.close();
            scanner.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        cliente.iniciarCliente();
    }
}
