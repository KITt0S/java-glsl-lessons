#version 330

in vec3 LightIntensity;
out vec4 FragColor;

void main() {

    FragColor =  vec4( LightIntensity, 1.0 ) * vec4( 0.9, 0.6, 0.4, 1.0 );
}