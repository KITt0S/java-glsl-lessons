#version 330

in vec3 VertexPosition;
in  vec3 VertexNormal;

out vec3 Position;
out vec3 Normal;

uniform mat4 MVMatrix;
uniform mat4 PMatrix;

void main() {

    mat3 NMatrix = transpose( inverse( mat3( MVMatrix ) ) );
    Normal = normalize( NMatrix * VertexNormal );
    Position = vec3( MVMatrix * vec4( VertexPosition, 1.0 ) );
    gl_Position = PMatrix * MVMatrix * vec4( VertexPosition, 1.0 );
}
