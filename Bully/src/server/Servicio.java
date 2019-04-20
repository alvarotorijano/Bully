package server;

import java.net.URI;
import java.util.*;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import clients.Proceso;


//singleton para que no se borren los atributos de la clase
@Singleton
@Path("servicio")
public class Servicio {
	
	//numero de procesos totales 
	private static int NUM_PROCESOS = 6;
	private static ArrayList<Proceso> procesos = new ArrayList<Proceso>();
	private static ArrayList<Integer> idsLibres = new ArrayList<Integer>();
	private static HashMap<Integer, String> ubicaciones = new HashMap<>();
	private static boolean inicializado = false; //Esta variable de momento esta aqui para decir que si ya hemos recibido un mensaje de inicializacion lo desechemos. Mas adente podemos contemplar si vamos a actualizar nuestro mapa de direcciones con lo que hemos recibido añadiendo o destruyendo procesos o lo que sea.
	//Este array nos servirï¿½ para saber que ids tienen que tener los procesos que nosotros crearemos, tambien quedarï¿½ alineado con los procesos locales de la maquina, asi cuando tengamos nuestro array de procesos locales, asi no tendremos que encuestar a cada uno de los metodos hasta dar con el que tiene el id que queremos. Otra opcion es recorrer el array haciendo un proceso[i].getID
	
	//Metodo main para la creacion y arranque de los procesos, se pasan por argumentos los ids de los procesos
	/* 
	public static void main(String[] args) {
		
		for (int i=0; i< NUM_PROCESOS; i++) {
			idsLibres.add(i+1);
		}
		
		if(args.length > 1) {
			for(int i = 0; i< args.length;) {
				
				// 6	192.168.1.2
				// 5	227.0.0.1
				// 3	192.168.1.3
				// 4	227.0.0.1
				
				idsLibres.remove(args[i]);
				ubicaciones.put(Integer.valueOf(args[i++]), args[i++]);
				
				// procesos.add(new Proceso(Integer.parseInt(args[i])));
				// procesos.get(i).start();
			}
		}
		//ahora que sabemos cuantos procesos remotos hay, generaremos nosotros el numero de ellos que falten.
		//Para eso hay que comprobar que ids tienen los demas y generar dos que no coincidan
		for (int i = 0; i< idsLibres.size(); i++) {
			procesos.add(new Proceso(idsLibres.get(i)));
			ubicaciones.put(idsLibres.get(i), "127.0.0.1");
		}
		for(Proceso p : procesos) {
			p.updateAddress(ubicaciones);
		} // Esta hecho asi para intentar que todos los procesos se lancen a la vez -cutre way-
		
		for(Proceso p : procesos) {
			p.start();
		}
		
		//Ahora tenemos todos nuestros procesos funcionando
		
		//Se me ocurre que para cada mensaje que un proceso envie, se la envie siempre
		// al servidor local, y este a su vez decida si se la tiene que enviar a 
		// otra maquina, o ejecutar un metodo de alguno de los procesos que tiene
	}
	*/
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("inicializa")
	public String inicializa(
			@QueryParam(value = "mapa")String mensaje // Este es el mapa de ids y direcciones
			) 
	{
		if(inicializado == false) {
			
		}
		ubicaciones = generaMapa (mensaje);
		System.out.println("Hola, soy la funcion inicializar");
		return "OK";
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("elegir")
	public String elegir(
			@QueryParam(value = "id")int id, // Este es el destinatario
			@QueryParam(value = "sender")int sender // este es el emisor
			) {
			
		for(int i=0; i<procesos.size(); i++) {
			if (procesos.get(i).ID == id) {
				procesos.get(i).confirmar(sender);
			}
		}
		
		return "Eleccion enviada";
		
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("confirma")
	public String confirma(
			@QueryParam(value = "id")int id) {
		for(int i=0; i<procesos.size(); i++) {
			if (procesos.get(i).ID == id) {
				procesos.get(i).Ok();
			}
		} 
		return "Ok";
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("coordinar")
	public String coordinar(@QueryParam(value = "coordinador") int coordinador) {

		for(int i = 0; i< NUM_PROCESOS; i++) {
			if(i!= coordinador) {
					procesos.get(i).coordinador(coordinador);
			}
		}
		return "Has indicado que eres el nuevo coordinaddor";
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("arranca")
	public String arranca(@QueryParam(value = "identificador") int identificador) {
		
		for(int i=0; i<procesos.size(); i++) {
			if (procesos.get(i).ID == identificador) {
				procesos.get(i).arrancar();
			}
		} 
		
		return ("Proceso arrancado");
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("para")
	public String para(@QueryParam(value = "identificador") int identificador) {
		
		for(int i=0; i<procesos.size(); i++) {
			if (procesos.get(i).ID == identificador) {
				procesos.get(i).parar();
			}
		}
		
		return ("Proceso parado");
	}

	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("computa")
	public int computa(@QueryParam(value = "coordinador") int coordinador) {
		int valor = 0;
		
		for(int i=0; i<procesos.size(); i++) {
			if (procesos.get(i).ID == coordinador) {
				valor = procesos.get(i).computar();
			}
		}
		
		return valor;

	}
	public static HashMap<Integer, String> generaMapa (String mapa) {
		
		String cadena = new String(Base64.getDecoder().decode(mapa)); //Decodificamos la cadena para volver a tener algo como esto: 1=192.168.1.2,2=127.0.0.1,3=192.168.1.3,4=127.0.0.1 
		HashMap<Integer, String> ubicacionesRecibidas = new HashMap<>();
		
		for(int i=0; i<cadena.split(",").length; i++) {
			ubicacionesRecibidas.put(Integer.valueOf(cadena.split(",")[i].split("=")[0]), cadena.split(",")[i].split("=")[1]);
		}
		
		return ubicacionesRecibidas;
		
	}
}
