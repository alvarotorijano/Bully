package server;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


//singleton para que no se borren los atributos de la clase
@Singleton
@Path("servicio")
public class Servicio {
	
	//numero de procesos totales 
	private static int NUM_PROCESOS = 6;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("elegir")
	public String elegir(@QueryParam(value = "id")int id) {
		System.out.println("esta es la prueba del metodo elegir");
		for(int i = id+1; i<= NUM_PROCESOS; i++) {
			//mirar donde esta cada proceso y enviar mensaje eleccion
		}
		
		return "Eleccion enviada";
		
	}
}
