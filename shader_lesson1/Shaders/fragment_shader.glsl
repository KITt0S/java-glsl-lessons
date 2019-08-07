#version 430

precision mediump float;
uniform vec4 u_Color;
out vec4 outColor;

void main() {

    vec4 myColor = vec4( 0, 0, 0, 1 );
    for( int i = 0; i < 20; i++ ) {

        outColor = vec4( myColor.r + i / 20, myColor.g + 0.5 * i / 20, myColor.b + i / 20, 1.0 );
    }
    outColor = u_Color;
}