#version 330

in vec3 TexCord;

uniform BlobSettings {

    vec4 InnerColor;
    vec4 OuterColor;
    float RadiusInner;
    float RadiusOuter;
};

out vec4 FragColor;

void main() {

    float dx = TexCord.x - 0.5;
    float dy = TexCord.y - 0.5;
    float dist = sqrt( dx * dx + dy * dy );
    FragColor = mix( InnerColor, OuterColor, smoothstep( RadiusInner, RadiusOuter, dist ) );
    //FragColor = vec4( 0.5f, 0.5f, 0.5f, 1f );
}