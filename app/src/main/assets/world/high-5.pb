�"
high-5white"�

outlinegreen!
b0blockblack"
b1blockblack"��������� ���������",
g0goalyellow"��������� ���������2%
s0sphereorange"��������� :1@"�

outlinegreen
b0blockblack" #
b1blockblack" ���������
b2blockblack" !
b3blockblack"
g0goalyellow"2,
s0sphereorange"��������� ���������:2@"�

outlinegreen!
b0blockblack"
b1blockblack"��������� !
b2blockblack"
b3blockblack"��������� ���������"
g0goalyellow" 2%
s0sphereorange"��������� :3@"�

outlinegreen
b0blockblack" 
b1blockblack" 
b2blockblack" #
b3blockblack" ���������#
b4blockblack" ���������,
b5blockblack"��������� ���������"5
g0goalyellow"!������������������ ���������2.
s0sphereorange"��������� ���������:4@"�

outlinegreen
b0blockblack"���������*
b1blockblack"��������� ���������#
b2blockblack" ���������,
b3blockblack"��������� ���������,
b4blockblack"��������� ���������5
b5blockblack"!������������������ ���������"#
g0goalyellow" ���������27
s0sphereorange"!������������������ ���������:5@"�

outlinegreen#
b0blockblack" ���������,
b1blockblack"��������� ���������
b2blockblack" !
b3blockblack"
b4blockblack"��������� 
b5blockblack" "
g0goalyellow"2%
s0sphereorange"��������� :6@"�

outlinegreen
b0blockblack"
b1blockblack" *
b2blockblack"������������������
b3blockblack" 
b4blockblack" 
b5blockblack" #
b6blockblack"��������� #
b7blockblack"��������� "
g0goalyellow" 2.
s0sphereorange"������������������ :7@	"�

outlinegreen
b0blockblack"
b1blockblack" *
b2blockblack"������������������
b3blockblack" 
b4blockblack" 
b5blockblack" #
b6blockblack"��������� #
b7blockblack"��������� "
g0goalyellow" 2.
s0sphereorange"������������������ :8@	"�

outlinegreen
b0blockblack" *
b1blockblack"��������� ���������,
b2blockblack"������������������ !
b3blockblack"
b4blockblack"
b5blockblack" ���������!
b6blockblack"
b7blockblack"!������������������ ���������"
g0goalyellow"2
s0sphereorange" :9@	"�

outlinegreen#
b0blockblack" ���������
b1blockblack" ,
b2blockblack"��������� ���������,
b3blockblack"��������� ���������
b4blockblack" 
b5blockblack"!
b6blockblack"
b7blockblack"��������� "5
g0goalyellow"!������������������ ���������2.
s0sphereorange"��������� ���������:10@	*�	
basic�	
basic�#if __VERSION__ >= 130
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
�#if __VERSION__ >= 130
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