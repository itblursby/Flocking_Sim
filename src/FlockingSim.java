import processing.core.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;


public class FlockingSim extends PApplet {

	public static void main(String[] args) {
		String[] processingArgs = {"FlockingSim"};
		FlockingSim sketch = new FlockingSim();
		PApplet.runSketch(processingArgs, sketch);
	}

	private int birdsTotal = 100;

	private float vision = 50f;
	private float separationDist = 30f;
	private float maxSpeed = 5f;
	private int gridSize = 20;
	private int margin = 100;
	private float edgeRepel = 1f;
	private float alignment = 0.1f;
	private float cohesion = 0.005f;
	private float separation = 0.05f;



	private Bird[] birds = new Bird[birdsTotal];
	ArrayList<Integer> keys = new ArrayList<Integer>(100);
	HashMap<Integer, HashSet<Integer>> space = new HashMap<>();


	@Override
	public void settings() {
		size(800,600);
	}
	@Override
	public void setup() {
		// for (int i = 0; i < ((int)ceil(width/gridSize)) *((int)ceil(height/gridSize)); i++){
		// 	space.put(i,new HashSet<Bird>());
		// }
		for (int i = 0; i < birdsTotal; i++){
			birds[i] = new Bird();
		}
	}
	@Override
	public void draw() {
		// for (int i = 0; i < ((int)ceil(width/gridSize)) *((int)ceil(height/gridSize)); i++){
		// 	space.get(i).clear();
		// }

		for (int i = 0; i < birdsTotal; i++){
			int hash = birds[i].getPosHash();
			if (!space.containsKey(hash)){
				keys.add(hash);
				// System.out.println(keys);
				space.put(hash,new HashSet<Integer>());
			}
			space.get(hash).add(i);
		}
		background(255,255,255);
		int checks = 0;

		for (int i = 0; i < birdsTotal; i++){
			Bird current = birds[i];
			float ax = 0f, ay = 0f;
			if (current.x > width-margin){
				ax += -edgeRepel;
			}
			if (current.x < margin){
				ax += edgeRepel;
			}
			if (current.y > height-margin){
				ay += -edgeRepel;
			}
			if (current.y < margin){
				ay += edgeRepel;
			}
			float avgx = 0;
			float avgy = 0;
			float avgvx = 0;
			float avgvy = 0;
			float avoidvx = 0;
			float avoidvy = 0;
			int birdsWithin = 0;
			//USING HASHMAP TO ONLY CHECK NEARBY
			// for (int dx = (int)-ceil(vision/gridSize); dx <= (int)ceil(vision/gridSize); dx++){
			// 	for (int dy = (int)-ceil(vision/gridSize); dy <= (int)ceil(vision/gridSize); dy++){
			// 		int hash = current.getPosHash(dx,dy);
			// 		if (space.containsKey(hash)){
			// 			// System.out.println(space.get(hash).size());
			// 			for (Integer jInteger : space.get(hash)){
			// 				int j = jInteger.intValue();
			// 				if (j == i){
			// 					continue;
			// 				}
			// 				Bird other = birds[j];
			// 				checks++;
			// 				float dist = dist(current.x,current.y,other.x,other.y);
			// 				if (dist < vision){
			// 					avgvx += other.vx;
			// 					avgvy += other.vy;
			// 					avgx += other.x;
			// 					avgy += other.y;
			// 					if (dist < separationDist){
			// 						avoidvx += current.x - other.x;
			// 						avoidvy += current.y - other.y;
			// 					}
			// 					birdsWithin++;
			// 				}
			// 			}
			// 		}
			// 	}
			// }


			//NAIVE SOLUTION
			for (int j = 0; j < birdsTotal; j++){
				if (j == i){
					continue;
				}
				checks++;
				Bird other = birds[j];
				float dist = dist(current.x,current.y,other.x,other.y);
				if (dist < vision){
					avgvx += other.vx;
					avgvy += other.vy;
					avgx += other.x;
					avgy += other.y;
					if (dist < separationDist){
						avoidvx += current.x - other.x;
						avoidvy += current.y - other.y;
					}
					birdsWithin++;
				}
			}
			//other
			if (birdsWithin != 0){
				avgx /= birdsWithin;
				avgy /= birdsWithin;
				avgvx /= birdsWithin;
				avgvy /= birdsWithin;
				ax += (avgx - current.x) * cohesion;
				ay += (avgy - current.y) * cohesion;
				ax += (avgvx - current.vx) * alignment;
				ay += (avgvy - current.vy) * alignment;
				ax += avoidvx * separation;
				ay += avoidvy * separation;

			}
			current.accelerate(ax,ay);
		}
		for (int i = 0; i < birdsTotal; i++){
			space.get(birds[i].getPosHash()).clear();

		}
		// System.out.println(checks);
		strokeWeight(2);

		for (int i = 0; i < birdsTotal; i++){
			birds[i].update();

		}
		for (int i = 0; i < birdsTotal; i++){
			birds[i].display();
		}
		System.out.println(frameRate);
		// ellipse(mouseX,mouseY,10,10);
	}
	// private int getPosHash(float x, float y){
	// 	return (int)x/gridSize + (int)y/gridSize;
	// }
	// private int getX(int key){
	// 	return
	// }
	private float distSq(float x1, float y1, float x2, float y2){
		return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
	}
	private class Bird{
	  public float x;
	  public float y;
		public float vx;
		public float vy;
	  public Bird(){
	    this.x = random(width);
	    this.y = random(height);
			this.vx = random(-5,5);
			this.vy = random(-5,5);
	  }
		public void update(){
			if (vx*vx + vy*vy > maxSpeed){
				float multiplier = maxSpeed/sqrt(vx*vx + vy*vy);
				vx *= multiplier;
				vy *= multiplier;
			}
			x += vx;
			y += vy;
		}
		public void accelerate(float ax, float ay){
			vx += ax;
			vy += ay;

		}
		public void display(){
			line(x,y,x-vx,y-vy);
		}
		public int getPosHash(int addx, int addy){
			return ((addx+(int)(x/gridSize) + (1 << 15))) | ((addy+(int)(y/gridSize) + (1 << 15)) << 16);
		}
		public int getPosHash(){
			return getPosHash(0,0);
		}
		// public int

	}
}
