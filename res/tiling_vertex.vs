#version 130

in vec3 in_Position;
in vec2 in_Uv;
in vec4 in_Color;

out vec4 pass_Color;
out vec2 pass_Uv;


uniform mat4 transformationMatrix;
uniform mat4 projectionView;

void main() {
	gl_Position = projectionView * transformationMatrix * vec4(in_Position, 1);
	pass_Color = in_Color;
	pass_Uv = in_Uv;
}