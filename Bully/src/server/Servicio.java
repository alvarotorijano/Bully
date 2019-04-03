package server;

import java.util.ArrayList;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import clients.Proceso;


//singleton para que no se borren los atributos de la clase
@Singleton
@Path("servicio")
public class Servicio {
	
	//numero de procesos totales 
	private static int NUM_PROCESOS = 6;
	private static ArrayList<Proceso> procesos = new ArrayList<Proceso>();

	//Metodo main para la creacion y arranque de los procesos, se pasan por argumentos los ids de los procesos
	public static void main(String[] args) {
		
		if(args.length > 1) {
			for(int i = 1; i< args.length; i++ ) {
				procesos.add(new Proceso(Integer.parseInt(args[i])));
				procesos.get(i).start();
			}
		}
		
	}
	
	
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("elegir")
	public String elegir(@QueryParam(value = "id")int id) {
		
		for(int i = id+1; i<= NUM_PROCESOS; i++) {
			//mirar donde esta cada proceso y enviar mensaje eleccion
		}
		
		return "Elección enviada";
		
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("coordinar")
	public String coordinar(@QueryParam(value = "coordinador") int coordinador) {
		for(int i = 0; i< NUM_PROCESOS; i++) {
			if(i!= coordinador) {
				//Mandar mensaje coordinador al proceso i
			}
		}
		return "Has indicado que eres el nuevo coordinaddor";
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("arranca")
	public String arranca(@QueryParam(value = "identificador") int identificador) {
		
		for(int i = 0; i< procesos.size(); i++) {
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
		
		for(int i = 0; i< procesos.size(); i++) {
			if (procesos.get(i).ID == identificador) {
				procesos.get(i).parar();;
			}
			}
		
		return ("Proceso parado");
	}
}
