package game;

import java.awt.event.KeyEvent;

import java.awt.event.KeyListener;

import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.image.BufferedImage;

import java.util.*;

@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable, KeyListener {
	public static final int WIDTH = 400;
	public static final int HEIGHT = 400;
	//Render
	private Graphics2D g2d;
	private BufferedImage image;
	
	
	//Game Loop
	private Thread thread;
	private boolean running;
	private long targetTime;
	
	//Game THings
	private Entity head, food;
	private ArrayList<Entity> snke;
	private final int SIZE = 10;
	private int score;
	private int level;
	private double special;
	private boolean isSpecial;
	private boolean gameOver, beatHighScore;
	private int highScore = -1; 
	private int moveCount;
	
	
	
	//movement
	private int dx, dy;
	
	//input
	private boolean up, down, right, left, start;
	
	public GamePanel(){
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
		addKeyListener(this);
		
	}
	@Override 
	//find out what this does
	public void addNotify(){
		super.addNotify();
		thread = new Thread(this);
		thread.start();
	}
	private void setFPS(int fps){
		targetTime = 1000 / fps;
	}
	@Override
	public void keyPressed(KeyEvent e) {
		int k = e.getKeyCode();
		if(k==KeyEvent.VK_UP) up = true;
		if(k==KeyEvent.VK_DOWN )down = true;
		if(k==KeyEvent.VK_RIGHT) right = true;
		if(k==KeyEvent.VK_LEFT) left = true;
		if(k==KeyEvent.VK_SPACE) start = true;

	}

	@Override
	public void keyReleased(KeyEvent e) {
		int k = e.getKeyCode();
		if(k==KeyEvent.VK_UP) up = false;
		if(k==KeyEvent.VK_DOWN )down = false;
		if(k==KeyEvent.VK_RIGHT) right = false;
		if(k==KeyEvent.VK_LEFT) left = false;
		if(k==KeyEvent.VK_SPACE) start = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void run() {
		if(running) return;
		init();
		long startTime;
		long elapsed;
		long wait;
		while(running){
			startTime = System.nanoTime();
			update();
			requestRender();
			elapsed = System.nanoTime() - startTime;
			wait = targetTime - elapsed / 1000000;
			if(wait > 0){
				try{
					Thread.sleep(wait);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	private void init(){
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();
		running = true;
		setUplevel();
		
		
	}
	private void setUplevel(){
		snke = new ArrayList<Entity>();
		head = new Entity(SIZE);
		//set it in middle
		head.setPosition(WIDTH/2, HEIGHT/2);
		snke.add(head);
		
		for(int i = 1; i < 10;i++){
			Entity e = new Entity(SIZE);
			//add a new entity at same Y as head and 
			//to the right of the head 
			e.setPosition(head.getX() + i*SIZE,head.getY() );
			snke.add(e);
			
		}
		food = new Entity(SIZE);
		setFood();
		score = 0;
		moveCount = 0;
		gameOver = false;
		beatHighScore = false;
		level = 1;
		dx = dy = 0;
		setFPS(5+level*5);
	}
	public void setFood(){
		special = Math.random();
		if(special <= 0.05)isSpecial = true;
		int x =(int) (Math.random()*(WIDTH - SIZE*2)) + SIZE;
		int y =(int) (Math.random()*(HEIGHT - SIZE*2)) + SIZE;
		
		//because SIZE == 10, this ensures the food
		//is at a place where the head can reach it,
		//so at a multiple of 10
		y -= y%SIZE;
		x -= x&SIZE;
		
		food.setPosition(x,y);
	}
	
	private void update(){
		if(gameOver){
			if(start){
				setUplevel();
			}
			return;
		}
		if(up && dy == 0){
			dy = -SIZE;
			dx = 0;
		}
		if(down && dy == 0){
			dy = SIZE;
			dx = 0;
		}
		if(left && dx == 0){
			dy = 0;
			dx = -SIZE;
		}
		if(right && dx == 0 && dy != 0){
			dy = 0;
			dx = SIZE;
		}
		if(dx!= 0 || dy!= 0){
			//change the position of all entities to the one of the one
			//behind it
			for(int i = snke.size() - 1; i > 0; i--){
				snke.get(i).setPosition(snke.get(i-1).getX(), snke.get(i-1).getY());
			}
			head.move(dx, dy);
			moveCount++;
		}
		for(Entity e : snke){
		
			if(e.isCollision(head)){
				gameOver = true;
				break;
			}
		}
		
		if(food.isCollision(head)){
			if(isSpecial){
				score += 10;
				isSpecial = false;
			}
			else{
				score++;
				moveCount = 0;
			}
			if(score > highScore){
				highScore = score;
				beatHighScore = true;
			}
			setFood();
			Entity e = new Entity(SIZE);
			//initially sets it offscreen
			e.setPosition(-100, -100);
			
			//when you add it to the list
			//it gets put in the right psoition
			snke.add(e);
			if(score % 10 == 0){
				
				//gets faster
				level++;
				if(level>10)level = 10;
				setFPS(level*10);
				
			}
		}
		
		// dies if you hit bounds
		//can also modify it so you go through
		if(head.getX() < 0 || head.getX() > WIDTH)gameOver = true;
		if(head.getY() < 0 || head.getY() > HEIGHT)gameOver = true;
	}
	private void requestRender(){
		render(g2d);
		Graphics g = getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
	}
	public void render(Graphics2D g2d){
		g2d.clearRect(0, 0, WIDTH, HEIGHT);
		g2d.setColor(Color.YELLOW);
		for(Entity e: snke){
			e.render(g2d);;
		}
		if(isSpecial){
			if(moveCount%3 ==0)g2d.setColor(Color.WHITE);
			else g2d.setColor(Color.GREEN);
			food.render(g2d);
			/**
			while(!food.isCollision(head)){
				g2d.setColor(Color.GREEN);
				food.render(g2d);
				g2d.setColor(Color.WHITE);
				food.render(g2d);
				
			}
			*/
		}else{
			g2d.setColor(Color.BLUE);
			food.render(g2d);
		}
		
		
		g2d.setColor(Color.WHITE);
		if(gameOver){
			g2d.drawString("GAME OVER", WIDTH/2-WIDTH/50-15, HEIGHT/2);
			g2d.drawString("PRESS SPACE TO PLAY",WIDTH/2-WIDTH/50-50 , HEIGHT/2+20);
		}
		if(dx == 0 && dy ==0){
			g2d.drawString("Ready?", WIDTH/2 - (int)WIDTH/200, HEIGHT/2);
		}
		g2d.drawString("Score : " + score + "    Level: " + level, 10, 10);
		if(score != 0 && score%10 ==0){
			if(moveCount < level*20)
			g2d.drawString("Level up!", WIDTH/2 - WIDTH/50, HEIGHT/2);
		}
		g2d.drawString("Highscore: ",WIDTH - 100, 10);
		if(highScore >0){
			if(beatHighScore)g2d.setColor(Color.RED);
			else g2d.setColor(Color.WHITE);
			g2d.drawString(highScore +"",WIDTH - 30, 10);
		}
		if(gameOver && beatHighScore){
			g2d.setColor(Color.RED);
			g2d.drawString("CONGRATS YOU BEAT THE HIGHSCORE", WIDTH/2 -100, HEIGHT - 100);
		}
	}

}
