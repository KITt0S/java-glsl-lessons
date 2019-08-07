#version 330

in vec3 Position;
in vec3 Normal;

struct SpotLightInfo {

    vec4 position;
    vec3 intensity;
    vec3 direction;
    float exponent;
    float cutoff;
};

uniform SpotLightInfo Spot;

uniform vec3 Kd;
uniform vec3 Ka;
uniform vec3 Ks;
uniform float Shininess;

const vec4 SurfaceColor = vec4( 0.9, 0.6, 0.4, 1.0 );

out vec4 FragColor;

vec3 adsWithSpotlight() {

    vec3 s = normalize( vec3( Spot.position ) - Position );
    float angle = acos( dot( -s, Spot.direction ) );
    float cutoff = radians( clamp( Spot.cutoff, 0.0, 90.0 ) );
    vec3 ambient = Spot.intensity * Ka;
    if( angle < cutoff ) {

        float spotFactor = pow( dot( -s, Spot.direction ), Spot.exponent );
        vec3 v = normalize( -Position );
        vec3 h = normalize( v + s );
        vec3 n = normalize( Normal );
        return ambient + spotFactor * Spot.intensity * ( Kd * max( dot( s, n ), 0.0 ) + Ks *
         pow( max( dot( h, n ), 0.0 ), Shininess ) );
    } else {

        return ambient;
    }
}

void main() {

    FragColor = vec4( adsWithSpotlight(), 1.0 ) * SurfaceColor;
}
