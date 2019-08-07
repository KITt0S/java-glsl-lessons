#version 330

in vec3 Position;
in vec3 Normal;

uniform vec4 LightPosition;
uniform vec3 LightIntensity;
uniform vec3 Kd;
uniform vec3 Ka;
uniform vec3 Ks;
uniform float Shininess;

out vec4 FragColor;

const vec4 SurfaceColor = vec4( 0.9, 0.6, 0.4, 1.0 );

vec3 ads() {

    vec3 n = normalize( Normal );
    vec3 s = normalize( vec3( LightPosition ) - Position );
    vec3 v = normalize( vec3( -Position ) );
    vec3 h = normalize( v + s );
    return LightIntensity * ( Ka + Kd * max( dot( s, n ), 0.0 ) + Ks * pow( max( dot( h, n ), 0.0 ), Shininess ) );
}

void main() {

    FragColor = vec4( ads(), 1.0 ) * SurfaceColor;
}
