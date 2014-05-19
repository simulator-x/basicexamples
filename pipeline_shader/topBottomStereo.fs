#version 100
precision highp float;
uniform sampler2D rightEye;
uniform sampler2D leftEye;
uniform float intensity;
varying vec2 texCoord;

void main (void)
{
    if (texCoord.y < 0.5)
        gl_FragColor = texture2D(rightEye, vec2(texCoord.x, texCoord.y*2.0));
    else
        gl_FragColor = texture2D(leftEye, vec2(texCoord.x, texCoord.y*2.0-1.0));
}
