package clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Base64;
import java.util.Collection;
import java.util.Enumeration;
import java.util.*;
//import java.util.Iterator;

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
	
	public static void iniciaServicios (HashMap<Integer, String> ubicaciones){
		
		String mensaje;
		ArrayList<String> maquinas = obtenerElementosUnicos(new ArrayList<String>(ubicaciones.values()));
		
		System.out.println("Inicializando servicios");
		mensaje = ubicaciones.entrySet().toString();//Esto nos devuelve una cadena asi: [1=192.168.1.2, 2=127.0.0.1, 3=192.168.1.3, 4=127.0.0.1]
		mensaje = mensaje.replace("[", "").replace("]", "").replace(" ", ""); //Con esto quitamos los corchetes y los espacios porque dan muchos problemas. Nos queda algo como esto 1=192.168.1.2,2=127.0.0.1,3=192.168.1.3,4=127.0.0.1
		mensaje=Base64.getEncoder().encodeToString(mensaje.getBytes()); //Esto nos permite convertir nuestra cadena en una sucesion de caracteres segura para ponerla en una URL y que los puntos, espacios, barras inclinadas etc no vuelvan loco al servidor web. Nos queda algo como esto MT0xOTIuMTY4LjEuMiwyPTEyNy4wLjAuMSwzPTE5Mi4xNjguMS4zLDQ9MTI3LjAuMC4x
		
		for (int i = 0; i<maquinas.size(); i++) {
			System.out.println("Llamo a la maquina " + maquinas.get(i) + " y tengo estas maquinas: " + maquinas.size());
			Client client = ClientBuilder.newClient();
			URI uri = UriBuilder.fromUri("http://" + maquinas.get(i) + ":8080/Bully").build();
			WebTarget target = client.target(uri);
			System.out.println("http://" + maquinas.get(i) + ":8080/Bully/rest/servicio/inicliza" );
			System.out.println(target.path("rest").path("servicio").path("inicializa").queryParam("mapa", mensaje).request(MediaType.TEXT_PLAIN).get(String.class));
		}
	}
	
	public static boolean arraylistContains (ArrayList<String> objetivo, ArrayList<String> origen) {
		for (int i = 0; i<objetivo.size(); i++) {
			if (origen.contains(objetivo.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<String> listarDireccionesLocales () throws SocketException{
		
		ArrayList<String> direcciones = new ArrayList<String>();
		Enumeration e = NetworkInterface.getNetworkInterfaces();
		
		while(e.hasMoreElements())
		{
		    NetworkInterface n = (NetworkInterface) e.nextElement();
		    Enumeration ee = n.getInetAddresses();
		    while (ee.hasMoreElements())
		    {
		        InetAddress i = (InetAddress) ee.nextElement();
		        direcciones.add(i.getHostAddress());
		    }
		}
		return direcciones;
	}
	
	public static ArrayList<String> obtenerElementosUnicos (ArrayList<String> entrada){
	
		ArrayList<String> salida = new ArrayList<String>();

		for(int i = 0; i<entrada.size(); i++) {
			if (salida.contains(entrada.get(i)) == false ){
				salida.add(entrada.get(i));
			}
		}
		return salida;
	}
	
	public static boolean compruebaArgumentos(String[] args) {
		//Este metodo compreuaba si el numero y tipo de los parametros es correcto y si hay algun id repetido o negativo
		
		ArrayList<String> ids = new ArrayList<String>();
		Integer i = 0, j = 0;
		
		if (args.length % 2 != 0 || args.length == 0) {
			mensajeDepuracion("El numero de argumentos es invalido");
			return false;
		}
		else {
			for(i = 0; i<args.length; i++) {
				if (i%2 == 0) { //este tiene que ser un ID
				  if(ids.contains(args[i]) == true || Integer.parseInt(args[i]) < 0) {// Esta linea comprueba ademas que los ids sean positivos
						mensajeDepuracion(String.join("El id: ", args[i], " es invalido o esta repetido"));
						return false;
					}
					else {
						ids.add(args[i]);
					}
				}
				else {
					if ( args[i].equals("127.0.0.1") || args[i].split("\\.").length != 4) {
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
					}
				}
			}
			return true;
		}
	}
	
	
	// Esta funcion imprime un mensaje si la depuracion est� activada, asi puedo deactivar todos los mensajes de depuracion cambiando solamente una variable. Esto tiene que hacerse asi porque en java no existe la compilacion condicional.
	public static void mensajeDepuracion(String mensaje) {
		if (depuracion == true) {
			System.out.println(mensaje);
		}
	}
	
	public static void menu() {
		int eleccion = 1;
		Scanner input = new Scanner(System.in); //Esto estaba dentro del bucle y cada vez que el bucle iteraba se ejecutaba...
		
		while(eleccion != 5) {

			mostrarMenu();
			eleccion = input.nextInt();
			
			switch(eleccion) {
			case 1:
				muestraProcesos();
				break;
				
			case 2:
				paraProceso(input);
				break;
				
			case 3:
				arrancaProceso(input);
				break;
				
			case 4:
				addProceso(input);
				break;
			
			case 5:
				System.out.println("Saliendo");
				input.close(); //A�adida esta linea para evitar la advertencia de que nunca cerramos el stream de entrada
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

	private static void arrancaProceso(Scanner input) {
		
		System.out.println("Indica el ID del proceso que quieras arranacar");
		
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
		//input.close();
		
	}

	private static void paraProceso(Scanner input) {
		//System.out.flush();
		
		System.out.println("Indica el ID del proceso que quieras parar comprendido entre los valores 1 y " + ubicaciones.size());
		
		//Scanner inputP = new Scanner(System.in);
		
		int id = input.nextInt();
		
		while (ubicaciones.get(id) == null) {
			System.out.println("Por favor introduzca un id valido o 0 para abortar");
			id = input.nextInt();
		}
		
		if (id != 0) {
			Client client = ClientBuilder.newClient();
			String ip = ubicaciones.get(id);
			URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
			System.out.println("Llamo parar al proceso " + id + " con la ip " + ip);
			WebTarget target = client.target(uri);
			
			System.out.println(target.path("rest").path("servicio").path("para").queryParam("identificador", id).request(MediaType.TEXT_PLAIN).get(String.class));
			System.out.println("Llamo parar al proceso " + id + " con la ip " + ip + " despues");
		}
		else {
			System.out.println("Operacion abortada: parar proceso");
		}
		//input.close();
	
	}

	private static void muestraProcesos() { //Esta funcion supongo que hay que rehacerla porque no est� haciendo nada, pero no la quiero cambiar porque no se que quieres hacer con ella.
	//Esto hau que asgurarse de que los procesos esten en orden
			
			//Sacar clave del HashMap
			//ubicaciones.

			Iterator iteradorUbicaciones =  ubicaciones.entrySet().iterator();
			while(iteradorUbicaciones.hasNext()) {
				Map.Entry entradaUbicaciones = (Map.Entry) iteradorUbicaciones.next();
				Client client = ClientBuilder.newClient();
				String ip =(String) entradaUbicaciones.getValue(); //Esto te devuelve el value de la key i. Si no hay nada, te devuelve NULL
				URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
				WebTarget target = client.target(uri);
				
				System.out.println(target.path("rest").path("servicio").path("muestra").queryParam("id", (Integer) entradaUbicaciones.getKey()).request(MediaType.TEXT_PLAIN).get(String.class));
				
			}
			System.out.println();
	}
	
	private static void addProceso(Scanner input) {
		
		System.out.println("Indica la IP de la m�quina en la que arrancar el nuevo proceso");
		Scanner input2 = new Scanner(System.in);
		String servidor = input2.nextLine();
		int nextProcess = ubicaciones.size()+1;
		
		boolean direccionCorrecta = true;
		if ( servidor.equals("127.0.0.1") || servidor.split("\\.").length != 4) {
			mensajeDepuracion(String.join("El argumento ", servidor, " esta mal formado"));
			direccionCorrecta = false;
		}
		else {
			for(int j = 0; j < 4; j++) {
				if(Integer.parseInt(servidor.split("\\.")[j]) < 0 || Integer.parseInt(servidor.split("\\.")[j]) > 255) {
					mensajeDepuracion(String.join("El elemento ", servidor.split("\\.")[j], " de la direccion ", servidor, " es invalido") );
					direccionCorrecta =  false;
				}
			}
		}
		
		if(direccionCorrecta) {
			ubicaciones.put(nextProcess, servidor);
			
			Iterator iteradorUbicaciones =  ubicaciones.entrySet().iterator();
			
			while(iteradorUbicaciones.hasNext()) {
				
				Map.Entry entradaUbicaciones = (Map.Entry) iteradorUbicaciones.next();
				
				if((Integer) entradaUbicaciones.getKey() != nextProcess) {
					
					Client client = ClientBuilder.newClient();
					String ip =(String) entradaUbicaciones.getValue(); //Esto te devuelve el value de la key i. Si no hay nada, te devuelve NULL
					URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
					WebTarget target = client.target(uri);
					
					System.out.println(target.path("rest").path("servicio").path("anadir").queryParam("idReceptor", (Integer) entradaUbicaciones.getKey()).queryParam("idNuevo", nextProcess).queryParam("ipNueva", servidor).request(MediaType.TEXT_PLAIN).get(String.class));
				}
			}
			
			Client client = ClientBuilder.newClient();
			String ip =(String) ubicaciones.get(nextProcess); //Esto te devuelve el value de la key i. Si no hay nada, te devuelve NULL
			URI uri = UriBuilder.fromUri("http://" +  ip + ":8080/Bully").build();
			WebTarget target = client.target(uri);
			
			System.out.println(target.path("rest").path("servicio").path("arrancaNuevo").queryParam("identificador", nextProcess).request(MediaType.TEXT_PLAIN).get(String.class));
			//Una vez pasados los mapas arrancar el proceso en el servidor correspondiente mediante nuevo metodo rest.
		}
		System.out.println();
		
		
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
		System.out.println("\n\t4) A�adir un proceso");
		System.out.println("\n\t5) Terminar ejecucion");
		System.out.println("\n------------------------------");
	}
	
}
