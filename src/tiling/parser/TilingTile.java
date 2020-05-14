package tiling.parser;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import tiling.util.MathUtils;

public class TilingTile {
	private static final Vector4f[] DEFAULT_COLORS = new Vector4f[] {
		new Vector4f(1, 1, 0, 1),
		new Vector4f(1, 0, 1, 1),
		new Vector4f(0, 1, 1, 1),
	};
	
	public final TilingPattern parent;
	public final String name;
	public final int id;
	
	protected Vector3f[][] vertex;
	protected Vector2f[][] uv;
	protected Vector4f[] colors;
	protected Vector4f color;
	
	protected Vector4f[][] corners;
	
	protected TilingTile(TilingPattern parent, String name, int id) {
		this.parent = parent;
		this.name = name;
		this.id = id;
	}
	
	public void addInstruction(String line) {
		if(line.indexOf('>') < 0) return;
		String[] parts = line.split(">");
		
		String data = parts[0].trim();
		addInstructionData(data, parts[1].trim());
	}
	
	private List<Vector4f> display_corners = new ArrayList<Vector4f>();
	public void addDisplayData(String data) {
		if(data.trim().isEmpty()) return;
		
		String str = data.trim().toLowerCase();
		if(str.indexOf('n') != -1) {
			display_corners.add(null);
			return;
		}
		
		
		String[] arr = str.split("[ ,]+");
		
		Vector4f corner = null;
		
		for(int i = 0; i < arr.length; i++) {
			String tag = arr[i].trim();
			
			char type = tag.charAt(0);
			// C (Corner)
			
			if(type == 'c') {
				if(parent.show_corners) {
					if(tag.length() < 2) {
						corner = DEFAULT_COLORS[(display_corners.size() % 3)];
					} else {
						corner = ValueParser.parseColor(tag.substring(1));
					}
				}
				
				continue;
			} else {
				/*throw new TilingException(parent.program,
					"Invalid display type '" + type + "' index:" + i,
					TilingLoaderNew.ERROR_INVALID_VALUE
				);*/
			}
		}
		
		display_corners.add(corner);
	}
	
	private List<Vector3f> points_vertex = new ArrayList<Vector3f>();
	private List<Vector2f> points_uv = new ArrayList<Vector2f>();
	public void addVertexData(String data) {
		if(data.trim().isEmpty()) return;
		
		String[] arr = data.trim().split("[ ,]+");
		float[] xyz = new float[3];
		for(int i = 0; i < arr.length; i++) {
			xyz[i] = Float.parseFloat(arr[i].trim());
		}
		
		points_vertex.add(new Vector3f(xyz));
	}
	
	public void addUvData(String data) {
		if(data.trim().isEmpty()) return;
		
		String[] arr = data.trim().split("[ ,]+");
		points_uv.add(new Vector2f(Float.valueOf(arr[0]), Float.valueOf(arr[1])));
	}
	
	private List<TilingInstruction> instructions = new ArrayList<TilingInstruction>();
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
					part.commands.add(new TilingOp(MathUtils.deg2Rad * Float.valueOf(content)));
					
					sb.delete(0, sb.length());
					break;
				}
				default:
					sb.append(c);
					break;
			}
		}
		
		instructions.add(part);
	}
	
	public void build() {
		Vector4f[][] _colors = new Vector4f[points_vertex.size() / 3][3];
		vertex = new Vector3f[points_vertex.size() / 3][3];
		uv = new Vector2f[points_uv.size() / 3][3];
		colors = new Vector4f[3];
		
		for(int i = 0; i < colors.length; i++) {
			colors[i] = color;
		}
		
		
		for(int i = 0; i < _colors.length; i++) {
			for(int j = 0; j < 3; j++) {
				if(parent.debug) {
					_colors[i][j] = color.add(parent.debugColors[j], new Vector4f()).div(2);
				} else {
					_colors[i][j] = color;
				}
			}
		}
		
		for(int i = 0; i < vertex.length; i++) {
			for(int j = 0; j < 3; j++) {
				vertex[i][j] = points_vertex.get(i * 3 + j);
			}
		}
		
		for(int i = 0; i < uv.length; i++) {
			for(int j = 0; j < 3; j++) {
				uv[i][j] = points_uv.get(i * 3 + j);
			}
		}
		
		
		if(parent.show_corners) {
			corners = new Vector4f[vertex.length][3];
			
			for(int i = 0; i < Math.min(display_corners.size() / 3, vertex.length); i++) {
				for(int j = 0; j < 3; j++) {
					corners[i][j] = display_corners.get(i * 3 + j);
				}
			}
		}
		

		uv_array = MathUtils.toFloatArray(uv, 3, new float[vertex.length * 3 * 2]);
		colors_array = MathUtils.toFloatArray(_colors, 3);
		matColors_array = new float[vertex.length * 9 * 4];
		
		if(parent.show_corners) {
			for(int i = 0; i < Math.min(display_corners.size() / 3, vertex.length); i++) {
				for(int j = 0; j < 3; j++) {
					for(int k = 0; k < 3; k++) {
						Vector4f v = display_corners.get(i * 3 + k);
						if(v == null) continue;
						
						matColors_array[i * 36 + j * 12 + k * 4    ] = v.x;
						matColors_array[i * 36 + j * 12 + k * 4 + 1] = v.y;
						matColors_array[i * 36 + j * 12 + k * 4 + 2] = v.z;
						matColors_array[i * 36 + j * 12 + k * 4 + 3] = v.w;
					}
				}
			}
		}
		
		//System.out.println("uv: " + TilingUtil.arrayToString(uv_array));
		//System.out.println("colors: " + TilingUtil.arrayToString(colors_array));
		//System.out.println("matColors: " + TilingUtil.arrayToString(matColors_array));
		//System.out.println();
	}
	
	float[] matColors_array;
	float[] colors_array;
	float[] uv_array;
	
	protected int calculate_faces(int depth) {
		try {
			int size = parent.tiles.size();
			
			long[][] values = new long[size][size];
			for(int i = 0; i < size; i++) {
				TilingTile tile = parent.tiles.get(parent.names.get(i));
				
				for(TilingInstruction inst : tile.instructions) {
					values[i][inst.next_stage.id] ++;
				}
			}
			
			long[] shapes = new long[size];
			long[] last = new long[size];
			shapes[id] = 1;
			
			for(int i = 0; i < depth; i++) {
				for(int j = 0; j < size; j++) {
					last[j] = shapes[j];
					shapes[j] = 0;
				}
				for(int j = 0; j < size; j++) {
					long a = last[j];
					for(int k = 0; k < size; k++) {
						long val = Math.multiplyExact(a, values[j][k]);
						shapes[k] = Math.addExact(shapes[k], val);
					}
				}
			}
			
			//int total_shapes = 0;
			long total_faces = 0;
			for(int i = 0; i < size; i++) {
				long vertices = parent.tiles.get(parent.names.get(i)).vertex.length;
				long val = Math.multiplyExact(shapes[i], vertices);
				total_faces = Math.addExact(total_faces, val);
			}
			
			//System.out.println("Shapes: " + total_shapes);
			//System.out.println("Faces: " + total_faces);
			
			if((total_faces >> 31L) != 0) return -1;
			return (int)total_faces;
		} catch(ArithmeticException e) {
		}
		
		return -1;
	}
	
	protected java.math.BigInteger calculate_faces_exact(int depth) {
		int size = parent.tiles.size();
		
		long[][] values = new long[size][size];
		for(int i = 0; i < size; i++) {
			TilingTile tile = parent.tiles.get(parent.names.get(i));
			
			for(TilingInstruction inst : tile.instructions) {
				values[i][inst.next_stage.id] ++;
			}
		}
		
		java.math.BigInteger[] shapes = new java.math.BigInteger[size];
		java.math.BigInteger[] last = new java.math.BigInteger[size];
		shapes[id] = java.math.BigInteger.ONE;
		
		for(int i = 0; i < depth; i++) {
			for(int j = 0; j < size; j++) {
				if(shapes[j] == null) {
					last[j] = java.math.BigInteger.ZERO;
				} else {
					last[j] = shapes[j];
				}
				
				shapes[j] = java.math.BigInteger.ZERO;
			}
			for(int j = 0; j < size; j++) {
				java.math.BigInteger a = last[j];
				for(int k = 0; k < size; k++) {
					java.math.BigInteger val = a.multiply(java.math.BigInteger.valueOf(values[j][k]));
					shapes[k] = shapes[k].add(val);
				}
			}
		}
		
		java.math.BigInteger total_faces = java.math.BigInteger.ZERO;
		for(int i = 0; i < size; i++) {
			long vertices = parent.tiles.get(parent.names.get(i)).vertex.length;
			java.math.BigInteger val = shapes[i].multiply(java.math.BigInteger.valueOf(vertices));
			total_faces = total_faces.add(val);
		}
		
		return total_faces;
	}
	
	protected ThreadedMeshData calculate(Matrix4f _m, int depth) {
		ThreadedMeshData mesh = new ThreadedMeshData(calculate_faces(depth), false);
		try {
			_calculate(mesh, _m, depth);
		} catch(InterruptedException e) {
			mesh.cleanup();
			return null;
		}
		
		return mesh;
	}
	
	private void _calculate(ThreadedMeshData mesh, Matrix4f _m, int depth) throws InterruptedException {
		if(depth < 0 || depth == 0) {
			if(Thread.interrupted()) throw new InterruptedException();
			mesh.write(this, _m);
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
						transformationMatrix.get(matrix);
						break;
				}
			}
			
			i.next_stage._calculate(mesh, matrix, depth - 1);
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
