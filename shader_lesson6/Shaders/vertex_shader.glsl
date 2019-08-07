#version 330

in vec3 VertexPosition;
in vec3 VertexNormal;

struct LightInfo {

  vec4 Position;
  vec3 La;
  vec3 Ld;
  vec3 Ls;
};

uniform LightInfo Light;

struct MaterialInfo {

    vec3 Ka;
    vec3 Kd;
    vec3 Ks;
    float Shininess;
};

uniform MaterialInfo Material;

uniform mat4 ModelViewMatrix;
uniform mat4 ProjectionMatrix;

out vec3 LightIntensity;

void getEyeSpace( out vec3 norm, out vec4 position ) {

    mat3 NormalMatrix = transpose( inverse( mat3( ModelViewMatrix ) ) );
    norm = normalize( NormalMatrix * VertexNormal );
    position = ModelViewMatrix * vec4( VertexPosition, 1.0 );
}

vec3 phongModel( vec4 position, vec3 norm ) {

    vec3 s = normalize( vec3( Light.Position - position ) );
    vec3 v = normalize( -position.xyz );
    vec3 r = reflect( -s, norm );
    vec3 ambient = Light.La * Material.Ka;
    float sDotN = max( dot( s, norm ), 0.0 );
    vec3 diffuse = Light.Ld * Material.Kd * sDotN;
    vec3 spec = vec3( 0.0 );
    if( sDotN > 0 ) {

        spec = Light.Ls * Material.Ks * pow( max( dot( r, v ), 0.0 ), Material.Shininess );
    }
    return ambient + diffuse + spec;
}

void main() {

    vec3 eyeNorm;
    vec4 eyePosition;
    getEyeSpace( eyeNorm, eyePosition );
    LightIntensity = phongModel( eyePosition, eyeNorm );
    gl_Position = ProjectionMatrix * eyePosition;
}