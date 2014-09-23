#version 100
precision highp float;

uniform sampler2D jvr_Texture0;
uniform sampler2D jvr_Texture1;

uniform float alpha;

varying vec2 texCoord;

void main (void){
    vec4 overlay = texture2D(jvr_Texture1, vec2(texCoord.x, 1.0-texCoord.y));
    vec4 original = texture2D(jvr_Texture0, texCoord);
    gl_FragColor = vec4(mix(original, vec4(overlay.xyz, 1), overlay.a).xyz, 1);
}
