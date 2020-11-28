package de.fraunhofer.igd.geo.vgv.flink;
import java.lang.Math;
public class Position {

    private double lng;
    private double lat;

    public Position(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    /**
     * Calculates the distance from this position to the passed position.
     * @param other Another position.
     * @return The distance in kilometers.
     */
    public double distanceTo(Position other) {
	final int METHOD = 2;
	
	// Equatorial radius in meters according to https://en.wikipedia.org/wiki/Flattening
	double a = 6378137.0; 
	double d, sigma;
	// Flattening of earth according to https://en.wikipedia.org/wiki/Flattening
	double f = 1/298.257223563;
	double phi_1 = this.lat/180.0*Math.PI;
	double phi_2 = other.lat/180.0*Math.PI;
	double lmb_1 = this.lng/180.0*Math.PI;
	double lmb_2 = other.lng/180.0*Math.PI;
	switch(METHOD){
		
		case 0:
		/** Implementing Flat Surface Formula according to https://en.wikipedia.org/wiki/Geographical_distance#Spherical_Earth_projected_to_a_plane
		* Use this if the surface in the area of the two positions can be approximated by a flat 2D plane with low error and a perfect sphere is assumed
		*/

		d = a * Math.sqrt(Math.pow(phi_2 - phi_1,2) + Math.pow(Math.cos(.5 * (phi_2 + phi_1)) * (lmb_2 - lmb_1),2));
		break;

		case 1:	
	        /** Implementing Lambert's Formula according to https://en.wikipedia.org/wiki/Geographical_distance#Lambert's_formula_for_long_lines
		* Use this if the distances between positions is very high 
		*/
		
		// Convert latitudes of positions to reduced latitudes
		double beta_1 = Math.atan((1-f)*Math.tan(this.lat/180*Math.PI));
		double beta_2 = Math.atan((1-f)*Math.tan(other.lat/180*Math.PI));
		
		// Get longitudes
		double lambda_1 = this.lng/180*Math.PI;
		double lambda_2 = other.lng/180*Math.PI;
		
		// Calculate central angle sigma according to https://en.wikipedia.org/wiki/Great-circle_distance
		sigma = Math.acos(Math.sin(beta_1)*Math.sin(beta_2) + Math.cos(beta_1)*Math.cos(beta_2)*Math.cos(lambda_1-lambda_2));
		// Calculate distance according to Lambert
		double P = (beta_1 + beta_2)/2;
		double Q = (beta_2 - beta_1)/2;
		double X = (sigma - Math.sin(sigma)) * (Math.pow(Math.sin(P),2) * Math.pow(Math.cos(Q),2))/Math.pow(Math.cos(sigma/2),2);
		double Y = (sigma + Math.sin(sigma)) * (Math.pow(Math.sin(Q),2) * Math.pow(Math.cos(P),2))/Math.pow(Math.sin(sigma/2),2);
		d = a*(sigma - f/2*(X+Y));
    		break;
			
		case 2:	
	        /** Implementing Bowring's Formula according to https://en.wikipedia.org/wiki/Geographical_distance#Lambert's_formula_for_long_lines
		* Use this if the distances between positins is very low	
		*/
	
		// Calculate 2nd eccentricity e_2nd
		double e_2nd = f*(2-f)/Math.pow(1-f,2);
	
		double A = Math.sqrt(1+e_2nd*Math.pow(Math.cos(phi_1),4));
		double B = Math.sqrt(1+e_2nd*Math.pow(Math.cos(phi_1),2));
		double C = Math.sqrt(1+e_2nd);
		double w = A * (lmb_2 - lmb_1)/2;
		double delta_phi = phi_2 - phi_1;
		double delta_lambda = lmb_2 - lmb_1;
		double D = delta_phi/(2*B) * (1 + 3 * e_2nd/Math.pow(2*B,2) * delta_phi * Math.sin(2 * phi_1 + 2/3 * delta_phi));
		double E = Math.sin(D)*Math.cos(w);
		double F = 1/A * Math.sin(w) * (B * Math.cos(phi_1) * Math.cos(D) - Math.sin(phi_1) * Math.sin(D));
		// double G = Math.atan(F/E);
		sigma = 2 * Math.asin(Math.sqrt(E*E + F*F));
		// double H = Math.atan(1/A * Math.tan(w) * (Math.sin(phi_1 + B * Math.cos(phi_1) * Math.tan(D))));
		// double alpha_1 = G - h;
		// double alpha_2 = G + H + Math.PI;
		d = a * C * sigma/B/B;
		// Calculate spherical radius R_prime
		// double R_prime = Math.sqrt(1+e_2nd)/Math.pow(B,2)*a;
		
		// Calculate differences of primed parameters for the spherical coordinates
		// double delta_lambda_prime = A * delta_lambda;
		// double d = R_prime * Math.sqrt(Math.pow(Math.cos((this.lat-other.lat)/360.0*Math.PI)*delta_lambda_prime,2) + Math.pow(delta_phi_prime,2));
		break;
		
		default:
		d = 0;
		break;
	}
	System.out.println(d);	
	return d;
    }
}
