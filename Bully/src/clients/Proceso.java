//Comentario para modificar el archivo

package clients;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import java.math.*;
import java.net.URI;
import java.util.HashMap;

public class Proceso extends Thread {

	private static int NUM_PROCESOS = 6;

	public int ID;       //ID del proceso
	private int coordinador = -1;       //ID del coordinador
	private estado_proceso_t estadoProceso;       //estado de la eleccion que será algun valor de la enumeracion
	private estado_eleccion_t estadoEleccion;      //estado del proceso que será algun valor de la enumeracion
	int espera = (int) (Math.random() % 500) + 500;      //timeout entre 0.5 y 1 para el metodo run()
	private Object eleccion = new Object();      //objeto de sincronizacion del metodo eleccion()
	private int valor;
	private static HashMap<Integer, String> ubicaciones = new HashMap<>();

	private enum estado_eleccion_t {
		ACUERDO, ELECCION_ACTIVA, ELECCION_PASIVA
	}
	
	private enum estado_proceso_t {
		PARADO, CORRIENDO
	}
	
	
	//Constructor para la creación de la clase
	public Proceso(int ID) {
		this.ID = ID;
		this.estadoProceso = estado_proceso_t.CORRIENDO;
		System.out.println("Hola, soy el proceso " + this.ID + " y acabo de ser creado");
	}
	
	
	public void updateAddress(HashMap ubicaciones) {
		this.ubicaciones = ubicaciones;
	}
	
	public int getCoordinador() {
		return coordinador;
	}
	
	public estado_proceso_t getEstado() {
		return estadoProceso;
	}
	//************************************************************************************************
	//
	//
	// Metodo run() que se ejecuta si el proceso esta activo, el cual comprueba si hay coordinador
	//
	//
	//************************************************************************************************
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
				Client client = ClientBuilder.newClient();
				String ip = this.ubicaciones.get(this.coordinador);
				URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
				
				WebTarget target = client.target(uri);
				this.valor = target.path("rest").path("servicio").path("computa").queryParam("coordinador", this.coordinador).request(MediaType.TEXT_PLAIN).get(int.class);
				//mandar peticion al servidor
				if(this.valor<0)
					eleccion();
				
			}
		
	}
	
	
	//************************************************************************************************
	//
	//
	// Metodo eleccion() se invoca cada vez que se detecta que falta un coordinador
	//
	//
	//************************************************************************************************
	public void eleccion() {
		//estado de eleccion activa y mando pèticion al servidor para la eleccion pasandole mi ID
		this.estadoEleccion = estado_eleccion_t.ELECCION_ACTIVA;
		for(int i = this.ID +1; i< NUM_PROCESOS; i++) {
			Client client = ClientBuilder.newClient();
			String ip = this.ubicaciones.get(i);
			URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
			
			WebTarget target = client.target(uri);
			System.out.println(target.path("rest").path("servicio").path("elegir").queryParam("id", this.ID).request(MediaType.TEXT_PLAIN).get(String.class));

		}
		
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
			//Compruebo si el estado de la eleccion ha cambiado, es decir ha salido del wait por recibir un mensaje, si no vuelvo a realizar elecciones
			if(this.estadoEleccion != estado_eleccion_t.ACUERDO) {
				eleccion();
			}
		}
		else {
			//Me convierto en coordinador y llamo a la función coordinador para inidcarselo a todos los procesos
			this.coordinador = this.ID;
			coordinador(this.ID);
		}
	}

	
	//************************************************************************************************
	//
	//
	// Metodo coordinador(int) comprueba el argumento, si es el mismo que el del proceso se lo tiene que
	// indicar a los demas procesos, si no cambia el valor de su coordinador
	//
	//
	//************************************************************************************************
	public void coordinador(int idCoordinador) {
		if(this.ID == idCoordinador) {
			for(int i = 0; i< NUM_PROCESOS; i++) {
				if(i != this.ID) {
					Client client = ClientBuilder.newClient();
					String ip = this.ubicaciones.get(i);
					URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
					
					WebTarget target = client.target(uri);
					System.out.println(target.path("rest").path("servicio").path("coordinar").queryParam("coordinador", this.ID).request(MediaType.TEXT_PLAIN).get(String.class));

				}
			}
		}
		else {
			synchronized(this.eleccion) {
				this.estadoEleccion = estado_eleccion_t.ACUERDO;
				this.eleccion.notify();
			}
			this.coordinador = idCoordinador;
		}
	}
	
	
	//************************************************************************************************
	//
	//
	// Metodo computar() se llama ciclicamente, para comprobar el estado del coordinador
	//
	//
	//************************************************************************************************
	public int computar() {
		int valor;
		if(this.estadoProceso == estado_proceso_t.PARADO)
			valor = -1;
		else {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			valor = 1;
		}
		return valor;
	}
	
	
	//************************************************************************************************
	//
	//
	// Metodo parar() lo llama el gestor, modifica el estado del proceso y  pausa su eejcucion
	//
	//
	//************************************************************************************************
	public void parar() {
		this.estadoProceso = estado_proceso_t.PARADO;
		synchronized(this.getClass()) {
			try {
				this.getClass().wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	//************************************************************************************************
	//
	//
	// Metodo arrancar() lo llama el gestor para reanudar la ejecucion del proceso y realizar elecciones
	//
	//
	//************************************************************************************************
	public void arrancar() {
		synchronized(this.getClass()) {
			this.getClass().notify();
		}
		this.estadoProceso = estado_proceso_t.CORRIENDO;
		eleccion();
	}
	
	//************************************************************************************************
	//
	//
	// Metodo confirmar() para comprobar el estado del proceso con ID mas alto que el proceso que 
	// realiza el metodo eleccion
	//
	//
	//************************************************************************************************
	public void confirmar(int sender) {
		
		if(this.estadoProceso == estado_proceso_t.CORRIENDO) {
			Client client = ClientBuilder.newClient();
			String ip = this.ubicaciones.get(sender);
			URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
			
			WebTarget target = client.target(uri);
			System.out.println(target.path("rest").path("servicio").path("confirma").queryParam("id", sender).request(MediaType.TEXT_PLAIN).get(String.class));

			
		}

	}
	
	
	//************************************************************************************************
	//
	//
	// Metodo Ok() cuando se llama a este metodo es porque hay algun proceso con ID mas alto, por lo
	// tanto cambia el estado de la eleccion
	//
	//
	//************************************************************************************************
	public void Ok() {
		this.estadoEleccion = estado_eleccion_t.ELECCION_PASIVA;
		synchronized(this.eleccion) {
			this.eleccion.notify();
		}
	}
	
	
}

