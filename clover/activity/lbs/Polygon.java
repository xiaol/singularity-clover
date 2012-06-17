package com.singularity.clover.activity.lbs;

public class Polygon {
	// Polygon coodinates.
 
    private Float[] polyY, polyX;
 
 
 
    // Number of sides in the polygon.
 
    private int polySides;
 
 
 
    /**
 
     * Default constructor.
 
     * @param px Polygon y coods.
 
     * @param py Polygon x coods.
 
     * @param ps Polygon sides count.
 
     */
 
    public Polygon( Float[] px, Float[] py, int ps ) {
 
 
 
        polyX = px;
 
        polyY = py;
 
        polySides = ps;
 
    }
 
 
 
    /**
 
     * Checks if the Polygon contains a point.
 
     * @see "http://alienryderflex.com/polygon/"
 
     * @param x Point horizontal pos.
 
     * @param y Point vertical pos.
 
     * @return Point is in Poly flag.
 
     */
 
    public boolean contains( float x, float y ) {

        boolean oddTransitions = false;
 
        for( int i = 0, j = polySides -1; i < polySides; j = i++ ) {
 
            if( ( polyY[ i ] < y && polyY[ j ] >= y ) || ( polyY[ j ] < y && polyY[ i ] >= y ) ) {
 
                if( polyX[ i ] + ( y - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < x ) {
 
                    oddTransitions = !oddTransitions;          
 
                }
 
            }
 
        }
 
        return oddTransitions;
 
    }
    
    public boolean contains( float x ,float y, float r){
    	boolean isIn = false;
    	
    	for(int i = 0,j = polySides -1; i< polySides; j = i++){
    		if(isIn(y,polyY[i],polyY[j]) && isIn(x,polyX[i],polyX[j])){
    			if(distanceToLine(x, y, polyX[i], polyY[i],polyX[j],polyY[j]) < r ){
    				isIn = true;
    			}
    		}else{
    			if(distance(x, y, polyX[i], polyY[i]) < r 
    					|| distance(x,y,polyX[j],polyY[j]) < r){
    				isIn = true;
    			}
    		}
    	}
    	return isIn;
    }
    
    public float abs(float x){
    	return x>0?x:(-x);
    }
    
    public boolean isIn(float x,float a,float b){
    	float max = a>b?a:b;
    	float min = a<b?a:b;
    	return x > min && x < max;
    }
    
    public float distance(float x1, float y1, float x2,float y2){
    	return abs(x2 - x1) + abs(y1 -y2);
    }
    
    public float distanceToLine(float x,float y,float x1,float y1,float x2,float y2){
    	return distance(x,y,x1,y1) + distance(x,y,x2,y2) - distance(x1,y1,x2,y2);
    }
}
