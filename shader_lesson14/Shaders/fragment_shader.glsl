#version 330

in vec3 Position;
in vec3 Normal;

struct LightInfo {

    vec4 Position;
    vec3 Intensity;
};

uniform LightInfo Light;

struct FogInfo {

    float minDist;
    float maxDist;
    vec3 color;
};

uniform FogInfo Fog;

uniform vec3 Ka;
uniform vec3 Kd;
uniform vec3 Ks;
uniform float Shininess;

const vec4 SurfaceColor = vec4( 0.9, 0.6, 0.4, 1.0 );

out vec4 FragColor;

vec3 ads() {

    vec3 n = normalize( Normal );
    vec3 s = normalize( vec3( Light.Position ) - Position );
    vec3 ambient = Ka;
    vec3 diffuse = Kd * max( dot( n, s ), 0.0 );
    vec3 v = normalize( -Position.xyz );
    vec3 h = normalize( s + v );
    vec3 specular = Ks * pow( max( dot( h, n ), 0.0 ), Shininess );
    return Light.Intensity * ( ambient + diffuse + specular );
}

void main() {

    float dist = abs( Position.z );
    //float fogFactor = ( Fog.maxDist - dist ) / ( Fog.maxDist - Fog.minDist );
    float fogFactor = exp( -pow( 0.25 * dist, 2 ) );
    fogFactor = clamp( fogFactor, 0.0, 1.0 );
    vec3 shadeColor = ads();
    vec3 Color = mix( Fog.color, vec3( vec4( shadeColor, 1.0 ) * SurfaceColor ), fogFactor );
    FragColor = vec4( Color, 1.0 );
}