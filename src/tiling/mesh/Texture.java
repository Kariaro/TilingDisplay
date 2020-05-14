package tiling.mesh;


import static org.lwjgl.opengl.GL11.*;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import render.main.Tiling;

public class Texture {
	public BufferedImage bi;
	public int textureId;
	
	public final int height;
	public final int width;
	
	private static URL getURL(String str) {
		try {
			return new URL(str);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Texture(String path) {
		this(getURL(path));
	}
	
	public Texture(InputStream stream) {
		this(stream, true);
	}
	
	public Texture(InputStream stream, boolean nearest) {
		try {
			bi = ImageIO.read(stream);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		height = bi.getHeight();
		width = bi.getWidth();
		
		
		boolean alpha = bi.getTransparency() == Transparency.TRANSLUCENT;
		
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
		int[] pixels = new int[width * height];
		bi.getRGB(0, 0, width, height, pixels, 0, width);
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int pos = (height - i - 1) * width + (j);
				
				int pixel = pixels[pos];
				// int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >>  8) & 0xff;
				int b = (pixel      ) & 0xff;
				
				buf.put((byte)r);
				buf.put((byte)g);
				buf.put((byte)b);
				if(alpha) {
					buf.put((byte)((pixel >> 24) & 0xff));
				} else {
					buf.put((byte)0x7f);
				}
			}
		}
		buf.flip();
		
		textureId = GL11.glGenTextures();
		GL11.glBindTexture(GL_TEXTURE_2D, textureId);
		GL11.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		
		if(nearest) {
			GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		} else {
			GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		}
		
		GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height,
				0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

		
		
	}
	
	public Texture(URL url) {
		try {
			bi = ImageIO.read(url);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		height = bi.getHeight();
		width = bi.getWidth();
		
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
		int[] pixels = new int[width * height];
		bi.getRGB(0, 0, width, height, pixels, 0, width);
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int pos = (height - i - 1) * width + (j);
				
				int pixel = pixels[pos];
				// int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >>  8) & 0xff;
				int b = (pixel      ) & 0xff;
				
				buf.put((byte)r);
				buf.put((byte)g);
				buf.put((byte)b);
				buf.put((byte)0x7f);
			}
		}
		buf.flip();
		
		textureId = GL11.glGenTextures();
		GL11.glBindTexture(GL_TEXTURE_2D, textureId);
		GL11.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height,
				0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}
	
	public Texture(BufferedImage bi) {
		height = bi.getHeight();
		width = bi.getWidth();
		
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
		int[] pixels = new int[width * height];
		bi.getRGB(0, 0, width, height, pixels, 0, width);
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int pos = (height - i - 1) * width + (j);
				
				int pixel = pixels[pos];
				// int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >>  8) & 0xff;
				int b = (pixel      ) & 0xff;
				
				buf.put((byte)r);
				buf.put((byte)g);
				buf.put((byte)b);
				buf.put((byte)0x7f);
			}
		}
		buf.flip();
		
		textureId = GL11.glGenTextures();
		GL11.glBindTexture(GL_TEXTURE_2D, textureId);
		GL11.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height,
				0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}
	
	public void bind() {
		GL11.glBindTexture(GL_TEXTURE_2D, textureId);
	}
	
	public void unbind() {
		GL11.glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void cleanup() {
		glDeleteTextures(textureId);
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("Texture[id=").append(textureId).append("] (").append(width).append("x").append(height).append(")").toString();
	}
	
	public static BufferedImage loadBufferedImage(String path) {
		try {
			return ImageIO.read(Texture.class.getResourceAsStream(path));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ByteBuffer loadBufferLocal(String path) {
		try {
			return loadBuffer(ImageIO.read(Tiling.class.getResourceAsStream(path)));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ByteBuffer loadBuffer(BufferedImage bi) {
		if(bi == null) return null;
		
		int height = bi.getHeight();
		int width = bi.getWidth();
		
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * width * height);
		int[] pixels = new int[width * height];
		bi.getRGB(0, 0, width, height, pixels, 0, width);
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int pos = i * width + j;
				
				int pixel = pixels[pos];
				int a = (pixel >> 24) & 0xff;
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >>  8) & 0xff;
				int b = (pixel      ) & 0xff;
				
				buf.put((byte)r);
				buf.put((byte)g);
				buf.put((byte)b);
				buf.put((byte)a);
			}
		}
		buf.flip();
		
		return buf;
	}
}