#version 330

in vec3 Position;
in vec3 Normal;
in vec2 TexCoord;

uniform sampler2D Tex;

struct MaterialInfo {

    vec3 Ka;
    vec3 Kd;
    vec3 Ks;
    float Shininess;
};
uniform MaterialInfo Material;

struct LightInfo {

    vec3 Position;
    vec3 Intensity;
};
uniform LightInfo Light;

out vec4 FragColor;

vec3 getPhongModel( vec3 pos, vec3 norm ) {

    vec3 ambient = Material.Ka;
    vec3 s = normalize( Light.Position - pos );
    float sDotN = dot( s, norm );
    vec3 diffuse = Material.Kd * max( sDotN, 0.0 );
    vec3 v = normalize( -pos );
    vec3 r = reflect( -s, norm );
    vec3 spec = vec3( 0.0 );
    if( sDotN > 0.0 ) {

        spec = Material.Ks * pow( max( dot( v, r ), 0.0 ), Material.Shininess );
    }
    return Light.Intensity * ( ambient + diffuse + spec );
}


void main() {

    vec4 texColor = texture( Tex, TexCoord );
    FragColor = vec4( getPhongModel( Position, normalize( Normal ) ), 1.0 ) * texColor;
}
