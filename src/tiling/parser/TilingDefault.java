package tiling.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tiling.util.TilingUtil;

public class TilingDefault {
	private static List<TilingPattern> patterns = new ArrayList<>();
	public static final List<String> DEFAULT_TILINGS = Arrays.asList(
		"/patterns/Preset_Ammann_Beenker.tiling",
		"/patterns/Preset_Penrose.tiling",
		"/patterns/Preset_Socolar Square-Triangle.tiling",
		"/patterns/Preset_Tubingen.tiling",
		"/patterns/Preset_Danzer's 7-fold.tiling"
	);
	
	public static List<TilingPattern> loadPatterns() {
		if(!patterns.isEmpty()) return patterns;
		
		TilingUtil.execute(() -> {
			for(String name : DEFAULT_TILINGS) {
				try {
					TilingLoader.loadLocalPattern(patterns, name);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		return patterns;
	}
}
