#version 330

in vec3 Position;
in vec3 Normal;
in vec2 TexCoord;

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
    vec3 s = normalize( vec3( Light.Position ) - pos );
    vec3 diffuse = Material.Kd * max( dot( s, n ), 0.0 );
    ambAndDiff = Light.Intensity * ( ambient + diffuse );
    vec3 v = normalize( -pos );
    vec3 h = normalize( v + s );
    spec =  Light.Intensity * Material.Ks * pow( max( dot( h, n ), 0.0 ), Material.Shininess);
}

void main() {

    vec3 ambAndDiff, spec;
    vec4 texColor = texture( Tex1, TexCoord );
    phongModel( Position, Normal, ambAndDiff, spec );
    FragColor = vec4( ambAndDiff, 1.0 ) * texColor + vec4( spec, 1.0 );
}
