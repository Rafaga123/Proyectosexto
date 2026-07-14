#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time; // El tiempo que le pasaremos desde Java

void main() {
    // Coordenadas base
    vec2 uv = v_texCoords;

    // Generamos movimiento ondulante alterando las coordenadas con el tiempo
    uv.x += sin(uv.y * 5.0 + u_time) * 0.1;
    uv.y += cos(uv.x * 5.0 + u_time * 0.8) * 0.1;

    // Creamos un patrón de ruido suave usando senos combinados
    float pattern = sin(uv.x * 10.0) * cos(uv.y * 10.0) * 0.5 + 0.5;

    // Paleta de colores claros (R, G, B) de 0.0 a 1.0
    vec3 colorVerdeClaro = vec3(0.7, 0.95, 0.7);
    vec3 colorMenta = vec3(0.5, 0.85, 0.7);
    vec3 colorTurquesa = vec3(0.6, 0.9, 0.85);

    // Mezclamos los colores basados en el patrón en movimiento
    vec3 colorFinal = mix(colorVerdeClaro, colorMenta, pattern);
    colorFinal = mix(colorFinal, colorTurquesa, sin(u_time * 0.5 + uv.x) * 0.5 + 0.5);

    // Salida a la pantalla
    gl_FragColor = vec4(colorFinal, 1.0) * v_color;
}
