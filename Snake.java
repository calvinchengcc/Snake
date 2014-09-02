import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;
import java.text.*;

import javax.imageio.ImageIO;

public class Snake extends Panel implements KeyListener{

	private static final long serialVersionUID = 1L;
	private static final int START_SPEED = 100;
	private BufferedImage imageBuffer;
	private Graphics2D graphicsBuffer;
	private ScheduledExecutorService t;
	private ScheduledFuture<?> future;
	private TimerTask game;
	private Rectangle2D.Double[] wall = new Rectangle2D.Double[4];

	private Image banana, blueberry, orange, strawberry;

	private java.util.List<Segment> s;
	private Power_Up f;

	private boolean moveUp, moveDown, moveRight = true, moveLeft;
	private boolean paused = false;
	private boolean eaten = false;
	private boolean walls = false;

	private int tmp;
	private int maxLength = Integer.MIN_VALUE;
	private int gameRate = START_SPEED;
	private int prevRate = gameRate;

	public Snake() {
		//find images
		banana = getImage("Banana.png");
		blueberry = getImage("Blueberry.png");
		orange = getImage("Orange.png");
		strawberry = getImage("Strawberry.png");
		
		setPreferredSize(new Dimension(1401, 701));
		setSize(1401, 701);
	}
	
	public void init() {

		//double buffering
		imageBuffer = (BufferedImage)createImage(getWidth(), getHeight());
		graphicsBuffer = (Graphics2D) imageBuffer.getGraphics();

		//initiate 2 snake segments to start, all in the center
		s = new LinkedList<Segment>();

		for(int i=0; i<5; i++){
			s.add(new Segment(80-20*i, 20, 20, 20));
		}

		//initiate Power_Up
		f = new Power_Up((int)(Math.random()*getWidth()-13), (int)(Math.random()*getHeight()-15), 20, 20, strawberry);

		//set walls
		wall[0] = new Rectangle2D.Double(0, 0, getWidth(), 20);
		wall[1] = new Rectangle2D.Double(0, 0, 20, getHeight());
		wall[2] = new Rectangle2D.Double(getWidth()-20, 0, 20, getHeight());
		wall[3] = new Rectangle2D.Double(0, getHeight()-20, getWidth(), 20);

		/********************************************************************************************************************************************/

		addKeyListener(this);
		setFocusable(true);

		game = new TimerTask(){
			public void run(){
				if (prevRate != gameRate) {
					future.cancel(false);
					future = t.scheduleAtFixedRate(game, 0, gameRate, TimeUnit.MILLISECONDS);
				}
				prevRate = gameRate;
				gameLoop();
			}
		};

		//this delay controls the speed of all the movement
		t = Executors.newScheduledThreadPool(1);
		future = t.scheduleAtFixedRate(game, 100, gameRate, TimeUnit.MILLISECONDS);
	}

	private void gameLoop() {
		
		Graphics g = getGraphics();

		//call everything to move
		for(int i=0; i<s.size(); i++){
			s.get(i).move();
		}

		f.move();

		//to make the snake move, cut off the end (index: s.size()-1) by 1, and add one piece to the beginning (index: 0)
		if(moveUp){
			s.remove(s.size()-1);
			s.add(0, new Segment(s.get(0).getX(), s.get(0).getY()-s.get(0).getHeight(), s.get(0).getWidth(), s.get(0).getHeight()));
		}
		if(moveDown){
			s.remove(s.size()-1);
			s.add(0, new Segment(s.get(0).getX(), s.get(0).getY()+s.get(0).getHeight(), s.get(0).getWidth(), s.get(0).getHeight()));
		}
		if(moveLeft){
			s.remove(s.size()-1);
			s.add(0, new Segment(s.get(0).getX()-s.get(0).getWidth(), s.get(0).getY(), s.get(0).getWidth(), s.get(0).getHeight()));
		}
		if(moveRight){
			s.remove(s.size()-1);
			s.add(0, new Segment(s.get(0).getX()+s.get(0).getWidth(), s.get(0).getY(), s.get(0).getWidth(), s.get(0).getHeight()));
		}

		//if the snake has run into a wall, start from other side
		if(s.get(0).getX()<0){																//left wall
			s.get(0).setX(getWidth()-s.get(0).getWidth());
		}else if(s.get(0).getX()+s.get(0).getWidth()>getWidth()){							//right wall
			s.get(0).setX(0);
		}else if(s.get(0).getY()<0){														//top wall
			s.get(0).setY(getHeight()-s.get(0).getHeight());
		}else if(s.get(0).getY()+s.get(0).getHeight()>getHeight()){							//bottom wall
			s.get(0).setY(0);
		}

		//if walls are turned on, kill the snake if it hits
		if(walls){
			for(int i=0; i<wall.length; i++){
				if(s.get(0).intersects(wall[i])){
					s.get(0).setSize(0, 0);
				}
			}
		}

		//if snake has eaten a piece of food, make it grow longer
		if(s.get(0).intersects(f.getRectangle2D())){
			eaten = true;

			s.add(new Segment(s.get(s.size()-1).getX(), s.get(s.size()-1).getY(), s.get(s.size()-1).getWidth(), s.get(s.size()-1).getHeight()));
			gameRate -= gameRate / 50;
			
			if(s.size()==25){
				walls = true;
			}
		}

		//if the food is eaten, put it somewhere else, randomly
		if(eaten){

			tmp = (int)(Math.random()*4+1);

			switch(tmp){
				case 1:
					f = new Power_Up((int)(Math.random()*(getWidth()-100)+51), (int)(Math.random()*(getHeight()-100)+51), 20, 20, banana);
					break;
				case 2:
					f = new Power_Up((int)(Math.random()*(getWidth()-100)+51), (int)(Math.random()*(getHeight()-100)+51), 20, 20, blueberry);
					break;
				case 3:
					f = new Power_Up((int)(Math.random()*(getWidth()-100)+51), (int)(Math.random()*(getHeight()-100)+51), 20, 20, orange);
					break;
				default:
					f = new Power_Up((int)(Math.random()*(getWidth()-100)+51), (int)(Math.random()*(getHeight()-100)+51), 20, 20, strawberry);
					break;
			}

			eaten = false;
		}

		//if the snake hits itself, cut it off at that point
		for(int i=1; i<s.size(); i++){
			if(s.get(0).intersects(s.get(i).getRectangle2D())){
				for(int j = i; j<s.size(); j++){
					s.remove(i);
				}
			}
		}


		//set high scores
		if(s.size()>maxLength){
			maxLength = s.size();
		}
		
		paint(g);
	}
	
	public void keyPressed(KeyEvent e){

		if(e.getKeyCode() == KeyEvent.VK_UP && !moveDown  && s.get(1).getY()!=s.get(0).getY()-s.get(0).getHeight()){
			moveUp = true;
			moveDown = false;
			moveRight = false;
			moveLeft = false;
		}

		if(e.getKeyCode() == KeyEvent.VK_DOWN && !moveUp  && s.get(1).getY()!=s.get(0).getY()+s.get(0).getHeight()){
			moveUp = false;
			moveDown = true;
			moveRight = false;
			moveLeft = false;
		}

		if(e.getKeyCode() == KeyEvent.VK_RIGHT && !moveLeft && !moveRight && s.get(1).getX()!=s.get(0).getX()+s.get(0).getWidth()){
			moveUp = false;
			moveDown = false;
			moveRight = true;
			moveLeft = false;
		}

		if(e.getKeyCode() == KeyEvent.VK_LEFT && !moveRight && s.get(1).getX()!=s.get(0).getX()-s.get(0).getWidth()){
			moveUp = false;
			moveDown = false;
			moveRight = false;
			moveLeft = true;
		}

		//pause
		if(e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_Q){
			if(!paused){
				future.cancel(false);
				paused = true;
			}else{
				future = t.scheduleAtFixedRate(game, 0, gameRate, TimeUnit.MILLISECONDS);
				paused = false;
			}
		}
	}

	public void keyReleased(KeyEvent e){

		//toggle walls on/off
		if(e.getKeyCode() == KeyEvent.VK_W){
			walls = !walls;
		}

		//revive
		if(e.getKeyCode() == KeyEvent.VK_R){
			for(int i=0; i<s.size(); i++){
				s.get(i).setSize(20, 20);
			}
		}

		//easter egg... super snake growth
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			for(int i=0; i<5; i++){
				s.add(new Segment(s.get(s.size()-1).getX(), s.get(s.size()-1).getY(), s.get(s.size()-1).getWidth(), s.get(s.size()-1).getHeight()));
			}
		}

		//another easter egg... super speed
		if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET){
			gameRate += gameRate / 50;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
			gameRate -= gameRate / 50;
		}
	}

	public void keyTyped(KeyEvent e){}

	public void paint (Graphics g){

		Graphics2D g2 = (Graphics2D) g;

		graphicsBuffer.setColor(Color.black);
		graphicsBuffer.fillRect(0, 0, getWidth(), getHeight());

		//paint snake
		for(int i=0; i<s.size(); i++){
			s.get(i).paint(graphicsBuffer);
		}

		graphicsBuffer.draw(wall[0]);

		//paint Power_Up
		graphicsBuffer.setColor(new Color(1.0f, 1.0f, 1.0f, 0f));
		f.paint(graphicsBuffer, this);

		if(walls){
			for(int i = 0; i<wall.length; i++){
				graphicsBuffer.setColor(Color.gray);
				graphicsBuffer.fill(wall[i]);
			}
		}

		//display score and snake length
		graphicsBuffer.setColor(Color.yellow);
		
		graphicsBuffer.drawString("Longest snake length: " + maxLength, 5, getHeight()-5);
		
		String currLength = "Current snake length: " + s.size();
		int stringLength = (int)g2.getFontMetrics().getStringBounds(currLength, g2).getWidth();
		graphicsBuffer.drawString(currLength, getWidth()- stringLength - 10, getHeight()-5);
		
		String speed = new DecimalFormat("##0.00").format(1.0 * START_SPEED / gameRate).toString();
		graphicsBuffer.drawString("Snake speed: " + speed + "x", 5, 15);
		
		String instructions = "Arrow keys to move"
				+ "  |  '[' and ']' to adjust speed"
				+ "  |  'r' to revive"
				+ "  |  'w' to toggle walls"
				+ "  |  'p' to pause"
				+ "  |  'Enter' to cheat";
		stringLength = (int)g2.getFontMetrics().getStringBounds(instructions, g2).getWidth();
		graphicsBuffer.drawString(instructions, getWidth() - stringLength - 10, 15);

		//draw everything from buffer
		g2.drawImage(imageBuffer, 0,0, getWidth(), getHeight(), this);
	}
	
	public Image getIconImage() {
		return strawberry;
	}
	
	private Image getImage(String name) {
		try {
			return ImageIO.read(getClass().getClassLoader().getResource(name));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}