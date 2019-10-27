÷
tutorialwhite"blue*v

outlineblue"
g1goalyellow"ÿÿÿÿÿÿÿÿÿ2
s1sphereorange":(Tap to Enable Gravity and Drop into Goal*

outlineblue"
g1goalyellow" ÿÿÿÿÿÿÿÿÿ2
s1sphereorange" :1Swipe to Rotate Maze and Orient Sphere Above Goal@*•

outlineblue
b1cubegrey""
g1goalyellow"ÿÿÿÿÿÿÿÿÿ2#
s1sphereorange"ÿÿÿÿÿÿÿÿÿ:"Swipe to Orient Sphere Above Block@*¹

outlineblue
b1cubegrey" 
b2cubegrey"ÿÿÿÿÿÿÿÿÿ "
g1goalyellow" ÿÿÿÿÿÿÿÿÿ2%
s1sphereorange"ÿÿÿÿÿÿÿÿÿ :!Advance Towards Goal using Blocks@*Þ

outlineblue"*
g1goalyellow"ÿÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ*0
p1portalblue" ÿÿÿÿÿÿÿÿÿ*ÿÿÿÿÿÿÿÿÿ *0
p2portalblue"ÿÿÿÿÿÿÿÿÿ * ÿÿÿÿÿÿÿÿÿ2
s1sphereorange" :Traverse Portal to Reach Goal@*¿

outlineblue*
b1cubegrey"ÿÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ"*
g1goalyellow"ÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ*=
p2portalblue"ÿÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ*ÿÿÿÿÿÿÿÿÿ *=
p1portalblue"ÿÿÿÿÿÿÿÿÿ *ÿÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ2%
s1sphereorange"ÿÿÿÿÿÿÿÿÿ :-Advance Towards Goal using Blocks and Portals@2è	
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