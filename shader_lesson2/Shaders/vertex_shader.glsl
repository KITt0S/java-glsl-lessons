#version 330

layout (location=1) in vec3 VertexPosition;
layout (location=2) in vec3 VertexColor;
uniform float resolutionScale;

out vec3 Color;

void main() {

    Color = VertexColor;
    gl_Position = vec4( VertexPosition.x / resolutionScale, VertexPosition.y, 0.0, 1.0 );
}