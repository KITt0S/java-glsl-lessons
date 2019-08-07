#version 330

in vec3 Position;
in vec3 Normal;
in vec2 TexCoord;

uniform sampler2D RenderTex;
uniform float EdgeThreshhold;
uniform int Step;

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

const vec3 lum = vec3( 0.3, 0.59, 0.11 );

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

float getLuminance( vec3 color ) {

    return dot( lum, color );
}

vec4 pass1() {

    vec4 teapotColor = vec4( 0.1, 0.1, 0.9, 1.0 );
    return vec4( getPhongModel( Position, normalize( Normal ) ), 0.0 );
}

vec4 pass2() {

    ivec2 pix = ivec2( gl_FragCoord.xy );
    float s00 = getLuminance( texelFetchOffset( RenderTex, pix, 0, ivec2( -1, 1 ) ).rgb );
    float s10 = getLuminance( texelFetchOffset( RenderTex, pix, 0, ivec2( -1, 0 ) ).rgb );
    float s20 = getLuminance( texelFetchOffset( RenderTex, pix, 0, ivec2( -1, -1 ) ).rgb );
    float s01 = getLuminance( texelFetchOffset( RenderTex, pix, 0, ivec2( 0, 1 ) ).rgb );
    float s21 = getLuminance( texelFetchOffset( RenderTex, pix, 0, ivec2( 0, -1 ) ).rgb );
    float s02 = getLuminance( texelFetchOffset( RenderTex, pix, 0, ivec2( 1, 1 ) ).rgb );
    float s12 = getLuminance( texelFetchOffset( RenderTex, pix, 0, ivec2( 1, 0 ) ).rgb );
    float s22 = getLuminance( texelFetchOffset( RenderTex, pix, 0, ivec2( 1, -1 ) ).rgb );

    float sx = s02 + 2 * s12 + s22 - ( s00 + 2 * s10 + s20 );
    float sy = s20 + 2 * s21 + s22 - ( s00 + 2 * s01 + s02 );
    float g = sx * sx - sy * sy;

    if( g > 0.005 ) {

        return vec4( 1.0 );
    } else {

        return vec4( 0.0, 0.0, 0.0, 1.0 );
    }
}

void main() {

    if( Step == 0 ) {

        FragColor = pass1();
    } else {

        FragColor = pass2();
    }
}
