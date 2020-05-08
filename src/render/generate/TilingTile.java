package render.generate;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import render.math.MathUtils;

public class TilingTile {
	public final TilingPattern parent;
	public final String name;
	public final int id;
	
	public Vector3f[][] vertex;
	public Vector2f[][] uv;
	public Vector4f[] colors;
	protected Vector4f color;
	
	public TilingTile(TilingPattern parent, String name, int id) {
		this.parent = parent;
		this.name = name;
		this.id = id;
	}
	
	protected void addInstruction(String line) {
		if(line.indexOf('>') < 0) return;
		String[] parts = line.split(">");
		
		String data = parts[0].trim();
		addInstructionData(data, parts[1].trim());
	}
	
	private List<Vector3f> points_vertex = new ArrayList<Vector3f>();
	private List<Vector2f> points_uv = new ArrayList<Vector2f>();
	protected void addVertexData(String data) {
		if(data.trim().isEmpty()) return;
		
		String[] arr = data.trim().split(" ");
		if(arr.length == 2) {
			points_vertex.add(new Vector3f(Float.valueOf(arr[0]), Float.valueOf(arr[1]), 0));
		}
		if(arr.length == 3) {
			points_vertex.add(new Vector3f(Float.valueOf(arr[0]), Float.valueOf(arr[1]), Float.valueOf(arr[2])));
		}
	}
	
	public void addUvData(String data) {
		if(data.trim().isEmpty()) return;
		
		String[] arr = data.trim().split(" ");
		points_uv.add(new Vector2f(Float.valueOf(arr[0]), Float.valueOf(arr[1])));
	}
	
	List<TilingInstruction> instructions = new ArrayList<TilingInstruction>();
	private void addInstructionData(String data, String next_stage) {
		TilingInstruction part = new TilingInstruction();
		part.next_stage = parent.tiles.get(next_stage);
		data = data.replace(",", ""); // Remove comas
		
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			
			switch(c) {
				case '(': break;
				case '[': break;
				case ')': {
					String content = sb.toString().trim();
					//System.out.println("  split () -> " + content);
					
					if(content.equals("---")) {
						part.commands.add(new TilingOp());
					} else {
						part.commands.add(new TilingOp(content));
					}
					sb.delete(0, sb.length());
					break;
				}
				case ']': {
					String content = sb.toString().trim();
					//System.out.println("  split () -> " + content);
					part.commands.add(new TilingOp(MathUtils.deg2Rad * Float.valueOf(content)));
					
					sb.delete(0, sb.length());
					break;
				}
				default:
					sb.append(c);
					break;
			}
		}
		//System.out.println("  generated -> " + part);
		
		instructions.add(part);
	}
	
	protected void build() {
		vertex = new Vector3f[points_vertex.size() / 3][];
		uv = new Vector2f[points_uv.size() / 3][];
		colors = new Vector4f[parent.colors.size()];
		
		for(int i = 0; i < colors.length; i++) {
			colors[i] = color;
		}
		
		for(int i = 0; i < vertex.length; i++) {
			vertex[i] = new Vector3f[3];
			for(int j = 0; j < 3; j++) {
				vertex[i][j] = points_vertex.get(i * 3 + j);
			}
		}
		
		for(int i = 0; i < uv.length; i++) {
			uv[i] = new Vector2f[3];
			for(int j = 0; j < 3; j++) {
				uv[i][j] = points_uv.get(i * 3 + j);
			}
		}
	}
	
	protected List<TileMatrix4f> calculate(Matrix4f _m, int depth) {
		List<TileMatrix4f> list = new ArrayList<TileMatrix4f>();
		_calculate(list, _m, depth);
		return list;
	}
	
	private void _calculate(List<TileMatrix4f> list, Matrix4f _m, int depth) {
		if(depth < 0 || depth == 0) {
			list.add(new TileMatrix4f(this, new Matrix4f(_m)));
			return;
		}
		
		Matrix4f transformationMatrix = new Matrix4f(_m);
		transformationMatrix.scale(parent.scaling);
		
		Matrix4f matrix = new Matrix4f(transformationMatrix);
		
		for(TilingInstruction i : instructions) {
			for(TilingOp command : i.commands) {
				switch(command.type) {
					case TRANSFORM:
						matrix.translate(command.x, command.y, command.z);
						break;
					case ROTATE:
						matrix.rotateZ(command.r);
						break;
					case RESET:
						matrix = new Matrix4f(transformationMatrix);
						break;
				}
			}
			
			i.next_stage._calculate(list, matrix, depth - 1);
		}
	}
	
	private static final int TRANSFORM = 1;
	private static final int ROTATE = 2;
	private static final int RESET = 3;
	class TilingOp {
		public int type;
		public float x, y, z, r;
		
		public TilingOp(float x, float y, float z) {
			this.type = TRANSFORM;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public TilingOp(String transform) {
			this.type = TRANSFORM;
			String[] arr = transform.split(" ");
			this.x = Float.valueOf(arr[0]);
			if(arr.length > 1) this.y = Float.valueOf(arr[1]);
			if(arr.length > 2) this.z = Float.valueOf(arr[2]);
		}
		
		public TilingOp(float rotation)  {
			this.type = ROTATE;
			this.r = rotation;
		}
		
		public TilingOp() {
			this.type = RESET;
		}
		
		@Override
		public String toString() {
			switch(type) {
				case TRANSFORM:
					return new StringBuilder().append("TilingOp(TRANSFORM) { x=").append(x).append(" y=").append(y).append(" z=").append(z).append(" }").toString();
				case ROTATE:
					return new StringBuilder().append("TilingOp(ROTATE) { deg=").append(MathUtils.rad2Deg * r).append(" }").toString();
				default:
					return "TilingOp(RESET)";
			}
		}
	}
	
	class TilingInstruction {
		public TilingTile next_stage;
		public List<TilingOp> commands = new ArrayList<TilingOp>();
		
		@Override
		public String toString() {
			return new StringBuilder().append("\n").append(commands.toString()).append(" -> ").append(next_stage).toString();
		}
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("render.generate.TilingTile@[").append(name).append("]").toString();
	}
}
