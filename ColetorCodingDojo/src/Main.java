import crawler.PageFetcher;
import crawler.URLAddress;
import crawler.escalonadorCurtoPrazo.Escalonador;
import crawler.escalonadorCurtoPrazo.EscalonadorSimples;

import java.net.MalformedURLException;

/**
 * Created by felipe on 28/09/17.
 */
public class Main {

    public static void main(String [] args) throws MalformedURLException {
        Escalonador escalonador = new EscalonadorSimples();
        escalonador.adicionaNovaPagina(new URLAddress("https://stackoverflow.com", 1));
        escalonador.adicionaNovaPagina(new URLAddress("https://terra.com.br", 1));
        PageFetcher fetcher = new PageFetcher(escalonador);
        while(!escalonador.finalizouColeta())
            fetcher.run();
    }
}
