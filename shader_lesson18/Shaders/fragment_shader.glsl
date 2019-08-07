#version 330

in vec3 LightDir;
in vec2 TexCoord;
in vec3 ViewDir;

uniform sampler2D Tex[ 2 ]; // 0 - текстура, 1 - карта нормалей

struct LightInfo {

    vec3 Position;
    vec3 Intensity;
};

uniform LightInfo Light;

struct MaterialInfo {

    vec3 Ka;
    vec3 Ks;
    float Shininess;
};

uniform MaterialInfo Material;

out vec4 FragColor;

vec3 phongModel( vec3 norm, vec3 diffR ) {

    vec3 ambient = Light.Intensity * Material.Ka;
    float sDotN = max( dot( LightDir, norm ), 0.0 );
    vec3 diffuse = diffR * Light.Intensity * sDotN;
    vec3 r = reflect( -LightDir, norm );
    r = normalize( r );
    vec3 spec = vec3( 0.0 );
    if( sDotN > 0.0 ) {

        spec = Light.Intensity * Material.Ks * pow( max( dot( r, ViewDir ), 0.0 ), Material.Shininess );
    }
    return ambient + diffuse + spec;
}

void main() {

    vec4 texColor = texture( Tex[ 0 ], TexCoord );
    vec4 normal = 2.0 * texture( Tex[ 1 ], TexCoord ) - 1.0;
    if( gl_FrontFacing ) {

        FragColor = vec4( phongModel( normal.xyz, texColor.rgb ), 1.0 );
    } else {

        FragColor = vec4( phongModel( -normal.xyz, texColor.rgb ), 1.0 );
    }
}
