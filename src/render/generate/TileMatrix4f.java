package render.generate;

import org.joml.Matrix4f;

public class TileMatrix4f {
	public final TilingTile parent;
	public final Matrix4f matrix;
	
	public TileMatrix4f(TilingTile parent, Matrix4f matrix) {
		this.parent = parent;
		this.matrix = matrix;
	}
}
