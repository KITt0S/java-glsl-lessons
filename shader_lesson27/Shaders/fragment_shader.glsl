#version 330

in vec3 Position;
in vec3 Normal;

struct LightInfo {

    vec3 Intensity;
    vec3 Position;
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

vec3 getPhongModel() {

    vec3 ambient = Material.Ka;
    vec3 s = normalize( Light.Position - Position );
    vec3 n = normalize( Normal );
    float sDotN = dot( s, n );
    vec3 diffuse = max( sDotN, 0.0 ) * Material.Kd;
    vec3 v = normalize( -Position );
    vec3 r = reflect( -s, n );
    vec3 spec = vec3( 0.0 );
    if( sDotN > 0.0 ) {

        spec = pow( max( dot( v, r ), 0.0 ), Material.Shininess ) * Material.Ks ;
    }
    return Light.Intensity * ( ambient + diffuse + spec );
}

void main() {

   FragColor = vec4( getPhongModel(), 1.0 );
}
