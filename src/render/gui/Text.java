package render.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import tiling.mesh.Texture;

public class Text {
	private static final String characters =
		"abcdefghijklmnopqrstuvwxyzåäö" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ" +
		"0123456789" +
		"._- *()[]{}?+/\\.,<>'";
	
	private BufferedImage attlas;
	private FontMetrics metrics;
	private Texture texture;
	
	public Text(Font font) {
		//createFontAttlas(font);
	}
	
	public Text(String name) {
		Font tmp = null;
		try {
			tmp = Font.createFont(Font.TRUETYPE_FONT, Text.class.getResourceAsStream(name));
			tmp = tmp.deriveFont(121.0f);
		} catch(Exception e) {
			tmp = new Font("Arial", Font.PLAIN, 20);
			e.printStackTrace();
		}
		
		//createFontAttlas(tmp);
		try {
			texture = Texture.loadLocalTexture("/font.png", GL11.GL_LINEAR);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void createFontAttlas(Font font) {
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		GraphicsConfiguration config = device.getDefaultConfiguration();
		int wi = 1024;
		int he = 1024;
		
		attlas = config.createCompatibleImage(wi, he, Transparency.TRANSLUCENT);
		
		Graphics2D g = attlas.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		metrics = g.getFontMetrics();
		
		Rectangle2D box = metrics.getStringBounds(" ", g);
		
		{
			g.setColor(Color.WHITE);
			
			int max_width = (int)(wi / box.getWidth());
			for(int i = 0; i < characters.length(); i++) {
				char c = characters.charAt(i);
				
				float x = (float)box.getWidth() * (i % max_width);
				float y = (float)(box.getHeight() + 10) * (i / max_width) - (float)box.getY();
				g.drawString("" + c, (int)x, (int)y);
				
				//Rectangle2D rect = metrics.getStringBounds("" + c, g);
				//g.drawRect((int)(x + rect.getX()), (int)(y + rect.getY() - 10), (int)rect.getWidth(), (int)(rect.getHeight() + 10));
			}
		}
		
		try {
			ImageIO.write(attlas, "png", new File("res/font.png"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Rectangle2D box = new Rectangle2D.Double(0, -100.203125, 67.0, 141.67871);
	// [x=0.0,y=-100.203125,w=67.0,h=141.67871]
	public void drawText(String chars, float x, float y, float scale) {
		int max_width = (int)(1028 / box.getWidth());
		scale *= (1 / box.getHeight());
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		texture.bind();
		GL11.glBegin(GL11.GL_TRIANGLES);
		for(int i = 0; i < chars.length(); i++) {
			int index = characters.indexOf(chars.charAt(i));
			
			double xx = (box.getWidth()) * (index % max_width);
			double yy = (box.getHeight() + 10) * (index / max_width);
			
			double x0 = ((int)xx) / 1024.0;
			double y0 = ((int)yy) / 1024.0;
			double x1 = ((int)(xx + box.getWidth())) / 1024.0;
			double y1 = ((int)(yy + box.getHeight())) / 1024.0;
			
			y0 = 1 - y0;
			y1 = 1 - y1;
			
			double vx = scale * box.getWidth() * i + x;
			double vy = y;
			double vw = scale * box.getWidth();
			double vh = scale * box.getHeight();
			
			GL11.glTexCoord2d(x0, y0);
			GL11.glVertex2d(vx     , vy     );
			GL11.glTexCoord2d(x1, y0);
			GL11.glVertex2d(vx + vw, vy     );
			GL11.glTexCoord2d(x1, y1);
			GL11.glVertex2d(vx + vw, vy + vh);
			
			
			GL11.glTexCoord2d(x0, y0);
			GL11.glVertex2d(vx     , vy     );
			GL11.glTexCoord2d(x1, y1);
			GL11.glVertex2d(vx + vw, vy + vh);
			GL11.glTexCoord2d(x0, y1);
			GL11.glVertex2d(vx     , vy + vh);
			
		}
		GL11.glEnd();
		texture.unbind();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}
