package crawler.escalonadorCurtoPrazo;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	
	
	public EscalonadorSimples(){
		fila = new HashMap<>();
		pagVisitadas = new HashSet<>();
		servidores = new PriorityQueue<>();
	}
	
	
	@Override
	public synchronized URLAddress getURL() {
		do{
			Servidor servidor = servidores.remove();
			if(servidor.isAccessible()){
				servidores.add(servidor);
				try{
					wait(1000);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				continue;
			}else{
				if(!fila.get(servidor).isEmpty()){
					URLAddress url = fila.get(servidor).remove(0);
					servidor.acessadoAgora();
					servidores.add(servidor);
					return url;
				}else{
					servidores.add(servidor);
				}
			}
		}while(true);
	}

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
	public Record getRecordAllowRobots(URLAddress url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putRecorded(String domain, Record domainRec) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean finalizouColeta() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void countFetchedPage() {
		// TODO Auto-generated method stub
		
	}

	
}
