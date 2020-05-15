package render.main;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;

import tiling.util.TilingUtil;

public class Mouse extends GLFWCursorPosCallback implements GLFWMouseButtonCallbackI {
	public static final boolean[] buttons = new boolean[256];
	public static double mouseX;
	public static double mouseY;
	
	private final int type;
	public Mouse(int type) {
		this.type = type;
	}
	
	@Override
	public void invoke(long window, double xpos, double ypos) {
		/*if(TilingUtil.checkBlockingInput(false)) {
			return;
		}*/
		
		mouseX = xpos;
		mouseY = ypos;
	}
	
	@Override
	public void invoke(long window, int button, int action, int mods) {
		if(action == 1) {
			if(TilingUtil.checkBlockingInput()) {
				return;
			}
		}
		
		buttons[button] = action == 1;
	}

	@Override
	public String getSignature() {
		if(type == 0) {
			return super.getSignature();
		} else {
			return GLFWMouseButtonCallbackI.super.getSignature();
		}
	}

	@Override
	public void callback(long args) {
		if(type == 0) {
			super.callback(args);
		} else {
			GLFWMouseButtonCallbackI.super.callback(args);
		}
	}
	
	public static boolean inside(float x, float y, float w, float h) {
		return !(mouseX <= x
			  || mouseX >= x + w
			  || mouseY <= y
			  || mouseY > y + h);
	}
}
