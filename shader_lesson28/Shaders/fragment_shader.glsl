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

    vec3 Kd;
};
uniform MaterialInfo Material;

uniform sampler2D PositionTex;
uniform sampler2D NormalTex;
uniform sampler2D ColorTex;

layout ( location = 0 ) out vec4 FragColor;
layout ( location = 1 ) out vec3 PositionData;
layout ( location = 2 ) out vec3 NormalData;
layout ( location = 3 ) out vec3 ColorData;

vec3 diffuseModel( vec3 pos, vec3 norm, vec3 diff ) {

    vec3 s = normalize( Light.Position - pos );
    float sDotN = max( dot( s, norm ), 0.0 );
    return Light.Intensity * diff * sDotN;
}

uniform int Step;

void pass1() {

    PositionData = Position;
    NormalData = normalize( Normal );
    ColorData = Material.Kd;
}

void pass2() {

    vec3 pos = vec3( texture( PositionTex, TexCoord ) );
    vec3 norm = vec3( texture( NormalTex, TexCoord ) );
    vec3 diffColor = vec3( texture( ColorTex, TexCoord ) );
    FragColor = vec4( diffuseModel( pos, norm, diffColor ), 1.0 );
}

void main() {

    if( Step == 0 ) {

        pass1();
    } else if( Step == 1 ) {

        pass2();
    }
}
