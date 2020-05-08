#version 130

in vec4 pass_Color;
in vec2 pass_Uv;

uniform sampler2D _main_tex;

out vec4 out_color;
void main() {
	vec4 diffuse = texture2D(_main_tex, pass_Uv);
	out_color = pass_Color + diffuse;
}