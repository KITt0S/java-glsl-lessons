#version 330

in vec4 MCvertex;
in vec3 MCnormal;

uniform mat4 MVMatrix;
uniform mat4 MVPMatrix;

uniform vec3 LightPosition;

const float SpecularContribution = 0.3;
const float DiffuseContribution = 1.0 - SpecularContribution;

out float LightIntensity;
out vec2 MCposition;

void main() {


    mat3 NormalMatrix = transpose( inverse( mat3( MVMatrix ) ) );
    vec3 ecPosition = vec3( MVMatrix * MCvertex );
    vec3 tNorm = normalize( NormalMatrix * MCnormal );
    vec3 lightVec = normalize( LightPosition - ecPosition );
    vec3 reflectVec = reflect( -lightVec, tNorm );
    vec3 viewVec = normalize( -ecPosition );
    float diffuse = max( dot( lightVec, tNorm ), 0.0 );
    float spec = 0.0;

    if( diffuse > 0.0 ) {

        spec = max( dot( reflectVec, viewVec ), 0.0 );
        spec = pow( spec, 16.0 );
    }

    LightIntensity = DiffuseContribution * diffuse + SpecularContribution * spec;
    MCposition = MCvertex.xy;
    gl_Position = MVPMatrix * MCvertex;
}