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
		servidores = new PriorityQueue<>();
		mapaRobots = new HashMap<>();
		cont_paginas = 0;
	}
	
	
	@Override
	public synchronized URLAddress getURL() {
		boolean isAcessivel = true;
		do{
			if(!servidores.isEmpty()){
				for (Servidor servidor : servidores){
					if(!servidor.isAccessible()){
						isAcessivel = false;
					}else{
						if(!fila.get(servidor).isEmpty()){
							URLAddress url = fila.get(servidor).remove(0);
							servidor.acessadoAgora();
							this.notifyAll();
							servidores.add(servidor);
							return url;
						}else{
							servidores.remove(servidor);
						}
					}
				}
				if(!isAcessivel){
					try{
						this.wait(1000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		} while(!this.finalizouColeta());
		return null;
	}
	
	/*
	@Override
	public synchronized URLAddress getURL() {
		do{
			if(!servidores.isEmpty()){
				Servidor servidor = servidores.remove();
				if(!servidor.isAccessible()){
					servidores.add(servidor);
					try{
						this.wait(1000);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
					continue;
				}else{
					if(!fila.get(servidor).isEmpty()){
						URLAddress url = fila.get(servidor).remove(0);
						servidor.acessadoAgora();
						this.notifyAll();
						servidores.add(servidor);
						return url;
					}else{
						servidores.add(servidor);
					}
				}
			}
		} while(!this.finalizouColeta());
		return null;
	}
	 * */

	@Override
	public synchronized boolean adicionaNovaPagina(URLAddress urlAdd) {

		if(!pagVisitadas.contains(urlAdd) && urlAdd.getDepth() < DEPTH_LIMIT){
			Servidor servidor = new Servidor(urlAdd.getDomain());
			if(!fila.containsKey(servidor)){
				List<URLAddress> lista = new LinkedList<URLAddress>();
				lista.add(urlAdd);
				fila.put(servidor, lista);
				servidores.add(servidor);
			}else{
				List<URLAddress> lista = new LinkedList<URLAddress>();
				lista.add(urlAdd);
				fila.get(urlAdd.getDomain()).add(urlAdd);
			}
			pagVisitadas.add(urlAdd);
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
