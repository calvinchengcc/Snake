import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

class Segment{

	private int xpos, ypos, width, height;
	private Rectangle2D.Double r;

	public Segment(int xpos, int ypos, int width, int height){

		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;

		r = new Rectangle2D.Double(xpos, ypos, width, height);
	}

	public void move(){

		r.setFrame(xpos, ypos, width, height);
	}

	public boolean intersects(Rectangle2D.Double rectangle){

		return r.intersects(rectangle);
	}

	public boolean intersects(int xpos, int ypos, int width, int height){

		return r.intersects(xpos, ypos, width, height);
	}

	public void setPos(int xpos, int ypos){

		this.xpos = xpos;
		this.ypos = ypos;
	}

	public void setX(int xpos){

		this.xpos = xpos;
	}

	public void setY(int ypos){

		this.ypos = ypos;
	}

	public void setSize(int width, int height){

		this.width = width;
		this.height = height;
	}

	public void setWidth(int width){

		this.width = width;
	}

	public void setHeight(int height){

		this.height = height;
	}

	public int getX() { return xpos; }
	public int getY() {return ypos; }
	public int getWidth() { return width; }
	public int getHeight() { return height; }
	public Rectangle2D.Double getRectangle2D() { return r; }

	//white skin, green body
	public void paint(Graphics2D g){

		g.setColor(Color.green);
		g.fill(r);

		g.setColor(Color.black);
		g.draw(r);
	}
}