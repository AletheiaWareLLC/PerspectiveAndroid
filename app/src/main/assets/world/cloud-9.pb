�9
cloud-9	
light-blue"�

outlinewhite
b0cloudwhite" #
b1cloudwhite" ���������
b2cloudwhite" #
b3cloudwhite"��������� "
g0goalyellow" 2.
s0sphereorange"��������� ���������:1@"�

outlinewhite!
b0cloudwhite" ���������#
b1cloudwhite" ���������
b2cloudwhite"���������!
b3cloudwhite"���������
b4cloudwhite" *
b5cloudwhite"������������������",
g0goalyellow"��������� ���������2%
s0sphereorange"��������� :2@"�

outlinewhite
b0cloudwhite" ,
b1cloudwhite"������������������ !
b2cloudwhite"���������!
b3cloudwhite"��������� 
b4cloudwhite" #
b5cloudwhite" ���������!
b6cloudwhite"���������!
b7cloudwhite"��������� "#
g0goalyellow"��������� 2#
s0sphereorange" ���������:3@"�

outlinewhite5
b0cloudwhite"!������������������ ���������#
b1cloudwhite"��������� #
b2cloudwhite" ���������!
b3cloudwhite"��������� ,
b4cloudwhite"��������� ���������,
b5cloudwhite"��������� ���������#
b6cloudwhite"��������� *
b7cloudwhite"������������������#
b8cloudwhite"��������� 5
b9cloudwhite"!������������������ ���������"!
g0goalyellow"���������2
s0sphereorange" :4@"�

outlinewhite
b0cloudwhite" #
b1cloudwhite"��������� ,
b2cloudwhite"��������� ���������!
b3cloudwhite" ���������!
b4cloudwhite"��������� ,
b5cloudwhite"��������� ���������5
b6cloudwhite"!������������������ ���������,
b7cloudwhite"������������������ 5
b8cloudwhite"!������������������ ���������#
b9cloudwhite"��������� "
b10cloudwhite"��������� -
b11cloudwhite"��������� ���������",
g0goalyellow"������������������ 2%
s0sphereorange"��������� :5@"�

outlinewhite!
b0cloudwhite" ���������
b1cloudwhite"���������*
b2cloudwhite"������������������
b3cloudwhite" 
b4cloudwhite" *
b5cloudwhite"������������������,
b6cloudwhite"������������������ 
b7cloudwhite" 5
b8cloudwhite"!������������������ ���������!
b9cloudwhite"���������-
b10cloudwhite"������������������ -
b11cloudwhite"��������� ���������"
b12cloudwhite"���������$
b13cloudwhite"��������� ",
g0goalyellow"������������������ 2.
s0sphereorange"��������� ���������:6@	"�

outlinewhite#
b0cloudwhite"��������� 
b1cloudwhite" *
b2cloudwhite"��������� ���������*
b3cloudwhite"��������� ���������*
b4cloudwhite"������������������!
b5cloudwhite" ���������5
b6cloudwhite"!������������������ ���������,
b7cloudwhite"������������������ #
b8cloudwhite"��������� ,
b9cloudwhite"��������� ���������"
b10cloudwhite" ���������$
b11cloudwhite"��������� $
b12cloudwhite" ���������$
b13cloudwhite"��������� $
b14cloudwhite"��������� $
b15cloudwhite"��������� "*
g0goalyellow"��������� ���������2%
s0sphereorange"��������� :6@
"�

outlinewhite,
b0cloudwhite"��������� ���������
b1cloudwhite" 
b2cloudwhite" #
b3cloudwhite"��������� ,
b4cloudwhite"������������������ 5
b5cloudwhite"!������������������ ���������
b6cloudwhite" ,
b7cloudwhite"������������������ !
b8cloudwhite"��������� #
b9cloudwhite"��������� -
b10cloudwhite"��������� ���������-
b11cloudwhite"��������� ���������$
b12cloudwhite"��������� 
b13cloudwhite"
b14cloudwhite" 
b15cloudwhite" "
b16cloudwhite" ���������$
b17cloudwhite"��������� "
g0goalyellow" 2%
s0sphereorange"��������� :8@"�

outlinewhite
b0cloudwhite" #
b1cloudwhite"��������� !
b2cloudwhite" ���������
b3cloudwhite"���������#
b4cloudwhite"��������� 
b5cloudwhite" #
b6cloudwhite"��������� 
b7cloudwhite" #
b8cloudwhite"��������� #
b9cloudwhite"��������� -
b10cloudwhite"��������� ���������-
b11cloudwhite"��������� ���������
b12cloudwhite" $
b13cloudwhite"��������� "
b14cloudwhite"��������� 6
b15cloudwhite"!������������������ ���������-
b16cloudwhite"��������� ���������$
b17cloudwhite"��������� -
b18cloudwhite"������������������ -
b19cloudwhite"������������������ "
g0goalyellow" 2.
s0sphereorange"��������� ���������:9@"�

outlinewhite,
b0cloudwhite"��������� ���������,
b1cloudwhite"��������� ���������#
b2cloudwhite"��������� #
b3cloudwhite"��������� !
b4cloudwhite" ���������#
b5cloudwhite"��������� #
b6cloudwhite" ���������,
b7cloudwhite"������������������ #
b8cloudwhite"��������� !
b9cloudwhite" ���������+
b10cloudwhite"������������������-
b11cloudwhite"��������� ���������
b12cloudwhite" +
b13cloudwhite"��������� ���������$
b14cloudwhite"��������� -
b15cloudwhite"��������� ���������$
b16cloudwhite"��������� $
b17cloudwhite"��������� -
b18cloudwhite"������������������  
b19cloudwhite"���������-
b20cloudwhite"��������� ���������
b21cloudwhite" "
g0goalyellow" 2%
s0sphereorange"��������� :10@*�	
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