package render.generate;

import java.util.ArrayList;
import java.util.List;

public class TilingDefault {
	private static List<TilingPattern> patterns = new ArrayList<TilingPattern>();
	
	public static List<TilingPattern> loadPatterns() {
		if(!patterns.isEmpty()) return patterns;
		
		try {
			//TilingLoaderNew.loadLocalPattern(patterns, "/patterns/Template.tiling");
			TilingLoaderNew.loadLocalPattern(patterns, "/patterns/Preset_Ammann_Beenker.tiling");
			TilingLoaderNew.loadLocalPattern(patterns, "/patterns/Preset_Penrose.tiling");
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return patterns;
	}
}
