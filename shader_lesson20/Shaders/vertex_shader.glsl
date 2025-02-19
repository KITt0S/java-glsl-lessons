#version 330

in vec3 VertexPosition;
in vec3 VertexNormal;

out vec3 ReflectDir;
out vec3 RefractDir;

struct MaterialInfo {

    float Eta;
    float ReflectionFactor;
};
uniform MaterialInfo Material;

uniform bool DrawSkyBox;

uniform vec3 WorldCameraPosition;
uniform mat4 ModelMatrix;
uniform mat4 MVPMatrix;

void main() {

    if( DrawSkyBox ) {

        ReflectDir = VertexPosition;
    } else {

        vec3 worldPos = vec3( ModelMatrix * vec4( VertexPosition, 1.0 ) );
        mat3 NMatrix = transpose( inverse( mat3( ModelMatrix ) ) );
        vec3 worldNorm = NMatrix * VertexNormal;
        vec3 worldView = normalize( WorldCameraPosition - worldPos );
        ReflectDir = reflect( -worldView, worldNorm );
        RefractDir = refract( -worldView, worldNorm, Material.Eta );
    }
    gl_Position = MVPMatrix * vec4( VertexPosition, 1.0 );
}
