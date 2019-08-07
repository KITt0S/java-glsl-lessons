#version 330

in vec3 VertexPosition;
in vec3 VertexNormal;

out vec3 EyePosition;
out vec3 EyeNormal;
out vec4 ProjTexCoord;

uniform mat4 ProjectorMatrix;
uniform mat4 ModelMatrix;
uniform mat4 MVMatrix;
uniform mat4 MVP;

void main() {

    vec4 pos4 = vec4( VertexPosition, 1.0 );
    EyePosition = vec3( MVMatrix * pos4 );
    mat3 NMatrix = transpose( inverse( mat3( MVMatrix ) ) );
    EyeNormal = normalize( NMatrix * VertexNormal );
    ProjTexCoord = ProjectorMatrix * ( MVMatrix * pos4 );
    gl_Position = MVP * pos4;
}
