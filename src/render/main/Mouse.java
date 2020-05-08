package render.main;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;

public class Mouse extends GLFWCursorPosCallback implements GLFWMouseButtonCallbackI {
	public static double mouseX;
	public static double mouseY;
	
	public static double mouseDx;
	public static double mouseDy;
	
	public static boolean[] buttons = new boolean[256];
	
	private final int type;
	public Mouse(int type) {
		this.type = type;
	}
	
	@Override
	public void invoke(long window, double xpos, double ypos) {
		mouseDx = mouseX - xpos;
		mouseDy = mouseY - ypos;
		mouseX = xpos;
		mouseY = ypos;
	}
	
	@Override
	public void invoke(long window, int button, int action, int mods) {
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
		if(mouseX < x) return false;
		if(mouseX > x + w) return false;
		if(mouseY < y) return false;
		if(mouseY > y + h) return false;
		return true;
	}
}
