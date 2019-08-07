#version 330

in vec3 Color;
const vec4 SurfaceColor = vec4( 0.9, 0.6, 0.4, 1.0 );
out vec4 FragColor;

void main() {

    FragColor = vec4( Color, 1.0 ) * SurfaceColor;
}
