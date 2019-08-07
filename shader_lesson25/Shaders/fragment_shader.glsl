#version 330

in vec3 Position;
in vec3 Normal;

uniform sampler2D Texture0;

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

int PixelOffset[ 5 ] = int[](0, 1, 2, 3, 4);
uniform float Weight[ 5 ];

uniform int Step;

out vec4 FragColor;

vec3 getPhongModel() {

    vec3 ambient = Material.Ka;
    vec3 s = normalize( Light.Position - Position );
    vec3 n = normalize( Normal );
    float sDotN = dot( s, n );
    vec3 diffuse = Material.Kd * max( sDotN, 0.0 );
    vec3 v = normalize( -Position );
    vec3 r = reflect( -s, n );
    vec3 spec = vec3( 0.0 );
    if( sDotN > 0.0 ) {

        spec = Material.Ks * pow( max( dot( v, r ), 0.0 ), Material.Shininess );
    }
    return Light.Intensity * ( ambient + diffuse + spec );
}

vec4 pass1() {

    return vec4( getPhongModel(), 1.0 );
}

vec4 pass2() {

    ivec2 pix = ivec2( gl_FragCoord.xy );
    vec4 sum = texelFetch( Texture0, pix, 0 ) * Weight[ 0 ];
    for( int i = 1; i < 5; i++ ) {

        sum += texelFetchOffset( Texture0, pix, 0, ivec2( 0, PixelOffset[ i ] ) ) * Weight[i];
        sum += texelFetchOffset( Texture0, pix, 0, ivec2( 0, -PixelOffset[ i ] ) ) * Weight[i];
    }
    return sum;
}

vec4 pass3() {

    ivec2 pix = ivec2( gl_FragCoord.xy );
    vec4 sum = texelFetch( Texture0, pix, 0 ) * Weight[ 0 ];
    for( int i = 1; i < 5; i++ ) {

        sum += texelFetchOffset( Texture0, pix, 0, ivec2( PixelOffset[ i ], 0 ) ) * Weight[ i ];
        sum += texelFetchOffset( Texture0, pix, 0, ivec2( -PixelOffset[ i ], 0 ) ) * Weight[ i ];
    }
    return sum;
}

void main() {

    if( Step == 0 ) {

        FragColor = pass1();
    } else if( Step == 1 ) {

        FragColor = pass2();
    } else if( Step == 2 ) {

        FragColor = pass3();
    }
}
