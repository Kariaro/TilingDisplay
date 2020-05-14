#version 130

in vec4 in_Position;
in vec2 in_Uv;
in vec4 in_Color;

in vec3 in_Verts_0;
in vec3 in_Verts_1;
in vec3 in_Verts_2;

in vec4 in_Colors_0;
in vec4 in_Colors_1;
in vec4 in_Colors_2;


out vec4 pass_Color;
out vec2 pass_Uv;
out vec3 pass_Pos;
out mat3 pass_Verts;
out mat3x4 pass_Colors;

uniform mat4 transformationMatrix;
uniform mat4 projectionView;

void main() {
	gl_Position = projectionView * transformationMatrix * in_Position;
	pass_Color = in_Color;
	pass_Uv = in_Uv;
	
	pass_Pos = in_Position.xyz;
	pass_Verts = mat3(
		in_Verts_0,
		in_Verts_1,
		in_Verts_2
	);
	pass_Colors = mat3x4(
		in_Colors_0,
		in_Colors_1,
		in_Colors_2
	);
}