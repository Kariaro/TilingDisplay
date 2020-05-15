package tiling.parser;

import org.joml.Vector4f;

public class ValueParser {
	public static Vector4f parseColor(String value) throws NumberFormatException {
		int rgb, a, r, g, b;
		String str = value.trim();
		
		if(str.startsWith("#")) {
			str = str.substring(1);
			rgb = Integer.valueOf(str, 16);
			
			if(str.length() < 4) {
				r = (rgb >>  8) & 0xf;
				g = (rgb >>  4) & 0xf;
				b = (rgb      ) & 0xf;
				
				r *= 17;
				g *= 17;
				b *= 17;
				
				return new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f, 1);
			}
			if(str.length() == 4) {
				r = (rgb >> 12) & 0xf;
				g = (rgb >>  8) & 0xf;
				b = (rgb >>  4) & 0xf;
				a = (rgb      ) & 0xf;
				
				a *= 17;
				r *= 17;
				g *= 17;
				b *= 17;
				
				return new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
			}
			
			if(str.length() > 6) {
				r = (rgb >> 24) & 0xff;
				g = (rgb >> 16) & 0xff;
				b = (rgb >>  8) & 0xff;
				a = (rgb      ) & 0xff;
				
				return new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
			}
			
			r = (rgb >> 16) & 0xff;
			g = (rgb >>  8) & 0xff;
			b = (rgb      ) & 0xff;
			
			return new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f, 1);
		} else if(str.startsWith("0x")) {
			str = value.substring(2);
		}
		
		rgb = Integer.valueOf(str, 16);
		
		a = (rgb >> 24) & 0xff;
		r = (rgb >> 16) & 0xff;
		g = (rgb >>  8) & 0xff;
		b = (rgb      ) & 0xff;
		
		return new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f,
			str.length() < 7 ? 1:(a / 255.0f)
		);
	}
}
