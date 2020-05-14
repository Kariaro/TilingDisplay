#version 130

in vec4 pass_Color;
in vec2 pass_Uv;

in vec3 pass_Pos;
in mat3 pass_Verts;
in mat3x4 pass_Colors;

uniform sampler2D _main_tex;
uniform int hasTexture;
uniform int isTiling;
uniform float radius;

out vec4 out_color;
void main() {
	vec4 diffuse = texture2D(_main_tex, pass_Uv);
	vec4 color = (1 - hasTexture) * pass_Color + hasTexture * diffuse;
	
	if(isTiling > 0) {
		if(pass_Colors[0].a > 0) {
			if(distance(pass_Pos, pass_Verts[0]) < radius) color = pass_Colors[0];
		}
		if(pass_Colors[1].a > 0) {
			if(distance(pass_Pos, pass_Verts[1]) < radius) color = pass_Colors[1];
		}
		if(pass_Colors[2].a > 0) {
			if(distance(pass_Pos, pass_Verts[2]) < radius) color = pass_Colors[2];
		}
	}/* else {
		// Background
		//color.rgb *= 10 * (gl_FragCoord.w / gl_FragCoord.z);
	}*/
	
	out_color = color;
}