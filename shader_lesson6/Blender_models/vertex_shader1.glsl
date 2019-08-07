#version 330

in vec3 VertexPosition;
in vec3 VertexNormal;

out vec3 ReflectDir;

uniform bool DrawSkyBox;
uniform vec3 WorldCameraPosition;
uniform mat4 ModelMatrix;
uniform mat4 MVPMatrix;

void main() {


    if( DrawSkyBox ) {

        ReflectDir = VertexPosition;
    } else {

        mat3 NMatrix = transpose( inverse( mat3( ModelMatrix ) ) );
        vec3 worldPos = vec3( ModelMatrix * vec4( VertexPosition, 1.0 ) );
        vec3 worldNorm = vec3( NMatrix * VertexNormal );
        vec3 worldView = normalize( WorldCameraPosition - worldPos );
        ReflectDir = reflect( -worldView, worldNorm );
    }

    gl_Position = MVPMatrix * vec4( VertexPosition, 1.0 );
}
