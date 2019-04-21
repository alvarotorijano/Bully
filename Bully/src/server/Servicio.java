package server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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
	private static int NUM_PROCESOS = 0;
	private static HashMap<Integer, Proceso> procesos = new HashMap<Integer, Proceso>();
	private static HashMap<Integer, String> ubicaciones = new HashMap<>();
	private static boolean inicializado = false; //Esta variable de momento esta aqui para decir que si ya hemos recibido un mensaje de inicializacion lo desechemos. Mas adente podemos contemplar si vamos a actualizar nuestro mapa de direcciones con lo que hemos recibido a�adiendo o destruyendo procesos o lo que sea.
	//Este array nos servir� para saber que ids tienen que tener los procesos que nosotros crearemos, tambien quedar� alineado con los procesos locales de la maquina, asi cuando tengamos nuestro array de procesos locales, asi no tendremos que encuestar a cada uno de los metodos hasta dar con el que tiene el id que queremos. Otra opcion es recorrer el array haciendo un proceso[i].getID
	
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
			}
		}
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
		
	}
	*/
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("inicializa")
	public String inicializa(
			@QueryParam(value = "mapa")String mensaje // Este es el mapa de ids y direcciones
			) 
	{
		
		ArrayList<String> direccionesPropias = new ArrayList<String>();
		
		//Aqui hay un error porque tienes una linea borrada, no se que quieres hacer
		//procesos.get
		
		if(inicializado == false) {
			inicializado = true;
			try {
				ubicaciones = generaMapa (mensaje);

				System.out.println("Hola soy la funcion incializar y he recibido este mapa: " + ubicaciones);
				direccionesPropias = listarDireccionesLocales();
				
				Iterator iteradorUbicaciones = ubicaciones.entrySet().iterator();
				
				while (iteradorUbicaciones.hasNext()) {
					Map.Entry proceso = (Map.Entry)iteradorUbicaciones.next();
					if (direccionesPropias.contains(proceso.getValue())) {
						//Aqui lanzo los procesos
						System.out.println("Lanzo el proceso con id + " + proceso.getKey());
						procesos.put((Integer)proceso.getKey(), new Proceso((Integer)proceso.getKey()));
					}
				}
				
			}
			catch (Exception e) {
				System.out.println("Error leyando las direcciones IP locales");
			}
			
		}
		
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
			
			procesos.get(id).confirmar(sender);
		
		
		return "Eleccion enviada";
		
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("confirma")
	public String confirma(
			@QueryParam(value = "id")int id) {
		
			procesos.get(id).Ok();
		return "Ok";
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("coordinar")
	public String coordinar(@QueryParam(value = "coordinador") int coordinador) {
		
		//NO SE MUY BIEN SI SE HACE DE ESTA MANERA
		
		Iterator iteradorProcesos = procesos.entrySet().iterator();
		
		while(iteradorProcesos.hasNext()) {
			Map.Entry proceso = (Map.Entry)iteradorProcesos.next();
			if((Integer)proceso.getKey() != coordinador) {
				procesos.get(proceso.getKey()).coordinador(coordinador);
			}
				
		}
		/*for(int i = 0; i< NUM_PROCESOS; i++) {
			if(i!= coordinador) {
					procesos.get(i).coordinador(coordinador);
			}
		}*/
		return "Has indicado que eres el nuevo coordinaddor";
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("arranca")
	public String arranca(@QueryParam(value = "identificador") int identificador) {

		procesos.get(identificador).arrancar();
		
		return ("Proceso arrancado");
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("para")
	public String para(@QueryParam(value = "identificador") int identificador) {

		procesos.get(identificador).parar();
		
		return ("Proceso parado");
	}

	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("computa")
	public int computa(@QueryParam(value = "coordinador") int coordinador) {
		
		int valor = 0;
		
		valor = procesos.get(coordinador).computar();
		
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
	
	//ESTA FUNCION NO SE PARA QUE LA QUIERES, NO LA USAS EN NINGUN LADO
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
	
}
