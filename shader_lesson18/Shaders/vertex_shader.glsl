#version 330

in vec3 VertexPosition;
in vec3 VertexNormal;
in vec2 VertexTexCoord;
in vec4 VertexTangent;

struct LightInfo {

    vec3 Position;
    vec3 Intensity;
};

uniform LightInfo Light;

out vec3 LightDir;
out vec2 TexCoord;
out vec3 ViewDir;

uniform mat4 MVMatrix;
uniform mat4 PMatrix;

void main() {

    mat3 NMatrix = transpose( inverse( mat3( MVMatrix ) ) );
    vec3 norm = normalize( NMatrix * VertexNormal );
    vec3 tang = normalize( NMatrix * vec3( VertexTangent ) );

    vec3 binormal = normalize( cross( norm, tang ) ) * VertexTangent.w;

    mat3 toObjectLocal = mat3( tang.x, binormal.x, norm.x,
                               tang.y, binormal.y, norm.y,
                               tang.z, binormal.z, norm.z );

    vec3 pos = vec3( MVMatrix * vec4( VertexPosition, 1.0 ) );

    LightDir = normalize( toObjectLocal * ( Light.Position - pos ) );
    ViewDir = toObjectLocal * normalize( -pos );
    TexCoord = VertexTexCoord;
    gl_Position = PMatrix * MVMatrix * vec4( VertexPosition, 1.0 );
}
