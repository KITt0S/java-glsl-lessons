#version 330

in vec3 Position;
in vec3 Normal;

struct LightInfo {

    vec4 Position;
    vec3 Intensity;
};

uniform LightInfo Light;

uniform vec3 Kd;
uniform vec3 Ka;

const int levels = 3;
const float scaleFactor = 1.0 / levels;
const vec4 SurfaceColor = vec4( 0.9, 0.6, 0.4, 1.0 );

out vec4 FragColor;

vec3 toonShade() {

    vec3 s = normalize( Light.Position.xyz - Position );
    vec3 n = normalize( Normal );
    float cosine = max( 0.0, dot( s, n ) );
    vec3 diffuse = Kd * floor( cosine * levels ) * scaleFactor;
    return Light.Intensity * ( Ka + diffuse );
}

void main() {

    FragColor = vec4( toonShade(), 1.0 ) * SurfaceColor;
}
