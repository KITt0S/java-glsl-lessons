#version 330

in vec3 Position;
in vec3 Normal;
in vec2 TexCoord;

struct LightInfo {

    vec3 Position;
    vec3 Intensity;
};

uniform LightInfo Light;

struct MaterialInfo {

    vec3 Ka;
    vec3 Kd;
    vec3 Ks;
    float Shininess;
};

uniform MaterialInfo Material;

uniform sampler2D Tex[2];

out vec4 FragColor;

vec3 adsPhongModel( in vec3 pos, in vec3 norm ) {

    vec3 ambient = Material.Ka;
    vec3 n = normalize( norm );
    vec3 s = normalize( Light.Position - pos );
    vec3 diffuse = Material.Kd * max( dot( s, n ), 0.0 );
    vec3 v = normalize( -pos );
    vec3 r = normalize( reflect( -s, n ) );
    vec3 spec = Material.Ks * pow( max( dot( v, r ), 0.0 ), Material.Shininess );
    return Light.Intensity * ambient + Light.Intensity * diffuse + Light.Intensity * spec;
}


void main() {

    vec4 baseColor = texture( Tex[ 0 ], TexCoord );
    vec4 alphaMap = texture( Tex[ 1 ], TexCoord );
    if( alphaMap.a < 0.3 ) {

        discard;
    } else {

        if( gl_FrontFacing ) {

            FragColor = vec4( adsPhongModel( Position, Normal ), 1.0 ) * baseColor;
        } else {

            FragColor = vec4( adsPhongModel( Position, -Normal ), 1.0 ) * baseColor;
        }
    }
}
