package render.tiling;

public class TilingFaceList {
	public float[] vertex;
	public float[] uv = new float[6];
	
	public TilingFaceList(int size) {
		vertex = new float[size * 9];
	}
}
