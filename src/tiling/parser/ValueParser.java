package tiling.parser;

import org.joml.Vector4f;

public class ValueParser {
	public static Vector4f parseColor(String value) {
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
		}
		
		if(str.startsWith("0x")) str = value.substring(2);
		rgb = Integer.valueOf(str, 16);
		
		a = (rgb >> 24) & 0xff;
		r = (rgb >> 16) & 0xff;
		g = (rgb >>  8) & 0xff;
		b = (rgb      ) & 0xff;
		
		return new Vector4f(r / 255.0f, g / 255.0f, b / 255.0f,
			str.length() < 7 ? 1:(a / 255.0f)
		);
	}
	/*
	public static void main(String[] args) {
		String[] values = {
			"0x7700ff",
			"0x7ff000f",
			"0xfff",
			"#fff",
			"#fff7",
			"#079",
			"#007799",
			"#079a",
			"#007799aa",
		};
		
		Locale.setDefault(Locale.ENGLISH);
		
		for(String s : values) {
			Vector4f col = parseColor(s);
			Vector4f test = col.mul(256, new Vector4f());
			System.out.println("String '" + s + "' parsed " + String.format("(%d, %d, %d, %d)", (int)test.x, (int)test.y, (int)test.z, (int)test.w));
		}
	}*/
}
