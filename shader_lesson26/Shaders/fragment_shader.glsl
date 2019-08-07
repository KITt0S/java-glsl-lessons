#version 330

in vec3 Position;
in vec3 Normal;

uniform sampler2D Tex0;
uniform vec3 WorldCameraPosition;
uniform int Step;

uniform mat3 rgb2xyz = mat3( 2.768, 1.000, 0.0, 1.751, 4.590, 0.056, 1.130, 0.060, 5.594 );

struct LightInfo {

    vec3 Position;
    vec3 Intensity;
};
uniform LightInfo Light[ 3 ];

struct MaterialInfo {

    vec3 Ka;
    vec3 Kd;
    vec3 Ks;
    float Shininess;
};
uniform MaterialInfo Material;

out vec4 FragColor;

vec3 getPhongModel( vec3 lp, vec3 li ) {

    vec3 ambient = Material.Ka;
    vec3 n = normalize( Normal );
    vec3 s = normalize( lp - Position );
    float sDotN = dot( s, n );
    vec3 diffuse = max( sDotN, 0.0 ) * Material.Kd;
    vec3 r = reflect( -s, n );
    vec3 v = normalize( WorldCameraPosition - Position );
    vec3 spec = vec3( 0.0 );
    if( sDotN > 0.0 ) {

        spec = Material.Ks * pow( max( dot( v, r ), 0.0 ), Material.Shininess );
    }
    return li * ( ambient + diffuse + spec );
}

vec4 pass1() {

    vec3 phong = vec3( 0.0 );
    for( int i = 0; i < 3; i++ ) {

        phong += getPhongModel( Light[ i ].Position, Light[ i ].Intensity );
    }
    return vec4( phong, 1.0 );
}

vec4 pass2() {

    ivec2 pix = ivec2( gl_FragCoord.xy );
    vec4 res = texelFetch( Tex0, pix, 0 );
    return res;
}

const float Exposure = 0.1;
const float White = 1; //максимальная яркость
uniform float AveLum;

vec4 pass3() {

    //извлечение цвета пикселя
    ivec2 pix = ivec2( gl_FragCoord.xy );
    vec4 color = texelFetch( Tex0, pix, 0 );

    //пробразование в формат XYZ
    vec3 xyzColor = rgb2xyz * vec3( color );

    //пробразование в формат xyY
    float xyzSum = xyzColor.x + xyzColor.y + xyzColor.z;
    vec3 xyYColor = vec3( 0.0 );
    if( xyzSum > 0.0 ) {

        xyYColor = vec3( xyzColor.x / xyzSum, xyzColor.y / xyzSum, xyzColor.y );
    }

    //применение оператора компрессии тональности
    float L = ( Exposure * xyYColor.z ) / AveLum ;
    //L = L * ( 1 + L / ( White * White ) ) / ( 1 + L );
    if( xyYColor.y > 0.0 ) {

        xyzColor.x = ( L * xyYColor.x ) / xyYColor.y;
        xyzColor.y = L;
        xyzColor.z = ( L * ( 1 - xyYColor.x - xyYColor.y ) ) / xyYColor.y;
    }
    mat3 xyz2rgb = inverse( rgb2xyz );
    return vec4( xyz2rgb * xyzColor, 1.0 );
}

void main() {

    if( Step == 0 ) {

        FragColor = pass1();
    } else if( Step == 1 ) {

        FragColor = pass3();
    }
}
