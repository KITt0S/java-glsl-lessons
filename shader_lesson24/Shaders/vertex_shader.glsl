#version 330

layout (location = 0) in vec3 VertexPosition;
layout (location = 1) in vec3 VertexNormal;
layout (location = 2) in vec2 VertexTexCoord;

out vec3 Position;
out vec3 Normal;
out vec2 TexCoord;

uniform mat4 MVMatrix;
uniform mat4 MVP;

void main() {

    Position = vec3( MVMatrix * vec4( VertexPosition, 1.0  ) );
    mat3 NMatrix = transpose( inverse( mat3( MVMatrix ) ) );
    Normal = normalize( NMatrix * VertexNormal );
    TexCoord = VertexTexCoord;
    gl_Position = MVP * vec4( VertexPosition, 1.0 );
}
