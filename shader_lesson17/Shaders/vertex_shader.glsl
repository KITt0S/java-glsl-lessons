#version 330

in vec3 VertexPosition;
in vec3 VertexNormal;
in vec2 VertexTexCoord;

out vec3 Position;
out vec3 Normal;
out vec2 TexCoord;

uniform mat4 MVMatrix;
uniform mat4 PMatrix;

void main() {

    Position = vec3( MVMatrix * vec4( VertexPosition, 1.0 ) );
    mat3 NMatrix = transpose( inverse( mat3( MVMatrix ) ) );
    Normal = NMatrix * VertexNormal;
    TexCoord = VertexTexCoord;
    gl_Position = PMatrix * MVMatrix * vec4( VertexPosition, 1.0 );
}
