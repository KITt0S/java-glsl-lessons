#version 330

in vec3 FrontColor;
in vec3 BackColor;
in vec2 TexCoord;
const vec4 MainColor = vec4( 0.9, 0.6, 0.4, 1.0 );
out vec4 FragColor;

void main() {

    const float scale = 100.0;
    bvec2 toDiscard = greaterThan( fract( TexCoord * scale ), vec2( 0.2, 0.2 ) );

    if( all( toDiscard ) ) {

        discard;
    }

    if( gl_FrontFacing ) {

        FragColor = vec4( FrontColor, 1.0 ) * MainColor;
    } else {

        FragColor = mix( vec4( BackColor, 1.0 ) * vec4( 0.9, 0.6, 0.4, 1.0 ), vec4( 1.0, 0.0, 0.0, 1.0 ), 0.7 );
    }
}
