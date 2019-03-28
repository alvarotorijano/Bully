package clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import java.math.*;
import java.net.URI;

public class Proceso extends Thread {

	private int ID;       //ID del proceso
	private int coordinador = -1;       //ID del coordinador
	private estado_proceso_t estadoProceso;       //estado de la eleccion que será algun valor de la enumeracion
	private estado_eleccion_t estadoEleccion;      //estado del proceso que será algun valor de la enumeracion
	int espera = (int) (Math.random() % 500) + 500;      //timeout entre 0.5 y 1 para el metodo run()
	private Object eleccion = new Object();      //objeto de sincronizacion del metodo eleccion()

	private enum estado_eleccion_t {
		ACUERDO, ELECCION_ACTIVA, ELECCION_PASIVA
	}
	
	private enum estado_proceso_t {
		PARADO, CORRIENDO
	}

	Client client = ClientBuilder.newClient();
	
	URI uri = UriBuilder.fromUri("http://localhost:8080/Bully").build();
	
	WebTarget target = client.target(uri);
	
	
	//Constructor para la creación de la clase
	public Proceso(int ID) {
		this.ID = ID;
		this.estadoProceso = estado_proceso_t.CORRIENDO;
		
	}
	
	public void run()
	{
		if(this.estadoProceso == estado_proceso_t.PARADO) {
			synchronized(this.getClass()) {  //Si está parado paro la clase [forma de "matar" al proceso]
				try {
					this.getClass().wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
				synchronized(this.getClass()) {
					try {
						this.getClass().wait(espera); //espera aleatoria
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				espera = (int) (Math.random() % 500) + 500; //actualizo el valor aleatorio de la espera
				//mandar peticion al servidor
				eleccion();
			}
		
	}
	
	public void eleccion() {
		//estado de eleccion activa y mando pèticion al servidor para la eleccion pasandole mi ID
		this.estadoEleccion = estado_eleccion_t.ELECCION_ACTIVA;
		System.out.println(target.path("rest").path("servicio").path("elegir").queryParam("id", this.ID).request(MediaType.TEXT_PLAIN).get(String.class));
		
		//Espero el timeout de 1 segundo
		synchronized(this.eleccion) {
			try {
				this.eleccion.wait(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Compruebo si el estado de la eleccion ha cambiado, es decir ha salido del wait por recibir un mensaje
		if(this.estadoEleccion == estado_eleccion_t.ELECCION_PASIVA) {
			
			//Espero el timeout de 1 segundo
			synchronized(this.eleccion) {
				try {
					this.eleccion.wait(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Compruebo si el estado de la eleccion ha cambiado, es decir ha salido del wait por recibir un mensaje
			if(this.estadoEleccion == estado_eleccion_t.ACUERDO) {
//++++++++++++++++++++++++++++++++++cambio el this.coordinador con el valor de la funcion coordinador++++++++++++++++++++++++//
			}
			else {
				eleccion(); //vuelvo a inicio
			}
		}
		else {
			//Me convierto en coordinador y llamo a la función coordinador para inidcarselo a todos los procesos
			this.coordinador = this.ID;
			coordinador(this.ID);
		}
	}

	public void coordinador(int idCoordinador) {
		
	}
}
