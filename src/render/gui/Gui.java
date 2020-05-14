package render.gui;

import java.io.File;
import java.util.logging.Logger;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import render.main.Input;
import render.main.Mouse;
import render.main.Tiling;
import render.main.TilingRender;
import tiling.parser.TilingLoader;
import tiling.parser.TilingPattern;
import tiling.util.TilingUtil;

public class Gui {
	private static final Logger LOGGER = Logger.getLogger("TilingGui");
	
	private final TilingRender parent;
	public int height;
	public int width;
	
	private TilingPattern selectedTiling;
	private GuiFileChooser fileChooser;
	private Text text;
	
	public Gui(TilingRender parent) {
		this.parent = parent;
		text = new Text("/Consolas.ttf");
		
		fileChooser = new GuiFileChooser();
	}
	
	public void drawBox(float x, float y, float w, float h) {
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
			GL11.glVertex2f(x    , y    );
			GL11.glVertex2f(x + w, y    );
			GL11.glVertex2f(x + w, y + h);
			GL11.glVertex2f(x    , y + h);
		GL11.glEnd();
	}
	
	
	private boolean drawToggle(String text, boolean toggled, boolean small, float size, float x, float y, float tx, float ty, Vector4f bg_normal, Vector4f bg_hover, Vector4f fg_normal, Vector4f fg_hover) {
		return drawToggle(text, text, toggled, small, size, x, y, tx, ty, bg_normal, bg_hover, bg_hover, fg_normal, fg_hover, fg_hover);
	}
	
	private boolean drawToggle(String text, boolean toggled, boolean small, float size, float x, float y, float tx, float ty, Vector4f bg_normal, Vector4f bg_hover, Vector4f bg_selected, Vector4f fg_normal, Vector4f fg_hover, Vector4f fg_selected) {
		return drawToggle(text, text, toggled, small, size, x, y, tx, ty, bg_normal, bg_hover, bg_selected, fg_normal, fg_hover, fg_selected);
	}
	
	private boolean drawToggle(String text, String selected, boolean toggled, boolean small, float size, float x, float y, float tx, float ty, Vector4f bg_normal, Vector4f bg_hover, Vector4f fg_normal, Vector4f fg_hover) {
		return drawToggle(text, selected, toggled, small, size, x, y, tx, ty, bg_normal, bg_hover, bg_hover, fg_normal, fg_hover, fg_hover);
	}
	
	private boolean drawToggle(String normal, String selected, boolean toggled, boolean small, float size, float x, float y, float tx, float ty, Vector4f bg_normal, Vector4f bg_hover, Vector4f bg_selected, Vector4f fg_normal, Vector4f fg_hover, Vector4f fg_selected) {
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
	
	private boolean autoReload;
	private boolean pressing_up;
	private boolean pressing_down;
	
	private boolean pressing;
	private boolean show = true;
	
	private boolean flip = false;
	private long smooth_time = 0;
	private float smooth_value = 400;
	
	public void render() {
		TilingUtil.setDebugLevel(LOGGER);
		
		if(Input.keys[GLFW.GLFW_KEY_M]) {
			long now = System.currentTimeMillis() - smooth_time;
			if(!pressing && (!flip || now > 200)) {
				pressing = true;
				
				smooth_time = System.currentTimeMillis();
				flip = true;
				show = !show;
			}
		} else pressing = false;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		int pattern_index = parent.getPatternIndex();
		
		boolean changing = false;
		{
			long now = System.currentTimeMillis() - smooth_time;
			
			if(flip) {
				long nnow = 400 - now;
				float index = nnow / 400.0f;
				
				if(show) {
					changing = true;
					
					if(now > 400) {
						smooth_value = 400;
						flip = false;
					} else {
						smooth_value = 400 - (index * index) * nnow;
					}
				} else {
					changing = true;
					
					if(now > 400) {
						smooth_value = 0;
						flip = false;
					} else {
						smooth_value = (index * index) * nnow;
					}
				}
			}
		}
		
		if(show || changing) {
			float a = smooth_value;
			int total_patterns = parent.patterns.size();
			
			if(pattern_index != -1) {
				if(Input.keys[GLFW.GLFW_KEY_UP]) {
					if(!pressing_up) {
						pressing_up = true;
						if(pattern_index > 0) {
							pattern_index--;
							parent.setPatternIndex(pattern_index);
						}
					}
				} else pressing_up = false;
				
				if(Input.keys[GLFW.GLFW_KEY_DOWN]) {
					if(!pressing_down) {
						pressing_down = true;
						if(pattern_index < total_patterns - 1) {
							pattern_index++;
							parent.setPatternIndex(pattern_index);
						}
					}
				} else pressing_down = false;
			}
			
			{
				for(int i = 0; i < 9; i++) {
					int ix = ((i % 3) - 1) * 1;
					int iy = ((i / 3) - 1) * 1;
					
					if(ix == 0 && iy == 0) continue;
					GL11.glColor4f(0, 0, 0, 1);
					text.drawText("Author " + Tiling.AUTHOR, ix + 2, height + 2 - (a * 28 / 400.0f) + iy, 28);
				}
	
				GL11.glColor4f(1, 1, 1, 1);
				text.drawText("Author " + Tiling.AUTHOR, 2, height + 2 - (a * 28 / 400.0f), 28);
			}
			
			GL11.glColor4f(0, 0, 0, 0.5f);
			drawBox(width - a, 0, a, height);
			
			GL11.glColor4f(1, 1, 1, 1);
			text.drawText("Default Tilings", width - a + 4, 0, 32);
			text.drawText("fps " + parent.getFps() + "/" + Tiling.TARGET_FPS, width - a + 270, height - 24, 24);
			
			{
				text.drawText("zoom " + parent.getZoom(), width - a + 4, height - 48, 24);
				
				if(drawToggle("[Debug Off]", "[Debug On]", Tiling.DEBUG, true, 24, width - a, height - 24, 4, 0,
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(1, 1, 1, 0.1f),
					new Vector4f(0, 0, 0, 0.4f),
					new Vector4f(1, 1, 1, 0.4f),
					new Vector4f(1, 1, 1, 0.4f),
					new Vector4f(0.3f, 0.7f, 0.3f, 1)
					)) {
					Tiling.DEBUG = !Tiling.DEBUG;
				}
				
				String[] levels = new String[] {
					"Minimal",
					"Partial",
					"Greater",
					"Maximum"
				};
				
				if(drawToggle("[" + levels[Tiling.DEBUG_LEVEL] + "]", Tiling.DEBUG, true, 24, width - a + 147, height - 24, 4, 0,
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(0, 0, 0, 0.4f),
					new Vector4f(1, 1, 1, 0.4f),
					new Vector4f(1, 1, 1, 0.4f),
					new Vector4f(1, 1, 1, 0.8f)
					)) {
					
					if(Tiling.DEBUG) {
						Tiling.DEBUG_LEVEL++;
						if(Tiling.DEBUG_LEVEL > 3) Tiling.DEBUG_LEVEL = 0;
					}
				}
			}
			
			for(int i = 0; i < parent.patterns.size(); i++) {
				if(pattern_index != i) {
					if(Mouse.buttons[0] && Mouse.inside(width - a, 32 + i * 32, a, 32)) {
						Mouse.buttons[0] = false;
						parent.setPatternIndex(i);
						parent.resetZoom();
						
						LOGGER.finer("Tiling setPatternIndex = " + i);
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
				fileChooser.openDialog();
				LOGGER.finer("Opening file dialog.");
			}
			
			
			if(fileChooser.hasSelectedFile()) {
				String name = (selectedTiling == null) ? fileChooser.getFileName():selectedTiling.getName();
				
				if(drawToggle(name, "> " + name + " <", pattern_index == -1, false, 24, width - a, 332, 16, 4,
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(1, 1, 1, 0.3f),
					new Vector4f(1, 1, 1, 1),
					new Vector4f(1, 1, 1, 1)
					)) {
					
					if(parent.getPatternIndex() != -1) {
						parent.setPatternIndex(-1);
						parent.resetZoom();
						LOGGER.finer("Tiling setPatternIndex = -1");
					}
				}
				
				if(drawToggle("[Reload]", false, true, 24, width - a, 372, 4, 4,
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(0.7f, 0.7f, 0.7f, 0.1f),
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(0.7f, 0.7f, 0.7f, 1),
					new Vector4f(0.3f, 0.7f, 0.3f, 1),
					new Vector4f(1, 1, 1, 1)
					)) {
					updateFileSelection();
					LOGGER.fine("Reloading tiling");
				}
				
				if(drawToggle("[AutoReload]", "[AutoReload]", autoReload, true, 24, width - a + 110, 372, 4, 4,
					new Vector4f(0, 0, 0, 0.3f),
					new Vector4f(0.7f, 0.7f, 0.7f, 0.1f),
					new Vector4f(1, 1, 1, 0.1f),
					new Vector4f(0.7f, 0.7f, 0.7f, 1),
					new Vector4f(0.3f, 0.7f, 0.3f, 1),
					new Vector4f(0.3f, 0.7f, 0.3f, 1)
					)) {
					autoReload = !autoReload;
					LOGGER.finer("Tiling autoReload = " + autoReload);
				}
			}
		}
		
		if(pattern_index == -1 && autoReload && fileChooser.hasFileChanged()) {
			updateFileSelection();
			LOGGER.fine("AutoReloading tiling");
		}
		
		if(fileChooser.hasSelectionChanged()) {
			updateFileSelection();
			parent.resetZoom();
			LOGGER.fine("Loading new file");
		}
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA);
	}
	
	// TODO: Debug Function (remove)
	public void setTiling(TilingPattern pattern, File file) {
		this.fileChooser.setSelectedFile(file);
		this.selectedTiling = pattern;
		this.autoReload = true;
	}
	
	// TODO: Double buffer meshes... 
	private void updateFileSelection() {
		TilingPattern pattern = TilingLoader.loadGlobalPattern(fileChooser.getAbsolutePath());
		
		if(pattern == null) {
			// Failed to load file
			selectedTiling = null;
		} else {
			if(selectedTiling != null) {
				selectedTiling.cleanup();
			}
			
			selectedTiling = pattern;
			parent.customTiling = selectedTiling;
		}
		
		if(parent.getPatternIndex() != -1) {
			parent.resetZoom();
		}
		
		parent.setPatternIndex(-1);
	}
}
