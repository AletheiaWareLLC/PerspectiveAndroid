à
magic-8	green"¬

outlinepurple,
b0blockblack"ýÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ #
b1blockblack"ýÿÿÿÿÿÿÿÿ "
g0goalyellow" 2%
s0sphereorange"ýÿÿÿÿÿÿÿÿ :1@"Ð

outlinepurple
b0blockblack" 
b1blockblack" #
b2blockblack"ýÿÿÿÿÿÿÿÿ 
b3blockblack" "
g0goalyellow" 2%
s0sphereorange"ýÿÿÿÿÿÿÿÿ :2@"ª

outlinepurple!
b0blockblack"ÿÿÿÿÿÿÿÿÿ #
b1blockblack" ÿÿÿÿÿÿÿÿÿ,
b2blockblack"ÿÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ*
b3blockblack"ýÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ"5
g0goalyellow"!üÿÿÿÿÿÿÿÿþÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ27
s0sphereorange"!þÿÿÿÿÿÿÿÿýÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ:3@"¾

outlinepurple
b0blockblack" #
b1blockblack"ýÿÿÿÿÿÿÿÿ ,
b2blockblack"ýÿÿÿÿÿÿÿÿ þÿÿÿÿÿÿÿÿ#
b3blockblack"þÿÿÿÿÿÿÿÿ 
b4blockblack" !
b5blockblack" üÿÿÿÿÿÿÿÿ"!
g0goalyellow"ÿÿÿÿÿÿÿÿÿ 2.
s0sphereorange"ýÿÿÿÿÿÿÿÿýÿÿÿÿÿÿÿÿ :4@"„

outlinepurple*
b0blockblack"ÿÿÿÿÿÿÿÿÿ üÿÿÿÿÿÿÿÿ,
b1blockblack"ýÿÿÿÿÿÿÿÿ ýÿÿÿÿÿÿÿÿ,
b2blockblack"þÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ !
b3blockblack"ýÿÿÿÿÿÿÿÿ,
b4blockblack"ýÿÿÿÿÿÿÿÿ üÿÿÿÿÿÿÿÿ5
b5blockblack"!ÿÿÿÿÿÿÿÿÿýÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ"*
g0goalyellow"ýÿÿÿÿÿÿÿÿ þÿÿÿÿÿÿÿÿ2.
s0sphereorange"ÿÿÿÿÿÿÿÿÿýÿÿÿÿÿÿÿÿ :5@"é

outlinepurple,
b0blockblack"þÿÿÿÿÿÿÿÿ ÿÿÿÿÿÿÿÿÿ!
b1blockblack"þÿÿÿÿÿÿÿÿ,
b2blockblack"þÿÿÿÿÿÿÿÿüÿÿÿÿÿÿÿÿ #
b3blockblack"þÿÿÿÿÿÿÿÿ *
b4blockblack"ýÿÿÿÿÿÿÿÿ üÿÿÿÿÿÿÿÿ*
b5blockblack"þÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ"#
g0goalyellow"ýÿÿÿÿÿÿÿÿ 2.
s0sphereorange"þÿÿÿÿÿÿÿÿüÿÿÿÿÿÿÿÿ :6@*è	
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