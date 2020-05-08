package render.generate;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joml.Vector3f;
import org.joml.Vector4f;

import render.main.FileUtils;
import render.mesh.Texture;
import render.tiling.Tiling;

public class TilingLoaderNew {
	private static final Logger LOGGER = Logger.getLogger("TilingLoader");
	
	static {
		LOGGER.setLevel(Level.INFO);
	}
	
	public static TilingPattern loadGlobalPattern(String path) {
		return _loadPattern(path, FileUtils.readFile(path));
	}
	
	public static TilingPattern loadLocalPattern(String path) {
		return loadLocalPattern(null, path);
	}
	
	public static TilingPattern loadLocalPattern(List<TilingPattern> list, String path) {
		try {
			TilingPattern pattern = _loadPattern(path, FileUtils.readStream(TilingLoaderNew.class.getResourceAsStream(path)));
			if(pattern != null) {
				list.add(pattern);
			}
			
			return pattern;
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	

	private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzåäöABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ0123456789_-.+*#!()[]{}";
	private static final int TILE_DATA = 1;
	private static final int VERTEX_DATA = 2;
	private static final int TEXTURE_DATA = 3;
	
	public static final int ERROR_INVALID_CHARACTER = 1;
	public static final int ERROR_FORGOT_COLON = 2;
	public static final int ERROR_MULTIPLE_DECLARES = 3;
	public static final int ERROR_EMPTY_NAME = 4;
	public static final int ERROR_INVALID_VALUE = 5;
	public static final int ERROR_INVALID_POSITION = 6;
	public static final int ERROR_NOT_DECLARED = 7;
	public static final int ERROR_INVALID_INDENTATION = 8;
	
	private static int ERROR_CODE = 0;
	private static synchronized TilingPattern _loadPattern(String filePath, String content) {
		TilingPattern pattern = new TilingPattern();
		content = content.replace("\r\n", "\n");
		content = content.replace("\t", "  ");
		
		LOGGER.setLevel(Tiling.DEBUG ? Level.ALL:Level.INFO);
		boolean error = false;
		ERROR_CODE = 0;
		
		HashSet<String> checks = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		String currentTile = "";
		int data_type = 0;
		String line = "";
		
		int lineIndex = 0;
		for(int i = 0; i < content.length(); i++) {
			if(error) break;
			
			char c = content.charAt(i);
			
			if(c == '\n') {
				lineIndex++;
				
				if(sb.length() < 1) continue;
				line = sb.toString();
				
				sb.delete(0, sb.length());
				if(line.trim().length() < 1) continue;
			} else {
				sb.append(c);
				
				if(i == content.length() - 1) {
					line = sb.toString();
					sb.delete(0, sb.length());
					
					if(line.isEmpty()) continue;
				} else {
					continue;
				}
			}
			
			if(line.trim().indexOf('#') == 0) continue;
			
			//LOGGER.info(String.format("%3d: ", lineIndex) + line);
			
			{
				if(line.indexOf(':') < 0) {
					String name = line.trim();
					
					if(data_type == TILE_DATA) {
						try {
							pattern.tiles.get(currentTile).addInstruction(name);
						} catch(Exception e) {
							e.printStackTrace();
							logParsingError(filePath, lineIndex, line,
								line.indexOf(name),
								" Error " + e.getMessage()
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
						
						continue;
					}
					
					if(data_type == VERTEX_DATA) {
						try {
							pattern.tiles.get(currentTile).addVertexData(name);
						} catch(Exception e) {
							e.printStackTrace();
							logParsingError(filePath, lineIndex, line,
								line.indexOf(name),
								" Error " + e.getMessage()
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
						
						continue;
					}
					
					if(data_type == TEXTURE_DATA) {
						if(line.startsWith("   ")) {
							try {
								pattern.tiles.get(currentTile).addUvData(name);
							} catch(Exception e) {
								e.printStackTrace();
								logParsingError(filePath, lineIndex, line,
									line.indexOf(name),
									" Error "
								);
								ERROR_CODE = ERROR_INVALID_VALUE;
								error = true;
								break;
							}
						} else {
							currentTile = name;
						}
						
						continue;
					}
				}
			}
			
			if(line.indexOf(':') < 0) {
				int _index = line.indexOf(' ');
				logParsingError(filePath, lineIndex, line,
					_index < 0 ? line.length():_index,
					" Did you forget a colon ':' here?"
				);
				ERROR_CODE = ERROR_FORGOT_COLON;
				error = true;
				break;
			}
			
			if(line.indexOf(':') != line.lastIndexOf(':')) {
				logParsingError(filePath, lineIndex, line,
					line.lastIndexOf(':'),
					" Invalid color character ':'"
				);
				ERROR_CODE = ERROR_INVALID_CHARACTER;
				error = true;
				break;
			}
			
			//System.out.printf("Read: \"%s\"\n", line);
			String[] array = line.split(":");
			
			String command = array[0].trim().toLowerCase();
			
			{
				if(checks.contains(command)) {
					//System.out.println("AA\"" + command + "\"");
					if(command.length() > 0) {
						logParsingError(filePath, lineIndex, line,
							0,
							" " + Character.toUpperCase(command.charAt(0)) + command.substring(1) + " was declared multiple times."
						);
						ERROR_CODE = ERROR_MULTIPLE_DECLARES;
						error = true;
						break;
					}
				} else checks.add(command);
				//System.out.println("BB\"" + command + "\"");
				if(data_type == 0 && array[0].toLowerCase().indexOf(command) != 0) {
					logParsingError(filePath, lineIndex, line,
						array[0].toLowerCase().indexOf(command),
						" Invalid indentation"
					);
					ERROR_CODE = ERROR_INVALID_INDENTATION;
					error = true;
					break;
				}
			}
			
			switch(command) {
				case "name": {
					String name = array[1].trim();
					if(name.isEmpty()) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + 1,
							" The name can't be empty"
						);
						error = true;
						ERROR_CODE = ERROR_EMPTY_NAME;
						break;
					}
					
					pattern.setName(name);
					LOGGER.finest("Tiling name       = " + name);
					continue;
				}
				case "symmetry": {
					int value = 0;
					try {
						String str = array[1].trim();
						value = Integer.valueOf(str);
						
						if(value < 1) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + array[1].indexOf(str) + 1,
								" Symmetry needs to be greater than zero."
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
					} catch(Exception e) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + 1,
							" Invalid integer value"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					
					LOGGER.finest("Tiling symmetry   = " + value);
					pattern.setSymmetry(value);
					continue;
				}
				case "shapes": {
					if(array.length < 2) {
						int _index = line.indexOf(' ');
						logParsingError(filePath, lineIndex, line,
							_index < 0 ? line.length():_index,
							" No shapes are defined"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					
					String str = array[1].trim();
					for(int k = 0; k < str.length(); k++) {
						char kc = str.charAt(k);
						if(kc != ',' && kc != ' ' && ALLOWED_CHARACTERS.indexOf(kc) < 0) {
							logParsingError(filePath, lineIndex, line,
								array[0].length() + array[1].length() - str.length() + 1 + k,
								" Invalid character"
							);
							LOGGER.warning("Allowed Characters: " + ALLOWED_CHARACTERS);
							ERROR_CODE = ERROR_INVALID_CHARACTER;
							error = true;
							break;
						}
					}
					if(error) break;
					
					String[] arr = array[1].split(",");
					for(int k = 0; k < arr.length; k++) {
						String name = arr[k].trim();
						if(name.indexOf(' ') != -1) {
							logParsingError(filePath, lineIndex, line,
								indexOf(line, k, ',') + arr[k].indexOf(name) + name.indexOf(' '),
								" Can't have spaces in shape names"
							);
							ERROR_CODE = ERROR_INVALID_CHARACTER;
							error = true;
							break;
						}
						
						pattern.names.add(name);
						pattern.tiles.put(name, new TilingTile(pattern, name, k));
						LOGGER.finest("Tiling addShape   = " + name);
					}
					
					continue;
				}
				case "scaling": { // Shapes
					try {
						String str = array[1].trim();
						float value = Float.valueOf(str);
						
						if(value == 0) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + array[1].indexOf(str) + 1,
								" Scaling can't be zero"
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
						
						pattern.scaling = value;
						LOGGER.finest("Tiling scaling    = " + value);
					} catch(Exception e) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + 1,
							" Invalid float value"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					continue;
				}
				case "startscale": {
					try {
						String str = array[1].trim();
						float value = Float.valueOf(str);
						
						if(value == 0) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + array[1].indexOf(str) + 1,
								" StartScale can't be zero"
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
						
						pattern.startScale = value;
						LOGGER.finest("Tiling startScale    = " + value);
					} catch(Exception e) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + 1,
							" Invalid float value"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					continue;
				}
				case "startrotation": {
					try {
						String str = array[1].trim();
						float value = Float.valueOf(str);
						
						pattern.startRotation = value;
						LOGGER.finest("Tiling startRotation = " + value);
					} catch(Exception e) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + 1,
							" Invalid float value"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					continue;
				}
				case "starttransform": {
					String[] arr = array[1].split(",");
					float[] xyz = new float[3];
					
					for(int j = 0; j < arr.length; j++) {
						String str = arr[j].trim();
						
						try {
							xyz[j] = Float.valueOf(str);
						} catch(Exception e) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + 1,
								" Invalid float value"
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
					}
					if(error) break;
					LOGGER.finest(String.format("Tiling startTransform = (%.4f, %.4f, %.4f)", xyz[0], xyz[1], xyz[2]));
					pattern.startTransform.x = xyz[0];
					pattern.startTransform.y = xyz[1];
					pattern.startTransform.z = xyz[2];
					continue;
				}
				case "debugrotation": {
					String val = array[1].trim();
					
					try {
						boolean value = Boolean.parseBoolean(val);
						pattern.debugRotation = value;
						LOGGER.finest("Tiling debugRotation = " + value);
					} catch(Exception e) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + 1,
							" Invalid boolean value"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					continue;
				}
				case "debugcolors": {
					String[] arr = array[1].split(",");
					for(int j = 0; j < arr.length; j++) {
						String str = arr[j].trim();
						int r = 0;
						int g = 0;
						int b = 0;
						
						try {
							if(str.startsWith("0x")) {
								int rgb = Integer.valueOf(str.substring(2), 16);
								r = (rgb >> 16) & 0xff;
								g = (rgb >>  8) & 0xff;
								b = (rgb      ) & 0xff;
							} else {
								boolean shrt = false;
								int rgb = 0;
								if(str.startsWith("#")) {
									rgb = Integer.valueOf(str.substring(1), 16);
									shrt = str.length() < 5;
								} else {
									rgb = Integer.valueOf(str, 16);
									shrt = str.length() < 4;
								}
								
								if(shrt) {
									r = (rgb >> 8) & 0xf;
									g = (rgb >> 4) & 0xf;
									b = (rgb     ) & 0xf;
									
									r = r * 16 + r;
									g = g * 16 + g;
									b = b * 16 + b;
								} else {
									r = (rgb >> 16) & 0xff;
									g = (rgb >>  8) & 0xff;
									b = (rgb      ) & 0xff;
								}
							}
						} catch(Exception e) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + 1 + indexOf(array[1], j, ',') + arr[j].indexOf(str),
								" Invalid color value"
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
						
						pattern.debugColors[j] = new Vector3f(r / 256.0f, g / 256.0f, b / 256.0f);
						LOGGER.finest(String.format("Tiling debugColor = Color[0x%02X%02X%02X]", r, g, b));
					}
					
					continue;
				}
				case "texture": {
					try {
						if(pattern.getTexture() == null && array.length > 1) {
							if(array[1].startsWith("!")) { // Inside Jar
								pattern.setTexture(new Texture(TilingLoaderNew.class.getResourceAsStream(array[1].substring(1))));
							} else {
								pattern.setTexture(new Texture(array[1]));
							}
						}
					} catch(Exception e) {
						LOGGER.warning("Invalid texture!");
					}
					continue;
				}
				case "colors": {
					String[] arr = array[1].split(",");
					for(int j = 0; j < arr.length; j++) {
						String str = arr[j].trim();
						int r = 0;
						int g = 0;
						int b = 0;
						
						try {
							if(str.startsWith("0x")) {
								int rgb = Integer.valueOf(str.substring(2), 16);
								r = (rgb >> 16) & 0xff;
								g = (rgb >>  8) & 0xff;
								b = (rgb      ) & 0xff;
							} else {
								boolean shrt = false;
								int rgb = 0;
								if(str.startsWith("#")) {
									rgb = Integer.valueOf(str.substring(1), 16);
									shrt = str.length() < 5;
								} else {
									rgb = Integer.valueOf(str, 16);
									shrt = str.length() < 4;
								}
								
								if(shrt) {
									r = (rgb >> 8) & 0xf;
									g = (rgb >> 4) & 0xf;
									b = (rgb     ) & 0xf;
									
									r = r * 16 + r;
									g = g * 16 + g;
									b = b * 16 + b;
								} else {
									r = (rgb >> 16) & 0xff;
									g = (rgb >>  8) & 0xff;
									b = (rgb      ) & 0xff;
								}
							}
						} catch(Exception e) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + 1 + indexOf(array[1], j, ',') + arr[j].indexOf(str),
								" Invalid color value"
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
						
						Vector4f color = new Vector4f(r / 256.0f, g / 256.0f, b / 256.0f, 1);
						pattern.colors.add(color);
						LOGGER.finest(String.format("Tiling addColor   = Color[0x%02X%02X%02X]", r, g, b));
					}
					
					continue;
				}
				case "startshape": {
					String name = array[1].trim();
					if(!pattern.names.contains(name)) {
						if(!checks.contains("shapes")) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + array[1].indexOf(name) + 1,
								" StartShape can't be called before Shapes"
							);
							ERROR_CODE = ERROR_INVALID_POSITION;
							error = true;
							break;
						}
						
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + array[1].indexOf(name) + 1,
							" That shape does not exist"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					
					pattern.setDefaultTile(name);
					LOGGER.finest("Tiling startShape = " + name);
					continue;
				}
				case "minimumzoom": {
					try {
						String str = array[1].trim();
						int value = Integer.valueOf(str);
						
						if(value < 0) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + array[1].indexOf(str) + 1,
								" ZoomMin can't be less than zero"
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
						
						pattern.setMinimumZoom(value);
						LOGGER.finest("Tiling min zoom   = " + value);
					} catch(Exception e) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + 1,
							" Invalid integer value"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					
					continue;
				}
				case "maximumzoom": {
					try {
						String str = array[1].trim();
						int value = Integer.valueOf(str);
						
						if(value < 1) {
							logParsingError(filePath, lineIndex, line,
								line.lastIndexOf(':') + array[1].indexOf(str) + 1,
								" ZoomMax can't be less than one"
							);
							ERROR_CODE = ERROR_INVALID_VALUE;
							error = true;
							break;
						}
						
						pattern.setMaximumZoom(value);
						LOGGER.finest("Tiling max zoom   = " + value);
					} catch(Exception e) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + 1,
							" Invalid integer value"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					
					continue;
				}
				case "splitdata": {
					LOGGER.finest("Tiling TILE_DATA");
					data_type = TILE_DATA;
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
				case "": {
					String name = array[1].trim();
					if(!pattern.names.contains(name)) {
						logParsingError(filePath, lineIndex, line,
							line.lastIndexOf(':') + array[1].indexOf(name) + 1,
							" That shape does not exist"
						);
						ERROR_CODE = ERROR_INVALID_VALUE;
						error = true;
						break;
					}
					
					LOGGER.finest("Tiling setTile    = " + name);
					currentTile = name;
					continue;
				}
				default: {
					logParsingError(filePath, lineIndex, line,
						0, " This is not a valid command"
					);
					ERROR_CODE = ERROR_INVALID_VALUE;
					error = true;
					break;
				}
			}
		}
		
		if(error) {
			return null;
		}
		
		if(!checks.contains("name")) {
			logInfoError(filePath);
			LOGGER.warning("Name was not declared");
			ERROR_CODE = ERROR_NOT_DECLARED;
			error = true;
		}
		
		if(!checks.contains("shapes")) {
			logInfoError(filePath);
			LOGGER.warning("Shapes was not declared");
			ERROR_CODE = ERROR_NOT_DECLARED;
			error = true;
		}
		
		if(!checks.contains("vertexdata")) {
			logInfoError(filePath);
			LOGGER.warning("VertexData was not declared");
			ERROR_CODE = ERROR_NOT_DECLARED;
			error = true;
		}
		
		if(!checks.contains("splitdata")) {
			logInfoError(filePath);
			LOGGER.warning("SplitData was not declared");
			ERROR_CODE = ERROR_NOT_DECLARED;
			return null;
		}
		
		if(pattern.getMaximumZoom() < pattern.getMinimumZoom()) {
			logInfoError(filePath);
			LOGGER.warning("MaximumZoom can't be lower than MinimumZoom");
			ERROR_CODE = ERROR_INVALID_VALUE;
			return null;
		}
		
		if(error) {
			return null;
		}
		
		if(Tiling.DEBUG) {
			System.out.println("Pattern:");
			System.out.println("  name     = " + pattern.getName());
			System.out.println("  symmetry = " + pattern.getSymmetry());
			System.out.println("  names    = " + pattern.names);
			System.out.println("  tiles    = " + pattern.tiles);
			System.out.println("  texture  = " + pattern.getTexture());
			System.out.println("  colors   = " + pattern.colors);
			System.out.println();
		}
		
		pattern.build();
		pattern.generate(0);
		
		return pattern;
	}
	
	public static final int getLastError() {
		return ERROR_CODE;
	}
	
	private static int indexOf(String string, int skip, char c) {
		//System.out.println("\"" + string + "\" " + skip);
		
		int index = 0;
		while(index != -1 && skip >= 0) {
			int idx = string.indexOf(c, index) + 1;
			if(idx < 1 || skip == 0) {
				//System.out.println("\"" + string.substring(idx) + "\" " + skip + ", " + idx);
				return index;
			}
			//System.out.println("\"" + string.substring(idx) + "\" " + skip + ", " + idx);
			index = idx;
			skip --;
		}
		
		return 0;
	}
	
	private static void logInfoError(String filePath) {
		LOGGER.warning("#Error in '" + filePath + "'");
		LOGGER.warning("");
	}
	
	private static void logParsingError(String filePath, int lineIndex, String line, int pointer, String message) {
		LOGGER.warning("#Error in '" + filePath + "'");
		LOGGER.warning("");
		LOGGER.warning("Invalid command on line (" + lineIndex + ") '" + line + "'");
		LOGGER.warning(line);
		LOGGER.warning(logPointer(pointer, message));
	}
	
	private static String logPointer(int length, String message) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < length; i++) sb.append("-");
		return sb.append("^").append(message).toString();
	}
}
