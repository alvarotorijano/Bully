package clients;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class Gestor {
	
	private static HashMap<Integer, String> ubicaciones = new HashMap<>();
	private static ArrayList<Integer> procesosParados = new ArrayList<Integer>();
	
	public static void main(String[] args) {
		if(args.length > 0) {
			for(int i = 0; i< args.length; i++) {
				int aux = i+1;
				ubicaciones.put(Integer.valueOf(args[i]),args[aux]);
				i = aux;
			}
		}
		System.out.println("Se están iniciando los procesos");
		//indicar a los servicios que arranquen los procesos
		
		menu();
	}
	
	public static void menu() {
		boolean control = true;
		
		while(control) {
			System.out.println("------------------------------");
			System.out.println("-----------OPCIONES-----------");
			System.out.println("------------------------------");
			System.out.println();
			System.out.println("  1) Mostrar los procesos");
			System.out.println("  2) Parar un proceso");
			System.out.println("  3) Arrancar un proceso");
			System.out.println("  4) Terminar ejecucion");
			System.out.println();
			System.out.println("------------------------------");
			
			Scanner input = new Scanner(System.in); 
			
			int eleccion = input.nextInt();
			
			switch(eleccion) {
			case 1:
				muestraProcesos();
				break;
				
			case 2:
				paraProceso();
				break;
				
			case 3:
				arrancaProceso();
				break;
				
			case 4:
				control = false;
				break;
			
			default:
				System.out.println("El valor introducido debe ser 1, 2, 3 o 4");
			}
		}
	}

	private static void arrancaProceso() {
		System.out.flush();
		
		System.out.println("Indica el ID del proceso que quieras arrnacar entre los siguientes");
		for(int i = 0; i< procesosParados.size(); i++) {
			System.out.print(procesosParados.get(i) + "   " );
		}
		
		Scanner input = new Scanner(System.in);
		
		int id = input.nextInt();
		
		Client client = ClientBuilder.newClient();
		String ip = ubicaciones.get(id);
		URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
		WebTarget target = client.target(uri);
		
		System.out.println(target.path("rest").path("servicio").path("arranca").queryParam("identificador", id).request(MediaType.TEXT_PLAIN).get(String.class));
	}

	private static void paraProceso() {
		System.out.flush();
		
		System.out.println("Indica el ID del proceso que quieras parar comprendido entre los valores 1 y " + ubicaciones.size());
		
		Scanner input = new Scanner(System.in);
		
		int id = input.nextInt();
		
		Client client = ClientBuilder.newClient();
		String ip = ubicaciones.get(id);
		URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
		WebTarget target = client.target(uri);
		
		System.out.println(target.path("rest").path("servicio").path("para").queryParam("identificador", id).request(MediaType.TEXT_PLAIN).get(String.class));
	}

	private static void muestraProcesos() {
		for(int i = 0; i< ubicaciones.size(); i++) {
			Client client = ClientBuilder.newClient();
			String ip = ubicaciones.get(i);
			URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
			WebTarget target = client.target(uri);
			
			//Sacar clave del HashMap 
			System.out.println();
		}
	}

}
