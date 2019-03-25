package clients;

import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import java.math.*;
import java.net.URI;

public class Proceso extends Thread {

	private int ID;
	private int coordinador = -1;
	private estado_proceso_t estado;
	Date date = new Date();
	long last_check = date.getTime();
	int espera = (int) (Math.random() % 500) + 500;

	private enum estado_eleccion_t {
		ACUERDO, ELECCION_ACTIVA, ELECCION_PASIVA
	}
	
	private enum estado_proceso_t {
		PARADO, CORRIENDO
	}

	
	public Proceso(int ID) {
		this.ID = ID;
		this.estado = estado_proceso_t.CORRIENDO;
		
	}
	
	public void run()
	{
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/Bully").build();
		WebTarget target = client.target(uri);
		
		System.out.println();
		if(this.estado == estado_proceso_t.PARADO) {
			synchronized(this.getClass()) {
				try {
					this.getClass().wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			if(date.getTime() - last_check > espera){
				last_check = date.getTime();
				espera = (int) (Math.random() % 500) + 500;
				//mandar peticion al servidor
			}
		}
		
	}
}
