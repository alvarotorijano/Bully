package clients;

import java.net.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Base64;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;


public class Gestor {
	
	private static boolean depuracion = true;
	
	private static HashMap<Integer, String> ubicaciones = new HashMap<>();
	private static ArrayList<Integer> procesosParados = new ArrayList<Integer>();
	
	public static void main(String[] args) {
		
		
		if (compruebaArgumentos(args) == true) {
			System.out.println("Argumentos correctos, iniciando gestor: ");
			ubicaciones = generarMapa(args);
			iniciaServicios(ubicaciones);
			menu();
		}
		else {
			System.out.println("Argumentos invalidos");
		}
	}
	
	public static void iniciaServicios (HashMap<Integer, String> ubicaciones) {
		String mensaje;
		System.out.println("Inicializando servicios");
		mensaje = ubicaciones.entrySet().toString();//Esto nos devuelve una cadena asi: [1=192.168.1.2, 2=127.0.0.1, 3=192.168.1.3, 4=127.0.0.1]
		mensaje = mensaje.replace("[", "").replace("]", "").replace(" ", ""); //Con esto quitamos los corchetes y los espacios porque dan muchos problemas. Nos queda algo como esto 1=192.168.1.2,2=127.0.0.1,3=192.168.1.3,4=127.0.0.1
		mensaje=Base64.getEncoder().encodeToString(mensaje.getBytes()); //Esto nos permite convertir nuestra cadena en una sucesion de caracteres segura para ponerla en una URL y que los puntos, espacios, barras inclinadas etc no vuelvan loco al servidor web. Nos queda algo como esto MT0xOTIuMTY4LjEuMiwyPTEyNy4wLjAuMSwzPTE5Mi4xNjguMS4zLDQ9MTI3LjAuMC4x

		//Llamamos al servicio REST
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://" +  "127.0.0.1" + ":8080/Bully").build();
		WebTarget target = client.target(uri);
		
		System.out.println(target.path("rest").path("servicio").path("inicializa").queryParam("mapa", mensaje).request(MediaType.TEXT_PLAIN).get(String.class));
		
	}
	
	public static boolean compruebaArgumentos(String[] args) {
		//Este metodo compreuaba si el numero y tipo de los parametros es correcto y si hay alguno repetido
		
		ArrayList<String> ids = new ArrayList<String>();
		ArrayList<String> ips = new ArrayList<String>();
		Integer i = 0, j = 0;
		
		if (args.length % 2 != 0 || args.length == 0) {
			mensajeDepuracion("El numero de argumentos es invalido");
			return false;
		}
		else {
			for(i = 0; i<args.length; i++) {
				if (i%2 == 0) { //este tiene que ser un ID
				  //if(ids.contains(args[i]) == true || Integer.parseInt(args[i] < 0) { Esta linea comprueba ademas que los ids sean positivos
					if (ids.contains(args[i]) == true) {//si lo tenemos en el array entonces es que está repetido
						mensajeDepuracion(String.join("El id: ", args[i], " es invalido o esta repetido"));
						return false;
					}
					else {
						ids.add(args[i]);
					}
				}
				else {
					if ((ips.contains(args[i]) && !args[i].equals("127.0.0.1") )|| args[i].split("\\.").length != 4) {
						System.out.println();
						mensajeDepuracion(String.join("El argumento ", args[i], " esta repetido o mal formado"));
						return false;
					}
					else {
						for(j = 0; j < 4; j++) {
							if(Integer.parseInt(args[i].split("\\.")[j]) < 0 || Integer.parseInt(args[i].split("\\.")[j]) > 255) {
								mensajeDepuracion(String.join("El elemento ", args[i].split("\\.")[j], " de la direccion ", args[i], " es invalido") );
								return false;
							}
						}
						ips.add(args[i]);
					}
				}
			}
			return true;
		}
	}
	
	
	// Esta funcion imprime un mensaje si la depuracion está activada, asi puedo deactivar todos los mensajes de depuracion cambiando solamente una variable. Esto tiene que hacerse asi porque en java no existe la compilacion condicional.
	public static void mensajeDepuracion(String mensaje) {
		if (depuracion == true) {
			System.out.println(mensaje);
		}
	}
	
	public static void menu() {
		int eleccion = 1;
		Scanner input = new Scanner(System.in); //Esto estaba dentro del bucle y cada vez que el bucle iteraba se ejecutaba...
		
		while(eleccion != 4) {
			
			mostrarMenu();
			eleccion = input.nextInt();
			
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
				System.out.println("Saliendo");
				input.close(); //Añadida esta linea para evitar la advertencia de que nunca cerramos el stream de entrada
				break;
			
			default:
				System.out.println("El valor introducido debe ser 1, 2, 3 o 4");
			}
		}
	}

	private static HashMap<Integer, String> generarMapa(String[] args) {
		HashMap<Integer, String> ubicaciones = new HashMap<>();
		if(args.length > 0) {
			for(int i = 0; i< args.length; i++) {
				int aux = i+1;
				ubicaciones.put(Integer.valueOf(args[i]),args[aux]);
				i = aux;
			}
		}
		
		/// Estas lineas muestran el array de pids y el de direcciones
		//System.out.println("Estos son mis ids: " + ubicaciones.keySet() );
		//System.out.println("Estas son mis ips: " + ubicaciones.values() );
		
		return ubicaciones;
	}

	private static void arrancaProceso() {
		System.out.flush();
		
		System.out.println("Indica el ID del proceso que quieras arrnacar entre los siguientes");
		for(int i = 0; i< procesosParados.size(); i++) {
			System.out.print(procesosParados.get(i) + "   " );
		}
		
		Scanner input = new Scanner(System.in);
		
		int id = input.nextInt();
		
		while (ubicaciones.get(id) == null) {
			System.out.println("Por favor introduzca un id valido o 0 para abortar");
			id = input.nextInt();
		}
		
		if (id != 0) {
			Client client = ClientBuilder.newClient();
			String ip = ubicaciones.get(id);
			URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
			WebTarget target = client.target(uri);
			
			System.out.println(target.path("rest").path("servicio").path("arranca").queryParam("identificador", id).request(MediaType.TEXT_PLAIN).get(String.class));
		}
		else {
			System.out.println("Operacion abortada: arrancar proceso");
		}
		input.close();
		
	}

	private static void paraProceso() {
		System.out.flush();
		
		System.out.println("Indica el ID del proceso que quieras parar comprendido entre los valores 1 y " + ubicaciones.size());
		
		Scanner input = new Scanner(System.in);
		
		int id = input.nextInt();
		
		while (ubicaciones.get(id) == null) {
			System.out.println("Por favor introduzca un id valido o 0 para abortar");
			id = input.nextInt();
		}
		
		if (id != 0) {
			Client client = ClientBuilder.newClient();
			String ip = ubicaciones.get(id);
			URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
			WebTarget target = client.target(uri);
			
			System.out.println(target.path("rest").path("servicio").path("para").queryParam("identificador", id).request(MediaType.TEXT_PLAIN).get(String.class));
		}
		else {
			System.out.println("Operacion abortada: parar proceso");
		}
		input.close();
	
	}

	private static void muestraProcesos() { //Esta funcion supongo que hay que rehacerla porque no está haciendo nada, pero no la quiero cambiar porque no se que quieres hacer con ella.
		for(int i = 0; i< ubicaciones.size(); i++) {
			Client client = ClientBuilder.newClient();
			String ip = ubicaciones.get(i);
			URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
			WebTarget target = client.target(uri);
			
			//Sacar clave del HashMap 
			System.out.println();
		}
	}
	
	//Esta cosa no me gusta porque es grande y no hace nada, pero no he querido cambiarlo y meterlo todo en una linea. 
	private static void mostrarMenu() {
		System.out.println("------------------------------");
		System.out.println("-----------OPCIONES-----------");
		System.out.println("------------------------------");
		System.out.println();
		System.out.println("\n\t1) Mostrar los procesos");
		System.out.println("\n\t2) Parar un proceso");
		System.out.println("\n\t3) Arrancar un proceso");
		System.out.println("\n\t4) Terminar ejecucion");
		System.out.println("\n------------------------------");
	}
	
}
