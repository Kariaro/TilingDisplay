package render.gui;


import java.awt.Window.Type;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import render.generate.TilingLoaderNew;
import render.generate.TilingPattern;
import render.main.Input;
import render.main.Mouse;
import render.tiling.Tiling;

public class Gui {
	private final Tiling parent;
	private final long window;
	public int height;
	public int width;
	private Text text;
	
	public Gui(Tiling parent, long window) {
		this.window = window;
		this.parent = parent;
		
		int[] width = new int[1];
		int[] height = new int[1];
		GLFW.glfwGetWindowSize(this.window, width, height);
		this.height = height[0];
		this.width = width[0];
		
		text = new Text("/Consolas.ttf");
	}
	
	public void drawBox(float x, float y, float w, float h) {
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glVertex2f(x    , y    );
			GL11.glVertex2f(x + w, y    );
			GL11.glVertex2f(x + w, y + h);
			GL11.glVertex2f(x    , y + h);
		GL11.glEnd();
	}
	
	
	public boolean drawToggle(String text, boolean toggled, boolean small, float size, float x, float y, float tx, float ty, Vector4f bg_normal, Vector4f bg_hover, Vector4f fg_normal, Vector4f fg_hover) {
		return drawToggle(text, text, toggled, small, size, x, y, tx, ty, bg_normal, bg_hover, bg_hover, fg_normal, fg_hover, fg_hover);
	}
	
	public boolean drawToggle(String text, boolean toggled, boolean small, float size, float x, float y, float tx, float ty, Vector4f bg_normal, Vector4f bg_hover, Vector4f bg_selected, Vector4f fg_normal, Vector4f fg_hover, Vector4f fg_selected) {
		return drawToggle(text, text, toggled, small, size, x, y, tx, ty, bg_normal, bg_hover, bg_selected, fg_normal, fg_hover, fg_selected);
	}
	
	public boolean drawToggle(String text, String selected, boolean toggled, boolean small, float size, float x, float y, float tx, float ty, Vector4f bg_normal, Vector4f bg_hover, Vector4f fg_normal, Vector4f fg_hover) {
		return drawToggle(text, selected, toggled, small, size, x, y, tx, ty, bg_normal, bg_hover, bg_hover, fg_normal, fg_hover, fg_hover);
	}
	
	public boolean drawToggle(String normal, String selected, boolean toggled, boolean small, float size, float x, float y, float tx, float ty, Vector4f bg_normal, Vector4f bg_hover, Vector4f bg_selected, Vector4f fg_normal, Vector4f fg_hover, Vector4f fg_selected) {
		float width = (float)(Text.box.getWidth() / Text.box.getHeight());
		float height = size;
		String text = (toggled ? selected:normal);
		if(small) {
			width *= size * text.length();
		} else {
			width = this.width - x;
		}
		
		width += tx * 2;
		height += ty * 2;
		
		boolean inside = Mouse.inside(x, y, width, height);
		boolean pressed = false;
		
		if(inside && Mouse.buttons[0]) {
			Mouse.buttons[0] = false;
			pressed = true;
			toggled = !toggled;
		}
		
		if(toggled) {
			GL11.glColor4f(bg_selected.x, bg_selected.y, bg_selected.z, bg_selected.w);
		} else {
			if(inside) {
				GL11.glColor4f(bg_hover.x, bg_hover.y, bg_hover.z, bg_hover.w);
			} else {
				GL11.glColor4f(bg_normal.x, bg_normal.y, bg_normal.z, bg_normal.w);
			}
		}
		drawBox(x, y, width, height);
		
		if(toggled) {
			GL11.glColor4f(fg_selected.x, fg_selected.y, fg_selected.z, fg_selected.w);
		} else {
			if(inside) {
				GL11.glColor4f(fg_hover.x, fg_hover.y, fg_hover.z, fg_hover.w);
			} else {
				GL11.glColor4f(fg_normal.x, fg_normal.y, fg_normal.z, fg_normal.w);
			}
		}
		Gui.this.text.drawText(text, x + tx, y + ty, size);
		
		return pressed;
	}
	
	private boolean pressing_up;
	private boolean pressing_down;
	
	private boolean pressing;
	private boolean show = true;
	public void render() {
		if(Input.keys[GLFW.GLFW_KEY_M]) {
			if(!pressing) {
				pressing = true;
				show = !show;
			}
		} else pressing = false;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		if(show) {
			int total_patterns = parent.patterns.size();
			int pattern_index = parent.getPatternIndex();
			
			if(pattern_index != -1) {
				if(Input.keys[GLFW.GLFW_KEY_UP]) {
					if(!pressing_up) {
						pressing_up = true;
						if(pattern_index > 0) {
							pattern_index--;
							parent.loadPattern(pattern_index);
						}
					}
				} else pressing_up = false;
				
				if(Input.keys[GLFW.GLFW_KEY_DOWN]) {
					if(!pressing_down) {
						pressing_down = true;
						if(pattern_index < total_patterns - 1) {
							pattern_index++;
							parent.loadPattern(pattern_index);
						}
					}
				} else pressing_down = false;
			}
			
			GL11.glColor4f(0, 0, 0, 1);
			text.drawText("Author Victor Axberg", 2, height - 26, 28);
			
			int a = 400;
			GL11.glColor4f(0, 0, 0, 0.5f);
			drawBox(width - a, 0, a, height);
			
			GL11.glColor4f(1, 1, 1, 1);
			text.drawText("Default Tilings", width - a + 4, 0, 32);
			text.drawText("fps " + parent.fps + "/" + parent.TARGET_FPS, width - 130, height - 24, 24);
			
			{
				text.drawText("zoom " + parent.getZoom(), width - a + 4, height - 48, 24);
				
				if(drawToggle("[Debug On]", "[Debug Off]", Tiling.DEBUG, true, 24, width - a, height - 24, 4, 0,
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(1, 1, 1, 0.1f),
					new Vector4f(0, 0, 0, 0.4f),
					new Vector4f(1, 1, 1, 0.4f),
					new Vector4f(1, 1, 1, 0.4f),
					new Vector4f(0.3f, 0.7f, 0.3f, 1)
					)) {
					Tiling.DEBUG = !Tiling.DEBUG;
				}
			}
			
			for(int i = 0; i < parent.patterns.size(); i++) {
				if(pattern_index != i) {
					if(Mouse.buttons[0] && Mouse.inside(width - a, 32 + i * 32, a, 32)) {
						Mouse.buttons[0] = false;
						parent.loadPattern(i);
					}
				}
			}
			
			for(int i = 0; i < parent.patterns.size(); i++) {
				if(pattern_index == i) {
					GL11.glColor4f(1, 1, 1, 0.3f);
				} else {
					GL11.glColor4f(0, 0, 0, 0.3f);
					
					if(Mouse.inside(width - a, 32 + i * 32, a, 32)) {
						GL11.glColor4f(1, 1, 1, 0.1f);
					}
				}
				
				drawBox(width - a, 32 + i * 32, a, 32);
				
				
				GL11.glColor4f(1, 1, 1, 1.0f);
				TilingPattern pattern = parent.patterns.get(i);
				if(pattern_index == i) {
					text.drawText("> " + pattern.getName() + " <", width - a + 16, 32 + 4 + i * 32, 24);
				} else {
					text.drawText(pattern.getName(), width - a + 16, 32 + 4 + i * 32, 24);
				}
			}
			
			if(drawToggle("[Open Custom Tilings Folder]", false, false, 24, width - a, 300, 4, 0,
				new Vector4f(0, 0, 0, 0.3f),
				new Vector4f(1, 1, 1, 0.1f),
				new Vector4f(1, 1, 1, 1),
				new Vector4f(1, 1, 1, 1)
				)) {
				openPattern();
			}
			
			if(selectedUpdate) {
				selectedUpdate = false;
				updateFileSelection();
			}
			
			if(selectedFile != null) {
				String name = (selectedTiling == null) ? selectedFile.getName():selectedTiling.getName();
				
				if(drawToggle(name, "> " + name + " <", pattern_index == -1, false, 24, width - a, 332, 16, 4,
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(1, 1, 1, 0.3f),
					new Vector4f(1, 1, 1, 1),
					new Vector4f(1, 1, 1, 1)
					)) {
					parent.setPatternIndex(-1);
				}
				
				if(drawToggle("[Reload]", false, true, 24, width - a, 372, 4, 4,
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(1, 1, 1, 0.1f),
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(1, 1, 1, 1),
					new Vector4f(0.3f, 0.7f, 0.3f, 1),
					new Vector4f(1, 1, 1, 1)
					)) {
					updateFileSelection();
				}
			}
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA);
	}
	
	private void updateFileSelection() {
		TilingPattern pattern = TilingLoaderNew.loadGlobalPattern(selectedFile.getAbsolutePath());
		
		if(pattern == null) {
			// Failed to load file
			selectedTiling = null;
		} else {
			selectedTiling = pattern;
			parent.customTiling = selectedTiling;
		}
		
		parent.setPatternIndex(-1);
	}
	
	private TilingPattern selectedTiling = null;
	private boolean selectedUpdate = false;
	private File selectedFile = null;
	
	private volatile boolean openDialog = false;
	private FileFilter TILING_FILTER = new FileFilter() {
		public String getDescription() { return "Tiling Files"; }
		public boolean accept(File f) {
			String name = f.getName().toLowerCase();
			return f.isDirectory() || name.endsWith(".example") || name.endsWith(".tiling");
		}
	};
	
	private void openPattern() {
		if(openDialog) return;
		openDialog = true;
		/*
		Thread thread = new Thread(() -> {
			File file = GuiChooser.showOpenDialog();
			System.out.println("File: " + file);
			openDialog = false;
		});
		thread.setDaemon(true);
		thread.start();*/
		
		SwingUtilities.invokeLater(() -> {
			final JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(Tiling.customTilingFolder);
			fc.addChoosableFileFilter(TILING_FILTER);
			fc.setFileFilter(TILING_FILTER);
			
			JFrame frame = new JFrame();
			frame.setType(Type.UTILITY);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(0, 0);
			frame.setLocation(-1000, -1000);
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);
			
			int returnVal = fc.showOpenDialog(frame);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				this.selectedFile = fc.getSelectedFile();
				this.selectedUpdate = true;
			}
			
			frame.setVisible(false);
			frame.dispose();
			
			openDialog = false;
		});
	}
}
