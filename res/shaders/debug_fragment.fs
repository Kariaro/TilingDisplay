#version 130

in vec4 pass_Color;
in vec2 pass_Uv;
in mat3 pass_Verts;

out vec4 out_color;

uniform sampler2D _main_tex;


void main() {
	vec4 diffuse = texture2D(_main_tex, pass_Uv);
	vec4 color = pass_Color + diffuse;
	
	//if(length(pass_Pos - pass_Verts[0]) < radius) color = vec4(1, 0, 0, 1);
	//if(length(pass_Pos - pass_Verts[1]) < radius) color = vec4(0, 1, 0, 1);
	//if(length(pass_Pos - pass_Verts[2]) < radius) color = vec4(0, 0, 1, 1);
	
	out_color = (color + vec4(pass_Verts[0] + pass_Verts[1] + pass_Verts[2], 1)) / 2.0;
}