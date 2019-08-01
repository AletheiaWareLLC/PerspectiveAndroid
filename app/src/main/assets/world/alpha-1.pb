�#
alpha-1red"�

outlineblack0
b0block	dark-grey"��������� ���������",
g0goalyellow"��������� ���������2%
s0sphereorange" ���������:1@"�

outlineblack%
b0block	dark-grey"
b1block	dark-grey""
g0goalyellow"2,
s0sphereorange"������������������:2@"�

outlineblack%
b0block	dark-grey"
b1block	dark-grey"��������� 
b2block	dark-grey" "#
g0goalyellow"��������� 2%
s0sphereorange" ���������:3@"�

outlineblack
b0block	dark-grey" %
b1block	dark-grey"
b2block	dark-grey" ���������
b3block	dark-grey""!
g0goalyellow"
s0sphereorange"��������� ���������:4@"�

outlineblack%
b0block	dark-grey"
b1block	dark-grey" ���������'
b2block	dark-grey" ���������0
b3block	dark-grey"��������� ���������9
b4block	dark-grey"!������������������ ���������"!
g0goalyellow"
s0sphereorange"��������� ���������:5@"�

outlineblack'
b0block	dark-grey"��������� #
b1block	dark-grey" ���������
b2block	dark-grey" '
b3block	dark-grey"��������� 0
b4block	dark-grey"��������� ���������
b5block	dark-grey""#
g0goalyellow" ���������2.
s0sphereorange"������������������ :6@"�

outlineblack'
b0block	dark-grey" ���������0
b1block	dark-grey"��������� ���������
b2block	dark-grey"
b3block	dark-grey" '
b4block	dark-grey"��������� .
b5block	dark-grey"������������������
b6block	dark-grey" "5
g0goalyellow"!������������������ ���������2%
s0sphereorange"��������� :7@	"�

outlineblack0
b0block	dark-grey"������������������ 0
b1block	dark-grey"��������� ���������%
b2block	dark-grey"
b3block	dark-grey" %
b4block	dark-grey"
b5block	dark-grey" 9
b6block	dark-grey"!������������������ ���������#
b7block	dark-grey" ���������"*
g0goalyellow"������������������2%
s0sphereorange"��������� :8@
"�

outlineblack'
b0block	dark-grey" ���������.
b1block	dark-grey"��������� ���������%
b2block	dark-grey"
b3block	dark-grey"��������� %
b4block	dark-grey"
b5block	dark-grey"��������� ���������0
b6block	dark-grey"��������� ���������.
b7block	dark-grey"������������������9
b8block	dark-grey"!������������������ ���������"
g0goalyellow"2%
s0sphereorange"��������� :9@"�

outlineblack%
b0block	dark-grey"
b1block	dark-grey"��������� #
b2block	dark-grey"���������
b3block	dark-grey"
b4block	dark-grey" #
b5block	dark-grey" ���������0
b6block	dark-grey"��������� ���������
b7block	dark-grey" .
b8block	dark-grey"��������� ���������0
b9block	dark-grey"��������� ���������"*
g0goalyellow"��������� ���������2%
s0sphereorange"��������� :10@*�	
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