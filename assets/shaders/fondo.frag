#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time; // El tiempo que le pasaremos desde Java
uniform vec3 u_colorBase;

void main() {
    // Coordenadas base
    vec2 uv = v_texCoords;

    // Generamos movimiento ondulante alterando las coordenadas con el tiempo
    uv.x += sin(uv.y * 5.0 + u_time) * 0.1;
    uv.y += cos(uv.x * 5.0 + u_time * 0.8) * 0.1;

    // Creamos un patrón de ruido suave usando senos combinados
    float pattern = sin(uv.x * 10.0) * cos(uv.y * 10.0) * 0.5 + 0.5;

    //Logica de colores basado en el tablero
    vec3 colorPrimario= u_colorBase;

    //Se calcula colores derivados tanto oscureciendo como iluminando al base
    vec3 colorSecundario = colorPrimario * 0.7;
    vec3 colorAcento = clamp(colorPrimario * 1.2,0.0,1.0);

    // Mezclamos los colores basados en el patrón en movimiento
    vec3 colorFinal = mix(colorPrimario, colorSecundario,pattern);
    colorFinal = mix(colorFinal, colorAcento, sin(u_time * 0.5 + uv.x) * 0.5 + 0.5);

    // Salida a la pantalla
    gl_FragColor = vec4(colorFinal, 1.0) * v_color;
}
