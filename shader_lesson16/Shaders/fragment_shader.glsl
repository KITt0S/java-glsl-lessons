#version 330

in vec3 Position;
in vec3 Normal;
in vec2 TexCoord;

uniform sampler2D Tex0;
uniform sampler2D Tex1;

struct LightInfo {

    vec4 Position;
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

out vec4 FragColor;

void phongModel( vec3 pos, vec3 norm, out vec3 ambAndDiff, out vec3 spec ) {

    vec3 ambient = Material.Ka;
    vec3 n = normalize( norm );
    vec3 s = normalize( vec3( Light.Position ) - Position );
    vec3 diffuse = Material.Kd * max( dot( s, n ), 0.0 );
    ambAndDiff = Light.Intensity * ( ambient + diffuse );
    vec3 v = normalize( -Position );
    vec3 h = normalize( v + s );
    spec = Light.Intensity * Material.Ks * pow( max( dot( h, n ), 0.0 ), Material.Shininess );
}

void main() {

    vec3 ambAndDiff, spec;
    phongModel( Position, Normal, ambAndDiff, spec );
    vec4 brickTexColor = texture( Tex0, TexCoord );
    vec4 mossTexColor = texture( Tex1, TexCoord );
    vec4 TexColor = mix( brickTexColor, mossTexColor, mossTexColor.a );
    //vec4 TexColor = brickTexColor;
    FragColor = vec4( ambAndDiff, 1.0 ) * TexColor + vec4( spec, 1.0 );
}
