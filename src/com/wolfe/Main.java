package com.wolfe;

import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressType;
import com.google.maps.model.ElevationResult;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;



public class Main {

    //Use this scanner to read text data that will be stored in String variables
    static Scanner stringScanner = new Scanner(System.in);
    //Use this scanner to read in numerical data that will be stored in int or double variables
    static Scanner numberScanner = new Scanner(System.in);

    static GeocodingResult locationResult = null;   // object to hold GeoCoding call result
    static GeocodingResult[] googleResult = null;   // array of GeoCoding objects

    static int numChoice = 0;                       // refines user choice from list of locations

    public static void main(String[] args) throws Exception {

        String key = null;
        String userChoice;

        // Read the Google key from key file
        try (BufferedReader reader = new BufferedReader(new FileReader("key"))) {
            key = reader.readLine();
            System.out.println(key);
        } catch (Exception ioe) {
            System.out.println("No key file found, or could not read key");
            System.exit(-1);
        }

        // set Google access key letting Google know who is accessing the API
        GeoApiContext context = new GeoApiContext().setApiKey(key);

        // main loop gets the user desired location and presents the result(s),
        // the user is asked to pick which location they would like the elevation info for
        // option 99 is used to start a new search if all returned results unacceptable
        do {
            System.out.println("Welcome to the Location/Elevation Finder Program");
            System.out.println();
            System.out.println("Please enter a location would like to know about or");
            String location = stringScanner.nextLine();

            // call the GeoCoding API with the desired location information
            GeocodingResult[] googleResults = getGeoCode(context, location);

            if (googleResults != null) {
                // refine the search
                if (googleResults.length > 0) {
                    System.out.println();
                    System.out.println("Please select the number of location you would like the elevation for or");
                    System.out.println("Enter 99 to start a new search: ");
                    numChoice = numberScanner.nextInt();
                }

                // validate the user's choice
                while ((numChoice < 0 || numChoice > googleResults.length) && (googleResults.length > 0)
                        && (numChoice != 99)) {
                    System.out.println();
                    System.out.println("Please select the number of location you would like the elevation for or");
                    System.out.println("Enter 99 to start a new search: ");
                    numChoice = numberScanner.nextInt();
                }

                // show the elevation for the location the user has selected
                if (googleResults.length > 0 && numChoice != 99) {
                    getLatLong(context, googleResults[numChoice].geometry.location);
                }
            }

            System.out.println();
            System.out.println("Would you like to select another location (y or n): ");
            userChoice = stringScanner.nextLine();

        } while (userChoice.equals("y"));

        // Close scanners. Good practice to clean up resources you use.
        // Don't try to use scanners after this point. All code that uses scanners goes above here.
        stringScanner.close();
        numberScanner.close();

    } // end main method


    // this method calls the GeoCoding API and presents the returned information to the user
    // can be zero to many items returned from API
    private static GeocodingResult[] getGeoCode(GeoApiContext context, String location) {

        try {
            googleResult = GeocodingApi.geocode(context, location).await();
            System.out.println();
            if (googleResult.length > 0) {
                for (int i = 0 ; i < googleResult.length ; i++) {
                    locationResult = googleResult[i];
                    System.out.println();
                    //System.out.println("#" + i + ": results length = " + googleResult.length);
                    System.out.println("#" + i + ": location address = " + locationResult.formattedAddress);
                    System.out.println("#" + i + ": location placeid = " + locationResult.placeId);
                    System.out.println("#" + i + ": location geometry = " + locationResult.geometry.location);
                    for (AddressType item : locationResult.types) {
                        System.out.println("#" + i + ": location types = " + item);
                    }
                }
            } else {
                System.out.println("Location requested not found");
            }

        }
        catch (Exception e) {  // if exception caught, try again
            System.out.println("There was a problem getting location information");
            System.out.println("Exception: " + e);
            return null;
        }
        return googleResult;
    }


    // this method calls the Elevation API for requested elevation and
    // displays the results
    private static void getLatLong(GeoApiContext context, LatLng newElevation) {

        try {
            // LatLng myTestLatLng = new LatLng(38.84087070,-105.04225950);
            ElevationResult[] elevation = ElevationApi.getByPoints(context, newElevation).await();
            if (elevation.length >= 1) {
                ElevationResult myTestElevation = elevation[0];
                System.out.println();
                System.out.println("Location: " + googleResult[numChoice].formattedAddress);
                System.out.println("Data for Latitude/Longitude: " + newElevation);
                System.out.println(String.format("The elevation above sea level is %.2f meters.", myTestElevation.elevation));
                System.out.println();
            }
        }
        catch (Exception e) {
            System.out.println("There was a problem getting elevation information");
            System.out.println("Exception: " + e);
        }
    }

} // end Main class
