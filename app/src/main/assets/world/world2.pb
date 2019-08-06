®
world2black"white*‹

outlinewhite/
b0block
light-grey"þÿÿÿÿÿÿÿÿ þÿÿÿÿÿÿÿÿ"!
g0goalyellow" ÿÿÿÿÿÿÿÿÿ2#
s0sphereorange"þÿÿÿÿÿÿÿÿ *Í

outlinewhite1
b0block
light-grey"þÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ(
b1block
light-grey" ÿÿÿÿÿÿÿÿÿ"5
g0goalyellow"!ÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ2#
s0sphereorange" ÿÿÿÿÿÿÿÿÿ@*Í

outlinewhite1
b0block
light-grey"þÿÿÿÿÿÿÿÿþÿÿÿÿÿÿÿÿ &
b1block
light-grey"þÿÿÿÿÿÿÿÿ"#
g0goalyellow"þÿÿÿÿÿÿÿÿ 27
s0sphereorange"!þÿÿÿÿÿÿÿÿþÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ@*Ø

outlinewhite1
b0block
light-grey"ÿÿÿÿÿÿÿÿÿ þÿÿÿÿÿÿÿÿ
b1block
light-grey"(
b2block
light-grey" ÿÿÿÿÿÿÿÿÿ"#
g0goalyellow" ÿÿÿÿÿÿÿÿÿ2#
s0sphereorange"þÿÿÿÿÿÿÿÿ@*á

outlinewhite1
b0block
light-grey"þÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ&
b1block
light-grey" þÿÿÿÿÿÿÿÿ
b2block
light-grey""*
g0goalyellow"ÿÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ2%
s0sphereorange"þÿÿÿÿÿÿÿÿ @*Ö

outlinewhite
b0block
light-grey" $
b1block
light-grey"þÿÿÿÿÿÿÿÿ&
b2block
light-grey"ÿÿÿÿÿÿÿÿÿ"*
g0goalyellow"ÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ2%
s0sphereorange" ÿÿÿÿÿÿÿÿÿ@*å

outlinewhite&
b0block
light-grey"þÿÿÿÿÿÿÿÿ &
b1block
light-grey" ÿÿÿÿÿÿÿÿÿ
b2block
light-grey" 
b3block
light-grey""
g0goalyellow" 2%
s0sphereorange"þÿÿÿÿÿÿÿÿ @*è

outlinewhite&
b0block
light-grey"ÿÿÿÿÿÿÿÿÿ$
b1block
light-grey"þÿÿÿÿÿÿÿÿ$
b2block
light-grey"ÿÿÿÿÿÿÿÿÿ
b3block
light-grey""#
g0goalyellow"ÿÿÿÿÿÿÿÿÿ 2
s0sphereorange"@*

outlinewhite(
b0block
light-grey"ÿÿÿÿÿÿÿÿÿ 1
b1block
light-grey"ÿÿÿÿÿÿÿÿÿþÿÿÿÿÿÿÿÿ &
b2block
light-grey"þÿÿÿÿÿÿÿÿ 1
b3block
light-grey"þÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ"#
g0goalyellow"þÿÿÿÿÿÿÿÿ 2
s0sphereorange" @*ð

outlinewhite
b0block
light-grey" 
b1block
light-grey" 
b2block
light-grey" 1
b3block
light-grey"ÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ "!
g0goalyellow" ÿÿÿÿÿÿÿÿÿ2%
s0sphereorange"þÿÿÿÿÿÿÿÿ @*

outlinewhite/
b0block
light-grey"ÿÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ
b1block
light-grey"/
b2block
light-grey"þÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ&
b3block
light-grey" þÿÿÿÿÿÿÿÿ"!
g0goalyellow"ÿÿÿÿÿÿÿÿÿ 2!
s0sphereorange" þÿÿÿÿÿÿÿÿ@*†

outlinewhite1
b0block
light-grey"ÿÿÿÿÿÿÿÿÿ þÿÿÿÿÿÿÿÿ(
b1block
light-grey"þÿÿÿÿÿÿÿÿ 
b2block
light-grey" &
b3block
light-grey"ÿÿÿÿÿÿÿÿÿ "#
g0goalyellow" ÿÿÿÿÿÿÿÿÿ2%
s0sphereorange" þÿÿÿÿÿÿÿÿ@2è	
basicÞ	
basic¬#if __VERSION__ >= 130
  #define attribute in
  #define varying out
#endif

uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;
attribute vec4 a_Position;
attribute vec3 a_Normal;
varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
    v_Position = vec3(u_MVMatrix * a_Position);
    vec3 norm = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
    v_Normal = norm / length(norm);
    gl_Position = u_MVPMatrix * a_Position;
}
à#if __VERSION__ >= 130
  #define varying in
  out vec4 mgl_FragColour;
#else
  #define mgl_FragColour gl_FragColor
#endif

#ifdef GL_ES
  #define MEDIUMP mediump
  precision MEDIUMP float;
#else
  #define MEDIUMP
#endif

uniform MEDIUMP vec3 u_LightPos;
uniform MEDIUMP vec4 u_Colour;
varying MEDIUMP vec3 v_Position;
varying MEDIUMP vec3 v_Normal;

void main() {
    vec3 diff = u_LightPos - v_Position;
    vec3 lightVector = normalize(diff);
    //float distance = length(diff);
    //float diffuse = max(dot(v_Normal, lightVector), 0.1);
    //diffuse *= 1.0 / (1.0 + (0.25 * distance * distance));
    float diffuse = (dot(v_Normal, lightVector) + 1.0) / 2.0;
    mgl_FragColour = u_Colour * diffuse;
    mgl_FragColour.a = 1.0;
}
"a_Normal"
a_Position*u_Colour*
u_LightPos*
u_MVMatrix*u_MVPMatrix