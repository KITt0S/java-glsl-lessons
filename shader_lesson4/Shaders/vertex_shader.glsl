# version 330

in vec4 VertexPosition;
in vec3 VertexTexCord;

uniform mat4 MVPMatrix;

out vec3 TexCord;

void main() {

    TexCord = VertexTexCord;
    gl_Position = MVPMatrix * VertexPosition;
}