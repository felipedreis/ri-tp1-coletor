import crawler.PageFetcher;
import crawler.URLAddress;
import crawler.escalonadorCurtoPrazo.Escalonador;
import crawler.escalonadorCurtoPrazo.EscalonadorSimples;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by felipe on 28/09/17.
 */
public class Main {

    public static void main(String [] args) throws FileNotFoundException {

        String seedsFileName = "seeds.txt";
        String outputFileName = "output.txt";
        int numOfThreads = 1;
        int numPagesToCollect = 100;

        for(int i = 1; i < args.length; ++i) {

            switch (args[i]) {
                case "-h":
                case "--help":
                    System.out.printf("Usage: %s [options]", args[0]);
                    System.out.println("Options may be: ");
                    System.out.println("-s --seeds <file> \tURL seeds file, with each seed in a different line. Default is seeds.txt");
                    System.out.println("-o --output <file> \tURL output file, with each seed in a different line. Default is output.txt");
                    System.out.println("-t --threads <number> \tNumber of threads. Default is 1");
                    System.out.println("-c --collect <number> \tNumber of pages to collect. Default is 100");
                    System.out.println("-h --help \tShow this help and exit");
                    System.exit(0);
                    break;
                case "-s":
                case "--seeds":
                    i++;
                    seedsFileName = args[i];
                    break;

                case "-t":
                case "--threads":
                    i++;
                    numOfThreads = Integer.parseInt(args[i]);
                    break;

                case "-c":
                case "--collect":
                    i++;
                    numPagesToCollect = Integer.parseInt(args[i]);
                    break;

                case "-o":
                case "--output":
                    i++;
                    outputFileName = args[i];
                    break;

                default:
                    System.err.println("Unknown param \"" + args[i] + "\"");
                    System.exit(1);
            }

        }

        try {
            long startTime = System.currentTimeMillis();

            Escalonador escalonador = new EscalonadorSimples(numPagesToCollect);
            BufferedReader fileReader = new BufferedReader(new FileReader(seedsFileName));
            List<String> lines = fileReader.lines().collect(Collectors.toList());

            for (String line : lines) {
                escalonador.adicionaNovaPagina(new URLAddress(line, 0));
            }

            PageFetcher[] fetchers = new PageFetcher[numOfThreads];
            Thread[] fetcherThreads = new Thread[numOfThreads];

            for (int i = 0; i < fetcherThreads.length; ++i) {
                fetchers[i] = new PageFetcher(escalonador);
                fetcherThreads[i] = new Thread(fetchers[i]);
                fetcherThreads[i].run();
            }

            BufferedWriter output = new BufferedWriter(new FileWriter(outputFileName));

            for (int i = 0; i < fetcherThreads.length; ++i) {
                fetcherThreads[i].join();
                for(URLAddress link : fetchers[i].getCollectedPages()) {
                    output.write(link.toString());
                }
            }

            long endTime = System.currentTimeMillis();

            System.out.println("Elapsed time: " + ((endTime - startTime) * 1000));

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
