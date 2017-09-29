package crawler.escalonadorCurtoPrazo;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.trigonic.jrobotx.Record;
import com.trigonic.jrobotx.RobotExclusion;

import crawler.Servidor;
import crawler.URLAddress;

public class EscalonadorSimples implements Escalonador{
	
	
	private final int DEPTH_LIMIT = 6;
	private Map<Servidor, List<URLAddress>> fila;
	private Set<URLAddress> pagVisitadas;
	private Queue<Servidor> servidores;
	private Map<String, Record> mapaRobots;
	private int cont_paginas;
	private final int limite_paginas = 10;
	
	
	public EscalonadorSimples(){
		fila = new LinkedHashMap<>();
		pagVisitadas = new HashSet<>();
		servidores = new PriorityQueue<>(Servidor.comparator);
		mapaRobots = new HashMap<>();
		cont_paginas = 0;
	}

	@Override
	public synchronized URLAddress getURL() {
		URLAddress url = null;

		while (url == null) {
			Servidor servidor = getProximoServidorAcessivel();

			if (fila.get(servidor).isEmpty()) {
				servidor.acessadoAgora();
				servidores.offer(servidor);
				System.out.println("No link found to server " + servidor.getNome());
			} else {
				url = fila.get(servidor).remove(0);
				servidor.acessadoAgora();
				servidores.add(servidor);
			}
		}
		return url;
	}


	private synchronized Servidor getProximoServidorAcessivel() {
		boolean isAcessivel;
		Servidor servidor = null;
		try {
			while(servidores.isEmpty() && !finalizouColeta()) {
				System.out.println("Esperando um novo servidor");
				wait(1000);
			}

			Iterator<Servidor> it = servidores.iterator();
			isAcessivel = false;
			while (!isAcessivel) {
				for (int i = 0; i < servidores.size(); ++i) {//while (servidores.isEmpty()) {
					servidor = servidores.remove();

					if (servidor.isAccessible()) {
						isAcessivel = true;
						break;
					} else {
						servidores.offer(servidor);
					}
				}
				if(!isAcessivel) {
					System.out.println("Esperando um servidor ficar acessivel");
					wait(1000);
				}
			}

		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		return servidor;
	}

	@Override
	public synchronized boolean adicionaNovaPagina(URLAddress urlAdd) {

		if(!pagVisitadas.contains(urlAdd) && urlAdd.getDepth() < DEPTH_LIMIT){
			Servidor servidor = new Servidor(urlAdd.getDomain());
			List<URLAddress> lista;

			if(!fila.containsKey(servidor)){
				lista = new LinkedList<>();
				lista.add(urlAdd);
				fila.put(servidor, lista);
				servidores.add(servidor);
			}else{
				lista = fila.get(servidor);
				lista.add(urlAdd);
			}
			pagVisitadas.add(urlAdd);
			notifyAll();
			return true;
		}
		return false;
	}


	@Override
	public synchronized Record getRecordAllowRobots(URLAddress url) {
		if (mapaRobots.containsKey(url.getDomain())) {
			return mapaRobots.get(url.getDomain());
		}
		return null;
	}

	@Override
	public synchronized void putRecorded(String domain, Record domainRec) {
		if (!mapaRobots.containsKey(domain)) {
			mapaRobots.put(domain, domainRec);
		}
	}
	
	@Override
	public synchronized boolean finalizouColeta() {
		if (cont_paginas >= limite_paginas)
			return true;
		return false;
	}

	@Override
	public synchronized void countFetchedPage() {
		cont_paginas++;
	}

	
}
