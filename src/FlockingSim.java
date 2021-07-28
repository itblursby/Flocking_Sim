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

//regular settings
	private int birdsTotal = 500;
	private float vision = 50f;
	private float mouseSeparation = 0.2f;
	private float mouseSeparationDist = 50f;
	private float separationDist = 15f;
	private float speed = 3f;
	private int margin = 100;
	private float alignment = 0.1f;
	private float cohesion = 0.005f;
	private float separation = 0.01f;
//internal settings
	private Bird[] birds = new Bird[birdsTotal];
	private int gridSize = 20;
	private float edgeRepel = speed/5f;

	ArrayList<Integer> keys = new ArrayList<Integer>(100);
	HashMap<Integer, HashSet<Integer>> space = new HashMap<>();


	@Override
	public void settings() {
		size(800,600);
	}
	@Override
	public void setup() {
		colorMode(HSB,360,100,100,100);
		for (int i = 0; i < birdsTotal; i++){
			birds[i] = new Bird(i);
		}
	}
	@Override
	public void draw() {
		for (int i = 0; i < birdsTotal; i++){
			int hash = birds[i].getPosHash();
			if (!space.containsKey(hash)){
				keys.add(hash);
				space.put(hash,new HashSet<Integer>());
			}
			space.get(hash).add(i);
		}
		fill(0,0,0,10);
		noStroke();
		rect(0,0,width,height);
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
			int currentAcquiredId = current.acquiredid;
			int max = -1;
			int maxbirth = current.birthid;
			float nexthue = Float.MIN_VALUE;
			int birdsWithin = 0;
			//USING HASHMAP TO ONLY CHECK NEARBY
			for (int dx = (int)-ceil(vision/gridSize); dx <= (int)ceil(vision/gridSize); dx++){
				for (int dy = (int)-ceil(vision/gridSize); dy <= (int)ceil(vision/gridSize); dy++){
					int hash = current.getPosHash(dx,dy);
					if (space.containsKey(hash)){
						// System.out.println(space.get(hash).size());
						for (Integer jInteger : space.get(hash)){
							int j = jInteger.intValue();
							if (j == i){
								continue;
							}
							Bird other = birds[j];
							float distSq = distSq(current.x,current.y,other.x,other.y);
							if (distSq < vision*vision){
								avgvx += other.vx;
								avgvy += other.vy;
								avgx += other.x;
								avgy += other.y;
								if (distSq < separationDist*separationDist){
									avoidvx += current.x - other.x;
									avoidvy += current.y - other.y;
								}
								int tmax = other.acquiredid;
								int tmaxbirth = other.birthid;
								if (tmaxbirth > maxbirth || tmax > max){
									max = tmax;
									nexthue = other.hue;
								}
								birdsWithin++;
							}
						}
					}
				}
			}

			// if (dist)
			//NAIVE SOLUTION
			// for (int j = 0; j < birdsTotal; j++){
			// 	if (j == i){
			// 		continue;
			// 	}
			// 	Bird other = birds[j];
			// 	float distSq = distSq(current.x,current.y,other.x,other.y);
			// 	if (distSq < vision*vision){
			// 		avgvx += other.vx;
			// 		avgvy += other.vy;
			// 		avgx += other.x;
			// 		avgy += other.y;
			// 		if (distSq < separationDist*separationDist){
			// 			avoidvx += current.x - other.x;
			// 			avoidvy += current.y - other.y;
			// 		}
			// 		int tmax = other.id;
			// 		if (tmax > max){
			// 			max = tmax;
			// 			nexthue = other.hue;
			// 		}
			// 		birdsWithin++;
			// 	}
			// }
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
				current.nexthue = nexthue;
				current.acquiredid = max;
				// avgcolorx+=100;
				// avgcolory+=100;
				// avgcolorx = Math.max(0,avgcolorx);
				// avgcolorx = Math.min(199,avgcolorx);
				//
				// avgcolory = Math.max(0,avgcolory);
				// avgcolory = Math.min(199,avgcolory);
				// System.out.println(avgcolorx);
				// System.out.println(avgcolory);
				//
				// int avgcolor = (int)lookup[(int)avgcolorx+(int)avgcolory*200];
				// // avgcolor = 0;
				// // System.out.println(binary(colorLookup.pixels[(int)avgcolorx+(int)avgcolory*200]));
				//
				// // System.out.println((avgcolor));
				// if (avgcolor - current.color > 50){
				// 	avgcolor -= 100;
				// }
				// if (avgcolor - current.color < -50){
				// 	avgcolor += 100;
				// }
				// float currentred = (current.color >> 16) & 0xff;
				// float currentgreen = (current.color >> 8) & 0xff;
				// float currentblue = (current.color) & 0xff;
				// currentred += (avgred - currentred)*0.1;
				// currentgreen += (avggreen - currentgreen)*0.1;
				// currentblue += (avgblue - currentblue)*0.1;
				// current.color = color(currentred,currentgreen,currentblue);
			}
			if (mousePressed && distSq(current.x,current.y,mouseX,mouseY) < mouseSeparationDist*mouseSeparationDist){
				ax += (mouseX - current.x) * -mouseSeparation;
				ay += (mouseY - current.y) * -mouseSeparation;
				current.hue = random(360);
				current.nexthue = Float.MIN_VALUE;
				current.acquiredid = current.birthid;
			}
			current.accelerate(ax,ay);
		}
		for (int i = 0; i < birdsTotal; i++){
			space.get(birds[i].getPosHash()).clear();
		}
		for (int i = 0; i < birdsTotal; i++){
			strokeWeight(random(3,8));

			birds[i].update();
			birds[i].display();
		}
	}
	private float distSq(float x1, float y1, float x2, float y2){
		return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
	}
	private class Bird{
	  public float x;
	  public float y;
		public float vx;
		public float vy;
		public float hue;
		public float nexthue;
		public int birthid;
		public int acquiredid;
	  public Bird(int id){
	    x = random(width);
	    y = random(height);
			vx = random(-5,5);
			vy = random(-5,5);
			hue = random(360);
			nexthue = Float.MIN_VALUE;
			birthid = id;
			acquiredid = birthid;
	  }
		public void update(){
			float scale = speed/sqrt(vx*vx + vy*vy);
			vx *= scale;
			vy *= scale;
			x += vx;
			y += vy;
			if (nexthue != Float.MIN_VALUE){
				hue = nexthue;
				nexthue = Float.MIN_VALUE;
			}
			hue = (hue + random(5) - 2.5f + 3600f) % 360;
			if (random(1)<0.01f){
				hue = random(360);
				acquiredid = birthid;
			}

		}
		public void accelerate(float ax, float ay){
			vx += ax;
			vy += ay;
		}
		public void display(){
			stroke(hue,100,100);
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
