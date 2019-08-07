#version 330

in vec3 VertexPosition;
in vec3 VertexNormal;

out vec3 Color;

struct LightInfo {

    vec4 Position;
    vec3 Intensity;
};

uniform LightInfo Lights[ 5 ];

uniform vec3 Kd;
uniform vec3 Ka;
uniform vec3 Ks;
uniform float Shininess;

uniform mat4 MVMatrix;
uniform mat4 PMatrix;

vec3 ads( int lightIndex, vec4 position, vec3 norm ) {

    vec3 s = normalize( vec3( Lights[ lightIndex ].Position - position ) );
    vec3 v = normalize( vec3( -position ) );
    vec3 r = reflect( -s, norm );
    vec3 I = Lights[ lightIndex ].Intensity;
    return 0.5 * I * ( Ka + Kd * max( dot( s, norm ), 0.0 ) + Ks * pow( max( dot( r, v ), 0.0 ), Shininess ) );
}

void main() {

    mat3 NMatrix = transpose( inverse( mat3( MVMatrix ) ) );
    vec3 eyeNorm = normalize( NMatrix * VertexNormal );
    vec4 eyePosition = MVMatrix * vec4( VertexPosition, 1.0 );
    Color = vec3( 0.0 );
    for( int i = 0; i < 5; i++ ) {

        Color += ads( i, eyePosition, eyeNorm );
    }
    gl_Position = PMatrix * eyePosition;
}
