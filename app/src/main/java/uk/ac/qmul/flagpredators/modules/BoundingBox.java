package uk.ac.qmul.flagpredators.modules;
/*
The MIT License (MIT)

Copyright (c) <2015> <Chris Veness>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
/**
 * Created by Ming-Jiun Huang on 15/7/20.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * The coordinate of southwestern corner is (minLat, minLng).
 * The coordinate of Northeastern corner is (maxLat, maxLng).
 *  (maxLat, minLng)    NW---------------------NE   (maxLat, maxLng)
 *                      |                       |
 *                      |                       |
 *                      |           C           |
 *                      |                       |
 *                      |                       |
 *  (minLat, minLng)    SW---------------------SE   (minLat, maxLng)
 *
 * Using distance/2 to get NESW coordinates  >>  maxLat, minLat, maxLng and minLng
 *                       (maxLat, centreLng)
 *                       —————————N—————————
 *                      |                   |
 *                      |                   |
 * (centreLat, minLng)  W  RED    C   BLUE  E  (centreLat, maxLng)
 *                      |                   |
 *                      |                   |
 *                       —————————S—————————
 *                       (minLat, centreLng)
 */
public class BoundingBox {
    // All lat and lng are in degree
    private Double minLat;
    private Double minLng;
    private Double maxLat;
    private Double maxLng;
    private double centreLat;
    private double centreLng;
    private int boundaryDistance;
    private boolean isRed;
    private Double boxLat;
    private Double boxLng;


    double EARTH_RADIUS = 6378137; //The major equatorial radius in metres from WGS-84
    //TODO The earth's radius in metres. Document for which value I choose to use

    /**
     *Create a square bounding box by the given location of the centre point and boundary length.
     *@param {double}    centreLat - The latitude of the centre of the bounding box.
     *@param {double}    centreLng - The longitude of the centre of the bounding box.
     *@param {int}       boundaryLength - The length of each edge given by the settings from a game initiator.
     */
    public BoundingBox(double centreLat, double centreLng, int boundaryLength) {
        this.centreLat = centreLat;
        this.centreLng = centreLng;
        this.boundaryDistance = boundaryLength/2;
        double[] n = this.buildCoordinate(boundaryDistance, 0);
        double[] e = this.buildCoordinate(boundaryDistance, 90);
        double[] s = this.buildCoordinate(boundaryDistance, 180);
        double[] w = this.buildCoordinate(boundaryDistance, 270);
        this.minLat = s[0];
        this.minLng = w[1];
        this.maxLat = n[0];
        this.maxLng = e[1];
        System.out.println(toString());
    }

    /**
     *Create a round bounding box by the given location of the centre point and boundary length.
     *@param {double}    centreLat - The latitude of the centre of the bounding box.
     *@param {double}    centreLng - The longitude of the centre of the bounding box.
     *@param {boolean}   isRed     - A flag for defining this round bounding box is for red team(true) or blue team(false).
     */
    public BoundingBox(double centreLat, double centreLng, boolean isRed, int boundaryLength){    //**
        this.centreLat = centreLat;
        this.centreLng = centreLng;
        this.isRed = isRed;
        this.boundaryDistance = boundaryLength/2;
        this.setBoxLocation();
        this.minLat = null;
        this.minLng = null;
        this.maxLat = null;
        this.maxLng = null;
        System.out.println(toString());
    }

    //Red team is on the north side, whereas blue team is on the south side.
    public void setBoxLocation(){
        double objectDistance = (boundaryDistance * 7 / 10);
        if(this.isRed){
            boxLat = this.buildCoordinate(objectDistance, 0)[0];
            boxLng = this.buildCoordinate(objectDistance, 0)[1];
        }else{
            boxLat = this.buildCoordinate(objectDistance, 180)[0];
            boxLng = this.buildCoordinate(objectDistance, 180)[1];
        }
    }

    public double[] getBoxLocation(){
        double[] boxLoaction = { boxLat, boxLng};
        return boxLoaction;
    }

    /**Return a destination location (Latitude and Longitude) in a double[] array by the given
     * distance in metres, bearing in degree and the current location(centreLat and centreLng).
     * @param   {double} distance - Distance in metres.
     * @param   {double} bearing  - The bearing is a clockwise from north in degree [θ].
     * @return  {double[]} dest   - Location of the destination point in degree. [0]Latitude, [1]Longitude
     */
//**  Need Reference!!!!!!!!!!!!!!!! see http://williams.best.vwh.net/avform.htm#LL
    public double[] buildCoordinate(double distance, double bearing){
        double cLat = this.toRadians(centreLat);
        double cLng = this.toRadians(centreLng);
        double angularDistance = distance/EARTH_RADIUS; //The angular distance in radians.
        double θ = this.toRadians(bearing); //The bearing in radians [θ]
        double[] dest = new double[2];
        dest[0] = Math.asin(Math.sin(cLat) * Math.cos(angularDistance) +
                Math.cos(cLat) * Math.sin(angularDistance) * Math.cos(θ)); //dest[0] latitude
        dest[1] = cLng + Math.atan2(Math.sin(θ) * Math.sin(angularDistance) * Math.cos(cLat),
                Math.cos(angularDistance) - Math.sin(cLat) * Math.sin(dest[0])); //dest[1] longitude
        dest[1] = (dest[1] + 3 * Math.PI) % (2 * Math.PI) - Math.PI; //Normalise to -180 ~ +180°
        dest[0] = this.toDegree(dest[0]); //Convert values back to degree
        dest[1] = this.toDegree(dest[1]);
        return dest;
    }

    /**Return the shortest distance between two points.
     * @param   {double} startLat - The latitude of the start point in degree.
     * @param   {double} startLng - The longitude of the start point in degree.
     * @param   {double} destLat - The latitude of the destination point in degree.
     * @param   {double} destLng - The longitude of the detination point in degree.
     * @return  {double} distance   - The distance between two points.
     */
//**Need Reference(Minos's Email)
//This method is an implementation of Haversine fomula based on code wirtten by Chris Veness http://www.movable-type.co.uk/scripts/latlong.html
    public double getDistance(double destLat, double destLng){
        double sLat = this.toRadians(boxLat);
        double dLat = this.toRadians(destLat);
        double sLng = this.toRadians(boxLng);
        double dLng = this.toRadians(destLng);
        double latDifference = this.toRadians(dLat - sLat);
        double lngDifference = this.toRadians(dLng - sLng);

        double a = Math.sin(latDifference/2) * Math.sin(latDifference/2) +
                Math.cos(sLat) * Math.cos(dLat) *
                        Math.sin(lngDifference/2) * Math.sin(lngDifference/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double distance = EARTH_RADIUS * c;
        return distance;
    }

//Vincenty
//This method is an implementation of Vincenty solutions of geodesics based on code wirtten by Chris Veness http://www.movable-type.co.uk/scripts/latlong-vincenty.html
    /**Return the shortest distance between two points.
     * @param   {double} startLat - The latitude of the start point in degree.
     * @param   {double} startLng - The longitude of the start point in degree.
     * @param   {double} destLat - The latitude of the destination point in degree.
     * @param   {double} destLng - The longitude of the detination point in degree.
     * @return  {double} distance   - The distance between two points.
     */
//**Need Reference(Minos's Email)
    public double getAccurateDistance(double destLat, double destLng){
        double f = 1 / 298.257223563; //The flattening from WGS-84
        double b = EARTH_RADIUS * (1 - f); //The polar semi-minor axis from WGS-84
        double φ1 = this.toRadians(boxLat);
        double φ2 = this.toRadians(destLat);
        double λ1 = this.toRadians(boxLng);
        double λ2 = this.toRadians(destLng);
        double L = λ2 - λ1; //The difference in Longitude
        double tanU1 = (1-f) * Math.tan(φ1);
        double cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1));
        double sinU1 = tanU1 * cosU1;
        double tanU2 = (1-f) * Math.tan(φ2);
        double cosU2 = 1 / Math.sqrt((1 + tanU2 * tanU2));
        double sinU2 = tanU2 * cosU2;
        double λ = L;
        double λʹ, iterationLimit = 100;
        double sinλ, cosλ, sinSqσ, sinσ, cosσ, σ, sinα, cosSqα, cos2σM, C;
        do {
            sinλ = Math.sin(λ);
            cosλ = Math.cos(λ);
            sinSqσ = (cosU2 * sinλ) * (cosU2 * sinλ) +
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosλ) * (cosU1 * sinU2 - sinU1 * cosU2 * cosλ);
            sinσ = Math.sqrt(sinSqσ);
            if (sinσ==0){ return 0; }   // Co-incident points
            cosσ = (sinU1 * sinU2) + (cosU1 * cosU2 * cosλ);
            σ = Math.atan2(sinσ, cosσ);
            sinα = cosU1 * cosU2 * sinλ / sinσ;
            cosSqα = 1 - (sinα * sinα);
            cos2σM = cosσ - (2 * sinU1 * sinU2 / cosSqα);
            if (Double.isNaN(cos2σM)){ cos2σM = 0; }   // Equatorial line: cosSqα=0 (§6)
            C = f / 16 * cosSqα * (4 + f * (4 - 3 * cosSqα));
            λʹ = λ;
            λ = L + (1 - C) * f * sinα * (σ + C * sinσ * (cos2σM + C * cosσ * (-1 + 2 * cos2σM * cos2σM)));
        } while (Math.abs(λ-λʹ) > 1e-12 && --iterationLimit > 0);
        if (iterationLimit == 0) {
            System.out.println("Formula failed to converge!");
            //return null; //Error
        }

        double uSq = cosSqα * (EARTH_RADIUS * EARTH_RADIUS - b * b) / (b * b);
        double A = 1 + uSq/16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq/1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double Δσ = B * sinσ * (cos2σM + B / 4 * (cosσ * (-1 + 2 * cos2σM * cos2σM) -
                B / 6 * cos2σM * (-3 + 4 * sinσ * sinσ) * (-3 + 4 * cos2σM * cos2σM)));

        double distance = b * A * (σ - Δσ);
//bearing
        double fwdAz = Math.atan2((cosU2 * sinλ), (cosU1 * sinU2) - (sinU1 * cosU2 * cosλ));
        double revAz = Math.atan2((cosU1 * sinλ), (-sinU1 * cosU2) + (cosU1 * sinU2 * cosλ));

        return distance;
    }

    //Convert the unit from degree to radians.
    public double toRadians(double degree){
        return (degree * Math.PI / 180);
    }
    //Convert the unit from radians to degree.
    public double toDegree(double radians){
        return (radians * 180 / Math.PI);
    }

    //Getters: return in degree.
    public double getMinLat(){
        return minLat;
    }
    public double getMinLng(){
        return minLng;
    }
    public double getMaxLat(){
        return maxLat;
    }
    public double getMaxLng(){
        return maxLng;
    }

    /**
     *Check whether the given location is in the bounds of this square bounding box.
     *@param    {double[]} location     - location[0] is latitude and location[1] is longitude in degree.
     *@return   {boolean}  inBounds     - If it is in bounds, return true vice versa.
     */
    public boolean checkInBoundsByCoordinate(double lat, double lng){
        //Latitude in degree
        if(minLat < maxLat){
            if(minLat <= lat && lat <= maxLat){
                //Longitude in degree
                if(minLng < maxLng && minLng <= lng && lng <= maxLng){
                    return true;
                    //if the bounding box contains antimeridian
                }else if (minLng > maxLng && (minLng <= lng || lng <= maxLng)){
                    return true;
                }
            }
        }else{
            //The poles
        }
        return false;
    }

    /**
     *Check whether the given location is in the bounds of this round bounding box.
     *@param    {double[]} location     - location[0] is latitude and location[1] is longitude in degree.
     *@return   {boolean}  inBounds     - If it is in bounds, return true vice versa.
     */
    /*
    public boolean checkInBoundsByDistance(double lat, double lng, double distance){
        if(isFlag){
            if (distance <= FLAG_RADIUS) {
                return true;
            }
        }else{
            if (distance <= BASE_RADIUS) {
                return true;
            }
        }
        return false;
    }*/
    public String toString(){
        String msg = "BOUNDING BOX INFO<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n";
        if(maxLat != null){
            msg += "[Centre] Latitude: " + centreLat + " Longitude: " + centreLng + "\n" +
                    "Boundary Distance: " + boundaryDistance + "\n" +
                    "Max Latitude: " + maxLat + " Max Longitude: " + maxLng + "\n" +
                    "Min Latitude: " + minLat + " Min Longitude: " + minLng;
        }else{
            msg += "[Centre] Latitude: " + centreLat + " Longitude: " + centreLng + "\n" +
                    "Boundary Distance: " + boundaryDistance + "\n" +
                    "Box Latitude: " + boxLat + " Box Longitude: " + boxLng;
        }
        return msg;
    }
}
