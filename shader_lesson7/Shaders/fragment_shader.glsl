#version 330

flat in vec3 FrontColor;
flat in vec3 BackColor;

out vec4 FragColor;

void main() {


    if( gl_FrontFacing ) {

        FragColor = vec4( FrontColor, 1.0 ) * vec4( 0.9, 0.6, 0.4, 1.0 );
    } else {

        FragColor = mix( vec4( BackColor, 1.0 ) * vec4( 0.9, 0.6, 0.4, 1.0 ), vec4( 1.0, 0.0, 0.0, 1.0 ), 0.7 );
    }
}
