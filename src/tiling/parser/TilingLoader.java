package tiling.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joml.Vector3f;
import org.joml.Vector4f;

import render.main.Tiling;
import tiling.util.FileUtils;
import tiling.util.MathUtils;
import tiling.util.TilingUtil;

public class TilingLoader {
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private static final Logger LOGGER = Logger.getLogger("TilingLoader");
	
	static {
		LOGGER.setLevel(Level.INFO);
	}
	
	private TilingLoader() {
		
	}
	
	public static TilingPattern loadGlobalPattern(String path) {
		return _loadPattern(path, FileUtils.readFile(path));
	}
	
	public static TilingPattern loadLocalPattern(String path) {
		return loadLocalPattern(null, path);
	}
	
	public static TilingPattern loadLocalPattern(List<TilingPattern> list, String path) {
		try {
			TilingPattern pattern = _loadPattern(path, FileUtils.readStream(TilingLoader.class.getResourceAsStream(path)));
			if(list != null && pattern != null) {
				list.add(pattern);
			}
			
			return pattern;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Future<TilingPattern> loadLocalPatternFuture(List<TilingPattern> list, String path) {
		Future<TilingPattern> future = executor.submit(() -> {
			TilingPattern pattern = _loadPattern(path, FileUtils.readStream(TilingLoader.class.getResourceAsStream(path)));
			if(list != null && pattern != null) {
				list.add(pattern);
			}
			
			return pattern;
		});
		
		return future;
	}
	

	private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzåäöABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ0123456789:;_-.+*#'!?/<>()[]{}";
	private static final int SPLIT_DATA = 1;
	private static final int VERTEX_DATA = 2;
	private static final int TEXTURE_DATA = 3;
	private static final int DISPLAY_DATA = 4;
	
	
	public static final int ERROR_INVALID_CHARACTER = 1;
	public static final int ERROR_FORGOT_COLON = 2;
	public static final int ERROR_MULTIPLE_DECLARES = 4;
	public static final int ERROR_EMPTY_NAME = 8;
	public static final int ERROR_INVALID_VALUE = 16;
	public static final int ERROR_INVALID_POSITION = 32;
	public static final int ERROR_NOT_DECLARED = 64;
	public static final int ERROR_INVALID_INDENTATION = 128;
	public static final int ERROR_EMPTY_VALUE = 256;
	
	public static final String[] ALLOWED_COMMANDS = new String[] {
		"name",
		"shapes",
		"colors",
		"symmetry",
		"scaling",
		"texture",
		
		"minimumzoom",
		"maximumzoom",
		
		"startshape",
		"starttransform",
		"startrotation",
		"startscale",
		
		"showcorners",
		"cornerweight",
		
		"debug",
		"debugcolors",
		"",
		
		"vertexdata",
		"displaydata",
		"texturedata",
		"splitdata",
	};
	
	private static synchronized TilingPattern _loadPattern(String filePath, String content) {
		if(content.isEmpty()) {
			return null;
		}
		
		TilingUtil.setDebugLevel(LOGGER);
		
		TilingProgram program = new TilingProgram(filePath, content);
		TilingPattern pattern = new TilingPattern(program);
		
		HashSet<String> checks = new HashSet<String>();
		String currentTile = "";
		int data_type = 0;
		
		try {
			while(program.hasMoreLines()) {
				if(program.hasErrors()) break;
				
				String line = program.getNextLine();
				if(line == null) break;
				
				// LOGGER.info(String.format("%3d: ", program.getLineIndex()) + line);
				
				String[] arr = line.split(":");
				String name = arr[0].trim().toLowerCase();
				
				if(line.indexOf(':') < 0) {
					String value = line.trim();
					
					switch(data_type) {
						case SPLIT_DATA:
							try {
								pattern.tiles.get(currentTile).addInstruction(value);
							} catch(Exception e) {
								throw new TilingException(program,
									"SplitData: Error " + e.getMessage(),
									ERROR_INVALID_VALUE, e
								);
							}
							continue;
						case VERTEX_DATA:
							try {
								pattern.tiles.get(currentTile).addVertexData(value);
							} catch(Exception e) {
								throw new TilingException(program,
									"VertexData: Error " + e.getMessage(),
									ERROR_INVALID_VALUE, e
								);
							}
							continue;
						case TEXTURE_DATA:
							try {
								pattern.tiles.get(currentTile).addUvData(value);
							} catch(Exception e) {
								throw new TilingException(program,
									"TextureData: Error " + e.getMessage(),
									ERROR_INVALID_VALUE, e
								);
							}
							continue;
						case DISPLAY_DATA:
							try {
								pattern.tiles.get(currentTile).addDisplayData(value);
							} catch(Exception e) {
								throw new TilingException(program,
									"DisplayData: Error " + e.getMessage(),
									ERROR_INVALID_VALUE, e
								);
							}
							continue;
					}
				} else {
					if(!name.isEmpty()) {
						if(checks.contains(name)) {
							throw new TilingException(program,
								name + " was declared multiple times",
								ERROR_MULTIPLE_DECLARES
							);
						} else checks.add(name);
						
						errorCheckField(program, false);
					}
				}
				
				if(name.isEmpty()) {
					if(arr.length < 2) {
						throw new TilingException(program,
							"That shape does not exist",
							line.lastIndexOf(':') + 2,
							ERROR_INVALID_VALUE
						);
					}
					
					String str = arr[1].trim();
					if(!pattern.names.contains(str)) {
						throw new TilingException(program,
							"That shape does not exist",
							line.lastIndexOf(':') + 1 + arr[1].indexOf(str),
							ERROR_INVALID_VALUE
						);
					}
					
					currentTile = str;
					continue;
				}
				
				switch(name) {
					case "name": {
						pattern.name = parseString(program);
						LOGGER.finest("Tiling name = " + pattern.name);
						break;
					}
					case "symmetry": {
						int value = parseInt(program);
						if(value < 1) {
							throw new TilingException(program,
								"Symmetry needs to be greater than or equal to one",
								line.lastIndexOf(':') + 1,
								ERROR_INVALID_VALUE
							);
						}
						pattern.symmetry = value;
						LOGGER.finest("Tiling symmetry = " + pattern.symmetry);
						break;
					}
					case "shapes": {
						List<String> list = parseStrings(program);
						for(String s : list) {
							if(pattern.names.contains(s)) {
								throw new TilingException(program,
									"Shape was declared multiple times",
									ERROR_MULTIPLE_DECLARES
								);
							}
							pattern.names.add(s);
						}
						for(int i = 0; i < pattern.names.size(); i++) {
							String s = pattern.names.get(i);
							pattern.tiles.put(s, new TilingTile(pattern, s, i));
						}
						LOGGER.finest("Tiling names = " + pattern.names);
						break;
					}
					case "colors": {
						pattern.colors = parseColors(program);
						break;
					}
					case "minimumzoom": {
						int value = parseInt(program);
						if(value < 0) {
							throw new TilingException(program,
								"MinimumZoom can't be negative",
								line.lastIndexOf(':') + 1,
								ERROR_INVALID_VALUE
							);
						}
						pattern.minimum_zoom = value;
						LOGGER.finest("Tiling minimum_zoom = " + pattern.minimum_zoom);
						break;
					}
					case "maximumzoom": {
						int value = parseInt(program);
						if(value < 1) {
							throw new TilingException(program,
								"MaximumZoom can't be lower than one",
								line.lastIndexOf(':') + 1,
								ERROR_INVALID_VALUE
							);
						}
						if(value > 128) {
							throw new TilingException(program,
								"MaximumZoom can't be greater than 128",
								line.lastIndexOf(':') + 1,
								ERROR_INVALID_VALUE
							);
						}
						pattern.maximum_zoom = value;
						LOGGER.finest("Tiling maximum_zoom = " + pattern.maximum_zoom);
						break;
					}
					case "scaling": {
						float value = parseFloat(program);
						if(value < 0) {
							throw new TilingException(program,
								"Scaling can't be less than or equal be zero",
								line.lastIndexOf(':') + 1,
								ERROR_INVALID_VALUE
							);
						}
						if(value > 1) {
							throw new TilingException(program,
								"Scaling can't be greater than one",
								line.lastIndexOf(':') + 1,
								ERROR_INVALID_VALUE
							);
						}
						pattern.scaling = value;
						LOGGER.finest("Tiling scaling = " + pattern.scaling);
						break;
					}
					
					case "startshape": {
						if(pattern.names.isEmpty()) {
							throw new TilingException(program,
								"StartShape needs to be placed after Shapes",
								ERROR_INVALID_POSITION
							);
						}
						String value = parseString(program);
						if(!pattern.names.contains(value)) {
							throw new TilingException(program,
								"That shape does not exist",
								line.lastIndexOf(':') + 1 + arr[1].indexOf(value),
								ERROR_INVALID_VALUE
							);
						}
						pattern.default_tile = parseString(program);
						LOGGER.finest("Tiling defaultTile = " + pattern.default_tile);
						break;
					}
					case "starttransform": {
						pattern.startTransform = parseVector3f(program);
						LOGGER.finest("Tiling startTransform = " + String.format("(%.4f, %.4f, %.4f)", pattern.startTransform.x, pattern.startTransform.y, pattern.startTransform.z));
						break;
					}
					case "startrotation": {
						pattern.startRotation = parseFloat(program) * MathUtils.deg2Rad;
						LOGGER.finest("Tiling startRotation = " + (pattern.startRotation * MathUtils.rad2Deg));
						break;
					}
					case "startscale": {
						float value = parseFloat(program);
						if(value <= 0) {
							throw new TilingException(program,
								"StartScale needs to be greater than zero",
								line.lastIndexOf(':') + 1,
								ERROR_INVALID_VALUE
							);
						}
						pattern.startScale = value;
						LOGGER.finest("Tiling startScale = " + pattern.startScale);
						break;
					}
					
					case "texture": {
						if(arr.length < 2) {
							throw new TilingException(program,
								"Field can't be empty",
								ERROR_EMPTY_VALUE
							);
						} else {
							if(pattern.getTexture() == null) {
								String val = arr[1].trim();
								pattern.setTexture(val);
								
								// TODO: Check if the url exists and fix it
								LOGGER.finest("Tiling texturePath = " + val);
							}
						}
						break;
					}
					
					case "showcorners": {
						pattern.show_corners = parseBoolean(program);
						LOGGER.finest("Tiling show_corners = " + pattern.show_corners);
						break;
					}
					case "cornerweight": {
						float value = parseFloat(program);
						if(value < 0) {
							throw new TilingException(program,
								"CornerWeight can't be less than zero",
								line.lastIndexOf(':') + 1,
								ERROR_INVALID_VALUE
							);
						}
						
						pattern.corner_weight = value;
						LOGGER.finest("Tiling corner_weight = " + pattern.corner_weight);
						break;
					}
					
					case "debug": {
						pattern.debug = parseBoolean(program);
						LOGGER.finest("Tiling debug = " + pattern.debug);
						break;
					}
					case "debugcolors": {
						List<Vector4f> list = parseColors(program);
						for(int i = 0; i < Math.min(3, list.size()); i++) {
							pattern.debugColors[i] = list.get(i);
						}
						break;
					}
					
					case "displaydata": {
						LOGGER.finest("Tiling DISPLAY_DATA");
						data_type = DISPLAY_DATA;
						continue;
					}
					case "splitdata": {
						LOGGER.finest("Tiling TILE_DATA");
						data_type = SPLIT_DATA;
						continue;
					}
					case "vertexdata": {
						LOGGER.finest("Tiling VERTEX_DATA");
						data_type = VERTEX_DATA;
						continue;
					}
					case "texturedata": {
						LOGGER.finest("Tiling TEXTURE_DATA");
						data_type = TEXTURE_DATA;
						continue;
					}
					
					default: {
						throw new TilingException(program,
							"Invalid command, Did you mean '" + guessCommand(program) + "'",
							ERROR_INVALID_VALUE
						);
					}
				}
			}
		} catch(TilingException e) {
			e.printStackTrace();
			
			if(Tiling.DEBUG) {
				e.printStackTrace(System.out);
			}
			
		} catch(Exception e) {
			LOGGER.severe("This is not a normal error");
			LOGGER.severe("Please report this if you can");
			e.printStackTrace(System.out);
			
			return null;
		}
		
		if(program.hasErrors()) {
			return null;
		}
		
		if(!checks.contains("name")) {
			logInfoError(filePath);
			LOGGER.warning("Name was not declared");
			program.addError(ERROR_NOT_DECLARED);
		}
		
		if(!checks.contains("shapes")) {
			logInfoError(filePath);
			LOGGER.warning("Shapes was not declared");
			program.addError(ERROR_NOT_DECLARED);
		}
		
		if(!checks.contains("vertexdata")) {
			logInfoError(filePath);
			LOGGER.warning("VertexData was not declared");
			program.addError(ERROR_NOT_DECLARED);
		}
		
		if(!checks.contains("splitdata")) {
			logInfoError(filePath);
			LOGGER.warning("SplitData was not declared");
			program.addError(ERROR_NOT_DECLARED);
		}
		
		if(pattern.maximum_zoom < pattern.minimum_zoom) {
			logInfoError(filePath);
			LOGGER.warning("MaximumZoom can't be lower than MinimumZoom");
			program.addError(ERROR_NOT_DECLARED);
		}
		
		if(program.hasErrors()) {
			return null;
		}
		
		pattern.build();
		
		{
			TilingTile tile = pattern.getDefaultTile();
			for(int i = pattern.minimum_zoom; i < pattern.maximum_zoom; i++) {
				int faces = tile.calculate_faces(i);
				if(faces < 0 || faces > 1000000) {
					if(i == pattern.minimum_zoom) {
						LOGGER.warning("Invalid minimum_zoom: " + pattern.maximum_zoom);
						LOGGER.warning("The zoom is outside the range for drawing.");
						LOGGER.warning("Triangles: " + tile.calculate_faces_exact(i));
						LOGGER.warning("");
						program.addError(ERROR_INVALID_VALUE);
						return null;
					}
					
					LOGGER.info("Invalid maximum_zoom: " + pattern.maximum_zoom);
					LOGGER.finest("Triangles: " + tile.calculate_faces_exact(pattern.maximum_zoom));
					LOGGER.info("The zoom is outside the range for drawing.");
					LOGGER.info("Changing the value to: " + (i - 1));
					pattern.maximum_zoom = i - 1;
				}
			}
		}
		
		if(Tiling.DEBUG) {
			LOGGER.finest("Pattern:");
			LOGGER.finest("  name        = " + pattern.name);
			LOGGER.finest("  symmetry    = " + pattern.symmetry);
			LOGGER.finest("  maximumzoom = " + pattern.maximum_zoom);
			LOGGER.finest("  minimumzoom = " + pattern.minimum_zoom);
			LOGGER.finest("  names       = " + pattern.names);
			LOGGER.finest("  texturePath = " + pattern.texturePath);
			LOGGER.finest("  startTile   = " + pattern.default_tile);
			LOGGER.finest("  startRotation = " + pattern.startRotation * MathUtils.rad2Deg);
			LOGGER.finest("");
		}
		
		return pattern;
	}
	
	private static String guessCommand(TilingProgram program) {
		String line = program.getCurrentLine();
		String find = line.split(":")[0].trim().toLowerCase();
		
		double fsc = -1;
		String fst = "";
		
		for(String test : ALLOWED_COMMANDS) {
			double score = 0;
			
			for(int j = 1; j < find.length(); j++) {
				for(int i = 0; i < find.length() - j + 1; i++) {
					String sub = find.substring(i, i + j);
					
					int index = test.indexOf(sub);
					if(index != -1) {
						double cal = j / (test.length() + 1.0);
						score += cal * cal;
					}
				}
			}
			
			if(score > fsc) {
				fsc = score;
				fst = test;
			}
		}
		
		return fst;
	}
	
	private static boolean parseBoolean(TilingProgram program) {
		errorCheckField(program);
		
		String line = program.getCurrentLine();
		String[] arr = line.split(":");
		
		String value = arr[1].trim().toLowerCase();
		if(value.equals("true")) return true;
		if(value.equals("false")) return false;
		
		throw new TilingException(program,
			"Invalid boolean value", arr[0].length() + 1 + arr[1].indexOf(arr[1].trim()),
			ERROR_INVALID_VALUE
		);
	}
	
	private static List<Vector4f> parseColors(TilingProgram program) {
		errorCheckField(program);
		
		String line = program.getCurrentLine();
		String[] arr = line.split(":");
		
		
		String[] split = arr[1].trim().split("[ ,]+");
		List<Vector4f> list = new ArrayList<>();
		for(int i = 0; i < split.length; i++) {
			String str = split[i].trim();
			try {
				list.add(ValueParser.parseColor(str));
			} catch(Exception e) {
				throw new TilingException(program,
					"Invalid color value at index:" + i + ", value:" + str,
					ERROR_INVALID_VALUE
				);
			}
		}
		
		return list;
	}
	
	private static Vector3f parseVector3f(TilingProgram program) {
		return new Vector3f(_parseVector(program, 3));
	}
	
	private static float[] _parseVector(TilingProgram program, int size) {
		errorCheckField(program);
		
		String line = program.getCurrentLine();
		String[] arr = line.split(":");
		
		String[] split = arr[1].trim().split("[ ,]+");
		float[] xyzw = new float[size];
		for(int i = 0; i < Math.min(size, split.length); i++) {
			try {
				xyzw[i] = Float.parseFloat(split[i].trim());
			} catch(Exception e) {
				throw new TilingException(program,
					"Invalid float value at index:" + i,
					ERROR_INVALID_VALUE
				);
			}
		}
		
		return xyzw;
	}
	
	private static float parseFloat(TilingProgram program) {
		errorCheckField(program);
		
		String line = program.getCurrentLine();
		String[] arr = line.split(":");
		
		try {
			return Float.parseFloat(arr[1].trim());
		} catch(Exception e) {
			throw new TilingException(program,
				"Invalid float value", arr[0].length() + 1 + arr[1].indexOf(arr[1].trim()),
				ERROR_INVALID_VALUE
			);
		}
	}
	
	private static int parseInt(TilingProgram program) {
		errorCheckField(program);
		
		String line = program.getCurrentLine();
		String[] arr = line.split(":");
		
		try {
			return Integer.parseInt(arr[1].trim());
		} catch(Exception e) {
			throw new TilingException(program,
				"Invalid integer value", arr[0].length() + 1 + arr[1].indexOf(arr[1].trim()),
				ERROR_INVALID_VALUE
			);
		}
	}
	
	private static List<String> parseStrings(TilingProgram program) {
		errorCheckField(program);
		
		String line = program.getCurrentLine();
		String[] arr = line.split(":");
		
		List<String> list = new ArrayList<String>();
		String[] split = arr[1].trim().split("[ ,]+");
		for(String s : split) {
			list.add(s.trim());
		}
		
		return list;
	}
	
	private static String parseString(TilingProgram program) {
		errorCheckField(program);
		
		String line = program.getCurrentLine();
		String[] arr = line.split(":");
		
		return arr[1].trim();
	}
	
	private static void errorCheckField(TilingProgram program) {
		errorCheckField(program, true);
	}
	
	private static void errorCheckField(TilingProgram program, boolean check_empty) {
		String line = program.getCurrentLine();
		if(line.indexOf(' ') == 0) {
			throw new TilingException(program,
				"Invalid indentation", line.indexOf(line.trim()),
				ERROR_INVALID_INDENTATION
			);
		}
		
		if(line.indexOf(':') < 0) {
			int _index = line.indexOf(' ');
			throw new TilingException(program,
				"Did you forget a colon ':' here?", _index < 0 ? line.length():_index,
				ERROR_FORGOT_COLON
			);
		}
		
		if(line.indexOf(':') != line.lastIndexOf(':')) {
			throw new TilingException(program,
				"Invalid color character ':'", line.lastIndexOf(':'),
				ERROR_INVALID_CHARACTER
			);
		}
		
		int index = indexOfInvalidCharacter(line);
		if(index != -1) {
			throw new TilingException(program,
				"Invalid character", index,
				ERROR_INVALID_CHARACTER
			);
		}
		
		if(check_empty) {
			String[] arr = line.split(":");
			if(arr.length < 2) {
				throw new TilingException(program,
					"Field can't be empty", line.indexOf(':') + 1,
					ERROR_EMPTY_VALUE
				);
			}
		}
	}
	
	private static int indexOfInvalidCharacter(String string) {
		String str = string.trim();
		
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c != ',' && c != ' ' && ALLOWED_CHARACTERS.indexOf(c) < 0) {
				return i;
			}
		}
		
		return -1;
	}
	
	private static void logInfoError(String filePath) {
		LOGGER.warning("#Error in '" + filePath + "'");
		LOGGER.warning("");
	}
}
